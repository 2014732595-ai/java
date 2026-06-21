import request from '../utils/request'

// 用户登录（账号密码）
export const login = (data) => request.post('/user/auth/login', data)

// 手机号验证码登录
export const loginBySms = (data) => request.post('/user/auth/login-by-sms', data)

// 发送验证码
export const sendSmsCode = (data) => request.post('/user/auth/sms-code', data)

// 用户注册
export const register = (data) => request.post('/user/auth/register', data)

// 获取个人信息
export const getUserInfo = () => request.get('/user/profile')

// 更新个人信息
export const updateProfile = (data) => request.put('/user/profile', data)

// 修改密码
export const updatePassword = (data) => request.post('/user/profile/update-password', data)

// 修改用户名
export const updateUsername = (data) => request.put('/user/profile/username', data)

// 商品列表
export const getProducts = (params) => request.get('/user/products', { params })

// 商品详情
export const getProductDetail = (id) => request.get(`/user/products/${id}`)

// 发布商品
export const createProduct = (data) => request.post('/user/products', data)

// 编辑商品
export const updateProduct = (id, data) => request.put(`/user/products/${id}`, data)

// 下架商品
export const deleteProduct = (id) => request.delete(`/user/products/${id}`)

// 商品上下架切换
export const toggleProductStatus = (id, status) => request.put(`/user/products/${id}/status`, { status })

// 我的商品
export const getMyProducts = (params) => request.get('/user/products/my', { params })

// 创建订单
export const createOrder = (data) => request.post('/user/orders', data)

// 我的购买订单
export const getBuyOrders = (params) => request.get('/user/orders/buy', { params })

// 我的卖出订单
export const getSellOrders = (params) => request.get('/user/orders/sell', { params })

// 更新订单状态
export const updateOrderStatus = (id, status) => request.put(`/user/orders/${id}/status`, { status })

// 分类列表
export const getCategories = () => request.get('/user/categories')

// 商品留言列表
export const getComments = (productId) => request.get(`/user/comments/${productId}`)

// 发布留言
export const createComment = (data) => request.post('/user/comments', data)

// 地址列表
export const getAddressList = () => request.get('/user/address/list')

// 创建地址
export const createAddress = (data) => request.post('/user/address', data)

// 更新地址
export const updateAddress = (id, data) => request.put(`/user/address/${id}`, data)

// 删除地址
export const deleteAddress = (id) => request.delete(`/user/address/${id}`)

// 默认地址
export const getDefaultAddress = () => request.get('/user/address/default')

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 收藏
export const addFavorite = (productId) => request.post('/user/favorites', { productId })
export const removeFavorite = (productId) => request.delete(`/user/favorites/${productId}`)
export const getFavorites = (params) => request.get('/user/favorites', { params })
export const checkFavorite = (productId) => request.get(`/user/favorites/check/${productId}`)

// 评价
export const createReview = (data) => request.post('/user/reviews', data)
export const getProductReviews = (productId, params) => request.get(`/user/reviews/product/${productId}`, { params })
export const getMyReviews = (params) => request.get('/user/reviews/my', { params })
export const checkReviewed = (orderId) => request.get(`/user/reviews/check/${orderId}`)

// 退款
export const applyRefund = (data) => request.post('/user/refunds', data)
export const getMyRefunds = (params) => request.get('/user/refunds/my', { params })
export const cancelRefund = (id) => request.put(`/user/refunds/${id}/cancel`)

// 聊天
export const getConversations = () => request.get('/user/chat/conversations')
export const getMessages = (userId, params) => request.get(`/user/chat/messages/${userId}`, { params })
export const markChatRead = (fromId) => request.put(`/user/chat/read/${fromId}`)
export const getUnreadCount = () => request.get('/user/chat/unread-count')

// 通知
export const getNotifications = (params) => request.get('/user/notifications', { params })
export const markNotificationRead = (id) => request.put(`/user/notifications/${id}/read`)
export const markAllNotificationsRead = () => request.put('/user/notifications/read-all')
export const getNotificationUnreadCount = () => request.get('/user/notifications/unread-count')
