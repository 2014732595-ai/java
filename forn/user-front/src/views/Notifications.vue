<template>
  <div class="notifications">
    <Header />
    <div class="main-content">
      <div class="noti-header">
        <h2>通知中心</h2>
        <el-button type="primary" link @click="handleReadAll" :disabled="unreadCount === 0">全部标记已读</el-button>
      </div>
      <el-card>
        <div class="noti-list">
          <div v-for="item in notifications" :key="item.id"
               :class="['noti-item', { unread: item.isRead === 0 }]"
               @click="handleRead(item)">
            <div class="noti-icon">
              <el-tag :type="typeColor(item.type)" size="small">{{ typeText(item.type) }}</el-tag>
            </div>
            <div class="noti-body">
              <div class="noti-title">{{ item.title }}</div>
              <div class="noti-content">{{ item.content }}</div>
              <div class="noti-time">{{ item.createTime }}</div>
            </div>
            <el-badge v-if="item.isRead === 0" is-dot type="danger" />
          </div>
          <el-empty v-if="!notifications.length" description="暂无通知" />
        </div>
        <el-pagination v-if="total > 0" v-model:current-page="pageNum" :page-size="pageSize" :total="total"
                       layout="prev, pager, next" @current-change="loadNotifications"
                       style="justify-content: center; margin-top: 20px" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/Header.vue'
import { getNotifications, markNotificationRead, markAllNotificationsRead, getNotificationUnreadCount } from '../api'
import { ElMessage } from 'element-plus'

const notifications = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const unreadCount = ref(0)

const typeColorMap = { ORDER: 'primary', REFUND: 'warning', CHAT: 'success', SYSTEM: 'info' }
const typeTextMap = { ORDER: '订单', REFUND: '退款', CHAT: '消息', SYSTEM: '系统' }
const typeColor = (t) => typeColorMap[t] || 'info'
const typeText = (t) => typeTextMap[t] || '通知'

const loadNotifications = async () => {
  try {
    const res = await getNotifications({ pageNum: pageNum.value, pageSize: pageSize.value })
    notifications.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  }
}

const loadUnread = async () => {
  try {
    const res = await getNotificationUnreadCount()
    unreadCount.value = res.count || 0
  } catch (e) {}
}

const handleRead = async (item) => {
  if (item.isRead === 0) {
    await markNotificationRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

const handleReadAll = async () => {
  await markAllNotificationsRead()
  ElMessage.success('已全部标记为已读')
  notifications.value.forEach(n => n.isRead = 1)
  unreadCount.value = 0
}

onMounted(() => {
  loadNotifications()
  loadUnread()
})
</script>

<style scoped>
.notifications { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 800px; margin: 0 auto; padding: 20px; }
.noti-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.noti-header h2 { margin: 0; }
.noti-item { display: flex; align-items: flex-start; gap: 12px; padding: 14px 0; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.noti-item:hover { background: #fafafa; }
.noti-item.unread { background: #f6f9ff; }
.noti-body { flex: 1; }
.noti-title { font-weight: 500; font-size: 14px; margin-bottom: 4px; }
.noti-content { font-size: 13px; color: #666; line-height: 1.5; }
.noti-time { font-size: 12px; color: #999; margin-top: 6px; }
</style>
