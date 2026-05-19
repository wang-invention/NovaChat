<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-card" style="max-width:480px;">
      <div class="modal-header">
        <h3>创建群聊</h3>
        <button class="modal-close" @click="$emit('close')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
        </button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">群名称</label>
          <input v-model="groupName" type="text" class="form-input" placeholder="请输入群名称" maxlength="32">
        </div>
        <div class="form-group">
          <label class="form-label">选择成员（至少2人，已选 {{ selectedCount }} 人）</label>
          <div v-if="friendList.length === 0" style="padding:20px;text-align:center;color:var(--text-muted);font-size:13px;">暂无好友，请先添加好友</div>
          <div v-for="(f, idx) in friendList" :key="'gf_' + idx"
               class="group-member-check"
               :class="{ 'is-checked': isChecked(idx) }"
               @click="toggleMember(idx)">
            <span class="check-mark">{{ isChecked(idx) ? '✓' : '' }}</span>
            <img v-if="f.avatar && !imgErrors['gf_' + idx]" :src="f.avatar" class="member-check-avatar" @error="imgErrors['gf_' + idx] = true" alt="">
            <div v-else class="member-check-avatar-placeholder">{{ (f.nickname || f.username || '?')[0] }}</div>
            <span class="member-check-name">{{ f.nickname || f.username }}</span>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" type="button" @click="$emit('close')">取消</button>
        <button class="btn btn-primary" type="button" :class="{ 'btn-disabled': !canCreate }" @click="handleCreate">
          {{ creating ? '创建中...' : '创建群聊' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createGroup } from '../api/chat'

const props = defineProps({
  friends: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'created'])

const groupName = ref('')
const selectedSet = reactive(new Set())
const creating = ref(false)
const imgErrors = reactive({})

const friendList = computed(() => props.friends || [])
const selectedCount = computed(() => selectedSet.size)
const canCreate = computed(() => {
  const nameOk = groupName.value.trim().length > 0
  const countOk = selectedSet.size >= 2
  return nameOk && countOk
})

function isChecked(index) { return selectedSet.has(index) }

function toggleMember(index) {
  if (selectedSet.has(index)) {
    selectedSet.delete(index)
  } else {
    selectedSet.add(index)
  }
}

async function handleCreate() {
  if (!canCreate.value || creating.value) return
  creating.value = true
  try {
    const memberIds = []
    selectedSet.forEach(i => {
      const fid = friendList.value[i]?.friendId
      if (fid) memberIds.push(Number(fid))
    })
    if (memberIds.length < 2) { ElMessage.error('请至少选择2个好友'); return }
    const res = await createGroup({ name: groupName.value.trim(), memberIds, maxMembers: 200 })
    ElMessage.success('群聊创建成功')
    emit('created', res.data)
    emit('close')
  } catch (e) {
    ElMessage.error(e?.message || '创建群聊失败')
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.group-member-check {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s;
}
.group-member-check:hover { background: #f5f5f5; }
.group-member-check.is-checked { background: #e8f5e9; }

.check-mark {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  flex-shrink: 0;
  transition: all 0.15s;
}
.is-checked .check-mark {
  background: #07C160;
  border-color: #07C160;
}

.member-check-avatar {
  width: 38px;
  height: 38px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.member-check-avatar-placeholder {
  width: 38px;
  height: 38px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: #10AEFF;
  flex-shrink: 0;
}
.member-check-name { font-size: 14px; flex: 1; }

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}
.btn-disabled {
  opacity: 0.5;
  pointer-events: none;
  cursor: not-allowed;
}
</style>