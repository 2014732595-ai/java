import request from '../utils/request'

// 管理员登录
export const adminLogin = (data) => request.post('/admin/auth/login', data)

// 用户管理
export const getUserList = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, { status })

// 商品管理
export const getAllProducts = (params) => request.get('/admin/products', { params })
export const forceOfflineProduct = (id) => request.put(`/admin/products/${id}/status`)

// 分类管理
export const getCategories = () => request.get('/admin/categories')
export const createCategory = (data) => request.post('/admin/categories', data)
export const updateCategory = (id, data) => request.put(`/admin/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/admin/categories/${id}`)

// 订单管理
export const getAllOrders = (params) => request.get('/admin/orders', { params })

// 留言管理
export const getComments = (params) => request.get('/admin/comments', { params })
export const deleteComment = (id) => request.delete(`/admin/comments/${id}`)

// 评价管理
export const getAllReviews = (params) => request.get('/admin/reviews', { params })
export const deleteReview = (id) => request.delete(`/admin/reviews/${id}`)

// 退款管理
export const getAllRefunds = (params) => request.get('/admin/refunds', { params })
export const approveRefund = (id, remark) => request.put(`/admin/refunds/${id}/approve`, { remark })
export const rejectRefund = (id, remark) => request.put(`/admin/refunds/${id}/reject`, { remark })

// 数据统计
export const getStats = () => request.get('/admin/stats')
