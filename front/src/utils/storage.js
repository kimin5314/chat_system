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
    
    // Clear all localStorage data
    localStorage.clear()
    
    // Restore E2EE data if it existed
    if (e2eePrivateKey) {
        localStorage.setItem('e2ee_private_key', e2eePrivateKey)
    }
    if (e2eePublicKey) {
        localStorage.setItem('e2ee_public_key', e2eePublicKey)
    }
    if (e2eeEnabled) {
        localStorage.setItem('e2ee_enabled', e2eeEnabled)
    }
    if (e2eeOriginalMessages) {
        localStorage.setItem('e2ee_original_messages', e2eeOriginalMessages)
    }
    
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
