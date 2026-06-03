<template>
  <div class="publish">
    <Header />
    <div class="main-content">
      <el-card>
        <h3>发布商品</h3>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="商品标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="商品描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述商品" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="售价" prop="price">
                <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="原价">
                <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
                  <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="成色" prop="conditionLevel">
                <el-select v-model="form.conditionLevel" placeholder="选择成色" style="width: 100%">
                  <el-option label="全新" :value="1" />
                  <el-option label="几乎全新" :value="2" />
                  <el-option label="轻微使用" :value="3" />
                  <el-option label="明显使用" :value="4" />
                  <el-option label="功能正常" :value="5" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="商品图片">
                <el-upload action="/api/file/upload" list-type="picture-card" :headers="uploadHeaders" :on-success="handleUploadSuccess" :on-remove="handleUploadRemove" :file-list="fileList" :limit="6">
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="loading">发布商品</el-button>
            <el-button @click="$router.push('/')">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/Header.vue'
import { createProduct, getCategories } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const categories = ref([])
const fileList = ref([])
const imageUrls = ref([])

const form = reactive({
  title: '',
  description: '',
  price: null,
  originalPrice: null,
  categoryId: null,
  conditionLevel: null,
  images: ''
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }]
}

const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    imageUrls.value.push(res.data)
  }
}

const handleUploadRemove = (file) => {
  const url = file.response?.data || file.url
  imageUrls.value = imageUrls.value.filter(u => u !== url)
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  form.images = imageUrls.value.join(',')
  loading.value = true
  try {
    await createProduct(form)
    ElMessage.success('发布成功')
    router.push('/my-products')
  } catch (e) {} finally {
    loading.value = false
  }
}

onMounted(async () => {
  try { categories.value = await getCategories() } catch (e) {}
})
</script>

<style scoped>
.publish { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 900px; margin: 0 auto; padding: 20px; }
.main-content h3 { margin: 0 0 20px; }
</style>
