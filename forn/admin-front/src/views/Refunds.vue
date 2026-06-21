<template>
  <el-card>
    <el-table :data="refunds" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderId" label="订单ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column label="退款金额" width="100">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="退款原因" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminRemark" label="管理员备注" width="150">
        <template #default="{ row }">{{ row.adminRemark || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button type="success" link @click="handleApprove(row.id)">同意</el-button>
            <el-button type="danger" link @click="handleReject(row.id)">拒绝</el-button>
          </template>
          <span v-else style="color: #999; font-size: 12px">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadRefunds" style="justify-content: center; margin-top: 20px" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllRefunds, approveRefund, rejectRefund } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const refunds = ref([])

const statusMap = { 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '已退款' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'success' }
const statusText = (s) => statusMap[s] || ''
const statusType = (s) => statusTypeMap[s] || ''

const loadRefunds = async () => {
  loading.value = true
  try {
    const res = await getAllRefunds({ pageNum: pageNum.value, pageSize: pageSize.value })
    refunds.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleApprove = async (id) => {
  ElMessageBox.confirm('确定同意此退款申请？').then(async () => {
    await approveRefund(id, '')
    ElMessage.success('已同意退款')
    loadRefunds()
  }).catch(() => {})
}

const handleReject = async (id) => {
  ElMessageBox.prompt('请输入拒绝原因', '拒绝退款', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '请输入拒绝原因'
  }).then(async ({ value }) => {
    await rejectRefund(id, value)
    ElMessage.success('已拒绝退款')
    loadRefunds()
  }).catch(() => {})
}

onMounted(() => { loadRefunds() })
</script>
