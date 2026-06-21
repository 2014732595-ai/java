import { useUserStore } from '../stores/user'

let ws = null
let messageHandler = null
let reconnectTimer = null

export const connectWebSocket = (onMessage) => {
  const userStore = useUserStore()
  if (!userStore.token) return

  messageHandler = onMessage

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = 'localhost:8080'
  const url = `${protocol}//${host}/ws/chat?token=${userStore.token}`

  ws = new WebSocket(url)

  ws.onopen = () => {
    console.log('WebSocket connected')
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (messageHandler) messageHandler(data)
    } catch (e) {
      console.error('WebSocket message parse error:', e)
    }
  }

  ws.onclose = () => {
    console.log('WebSocket disconnected')
    // 自动重连
    if (!reconnectTimer) {
      reconnectTimer = setTimeout(() => {
        connectWebSocket(onMessage)
      }, 5000)
    }
  }

  ws.onerror = (err) => {
    console.error('WebSocket error:', err)
  }
}

export const sendMessage = (toId, content, productId) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    const message = { toId, content, productId }
    ws.send(JSON.stringify(message))
  }
}

export const closeWebSocket = () => {
  if (ws) {
    ws.close()
    ws = null
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}
