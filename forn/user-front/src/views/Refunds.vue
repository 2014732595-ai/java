<template>
  <div class="refunds">
    <Header />
    <div class="main-content">
      <h2>退款记录</h2>
      <el-card>
        <el-table :data="refunds" v-loading="loading" style="width: 100%">
          <el-table-column prop="orderId" label="订单ID" width="100" />
          <el-table-column label="退款金额" width="120">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="退款原因" min-width="200" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="refundStatusType(row.status)">{{ refundStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="adminRemark" label="管理员备注" min-width="150">
            <template #default="{ row }">{{ row.adminRemark || '-' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" type="danger" link @click="handleCancel(row.id)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadRefunds" style="justify-content: center; margin-top: 20px" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/Header.vue'
import { getMyRefunds, cancelRefund } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const refunds = ref([])

const refundStatusMap = { 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '已退款' }
const refundStatusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'success' }
const refundStatusText = (s) => refundStatusMap[s] || ''
const refundStatusType = (s) => refundStatusTypeMap[s] || ''

const loadRefunds = async () => {
  loading.value = true
  try {
    const res = await getMyRefunds({ pageNum: pageNum.value, pageSize: pageSize.value })
    refunds.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id) => {
  ElMessageBox.confirm('确定取消此退款申请？').then(async () => {
    await cancelRefund(id)
    ElMessage.success('已取消')
    loadRefunds()
  }).catch(() => {})
}

onMounted(() => { loadRefunds() })
</script>

<style scoped>
.refunds { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 20px; }
.main-content h2 { margin-bottom: 20px; }
</style>
