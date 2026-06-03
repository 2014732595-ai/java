<template>
  <div class="my-products">
    <Header />
    <div class="main-content">
      <el-card>
        <h3>我的商品</h3>
        <el-table :data="products" v-loading="loading" style="width: 100%">
          <el-table-column prop="title" label="商品标题" min-width="200" />
          <el-table-column prop="price" label="售价" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="成色" width="100">
            <template #default="{ row }">{{ conditionText(row.conditionLevel) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在售' : '已下架' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 1" type="warning" link @click="handleOffline(row.id)">下架</el-button>
              <el-button v-else type="success" link @click="handleOnline(row.id)">上架</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadProducts" style="justify-content: center; margin-top: 20px" />
      </el-card>

      <!-- 编辑弹窗 -->
      <el-dialog v-model="editVisible" title="编辑商品" width="600px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
          <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
          <el-row :gutter="20">
            <el-col :span="12"><el-form-item label="售价"><el-input-number v-model="editForm.price" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="原价"><el-input-number v-model="editForm.originalPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          </el-row>
        </el-form>
        <template #footer>
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEdit">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/Header.vue'
import { getMyProducts, updateProduct, deleteProduct } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const products = ref([])
const editVisible = ref(false)
const editForm = ref({})
const editId = ref(null)

const conditionMap = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显使用', 5: '功能正常' }
const conditionText = (level) => conditionMap[level] || ''

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getMyProducts({ pageNum: pageNum.value, pageSize: pageSize.value })
    products.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  editId.value = row.id
  editForm.value = { ...row }
  editVisible.value = true
}

const confirmEdit = async () => {
  try {
    await updateProduct(editId.value, editForm.value)
    ElMessage.success('修改成功')
    editVisible.value = false
    loadProducts()
  } catch (e) {}
}

const handleOffline = async (id) => {
  ElMessageBox.confirm('确定下架此商品？', '提示').then(async () => {
    await deleteProduct(id)
    ElMessage.success('已下架')
    loadProducts()
  }).catch(() => {})
}

const handleOnline = async (id) => {
  try {
    await updateProduct(id, { status: 1 })
    ElMessage.success('已上架')
    loadProducts()
  } catch (e) {}
}

onMounted(() => { loadProducts() })
</script>

<style scoped>
.my-products { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 20px; }
.main-content h3 { margin: 0 0 20px; }
</style>
