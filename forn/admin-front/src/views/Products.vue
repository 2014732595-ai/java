<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索商品标题" style="width: 200px" @keyup.enter="loadProducts" />
      <el-button type="primary" @click="loadProducts">搜索</el-button>
    </div>
    <el-table :data="products" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="price" label="售价" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'danger'">
            {{ row.status === 1 ? '在售' : row.status === 0 ? '已下架' : '管理员下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="danger" link @click="forceOffline(row.id)">强制下架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadProducts" style="justify-content: center; margin-top: 20px" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllProducts, forceOfflineProduct } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const products = ref([])

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getAllProducts({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
    products.value = res.records
    total.value = res.total
  } catch (e) {} finally {
    loading.value = false
  }
}

const forceOffline = async (id) => {
  try {
    await forceOfflineProduct(id)
    ElMessage.success('已强制下架')
    loadProducts()
  } catch (e) {}
}

onMounted(() => { loadProducts() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
</style>
