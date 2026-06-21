<template>
  <el-card>
    <el-table :data="reviews" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="productId" label="商品ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column label="评分" width="120">
        <template #default="{ row }">
          <el-rate :model-value="row.rating" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="250">
        <template #default="{ row }">{{ row.content || '无文字评价' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="评价时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" link @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadReviews" style="justify-content: center; margin-top: 20px" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllReviews, deleteReview } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const reviews = ref([])

const loadReviews = async () => {
  loading.value = true
  try {
    const res = await getAllReviews({ pageNum: pageNum.value, pageSize: pageSize.value })
    reviews.value = res.records
    total.value = res.total
  } catch (e) {} finally {
    loading.value = false
  }
}

const remove = async (id) => {
  ElMessageBox.confirm('确定删除此评价？').then(async () => {
    await deleteReview(id)
    ElMessage.success('删除成功')
    loadReviews()
  }).catch(() => {})
}

onMounted(() => { loadReviews() })
</script>
