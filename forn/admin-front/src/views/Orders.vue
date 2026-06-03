<template>
  <el-card>
    <el-table :data="orders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column label="买家" width="100">
        <template #default="{ row }">用户{{ row.buyerId }}</template>
      </el-table-column>
      <el-table-column label="卖家" width="100">
        <template #default="{ row }">用户{{ row.sellerId }}</template>
      </el-table-column>
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
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadOrders" style="justify-content: center; margin-top: 20px" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllOrders } from '../api'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orders = ref([])

const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }
const statusTypeMap = { 0: 'warning', 1: '', 2: 'primary', 3: 'success', 4: 'info' }
const statusText = (s) => statusMap[s] || ''
const statusType = (s) => statusTypeMap[s] || ''

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getAllOrders({ pageNum: pageNum.value, pageSize: pageSize.value })
    orders.value = res.records
    total.value = res.total
  } catch (e) {} finally {
    loading.value = false
  }
}

onMounted(() => { loadOrders() })
</script>
