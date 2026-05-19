<template>
  <div class="group-panel" @click="memberMenu.show = false">
    <div class="gp-search">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="gp-search-icon"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
      <input v-model="memberSearch" type="text" placeholder="搜索群成员" class="gp-search-input">
    </div>

    <div class="gp-members-grid">
      <div v-for="m in filteredMembers" :key="(m.userId || m.id)"
           class="gp-member-card"
           :title="m.nickname || m.username"
           @click.prevent.stop="onMemberClick($event, m)">
        <img v-if="m.avatar && !imgErrors['gm_' + (m.userId || m.id)]"
             :src="m.avatar" class="gp-member-avatar"
             @error="imgErrors['gm_' + (m.userId || m.id)] = true" alt="">
        <div v-else class="gp-member-avatar-placeholder" :style="{ background: memberColor(m) }">{{ (m.nickname || m.username || '?')[0] }}</div>
        <span class="gp-member-name">
          {{ truncate(m.nicknameInGroup || m.nickname || m.username, 6) }}
          <span v-if="isOwner(m)" class="gp-role-tag owner">群主</span>
          <span v-else-if="isAdmin(m)" class="gp-role-tag admin">管理</span>
        </span>
      </div>
      <div v-if="!loadingMembers" class="gp-member-card gp-add-card" @click.stop="showAddMembers = true" title="添加成员">
        <div class="gp-add-icon">+</div>
        <span class="gp-member-name">添加</span>
      </div>
    </div>

    <div class="gp-info-list">
      <div class="gp-section-title">群聊名称</div>
      <div class="gp-info-row clickable" @click.stop="startEditName" v-if="!editingName">
        <span class="gp-info-value">{{ group.name || '未命名' }}</span>
        <svg v-if="canManage" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="gp-arrow"><path d="M9 18l6-6-6-6"/></svg>
      </div>
      <div class="gp-info-row" v-else>
        <input ref="nameInput" v-model="editNameValue" class="gp-edit-input" @keydown.enter="saveGroupName" @keydown.escape="cancelEditName" maxlength="32">
        <button class="gp-edit-save" @click.stop="saveGroupName">保存</button>
        <button class="gp-edit-cancel" @click.stop="cancelEditName">取消</button>
      </div>

      <div class="gp-section-title">群公告</div>
      <div class="gp-info-row clickable" @click.stop="startEditAnnouncement" v-if="!editingAnnouncement">
        <span class="gp-info-value muted">{{ group.announcement || (canManage ? '点击设置群公告' : '暂无公告') }}</span>
        <svg v-if="canManage" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="gp-arrow"><path d="M9 18l6-6-6-6"/></svg>
      </div>
      <div class="gp-info-row" v-else>
        <textarea ref="annoInput" v-model="editAnnouncementValue" class="gp-edit-textarea" rows="3" maxlength="500" placeholder="输入群公告"></textarea>
        <div class="gp-edit-actions">
          <button class="gp-edit-save" @click.stop="saveAnnouncement">保存</button>
          <button class="gp-edit-cancel" @click.stop="cancelEditAnnouncement">取消</button>
        </div>
      </div>

      <div class="gp-section-title">群成员 ({{ allMembers.length }}人)</div>

      <div class="gp-divider"></div>

      <div class="gp-action-item" @click.stop="ElMessage.info('查找聊天内容开发中')">
        <span>查找聊天内容</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="gp-arrow"><path d="M9 18l6-6-6-6"/></svg>
      </div>

      <div class="gp-action-item">
        <span>消息免打扰</span>
        <label class="gp-switch">
          <input type="checkbox" v-model="muteNotif">
          <span class="gp-switch-slider"></span>
        </label>
      </div>

      <div class="gp-action-item">
        <span>置顶聊天</span>
        <label class="gp-switch">
          <input type="checkbox" v-model="pinChat">
          <span class="gp-switch-slider"></span>
        </label>
      </div>

      <div class="gp-divider"></div>

      <button class="gp-danger-btn" @click.stop="handleLeaveGroup">
        {{ isCurrentUserOwner ? '解散群聊' : '退出群聊' }}
      </button>
    </div>

    <div v-if="memberMenu.show" class="gp-context-menu" :style="{ left: memberMenu.x + 'px', top: memberMenu.y + 'px' }" @click.stop>
      <div class="ctx-header">{{ memberMenu.member?.nickname || memberMenu.member?.username }}</div>
      <div v-if="isSelf(memberMenu.member)" class="ctx-item muted">我自己</div>
      <template v-else-if="canManage">
        <div v-if="!isOwner(memberMenu.member)" class="ctx-item danger" @click.stop="removeMemberConfirm(memberMenu.member)">移出群聊</div>
        <div v-if="isAdmin(memberMenu.member)" class="ctx-item" @click.stop="changeRole(memberMenu.member, 0)">取消管理员</div>
        <div v-else-if="!isOwner(memberMenu.member)" class="ctx-item" @click.stop="changeRole(memberMenu.member, 1)">设为管理员</div>
      </template>
      <div v-else class="ctx-item muted">仅群主/管理员可操作</div>
    </div>

    <div v-if="showAddMembers" class="gp-add-members-overlay" @click.stop="showAddMembers = false">
      <div class="gp-add-members-card" @click.stop>
        <div class="gp-add-header">
          <h3>添加成员</h3>
          <button class="modal-close" @click.stop="showAddMembers = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div class="gp-add-body">
          <div v-if="addableFriends.length === 0" style="padding:20px;text-align:center;color:#999;font-size:13px;">暂无可添加的好友</div>
          <div v-for="f in addableFriends" :key="'af_' + f.friendId"
               class="gp-add-item"
               :class="{ checked: addSelected.has(f.friendId) }"
               @click.stop="toggleAddSelect(f.friendId)">
            <span class="gp-add-check">{{ addSelected.has(f.friendId) ? '✓' : '' }}</span>
            <img v-if="f.avatar && !imgErrors['af_' + f.friendId]" :src="f.avatar" class="gp-add-avatar" @error="imgErrors['af_' + f.friendId] = true" alt="">
            <div v-else class="gp-add-avatar-placeholder">{{ (f.nickname || f.username || '?')[0] }}</div>
            <span class="gp-add-name">{{ f.nickname || f.username }}</span>
          </div>
        </div>
        <div class="gp-add-footer">
          <button class="btn btn-secondary" @click.stop="showAddMembers = false">取消</button>
          <button class="btn btn-primary" :disabled="addSelected.size === 0 || addingMembers" @click.stop="handleAddMembers">
            {{ addingMembers ? '添加中...' : '添加' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGroup, getGroupMembers, leaveGroup, dismissGroup, updateGroup, removeGroupMember, changeMemberRole, addGroupMembers, getFriends } from '../api/chat'
import { getUser } from '../utils/auth'

const props = defineProps({
  groupId: { type: [Number, String], required: true }
})

const emit = defineEmits(['close', 'left', 'updated'])

const group = ref({})
const allMembers = ref([])
const loadingMembers = ref(true)
const imgErrors = reactive({})
const myUserId = ref(getUser()?.id)
const memberSearch = ref('')
const muteNotif = ref(false)
const pinChat = ref(false)

const editingName = ref(false)
const editNameValue = ref('')
const editingAnnouncement = ref(false)
const editAnnouncementValue = ref('')
const nameInput = ref(null)
const annoInput = ref(null)

const memberMenu = reactive({ show: false, x: 0, y: 0, member: null })

const showAddMembers = ref(false)
const addingMembers = ref(false)
const addableFriends = ref([])
const addSelected = reactive(new Set())

const memberIds = computed(() => new Set(allMembers.value.map(m => Number(m.userId || m.id))))

const isCurrentUserOwner = computed(() => {
  return allMembers.value.find(m => Number(m.userId || m.id) === Number(myUserId.value))?.role === 2
})

const canManage = computed(() => {
  const me = allMembers.value.find(m => Number(m.userId || m.id) === Number(myUserId.value))
  return me && (me.role === 2 || me.role === 1)
})

function isOwner(m) { return m.role === 2 }
function isAdmin(m) { return m.role === 1 }
function isSelf(m) { return Number(m.userId || m.id) === Number(myUserId.value) }

function memberColor(m) {
  const colors = ['#07C160', '#10AEFF', '#FF6B6B', '#FFD93D', '#6C5CE7', '#00B894']
  const idx = ((m.nickname || m.username || '?').charCodeAt(0) || 0) % colors.length
  return colors[idx]
}

const filteredMembers = computed(() => {
  if (!memberSearch.value.trim()) return allMembers.value
  const q = memberSearch.value.toLowerCase()
  return allMembers.value.filter(m =>
    (m.nickname || '').toLowerCase().includes(q) ||
    (m.username || '').toLowerCase().includes(q) ||
    (m.nicknameInGroup || '').toLowerCase().includes(q)
  )
})

function truncate(str, len) {
  if (!str) return ''
  return str.length > len ? str.slice(0, len) + '..' : str
}

async function loadData() {
  try {
    const [gRes, mRes] = await Promise.all([
      getGroup(props.groupId),
      getGroupMembers(props.groupId)
    ])
    group.value = gRes.data || {}
    allMembers.value = (mRes.data || []).map(m => ({
      ...m,
      userId: m.userId || m.id,
      role: m.role ?? 0
    }))
  } catch {} finally {
    loadingMembers.value = false
  }
}

function onMemberClick(e, m) {
  if (!canManage.value) return
  const rect = e.currentTarget.getBoundingClientRect()
  memberMenu.show = true
  memberMenu.x = rect.left
  memberMenu.y = rect.bottom + 4
  memberMenu.member = m
}

function startEditName() {
  if (!canManage.value) return
  editNameValue.value = group.value.name || ''
  editingName.value = true
  nextTick(() => nameInput.value?.focus())
}

function cancelEditName() { editingName.value = false }

async function saveGroupName() {
  const val = editNameValue.value.trim()
  if (!val) { ElMessage.warning('群名称不能为空'); return }
  try {
    await updateGroup(props.groupId, { name: val })
    group.value.name = val
    editingName.value = false
    emit('updated')
    ElMessage.success('群名称已更新')
  } catch (e) { ElMessage.error(e?.message || '更新失败') }
}

function startEditAnnouncement() {
  if (!canManage.value) return
  editAnnouncementValue.value = group.value.announcement || ''
  editingAnnouncement.value = true
  nextTick(() => annoInput.value?.focus())
}

function cancelEditAnnouncement() { editingAnnouncement.value = false }

async function saveAnnouncement() {
  const val = editAnnouncementValue.value.trim()
  try {
    await updateGroup(props.groupId, { announcement: val })
    group.value.announcement = val
    editingAnnouncement.value = false
    emit('updated')
    ElMessage.success('群公告已更新')
  } catch (e) { ElMessage.error(e?.message || '更新失败') }
}

async function removeMemberConfirm(member) {
  memberMenu.show = false
  const name = member.nickname || member.username || '该成员'
  try {
    await ElMessageBox.confirm(`确定要将 ${name} 移出群聊吗？`, '移除成员', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeGroupMember(props.groupId, member.userId || member.id)
    allMembers.value = allMembers.value.filter(m => (m.userId || m.id) !== (member.userId || member.id))
    ElMessage.success('已移出群聊')
  } catch {}
}

async function changeRole(member, role) {
  memberMenu.show = false
  try {
    await changeMemberRole(props.groupId, member.userId || member.id, role)
    const target = allMembers.value.find(m => (m.userId || m.id) === (member.userId || member.id))
    if (target) target.role = role
    ElMessage.success(role === 1 ? '已设为管理员' : '已取消管理员')
    emit('updated')
  } catch (e) { ElMessage.error(e?.message || '操作失败') }
}

async function handleLeaveGroup() {
  const msg = isCurrentUserOwner.value ? '确定要解散该群聊吗？解散后不可恢复' : '确定要退出该群聊吗？'
  try {
    await ElMessageBox.confirm(msg, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    if (isCurrentUserOwner.value) {
      await dismissGroup(props.groupId)
    } else {
      await leaveGroup(props.groupId)
    }
    emit('left', props.groupId)
    emit('close')
  } catch {}
}

async function handleAddMembers() {
  if (addSelected.size === 0) return
  addingMembers.value = true
  try {
    const userIds = [...addSelected].map(Number)
    await addGroupMembers(props.groupId, { userIds })
    ElMessage.success('添加成功')
    showAddMembers.value = false
    addSelected.clear()
    await loadData()
  } catch (e) {
    ElMessage.error(e?.message || '添加失败')
  } finally {
    addingMembers.value = false
  }
}

async function loadAddableFriends() {
  try {
    const res = await getFriends()
    const friends = res.data || []
    addableFriends.value = friends.filter(f => !memberIds.value.has(Number(f.friendId)))
  } catch {}
}

function toggleAddSelect(friendId) {
  if (addSelected.has(friendId)) addSelected.delete(friendId)
  else addSelected.add(friendId)
}

const showAddMembersWatch = ref(false)

onMounted(async () => {
  await loadData()
})

watch(showAddMembers, async (val) => {
  if (val && addableFriends.value.length === 0) {
    await loadAddableFriends()
  }
})
</script>

<style scoped>
.group-panel {
  width: 320px;
  height: 100%;
  background: #f7f7f7;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  position: relative;
}

.gp-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.gp-search-icon { width: 16px; height: 16px; color: #bbb; flex-shrink: 0; }
.gp-search-input {
  flex: 1; border: none; outline: none; font-size: 13px; background: transparent; color: #333;
}
.gp-search-input::placeholder { color: #bbb; }

.gp-members-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.gp-member-card {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 8px 2px; cursor: pointer; border-radius: 4px; position: relative;
}
.gp-member-card:hover { background: #f5f5f5; }
.gp-member-avatar { width: 44px; height: 44px; border-radius: 6px; object-fit: cover; }
.gp-member-avatar-placeholder {
  width: 44px; height: 44px; border-radius: 6px; display: flex; align-items: center;
  justify-content: center; font-size: 18px; font-weight: 600; color: #fff;
}
.gp-member-name {
  font-size: 11px; color: #666; text-align: center; max-width: 100%;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  line-height: 1.2;
}
.gp-role-tag {
  display: block; font-size: 9px; padding: 0 4px; border-radius: 2px; margin-top: 1px;
}
.gp-role-tag.owner { color: #fa8c16; }
.gp-role-tag.admin { color: #1890ff; }

.gp-add-card { cursor: pointer; }
.gp-add-icon {
  width: 44px; height: 44px; border-radius: 6px; border: 1.5px dashed #ccc;
  display: flex; align-items: center; justify-content: center; font-size: 22px; color: #999;
  transition: all 0.15s;
}
.gp-add-card:hover .gp-add-icon { border-color: #07C160; color: #07C160; }

.gp-info-list { padding: 12px 0; }
.gp-section-title { padding: 10px 16px 4px; font-size: 13px; color: #888; }
.gp-info-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 16px; background: #fff; margin: 0 12px; border-radius: 4px;
  flex-wrap: wrap; gap: 6px;
}
.gp-info-row.clickable { cursor: pointer; }
.gp-info-row.clickable:hover { background: #f9f9f9; }
.gp-info-value { font-size: 14px; color: #333; }
.gp-info-value.muted { color: #999; font-size: 13px; }
.gp-arrow { width: 14px; height: 14px; color: #ccc; flex-shrink: 0; }

.gp-edit-input {
  flex: 1; border: 1px solid #ddd; border-radius: 4px; padding: 4px 8px;
  font-size: 14px; outline: none; min-width: 120px;
}
.gp-edit-input:focus { border-color: #07C160; }

.gp-edit-textarea {
  width: 100%; border: 1px solid #ddd; border-radius: 4px; padding: 6px 8px;
  font-size: 13px; outline: none; resize: vertical; font-family: inherit;
}
.gp-edit-textarea:focus { border-color: #07C160; }

.gp-edit-actions {
  display: flex; gap: 6px; width: 100%; justify-content: flex-end;
}
.gp-edit-save {
  padding: 2px 10px; border: none; border-radius: 3px; background: #07C160;
  color: #fff; font-size: 12px; cursor: pointer;
}
.gp-edit-cancel {
  padding: 2px 10px; border: 1px solid #ddd; border-radius: 3px;
  background: #fff; color: #666; font-size: 12px; cursor: pointer;
}

.gp-divider { height: 8px; background: #f7f7f7; margin: 8px 0; }

.gp-action-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; background: #fff; cursor: pointer; font-size: 14px; color: #333;
}
.gp-action-item:hover { background: #f9f9f9; }

.gp-switch { position: relative; display: inline-block; width: 40px; height: 22px; flex-shrink: 0; }
.gp-switch input { opacity: 0; width: 0; height: 0; }
.gp-switch-slider { position: absolute; inset: 0; background: #ddd; border-radius: 11px; transition: 0.2s; cursor: pointer; }
.gp-switch-slider::before { content: ''; position: absolute; width: 18px; height: 18px; left: 2px; top: 2px; background: #fff; border-radius: 50%; transition: 0.2s; }
.gp-switch input:checked + .gp-switch-slider { background: #07C160; }
.gp-switch input:checked + .gp-switch-slider::before { transform: translateX(18px); }

.gp-danger-btn {
  display: block; width: calc(100% - 24px); margin: 12px auto; padding: 10px;
  background: #fff; border: none; border-radius: 4px; color: #e74c3c; font-size: 14px;
  cursor: pointer; text-align: center;
}
.gp-danger-btn:hover { background: #fef0f0; }

.gp-context-menu {
  position: fixed; z-index: 1000; background: #fff; border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12); min-width: 140px; overflow: hidden;
}
.ctx-header { padding: 8px 14px; font-size: 13px; color: #999; border-bottom: 1px solid #f0f0f0; }
.ctx-item {
  padding: 10px 14px; font-size: 14px; color: #333; cursor: pointer;
}
.ctx-item:hover { background: #f5f5f5; }
.ctx-item.danger { color: #e74c3c; }
.ctx-item.muted { color: #bbb; cursor: default; }

.gp-add-members-overlay {
  position: fixed; inset: 0; z-index: 999; background: rgba(0,0,0,0.3);
  display: flex; align-items: center; justify-content: center;
}
.gp-add-members-card {
  width: 400px; max-height: 70vh; background: #fff; border-radius: 8px;
  display: flex; flex-direction: column; overflow: hidden;
}
.gp-add-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; border-bottom: 1px solid #eee;
}
.gp-add-header h3 { font-size: 16px; font-weight: 600; }
.modal-close {
  background: none; border: none; cursor: pointer; padding: 2px; color: #999;
}
.modal-close svg { width: 20px; height: 20px; }
.gp-add-body { flex: 1; overflow-y: auto; padding: 12px 16px; }
.gp-add-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 6px;
  border-radius: 6px; cursor: pointer; transition: background 0.15s;
}
.gp-add-item:hover { background: #f5f5f5; }
.gp-add-item.checked { background: #e8f5e9; }
.gp-add-check {
  width: 20px; height: 20px; border-radius: 50%; border: 2px solid #ddd;
  display: flex; align-items: center; justify-content: center; font-size: 11px;
  color: #fff; flex-shrink: 0; transition: all 0.15s;
}
.gp-add-item.checked .gp-add-check { background: #07C160; border-color: #07C160; }
.gp-add-avatar { width: 36px; height: 36px; border-radius: 4px; object-fit: cover; flex-shrink: 0; }
.gp-add-avatar-placeholder {
  width: 36px; height: 36px; border-radius: 4px; display: flex; align-items: center;
  justify-content: center; font-size: 15px; font-weight: 600; color: #fff;
  background: #10AEFF; flex-shrink: 0;
}
.gp-add-name { font-size: 14px; color: #333; }
.gp-add-footer {
  display: flex; justify-content: flex-end; gap: 8px; padding: 12px 16px;
  border-top: 1px solid #eee;
}
.btn { padding: 6px 16px; border-radius: 4px; font-size: 13px; cursor: pointer; border: none; }
.btn-primary { background: #07C160; color: #fff; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-secondary { background: #f0f0f0; color: #333; }
</style>