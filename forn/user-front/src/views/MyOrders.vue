<template>
  <div class="my-orders">
    <Header />
    <div class="main-content">
      <el-card>
        <el-tabs v-model="activeTab" @tab-change="loadOrders">
          <el-tab-pane label="我买的" name="buy" />
          <el-tab-pane label="我卖的" name="sell" />
        </el-tabs>
        <el-table :data="orders" v-loading="loading" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="200" />
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="收货地址" min-width="200" />
          <el-table-column prop="createTime" label="下单时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button v-if="activeTab === 'buy' && row.status === 0" type="primary" link @click="payOrder(row.id)">付款</el-button>
              <el-button v-if="activeTab === 'buy' && row.status === 0" type="danger" link @click="cancelOrder(row.id)">取消</el-button>
              <el-button v-if="activeTab === 'sell' && row.status === 1" type="warning" link @click="shipOrder(row.id)">发货</el-button>
              <el-button v-if="activeTab === 'buy' && row.status === 2" type="success" link @click="confirmReceive(row.id)">确认收货</el-button>
              <el-button v-if="activeTab === 'buy' && row.status === 3 && !row.reviewed" type="primary" link @click="openReview(row)">去评价</el-button>
              <el-tag v-if="activeTab === 'buy' && row.status === 3 && row.reviewed" type="info" size="small">已评价</el-tag>
              <el-button v-if="activeTab === 'buy' && (row.status === 1 || row.status === 2) && !row.refundStatus" type="warning" link @click="openRefund(row)">申请退款</el-button>
              <el-tag v-if="activeTab === 'buy' && row.refundStatus === 1" type="warning" size="small">退款中</el-tag>
              <el-tag v-if="activeTab === 'buy' && row.refundStatus === 2" type="success" size="small">已退款</el-tag>
              <el-tag v-if="activeTab === 'buy' && row.refundStatus === 3" type="danger" size="small">退款被拒</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadOrders" style="justify-content: center; margin-top: 20px" />
      </el-card>
    </div>
    <ReviewDialog ref="reviewDialogRef" :order-id="reviewOrderId" :product-id="reviewProductId" @success="loadOrders" />

    <!-- 退款申请弹窗 -->
    <el-dialog v-model="refundVisible" title="申请退款" width="450px">
      <el-form label-width="80px">
        <el-form-item label="退款原因">
          <el-input v-model="refundReason" type="textarea" :rows="4" placeholder="请填写退款原因..." maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="warning" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/Header.vue'
import ReviewDialog from '../components/ReviewDialog.vue'
import { getBuyOrders, getSellOrders, updateOrderStatus, checkReviewed, applyRefund } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('buy')
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orders = ref([])
const reviewDialogRef = ref(null)
const reviewOrderId = ref(0)
const reviewProductId = ref(0)
const refundVisible = ref(false)
const refundOrderId = ref(0)
const refundReason = ref('')

const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }
const statusTypeMap = { 0: 'warning', 1: '', 2: 'primary', 3: 'success', 4: 'info' }
const statusText = (s) => statusMap[s] || ''
const statusType = (s) => statusTypeMap[s] || ''

const loadOrders = async () => {
  loading.value = true
  try {
    const api = activeTab.value === 'buy' ? getBuyOrders : getSellOrders
    const res = await api({ pageNum: pageNum.value, pageSize: pageSize.value })
    // 检查每个已完成订单是否已评价
    if (activeTab.value === 'buy') {
      for (const order of res.records) {
        if (order.status === 3) {
          try {
            const check = await checkReviewed(order.id)
            order.reviewed = check.reviewed
          } catch (e) {
            order.reviewed = false
          }
        }
      }
    }
    orders.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const payOrder = async (id) => {
  try {
    await updateOrderStatus(id, 1)
    ElMessage.success('付款成功')
    loadOrders()
  } catch (e) {}
}

const cancelOrder = async (id) => {
  ElMessageBox.confirm('确定取消此订单？').then(async () => {
    await updateOrderStatus(id, 4)
    ElMessage.success('已取消')
    loadOrders()
  }).catch(() => {})
}

const shipOrder = async (id) => {
  try {
    await updateOrderStatus(id, 2)
    ElMessage.success('已发货')
    loadOrders()
  } catch (e) {}
}

const confirmReceive = async (id) => {
  try {
    await updateOrderStatus(id, 3)
    ElMessage.success('已确认收货')
    loadOrders()
  } catch (e) {}
}

onMounted(() => { loadOrders() })

const openReview = (row) => {
  reviewOrderId.value = row.id
  reviewProductId.value = row.productId
  reviewDialogRef.value.open()
}

const openRefund = (row) => {
  refundOrderId.value = row.id
  refundReason.value = ''
  refundVisible.value = true
}

const submitRefund = async () => {
  if (!refundReason.value.trim()) return ElMessage.warning('请填写退款原因')
  try {
    await applyRefund({ orderId: refundOrderId.value, reason: refundReason.value })
    ElMessage.success('退款申请已提交')
    refundVisible.value = false
    loadOrders()
  } catch (e) {}
}
</script>

<style scoped>
.my-orders { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 20px; }
</style>
