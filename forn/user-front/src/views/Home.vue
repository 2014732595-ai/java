<template>
  <div class="home">
    <Header />
    <div class="main-content">
      <!-- 搜索和分类 -->
      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索商品" clearable style="width: 300px" @keyup.enter="loadProducts">
          <template #append>
            <el-button @click="loadProducts"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-select v-model="categoryId" placeholder="全部分类" clearable style="width: 150px" @change="loadProducts">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </div>

      <!-- 商品列表 -->
      <div class="product-grid" v-loading="loading">
        <el-card class="product-card" v-for="item in productList" :key="item.id" @click="$router.push(`/product/${item.id}`)">
          <div class="product-image">
            <el-image v-if="item.images" :src="item.images.split(',')[0]" fit="cover" />
            <div v-else class="no-image">暂无图片</div>
          </div>
          <div class="product-info">
            <h4 class="product-title">{{ item.title }}</h4>
            <p class="product-desc">{{ item.description?.slice(0, 50) }}...</p>
            <div class="product-price">
              <span class="price">¥{{ item.price }}</span>
              <span class="original-price" v-if="item.originalPrice">¥{{ item.originalPrice }}</span>
            </div>
            <div class="product-meta">
              <el-tag size="small" type="info">{{ conditionText(item.conditionLevel) }}</el-tag>
              <span class="time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadProducts" style="justify-content: center; margin-top: 30px" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/Header.vue'
import { getProducts, getCategories } from '../api'

const loading = ref(false)
const keyword = ref('')
const categoryId = ref(null)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const productList = ref([])
const categories = ref([])

const conditionMap = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显使用', 5: '功能正常' }

const conditionText = (level) => conditionMap[level] || ''

const formatTime = (time) => time ? time.slice(0, 10) : ''

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getProducts({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value, categoryId: categoryId.value })
    productList.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #f5f7fa;
}
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  background: #fff;
  padding: 15px;
  border-radius: 8px;
}
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
.product-image .el-image {
  width: 100%;
  height: 100%;
}
.no-image {
  color: #999;
  font-size: 14px;
}
.product-info {
  padding: 12px 0 0;
}
.product-title {
  font-size: 14px;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-desc {
  font-size: 12px;
  color: #999;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-price {
  margin-bottom: 8px;
}
.price {
  font-size: 20px;
  font-weight: bold;
  color: #ff4d4f;
}
.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}
.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}
</style>
