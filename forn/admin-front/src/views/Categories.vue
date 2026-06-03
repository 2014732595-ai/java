<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="showAdd">新增分类</el-button>
    </div>
    <el-table :data="categories" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" link @click="showEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="400px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="分类名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', sort: 0 })

const loadCategories = async () => {
  loading.value = true
  try { categories.value = await getCategories() } catch (e) {} finally {
    loading.value = false
  }
}

const showAdd = () => {
  Object.assign(form, { id: null, name: '', sort: 0 })
  dialogVisible.value = true
}

const showEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const save = async () => {
  try {
    if (form.id) {
      await updateCategory(form.id, form)
    } else {
      await createCategory(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadCategories()
  } catch (e) {}
}

const remove = async (id) => {
  try {
    await deleteCategory(id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (e) {}
}

onMounted(() => { loadCategories() })
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
