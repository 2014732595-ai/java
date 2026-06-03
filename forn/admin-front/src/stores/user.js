import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_info') || 'null'))

  function setToken(t) {
    token.value = t
    localStorage.setItem('admin_token', t)
  }

  function setInfo(info) {
    userInfo.value = info
    localStorage.setItem('admin_info', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
  }

  return { token, userInfo, setToken, setInfo, logout }
})
