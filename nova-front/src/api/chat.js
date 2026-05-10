/**
 * AI 聊天相关接口
 * 对接 nova-ai-service (FastAPI)
 */

// #ifdef H5
const AI_BASE_URL = "/api/ai/api/v1";
// #endif

// #ifndef H5
const AI_BASE_URL = "http://129.211.0.210:8080/api/ai/api/v1";
// #endif

/**
 * 普通对话（非流式）
 * @param {Object} payload
 * @param {Array} payload.messages - 消息列表 [{role: 'user', content: '...'}]
 * @param {string} [payload.model] - 模型名称
 * @param {number} [payload.temperature] - 温度参数
 * @param {number} [payload.max_tokens] - 最大 token 数
 */
export function chatCompletion(payload) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync("token") || "";
    uni.request({
      url: AI_BASE_URL + "/chat/completions",
      method: "POST",
      data: {
        ...payload,
        stream: false,
      },
      header: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else {
          reject(res.data || { message: "请求失败" });
        }
      },
      fail: (err) => {
        reject(err);
      },
    });
  });
}

/**
 * 流式对话（SSE）
 * H5 环境使用 fetch + ReadableStream
 * 小程序环境降级为普通请求
 * @param {Object} payload - 同 chatCompletion
 * @param {Function} onChunk - 收到每个 chunk 的回调 (text: string) => void
 * @param {Function} onDone - 流结束回调 () => void
 * @param {Function} onError - 错误回调 (err) => void
 * @returns {Function} abort 函数，用于取消请求
 */
export function chatCompletionStream(payload, { onChunk, onDone, onError }) {
  const token = uni.getStorageSync("token") || "";

  // #ifdef H5
  const controller = new AbortController();

  fetch(AI_BASE_URL + "/chat/completions/stream", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({
      ...payload,
      stream: true,
    }),
    signal: controller.signal,
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";

      function read() {
        reader
          .read()
          .then(({ done, value }) => {
            if (done) {
              onDone && onDone();
              return;
            }

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split("\n");
            buffer = lines.pop() || "";

            for (const line of lines) {
              const trimmed = line.trim();
              if (!trimmed || !trimmed.startsWith("data: ")) continue;

              const data = trimmed.slice(6);
              if (data === "[DONE]") {
                onDone && onDone();
                return;
              }

              try {
                const parsed = JSON.parse(data);
                const content =
                  parsed.choices?.[0]?.delta?.content || "";
                if (content) {
                  onChunk && onChunk(content);
                }
              } catch (e) {
                // 忽略解析错误
              }
            }

            read();
          })
          .catch((err) => {
            if (err.name !== "AbortError") {
              onError && onError(err);
            }
          });
      }

      read();
    })
    .catch((err) => {
      if (err.name !== "AbortError") {
        onError && onError(err);
      }
    });

  return () => controller.abort();
  // #endif

  // #ifndef H5
  // 小程序降级：使用普通请求
  let aborted = false;

  chatCompletion(payload)
    .then((res) => {
      if (aborted) return;
      const content = res.choices?.[0]?.message?.content || "";
      if (content) {
        onChunk && onChunk(content);
      }
      onDone && onDone();
    })
    .catch((err) => {
      if (!aborted) {
        onError && onError(err);
      }
    });

  return () => {
    aborted = true;
  };
  // #endif
}
