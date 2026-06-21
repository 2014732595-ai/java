<template>
  <el-header class="header">
    <div class="header-content">
      <div class="logo" @click="$router.push('/')">
        <el-icon><ShoppingCart /></el-icon>
        <span>二手交易平台</span>
      </div>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/my-products">我的商品</router-link>
        <router-link to="/favorites">我的收藏</router-link>
        <router-link to="/my-orders">我的订单</router-link>
        <router-link to="/chat">
          <el-badge :value="chatUnreadCount" :hidden="chatUnreadCount === 0" type="danger">
            <el-icon :size="20" style="cursor: pointer"><ChatDotRound /></el-icon>
          </el-badge>
        </router-link>
        <router-link to="/notifications">
          <el-badge :value="notiUnreadCount" :hidden="notiUnreadCount === 0" type="warning">
            <el-icon :size="20" style="cursor: pointer"><Bell /></el-icon>
          </el-badge>
        </router-link>
        <router-link to="/publish">
          <el-button type="primary" size="small">发布商品</el-button>
        </router-link>
        <el-dropdown v-if="userStore.userInfo" trigger="click">
          <span class="user-info">
            <el-avatar :size="28">{{ userStore.userInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
            {{ userStore.userInfo.nickname }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { getUnreadCount, getNotificationUnreadCount } from '../api'
import { ChatDotRound, Bell } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const chatUnreadCount = ref(0)
const notiUnreadCount = ref(0)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const loadUnread = async () => {
  if (!userStore.token) return
  try {
    const res = await getUnreadCount()
    chatUnreadCount.value = res.count || 0
  } catch (e) {}
  try {
    const res2 = await getNotificationUnreadCount()
    notiUnreadCount.value = res2.count || 0
  } catch (e) {}
}

onMounted(() => {
  loadUnread()
  // 每 30 秒刷新一次未读数
  setInterval(loadUnread, 30000)
})
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}
.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}
.nav-links a {
  text-decoration: none;
  color: #333;
}
.nav-links a:hover {
  color: #409eff;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
</style>
