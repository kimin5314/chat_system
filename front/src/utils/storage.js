/**
 * Storage utilities for handling localStorage with E2EE preservation
 */

/**
 * Clear localStorage while preserving E2EE keys and data
 * This ensures that encryption keys persist across logout/login cycles
 */
export function clearLocalStoragePreservingE2EE() {
    // Preserve E2EE-related data
    const e2eePrivateKey = localStorage.getItem('e2ee_private_key')
    const e2eePublicKey = localStorage.getItem('e2ee_public_key')
    const e2eeEnabled = localStorage.getItem('e2ee_enabled')
    const e2eeOriginalMessages = localStorage.getItem('e2ee_original_messages')
    
    console.log('🔑 Preserving E2EE data during logout:', {
        hasPrivateKey: !!e2eePrivateKey,
        hasPublicKey: !!e2eePublicKey,
        isEnabled: e2eeEnabled,
        messageCount: e2eeOriginalMessages ? Object.keys(JSON.parse(e2eeOriginalMessages) || {}).length : 0
    })
    
    // Clear all localStorage data
    localStorage.clear()
    
    // Restore E2EE data if it existed
    if (e2eePrivateKey) {
        localStorage.setItem('e2ee_private_key', e2eePrivateKey)
        console.log('✅ Restored E2EE private key')
    }
    if (e2eePublicKey) {
        localStorage.setItem('e2ee_public_key', e2eePublicKey)
        console.log('✅ Restored E2EE public key')
    }
    if (e2eeEnabled) {
        localStorage.setItem('e2ee_enabled', e2eeEnabled)
        console.log('✅ Restored E2EE enabled status:', e2eeEnabled)
    }
    if (e2eeOriginalMessages) {
        localStorage.setItem('e2ee_original_messages', e2eeOriginalMessages)
        console.log('✅ Restored E2EE original messages')
    }
    
    console.log('🔄 localStorage cleared while preserving E2EE data')
}

/**
 * Clear authentication data only (selective clearing)
 * Use this for authentication failures where we don't want to clear everything
 */
export function clearAuthenticationData() {
    // Remove authentication cookies
    import('js-cookie').then(({ default: Cookies }) => {
        Cookies.remove('token')
        Cookies.remove('userId')
    })
    
    // Clear session storage
    sessionStorage.clear()
    
    // Remove specific authentication-related localStorage items
    localStorage.removeItem('chatToken')
    
    console.log('🔓 Authentication data cleared')
}

/**
 * Check if E2EE data exists in localStorage
 * @returns {Object} Status of E2EE data
 */
export function checkE2EEDataExists() {
    const hasPrivateKey = !!localStorage.getItem('e2ee_private_key')
    const hasPublicKey = !!localStorage.getItem('e2ee_public_key')
    const isEnabled = localStorage.getItem('e2ee_enabled') === 'true'
    const hasMessages = !!localStorage.getItem('e2ee_original_messages')
    
    return {
        hasPrivateKey,
        hasPublicKey,
        isEnabled,
        hasMessages,
        isComplete: hasPrivateKey && hasPublicKey
    }
}
