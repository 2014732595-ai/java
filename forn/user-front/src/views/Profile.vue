<template>
  <div class="profile">
    <Header />
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 个人信息 -->
        <el-col :span="10">
          <el-card>
            <h3>个人信息</h3>
            <el-form :model="profileForm" label-width="80px">
              <el-form-item label="用户名">
                <div style="display: flex; align-items: center;">
                  <el-input v-model="profileForm.username" disabled style="margin-right: 10px;" />
                  <el-button type="primary" @click="showUsernameDialog">修改</el-button>
                </div>
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="profileForm.nickname" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveProfile">保存</el-button>
                <el-button type="warning" @click="showPasswordDialog">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- 收货地址 -->
        <el-col :span="14">
          <el-card>
            <div class="card-header">
              <h3>收货地址</h3>
              <el-button type="primary" size="small" @click="showAddAddress">新增地址</el-button>
            </div>
            <el-table :data="addresses" style="width: 100%">
              <el-table-column prop="receiver" label="收货人" width="100" />
              <el-table-column prop="phone" label="电话" width="120" />
              <el-table-column prop="detail" label="详细地址" />
              <el-table-column label="默认" width="80">
                <template #default="{ row }">
                  <el-tag v-if="row.isDefault === 1" type="success" size="small">默认</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="primary" link @click="editAddress(row)">编辑</el-button>
                  <el-button type="danger" link @click="removeAddress(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <!-- 地址编辑弹窗 -->
      <el-dialog v-model="addressVisible" :title="addressForm.id ? '编辑地址' : '新增地址'" width="500px">
        <el-form :model="addressForm" label-width="80px">
          <el-form-item label="收货人"><el-input v-model="addressForm.receiver" /></el-form-item>
          <el-form-item label="联系电话"><el-input v-model="addressForm.phone" /></el-form-item>
          <el-form-item label="详细地址"><el-input v-model="addressForm.detail" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="设为默认">
            <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addressVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddress">保存</el-button>
        </template>
      </el-dialog>

      <!-- 修改密码弹窗 -->
      <el-dialog v-model="passwordVisible" :title="isFirstSetPassword ? '设置密码' : '修改密码'" width="500px">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword" v-if="!isFirstSetPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（6-20 位数字或字母）" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
          </el-form-item>
          <el-form-item>
            <el-alert 
              v-if="isFirstSetPassword"
              title="首次设置密码"
              type="info"
              description="您是通过手机号登录的新用户，请设置您的登录密码。设置后可以使用账号密码登录。"
              :closable="false"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="passwordVisible = false">取消</el-button>
          <el-button type="primary" @click="savePassword" :loading="savingPassword">保存</el-button>
        </template>
      </el-dialog>

      <!-- 修改用户名弹窗 -->
      <el-dialog v-model="usernameVisible" title="修改用户名" width="500px">
        <el-form :model="usernameForm" :rules="usernameRules" ref="usernameFormRef" label-width="100px">
          <el-form-item label="原用户名">
            <el-input v-model="usernameForm.oldUsername" disabled />
          </el-form-item>
          <el-form-item label="新用户名" prop="newUsername">
            <el-input v-model="usernameForm.newUsername" placeholder="请输入新用户名（3-20 位，字母、数字、下划线）" />
          </el-form-item>
          <el-form-item>
            <el-alert 
              title="用户名规则"
              type="info"
              description="用户名长度为 3-20 位，只能包含字母、数字和下划线，且不能与他人重复。"
              :closable="false"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="usernameVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUsername" :loading="savingUsername">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import Header from '../components/Header.vue'
import { getUserInfo, updateProfile, getAddressList, createAddress, updateAddress, deleteAddress, updatePassword, updateUsername } from '../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const profileForm = reactive({ id: null, username: '', nickname: '', phone: '' })
const addresses = ref([])
const addressVisible = ref(false)
const addressForm = reactive({ id: null, receiver: '', phone: '', detail: '', isDefault: 0 })

