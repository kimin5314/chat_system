/**
 * 文件名编码/解码工具
 * 用于处理包含中文字符的文件名
 */

/**
 * 将文件名编码为base64
 * @param {string} fileName - 原始文件名
 * @returns {string} - base64编码后的文件名
 */
export function encodeFileName(fileName) {
  try {
    // 使用UTF-8编码然后转为base64
    return btoa(unescape(encodeURIComponent(fileName)))
  } catch (error) {
    console.warn('文件名编码失败，使用原文件名:', error)
    return fileName
  }
}

/**
 * 将base64编码的文件名解码
 * @param {string} encodedFileName - base64编码的文件名
 * @returns {string} - 解码后的原始文件名
 */
export function decodeFileName(encodedFileName) {
  try {
    // 从base64解码然后转为UTF-8
    return decodeURIComponent(escape(atob(encodedFileName)))
  } catch (error) {
    console.warn('文件名解码失败，使用原编码名:', error)
    return encodedFileName
  }
}

/**
 * 检测字符串是否为有效的base64编码
 * @param {string} str - 待检测的字符串
 * @returns {boolean} - 是否为有效的base64
 */
export function isBase64(str) {
  try {
    return btoa(atob(str)) === str
  } catch (error) {
    return false
  }
}

/**
 * 智能解码文件名（如果是base64则解码，否则直接返回）
 * @param {string} fileName - 文件名
 * @returns {string} - 解码后的文件名
 */
export function smartDecodeFileName(fileName) {
  if (isBase64(fileName)) {
    return decodeFileName(fileName)
  }
  return fileName
}
