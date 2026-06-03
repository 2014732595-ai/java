<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名" style="width: 200px" @keyup.enter="loadUsers" />
      <el-button type="primary" @click="loadUsers">搜索</el-button>
    </div>
    <el-table :data="users" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : ''">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="warning" link @click="toggleStatus(row, 0)">禁用</el-button>
          <el-button v-else type="success" link @click="toggleStatus(row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadUsers" style="justify-content: center; margin-top: 20px" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, updateUserStatus } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const users = ref([])

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
    users.value = res.records
    total.value = res.total
  } catch (e) {} finally {
    loading.value = false
  }
}

const toggleStatus = async (row, status) => {
  try {
    await updateUserStatus(row.id, status)
    ElMessage.success('操作成功')
    loadUsers()
  } catch (e) {}
}

onMounted(() => { loadUsers() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
</style>
