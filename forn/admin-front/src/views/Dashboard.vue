<template>
  <div class="dashboard">
    <!-- 顶部数字卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a"><el-icon><Goods /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.productCount || 0 }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c"><el-icon><List /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.orderCount || 0 }}</div>
              <div class="stat-label">订单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.totalAmount || 0 }}</div>
              <div class="stat-label">交易总额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #909399"><el-icon><RefreshLeft /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.refundCount || 0 }}</div>
              <div class="stat-label">退款申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #b37feb"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.refundRate || 0 }}%</div>
              <div class="stat-label">退款率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span class="card-title">近 7 天订单趋势</span></template>
          <div ref="orderTrendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span class="card-title">商品分类占比</span></template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStats } from '../api'

const stats = ref({})
const orderTrendChartRef = ref(null)
const categoryChartRef = ref(null)
let orderTrendChart = null
let categoryChart = null

const loadStats = async () => {
  try {
    stats.value = await getStats()
    await nextTick()
    renderOrderTrendChart(stats.value.orderTrend || [])
    renderCategoryChart(stats.value.categoryStats || [])
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const renderOrderTrendChart = (orderTrend) => {
  if (!orderTrendChartRef.value) return
  orderTrendChart = echarts.init(orderTrendChartRef.value)
  const dates = orderTrend.map(item => item.date)
  const counts = orderTrend.map(item => item.count)
  orderTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { fontSize: 12 }
    },
    series: [{
      name: '订单数',
      type: 'line',
      data: counts,
      smooth: true,
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.35)' },
          { offset: 1, color: 'rgba(64,158,255,0.05)' }
        ])
      },
      lineStyle: { width: 3 }
    }]
  })
}

const renderCategoryChart = (categoryStats) => {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  const data = categoryStats.map(item => ({ name: item.categoryName, value: item.count }))
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { fontSize: 12 }
    },
    series: [{
      name: '分类',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: data,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      }
    }]
  })
}

const handleResize = () => {
  orderTrendChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  orderTrendChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}
.stat-value {
  font-size: 20px;
  font-weight: bold;
}
.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}
.chart-row {
  margin-bottom: 20px;
}
.chart-container {
  height: 340px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
}
</style>
