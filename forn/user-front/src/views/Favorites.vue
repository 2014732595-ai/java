<template>
  <div class="favorites">
    <Header />
    <div class="main-content">
      <h2>我的收藏</h2>
      <div class="product-grid" v-loading="loading">
        <el-card class="product-card" v-for="item in productList" :key="item.id" @click="$router.push(`/product/${item.id}`)">
          <div class="product-image">
            <el-image v-if="item.images" :src="item.images.split(',')[0]" fit="cover" />
            <div v-else class="no-image">暂无图片</div>
          </div>
          <div class="product-info">
            <h4 class="product-title">{{ item.title }}</h4>
            <div class="product-price">
              <span class="price">¥{{ item.price }}</span>
              <span class="original-price" v-if="item.originalPrice">¥{{ item.originalPrice }}</span>
            </div>
            <div class="product-actions">
              <el-button type="danger" size="small" plain @click.stop="handleRemove(item.id)">取消收藏</el-button>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty v-if="!loading && !productList.length" description="暂无收藏商品" />
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadFavorites"
        style="justify-content: center; margin-top: 30px"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/Header.vue'
import { getFavorites, removeFavorite } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const productList = ref([])

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavorites({ pageNum: pageNum.value, pageSize: pageSize.value })
    productList.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleRemove = async (productId) => {
  try {
    await removeFavorite(productId)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 20px; }
.main-content h2 { margin-bottom: 20px; }
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.product-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}
.product-image {
  height: 180px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.product-image .el-image { width: 100%; height: 100%; }
.no-image { color: #999; font-size: 14px; }
.product-info { padding: 12px 0 0; }
.product-title {
  font-size: 14px;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-price { margin-bottom: 10px; }
.price { font-size: 20px; font-weight: bold; color: #ff4d4f; }
.original-price { font-size: 12px; color: #999; text-decoration: line-through; margin-left: 8px; }
.product-actions { display: flex; justify-content: flex-end; }
</style>
