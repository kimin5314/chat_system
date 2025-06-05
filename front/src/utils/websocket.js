let ws = null
let reconnectTimer = null
let reconnectAttempts = 0
const MAX_RECONNECT_ATTEMPTS = 5
const RECONNECT_DELAY = 3000
let isIntentionalClose = false

export function connectWebSocket(token, onMessage) {
    // Get token from parameter or from sessionStorage/cookies
    if (!token) {
        token = sessionStorage.getItem('chatToken') || Cookies.get('token');
        if (!token) {
            console.error('No token available for WebSocket connection');
            return null;
        }
    }
    
    // Clear any existing reconnect timer
    if (reconnectTimer) {
        clearTimeout(reconnectTimer)
        reconnectTimer = null
    }
    
    // Reset the intentional close flag
    isIntentionalClose = false
    
    // Close existing connection if needed
    if (ws) {
        console.log('WebSocket已连接，关闭旧连接')
        isIntentionalClose = true // Mark this as intentional
        ws.close()
        ws = null
    }
    
    const wsUrl = `${import.meta.env.VITE_WS_URL}?token=${token}`
    console.log('正在连接WebSocket:', wsUrl)

    try {
        ws = new WebSocket(wsUrl)
        
        ws.onopen = () => {
            console.log('WebSocket连接成功')
            // Reset reconnect attempts on successful connection
            reconnectAttempts = 0
        }
        
        ws.onmessage = (event) => {
            // Only log if it's not a heartbeat ping/pong
            const data = event.data
            if (data !== 'ping' && data !== 'pong') {
                console.log('收到WebSocket消息:', data)
            }
            
            try {
                const message = JSON.parse(data)
                onMessage && onMessage(message)
            } catch (error) {
                // It might be a heartbeat message, not JSON
                if (data !== 'ping' && data !== 'pong') {
                    console.error('解析WebSocket消息失败:', error)
                }
            }
        }
        
        ws.onerror = (error) => {
            console.error('WebSocket错误:', error)
        }
        
        ws.onclose = (event) => {
            console.log('WebSocket连接关闭, code:', event.code, 'reason:', event.reason)
            
            // Don't reconnect if it was intentional or we've hit max attempts
            if (!isIntentionalClose && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++
                console.log(`尝试重新连接WebSocket (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`)
                
                reconnectTimer = setTimeout(() => {
                    if (token) {
                        connectWebSocket(token, onMessage)
                    }
                }, RECONNECT_DELAY)
            } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                console.log('达到最大重连次数，停止重连')
            }
        }
    } catch (error) {
        console.error('创建WebSocket连接失败:', error)
    }

    return ws
}

export function sendWebSocket(type, data) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        const message = JSON.stringify({ type, data })
        console.log('发送WebSocket消息:', message)
        ws.send(message)
    } else {
        console.error('WebSocket未连接或未就绪')
    }
}