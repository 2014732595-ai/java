<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>用户登录</h2>
      
      <!-- 登录方式切换 -->
      <el-tabs v-model="loginType" class="login-tabs">
        <el-tab-pane label="账号登录" name="account"></el-tab-pane>
        <el-tab-pane label="手机号登录" name="sms"></el-tab-pane>
      </el-tabs>

      <!-- 账号密码登录 -->
      <el-form v-if="loginType === 'account'" :model="accountForm" :rules="accountRules" ref="accountFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="accountForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="accountForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAccountLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
        <el-form-item>
          <span>还没有账号？<router-link to="/register">立即注册</router-link></span>
        </el-form-item>
      </el-form>

      <!-- 手机号验证码登录 -->
      <el-form v-else :model="smsForm" :rules="smsRules" ref="smsFormRef" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="smsForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input">
            <el-input v-model="smsForm.code" placeholder="请输入验证码" maxlength="6" />
            <el-button 
              type="primary" 
              @click="handleSendCode" 
              :loading="sendingCode" 
              :disabled="countdown > 0"
              class="send-code-btn"
            >
              {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSmsLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
        <el-form-item>
          <span>还没有账号？<router-link to="/register">立即注册</router-link></span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login, loginBySms, sendSmsCode, getUserInfo } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const accountFormRef = ref(null)
const smsFormRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const loginType = ref('account')

// 账号密码登录表单
const accountForm = reactive({
  username: '',
  password: ''
})

const accountRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 手机号登录表单
const smsForm = reactive({
  phone: '',
  code: ''
})

const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码格式不正确', trigger: 'blur' }
  ]
}

// 发送验证码
const handleSendCode = async () => {
  const valid = await smsFormRef.value.validateField('phone').catch(() => false)
  if (!valid) return

  sendingCode.value = true
  try {
    await sendSmsCode({ phone: smsForm.phone })
    ElMessage.success('验证码已发送，请查看后端控制台')
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e) {
    console.error(e)
  } finally {
    sendingCode.value = false
  }
}

// 账号密码登录
const handleAccountLogin = async () => {
  const valid = await accountFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const token = await login(accountForm)
    userStore.setToken(token.token)
    const info = await getUserInfo()
    userStore.setInfo(info)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 手机号验证码登录
const handleSmsLogin = async () => {
  const valid = await smsFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const result = await loginBySms(smsForm)
    console.log('登录结果:', result)
    
    if (!result || !result.token) {
      ElMessage.error('登录失败：未获取到 token')
      return
    }
    
    userStore.setToken(result.token)
    userStore.setInfo(result.user)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (e) {
    console.error('登录错误:', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
}
.login-card {
  width: 450px;
}
.login-card h2 {
  text-align: center;
  margin-bottom: 20px;
}
.login-tabs {
  margin-bottom: 20px;
}
.code-input {
  display: flex;
  align-items: center;
  gap: 10px;
}
.code-input .el-input {
  flex: 1;
}
.send-code-btn {
  width: 120px;
}
</style>
