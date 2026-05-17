import { getToken } from '../utils/auth'

const AI_BASE = import.meta.env.VITE_AI_BASE

export async function chatStream(messages, { onChunk, onDone, onError, signal }) {
  const token = getToken()
  try {
    const res = await fetch(AI_BASE + '/chat/completions/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: 'Bearer ' + token } : {})
      },
      body: JSON.stringify({ messages, stream: true }),
      signal
    })

    if (!res.ok) throw new Error('AI 请求失败')

    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || !trimmed.startsWith('data: ')) continue
        const data = trimmed.slice(6)
        if (data === '[DONE]') { onDone?.(); return }
        try {
          const parsed = JSON.parse(data)
          const content = parsed.choices?.[0]?.delta?.content || ''
          if (content) onChunk?.(content)
        } catch { }
      }
    }
    onDone?.()
  } catch (e) {
    if (e.name !== 'AbortError') onError?.(e)
  }
}

export async function polishMessage(text) {
  const token = getToken()
  const res = await fetch(AI_BASE + '/chat/polish', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: 'Bearer ' + token } : {})
    },
    body: JSON.stringify({ text })
  })
  if (!res.ok) throw new Error('润色请求失败')
  return res.json()
}