<template>
  <div class="detail">
    <Header />
    <div class="main-content" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="image-gallery">
            <el-image :src="currentImage" fit="contain" style="width: 100%; height: 400px; background: #f5f7fa" />
            <div class="thumbnails" v-if="images.length > 1">
              <img v-for="(img, i) in images" :key="i" :src="img" :class="{ active: currentImage === img }" @click="currentImage = img" />
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <h2>{{ product.title }}</h2>
          <div class="price-section">
            <span class="current-price">¥{{ product.price }}</span>
            <span class="original-price" v-if="product.originalPrice">原价 ¥{{ product.originalPrice }}</span>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="成色">{{ conditionText(product.conditionLevel) }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ categoryName }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ product.createTime }}</el-descriptions-item>
          </el-descriptions>
          <div class="description">
            <h4>商品描述</h4>
            <p>{{ product.description || '暂无描述' }}</p>
          </div>
          <div class="actions" v-if="product.sellerId !== userStore.userInfo?.id">
            <el-button type="primary" size="large" @click="handleBuy">立即购买</el-button>
          </div>
          <div class="actions" v-else>
            <el-tag type="info">这是您发布的商品</el-tag>
          </div>
        </el-col>
      </el-row>

      <!-- 留言区 -->
      <div class="comments-section">
        <h3>商品留言</h3>
        <div class="comment-input">
          <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="输入留言内容..." />
          <el-button type="primary" @click="submitComment" :disabled="!userStore.token">发表留言</el-button>
        </div>
        <div class="comment-list">
          <div class="comment-item" v-for="c in comments" :key="c.id">
            <div class="comment-user">用户{{ c.userId }}</div>
            <div class="comment-text">{{ c.content }}</div>
            <div class="comment-time">{{ c.createTime }}</div>
          </div>
          <el-empty v-if="!comments.length" description="暂无留言" />
        </div>
      </div>
    </div>

    <!-- 购买弹窗 -->
    <el-dialog v-model="buyVisible" title="确认购买" width="400px">
      <el-form label-width="80px">
        <el-form-item label="收货地址">
          <el-select v-model="selectedAddressId" placeholder="选择收货地址" style="width: 100%">
            <el-option v-for="addr in addresses" :key="addr.id" :label="addr.detail" :value="addr.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBuy">确认购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import Header from '../components/Header.vue'
import { getProductDetail, getCategories, getComments, createComment, createOrder, getAddressList } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const product = ref({})
const categories = ref([])
const comments = ref([])
const commentContent = ref('')
const addresses = ref([])
const buyVisible = ref(false)
const selectedAddressId = ref(null)

const conditionMap = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显使用', 5: '功能正常' }
const conditionText = (level) => conditionMap[level] || ''
const categoryName = computed(() => {
  const cat = categories.value.find(c => c.id === product.value.categoryId)
  return cat ? cat.name : '-'
})

const images = computed(() => {
  if (!product.value.images) return []
  return product.value.images.split(',')
})
const currentImage = ref('')

const loadDetail = async () => {
  loading.value = true
  try {
    product.value = await getProductDetail(route.params.id)
    currentImage.value = images.value[0] || ''
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try { categories.value = await getCategories() } catch (e) {}
}

const loadComments = async () => {
  try { comments.value = await getComments(route.params.id) } catch (e) {}
}

const loadAddresses = async () => {
  try { addresses.value = await getAddressList() } catch (e) {}
}

const submitComment = async () => {
  if (!commentContent.value.trim()) return ElMessage.warning('请输入留言内容')
  try {
    await createComment({ productId: route.params.id, content: commentContent.value })
    commentContent.value = ''
    ElMessage.success('留言成功')
    loadComments()
  } catch (e) {}
}

const handleBuy = async () => {
  if (!userStore.token) return router.push('/login')
  await loadAddresses()
  buyVisible.value = true
}

const confirmBuy = async () => {
  if (!selectedAddressId.value) return ElMessage.warning('请选择收货地址')
  try {
    await createOrder({ productId: route.params.id, addressId: selectedAddressId.value })
    ElMessage.success('下单成功')
    buyVisible.value = false
    router.push('/my-orders')
  } catch (e) {}
}

onMounted(() => {
  loadDetail()
  loadCategories()
  loadComments()
})
</script>

<style scoped>
.detail { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 20px; }
.image-gallery { background: #fff; padding: 20px; border-radius: 8px; }
.thumbnails { display: flex; gap: 8px; margin-top: 12px; }
.thumbnails img { width: 60px; height: 60px; object-fit: cover; border: 2px solid transparent; border-radius: 4px; cursor: pointer; }
.thumbnails img.active { border-color: #409eff; }
.price-section { margin: 16px 0; }
.current-price { font-size: 28px; font-weight: bold; color: #ff4d4f; }
.original-price { font-size: 14px; color: #999; text-decoration: line-through; margin-left: 12px; }
.description { margin-top: 20px; padding: 16px; background: #fff; border-radius: 8px; }
.description h4 { margin: 0 0 10px; }
.description p { color: #666; line-height: 1.6; }
.actions { margin-top: 20px; }
.comments-section { margin-top: 30px; background: #fff; padding: 20px; border-radius: 8px; }
.comment-input { display: flex; gap: 10px; margin-bottom: 20px; align-items: flex-end; }
.comment-input .el-input { flex: 1; }
.comment-item { padding: 12px 0; border-bottom: 1px solid #eee; }
.comment-user { font-weight: bold; font-size: 14px; }
.comment-text { margin: 6px 0; color: #333; }
.comment-time { font-size: 12px; color: #999; }
</style>