// 修改密码相关
const passwordVisible = ref(false)
const passwordFormRef = ref(null)
const savingPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 修改用户名相关
const usernameVisible = ref(false)
const usernameFormRef = ref(null)
const savingUsername = ref(false)
const usernameForm = reactive({
  oldUsername: '',
  newUsername: ''
})

// 密码验证规则
const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (!/^[a-zA-Z0-9]{6,20}$/.test(value)) {
    callback(new Error('密码格式不正确，长度为 6-20 位数字或字母'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 用户名验证规则
const validateNewUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新用户名'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度必须在 3-20 位之间'))
  } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字和下划线'))
  } else {
    callback()
  }
}

const usernameRules = {
  newUsername: [
    { validator: validateNewUsername, trigger: 'blur' }
  ]
}

// 判断是否是首次设置密码
const isFirstSetPassword = ref(false)

const loadProfile = async () => {
  try {
    const info = await getUserInfo()
    Object.assign(profileForm, info)
    // 判断是否是首次设置密码（密码为空）
    isFirstSetPassword.value = !info.password || info.password.trim() === ''
  } catch (e) {}
}

const saveProfile = async () => {
  try {
    await updateProfile(profileForm)
    userStore.setInfo({ ...userStore.userInfo, ...profileForm })
    ElMessage.success('保存成功')
  } catch (e) {}
}

// 显示修改密码弹窗
const showPasswordDialog = () => {
  // 重置表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  // 如果是首次设置密码，不验证旧密码
  if (isFirstSetPassword.value) {
    passwordRules.oldPassword = []
  } else {
    passwordRules.oldPassword = [
      { required: true, message: '请输入原密码', trigger: 'blur' }
    ]
  }
  passwordVisible.value = true
}

// 保存密码
const savePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  savingPassword.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success(isFirstSetPassword.value ? '密码设置成功' : '密码修改成功')
    passwordVisible.value = false
    // 重新加载用户信息
    await loadProfile()
  } catch (e) {
    console.error(e)
  } finally {
    savingPassword.value = false
  }
}

const loadAddresses = async () => {
  try { addresses.value = await getAddressList() } catch (e) {}
}

const showAddAddress = () => {
  Object.assign(addressForm, { id: null, receiver: '', phone: '', detail: '', isDefault: 0 })
  addressVisible.value = true
}

const editAddress = (row) => {
  Object.assign(addressForm, row)
  addressVisible.value = true
}

const saveAddress = async () => {
  try {
    if (addressForm.id) {
      await updateAddress(addressForm.id, addressForm)
    } else {
      await createAddress(addressForm)
    }
    ElMessage.success('保存成功')
    addressVisible.value = false
    loadAddresses()
  } catch (e) {}
}

const removeAddress = async (id) => {
  try {
    await deleteAddress(id)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (e) {}
}

// 显示修改用户名弹窗
const showUsernameDialog = () => {
  // 重置表单
  usernameForm.oldUsername = profileForm.username
  usernameForm.newUsername = ''
  usernameVisible.value = true
}

// 保存用户名
const saveUsername = async () => {
  const valid = await usernameFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  savingUsername.value = true
  try {
    const res = await updateUsername({
      username: usernameForm.newUsername
    })
    ElMessage.success('用户名修改成功')
    usernameVisible.value = false
    
    // 更新 Token
    if (res.data && res.data.token) {
      localStorage.setItem('token', res.data.token)
    }
    
    // 更新本地状态
    profileForm.username = usernameForm.newUsername
    userStore.setInfo({ ...userStore.userInfo, username: usernameForm.newUsername })
    // 重新加载用户信息
    await loadProfile()
  } catch (e) {
    console.error(e)
  } finally {
    savingUsername.value = false
  }
}

onMounted(() => { loadProfile(); loadAddresses() })
</script>

<style scoped>
.profile { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 20px; }
h3 { margin: 0 0 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.card-header h3 { margin: 0; }
</style>
