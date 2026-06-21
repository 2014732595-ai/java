<template>
  <el-dialog v-model="visible" title="发表评价" width="500px" :close-on-click-modal="false">
    <el-form label-width="80px">
      <el-form-item label="评分">
        <el-rate v-model="rating" :max="5" show-text :texts="['很差', '较差', '一般', '满意', '非常满意']" />
      </el-form-item>
      <el-form-item label="评价内容">
        <el-input v-model="content" type="textarea" :rows="4" placeholder="分享您的使用体验..." maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">提交评价</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { createReview } from '../api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  orderId: Number,
  productId: Number
})

const emit = defineEmits(['success'])

const visible = ref(false)
const rating = ref(5)
const content = ref('')
const submitting = ref(false)

const open = () => {
  rating.value = 5
  content.value = ''
  visible.value = true
}

const handleSubmit = async () => {
  if (!rating.value) return ElMessage.warning('请选择评分')
  submitting.value = true
  try {
    await createReview({
      orderId: props.orderId,
      productId: props.productId,
      rating: rating.value,
      content: content.value
    })
    ElMessage.success('评价成功')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
