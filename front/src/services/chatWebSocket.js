import { WS_MESSAGE_TYPES } from '@/constants/websocket'

class ChatWebSocketService {
    constructor() {
        this.ws = null
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = 5
        this.reconnectDelay = 1000
        this.heartbeatInterval = null
        this.messageHandlers = new Map()
        this.connectionListeners = []
    }

    // Connect to chat WebSocket
    connect(token) {
        // If we already have an open connection, return it
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            console.log('Chat WebSocket already connected')
            return Promise.resolve()
        }
        
        // If we have a pending connection, close it
        if (this.ws) {
            console.log('Closing pending WebSocket connection')
            this.ws.close()
            this.ws = null
        }

        return new Promise((resolve, reject) => {
            try {
                const wsUrl = `${import.meta.env.VITE_WS_BASE}/chat?token=${token}`
                console.log('Connecting to Chat WebSocket:', wsUrl)
                
                this.ws = new WebSocket(wsUrl)
                let connectTimeout = setTimeout(() => {
                    if (this.ws.readyState !== WebSocket.OPEN) {
                        console.warn('Chat WebSocket connection timed out')
                        this.ws.close()
                        reject(new Error('Connection timeout'))
                    }
                }, 10000) // 10 second timeout

                this.ws.onopen = () => {
                    console.log('Chat WebSocket connected successfully')
                    clearTimeout(connectTimeout)
                    this.reconnectAttempts = 0
                    this.startHeartbeat()
                    this.notifyConnectionListeners(true)
                    resolve()
                }

                this.ws.onclose = (event) => {
                    clearTimeout(connectTimeout)
                    console.log('Chat WebSocket disconnected:', event.code, event.reason || 'No reason provided')
                    this.stopHeartbeat()
                    this.notifyConnectionListeners(false)
                    
                    // Only reconnect for unexpected closures
                    const isNormalClosure = event.code === 1000 || event.code === 1001;
                    if (!isNormalClosure && this.reconnectAttempts < this.maxReconnectAttempts) {
                        console.log(`Scheduling reconnect attempt ${this.reconnectAttempts + 1}/${this.maxReconnectAttempts}`)
                        this.scheduleReconnect(token)
                    } else if (this.reconnectAttempts >= this.maxReconnectAttempts) {
                        console.log('Maximum reconnect attempts reached')
                    }
                }

                this.ws.onerror = (error) => {
                    console.error('Chat WebSocket error:', error)
                    // Don't reject here, let onclose handle it
                    // This prevents "uncaught promise rejection" errors
                }

                this.ws.onmessage = (event) => {
                    // Only log non-heartbeat messages
                    if (event.data !== 'ping' && event.data !== 'pong') {
                        this.handleMessage(event.data)
                    }
                }

            } catch (error) {
                console.error('Failed to create WebSocket connection:', error)
                reject(error)
            }
        })
    }

    // Disconnect WebSocket
    disconnect() {
        if (this.ws) {
            this.ws.close(1000, 'User disconnected')
            this.ws = null
        }
        this.stopHeartbeat()
        this.reconnectAttempts = 0
    }

    // Send message through WebSocket
    send(type, data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            const message = JSON.stringify({ type, data })
            this.ws.send(message)
            return true
        } else {
            console.warn('WebSocket not connected, cannot send message:', { type, data })
            return false
        }
    }

    // Handle incoming messages
    handleMessage(messageData) {
        try {
            const message = JSON.parse(messageData)
            const { type, data } = message

            console.log('Received WebSocket message:', { type, data })

            // Call registered handlers for this message type
            const handlers = this.messageHandlers.get(type) || []
            handlers.forEach(handler => {
                try {
                    handler(data)
                } catch (error) {
                    console.error('Error in message handler:', error)
                }
            })

        } catch (error) {
            console.error('Failed to parse WebSocket message:', error)
        }
    }

    // Register message handler
    on(messageType, handler) {
        if (!this.messageHandlers.has(messageType)) {
            this.messageHandlers.set(messageType, [])
        }
        this.messageHandlers.get(messageType).push(handler)

        // Return unsubscribe function
        return () => {
            const handlers = this.messageHandlers.get(messageType)
            if (handlers) {
                const index = handlers.indexOf(handler)
                if (index > -1) {
                    handlers.splice(index, 1)
                }
            }
        }
    }

    // Remove message handler
    off(messageType, handler) {
        const handlers = this.messageHandlers.get(messageType)
        if (handlers) {
            const index = handlers.indexOf(handler)
            if (index > -1) {
                handlers.splice(index, 1)
            }
        }
    }

    // Add connection listener
    onConnection(listener) {
        this.connectionListeners.push(listener)
        return () => {
            const index = this.connectionListeners.indexOf(listener)
            if (index > -1) {
                this.connectionListeners.splice(index, 1)
            }
        }
    }

    // Notify connection listeners
    notifyConnectionListeners(isConnected) {
        this.connectionListeners.forEach(listener => {
            try {
                listener(isConnected)
            } catch (error) {
                console.error('Error in connection listener:', error)
            }
        })
    }

    // Schedule reconnection with exponential backoff
    scheduleReconnect(token) {
        this.reconnectAttempts++
        // Calculate delay with exponential backoff, capped at 30 seconds
        const delay = Math.min(this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1), 30000)
        
        console.log(`Scheduling reconnect attempt ${this.reconnectAttempts} in ${delay}ms`)
        
        setTimeout(() => {
            if (this.reconnectAttempts <= this.maxReconnectAttempts) {
                console.log(`Attempting reconnect ${this.reconnectAttempts}/${this.maxReconnectAttempts}`)
                this.connect(token).catch(error => {
                    console.error('Reconnect attempt failed:', error.message || 'Unknown error')
                })
            } else {
                console.log('Maximum reconnect attempts reached, giving up')
            }
        }, delay)
    }

    // Start heartbeat with more reliable detection
    startHeartbeat() {
        this.stopHeartbeat()
        
        // Send a ping every 20 seconds
        const pingInterval = 20000
        let pongReceived = true
        
        this.heartbeatInterval = setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                // If we haven't received a pong since last ping, connection might be dead
                if (!pongReceived) {
                    console.warn('No pong received, connection may be dead. Closing socket to trigger reconnect.')
                    this.ws.close(3000, 'No heartbeat response')
                    return;
                }
                
                // Reset pong flag before sending ping
                pongReceived = false
                
                // Send ping
                try {
                    this.ws.send('ping');
                    console.log('Heartbeat ping sent');
                    
                    // Set a timeout to listen for pong
                    setTimeout(() => {
                        if (!pongReceived) {
                            console.warn('No pong received within timeout');
                        }
                    }, 5000); // Wait 5 seconds for pong
                } catch (e) {
                    console.error('Failed to send heartbeat:', e);
                }
            }
        }, pingInterval)
        
        // Add a message handler specifically for pong responses
        const pongHandler = (event) => {
            if (event.data === 'pong') {
                console.log('Heartbeat pong received');
                pongReceived = true;
            }
        };
        
        // Store the original onmessage handler
        const originalOnMessage = this.ws.onmessage;
        
        // Create a new handler that checks for pongs first
        this.ws.onmessage = (event) => {
            if (event.data === 'pong') {
                pongReceived = true;
            } else {
                // Pass to original handler for normal messages
                originalOnMessage(event);
            }
        };
    }

    // Stop heartbeat
    stopHeartbeat() {
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval)
            this.heartbeatInterval = null
        }
    }

    // Check if connected
    isConnected() {
        return this.ws && this.ws.readyState === WebSocket.OPEN
    }

    // Get connection state
    getConnectionState() {
        if (!this.ws) return 'DISCONNECTED'
        
        switch (this.ws.readyState) {
            case WebSocket.CONNECTING:
                return 'CONNECTING'
            case WebSocket.OPEN:
                return 'CONNECTED'
            case WebSocket.CLOSING:
                return 'CLOSING'
            case WebSocket.CLOSED:
                return 'DISCONNECTED'
            default:
                return 'UNKNOWN'
        }
    }
}

// Create singleton instance
const chatWebSocketService = new ChatWebSocketService()

export default chatWebSocketService
export { WS_MESSAGE_TYPES }
