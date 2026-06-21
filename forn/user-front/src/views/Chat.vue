<template>
  <div class="chat-page">
    <Header />
    <div class="chat-container">
      <!-- 会话列表 -->
      <div class="conversation-list">
        <h3>消息</h3>
        <div class="conv-item" v-for="conv in conversations" :key="conv.userId"
             :class="{ active: selectedUser?.userId === conv.userId }" @click="selectConversation(conv)">
          <el-avatar :size="40">{{ (conv.nickname || 'U').charAt(0) }}</el-avatar>
          <div class="conv-info">
            <div class="conv-name">
              <span>{{ conv.nickname }}</span>
              <span class="conv-time">{{ formatTime(conv.lastTime) }}</span>
            </div>
            <div class="conv-last-msg">
              <span class="msg-text">{{ conv.lastMessage }}</span>
              <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" type="danger" />
            </div>
          </div>
        </div>
        <el-empty v-if="!conversations.length" description="暂无消息" :image-size="60" />
      </div>

      <!-- 聊天窗口 -->
      <div class="chat-window">
        <template v-if="selectedUser">
          <div class="chat-header">
            <span>{{ selectedUser.nickname }}</span>
          </div>
          <div class="chat-messages" ref="messagesRef">
            <div v-for="msg in messages" :key="msg.id"
                 :class="['message', msg.fromId === currentUserId ? 'mine' : 'theirs']">
              <div class="msg-bubble">{{ msg.content }}</div>
              <div class="msg-time">{{ msg.createTime }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input v-model="inputMessage" placeholder="输入消息..." @keyup.enter="sendMsg" />
            <el-button type="primary" @click="sendMsg" :disabled="!inputMessage.trim()">发送</el-button>
          </div>
        </template>
        <el-empty v-else description="选择一个会话开始聊天" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import Header from '../components/Header.vue'
import { getConversations, getMessages, markChatRead, getUnreadCount } from '../api'
import { connectWebSocket, sendMessage, closeWebSocket } from '../utils/websocket'

const route = useRoute()
const userStore = useUserStore()
const conversations = ref([])
const selectedUser = ref(null)
const messages = ref([])
const inputMessage = ref('')
const messagesRef = ref(null)
const currentUserId = ref(userStore.userInfo?.id)
const currentProductId = ref(null)

const formatTime = (time) => {
  if (!time) return ''
  return time.substring(5, 16)
}

const loadConversations = async () => {
  try {
    conversations.value = await getConversations() || []
  } catch (e) {
    console.error(e)
  }
}

const selectConversation = async (conv) => {
  selectedUser.value = conv
  currentProductId.value = conv.productId || null
  try {
    await markChatRead(conv.userId)
    const res = await getMessages(conv.userId, { pageNum: 1, pageSize: 50 })
    messages.value = (res.records || []).reverse()
    scrollToBottom()
    // 刷新会话列表更新未读数
    loadConversations()
  } catch (e) {
    console.error(e)
  }
}

const sendMsg = () => {
  if (!inputMessage.value.trim() || !selectedUser.value) return
  sendMessage(selectedUser.value.userId, inputMessage.value, currentProductId.value)
  inputMessage.value = ''
}

const onWebSocketMessage = (data) => {
  // 如果消息属于当前选中的会话，添加到消息列表
  if (selectedUser.value && (data.fromId === selectedUser.value.userId || data.toId === selectedUser.value.userId)) {
    messages.value.push(data)
    scrollToBottom()
  }
  // 刷新会话列表
  loadConversations()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

onMounted(async () => {
  await loadConversations()
  connectWebSocket(onWebSocketMessage)

  // 如果 URL 带了参数，自动打开对应对话
  const targetUserId = route.query.userId
  if (targetUserId) {
    const conv = conversations.value.find(c => c.userId === Number(targetUserId))
    if (conv) {
      selectConversation(conv)
    } else {
      // 新建一个临时会话
      selectedUser.value = { userId: Number(targetUserId), nickname: '用户' + targetUserId }
      currentProductId.value = route.query.productId ? Number(route.query.productId) : null
      try {
        const res = await getMessages(Number(targetUserId), { pageNum: 1, pageSize: 50, productId: currentProductId.value })
        messages.value = (res.records || []).reverse()
        scrollToBottom()
      } catch (e) {}
    }
  }
})

onUnmounted(() => {
  closeWebSocket()
})
</script>

<style scoped>
.chat-page { min-height: 100vh; background: #f5f7fa; }
.chat-container { max-width: 1000px; margin: 20px auto; display: flex; height: calc(100vh - 120px); background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.conversation-list { width: 280px; border-right: 1px solid #eee; overflow-y: auto; }
.conversation-list h3 { padding: 15px; margin: 0; border-bottom: 1px solid #eee; }
.conv-item { display: flex; align-items: center; gap: 10px; padding: 12px 15px; cursor: pointer; border-bottom: 1px solid #f5f5f5; }
.conv-item:hover { background: #f9f9f9; }
.conv-item.active { background: #ecf5ff; }
.conv-info { flex: 1; min-width: 0; }
.conv-name { display: flex; justify-content: space-between; font-size: 14px; font-weight: 500; }
.conv-time { font-size: 11px; color: #999; font-weight: normal; }
.conv-last-msg { display: flex; justify-content: space-between; align-items: center; margin-top: 4px; }
.msg-text { font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 150px; }
.chat-window { flex: 1; display: flex; flex-direction: column; }
.chat-header { padding: 15px; border-bottom: 1px solid #eee; font-weight: bold; }
.chat-messages { flex: 1; overflow-y: auto; padding: 15px; }
.message { margin-bottom: 12px; display: flex; flex-direction: column; }
.message.mine { align-items: flex-end; }
.message.theirs { align-items: flex-start; }
.msg-bubble { max-width: 70%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5; word-break: break-word; }
.mine .msg-bubble { background: #409eff; color: #fff; border-bottom-right-radius: 4px; }
.theirs .msg-bubble { background: #f0f0f0; color: #333; border-bottom-left-radius: 4px; }
.msg-time { font-size: 11px; color: #999; margin-top: 4px; }
.chat-input { display: flex; gap: 10px; padding: 15px; border-top: 1px solid #eee; }
.chat-input .el-input { flex: 1; }
</style>
