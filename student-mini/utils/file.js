function headerValue(headers = {}, key = '') {
  if (!headers || !key) return ''
  const target = String(key).toLowerCase()
  const matchedKey = Object.keys(headers).find((item) => String(item).toLowerCase() === target)
  return matchedKey ? headers[matchedKey] : ''
}

function decodeFileName(value = '') {
  if (!value) return ''
  try {
    return decodeURIComponent(value)
  } catch (e) {
    return value
  }
}

function fileNameFromDisposition(disposition = '') {
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match && utf8Match[1]) {
    return decodeFileName(utf8Match[1].trim())
  }
  const plainMatch = disposition.match(/filename="?([^";]+)"?/i)
  return plainMatch && plainMatch[1] ? plainMatch[1].trim() : ''
}

function extensionFromContentType(contentType = '') {
  const normalized = String(contentType).toLowerCase()
  if (normalized.includes('pdf')) return '.pdf'
  if (normalized.includes('word') || normalized.includes('docx')) return '.docx'
  if (normalized.includes('msword')) return '.doc'
  if (normalized.includes('excel') || normalized.includes('spreadsheetml')) return '.xlsx'
  if (normalized.includes('presentation')) return '.pptx'
  if (normalized.includes('text/plain')) return '.txt'
  if (normalized.includes('image/png')) return '.png'
  if (normalized.includes('image/jpeg')) return '.jpg'
  return ''
}

function sanitizeFileName(fileName = '', contentType = '') {
  const raw = String(fileName || 'download-file').trim().replace(/[\\/:*?"<>|]/g, '_')
  if (/\.[a-z0-9]{1,8}$/i.test(raw)) {
    return raw
  }
  const extension = extensionFromContentType(contentType)
  return `${raw || 'download-file'}${extension}`
}

function openDocument(filePath) {
  return new Promise((resolve, reject) => {
    wx.openDocument({
      filePath,
      showMenu: true,
      success: resolve,
      fail: reject
    })
  })
}

function downloadByFile(url, header = {}) {
  return new Promise((resolve, reject) => {
    wx.downloadFile({
      url,
      header,
      success: (res) => {
        if (res.statusCode !== 200 || !res.tempFilePath) {
          reject(new Error(`download status ${res.statusCode || 0}`))
          return
        }
        resolve({
          filePath: res.tempFilePath,
          headers: res.header || {}
        })
      },
      fail: reject
    })
  })
}

function requestBinary(url, header = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url,
      method: 'GET',
      header,
      responseType: 'arraybuffer',
      success: (res) => {
        if (res.statusCode !== 200 || !res.data) {
          reject(new Error(`request status ${res.statusCode || 0}`))
          return
        }
        resolve({
          data: res.data,
          headers: res.header || {}
        })
      },
      fail: reject
    })
  })
}

function writeBinaryToFile(buffer, preferredFileName = '', contentType = '') {
  return new Promise((resolve, reject) => {
    const fileName = sanitizeFileName(preferredFileName, contentType)
    const filePath = `${wx.env.USER_DATA_PATH}/${Date.now()}-${fileName}`
    wx.getFileSystemManager().writeFile({
      filePath,
      data: buffer,
      success: () => resolve(filePath),
      fail: reject
    })
  })
}

async function tryOpenUrl(url, header = {}, preferredFileName = '') {
  try {
    const downloadRes = await downloadByFile(url, header)
    await openDocument(downloadRes.filePath)
    return
  } catch (downloadError) {
    const requestRes = await requestBinary(url, header)
    const disposition = headerValue(requestRes.headers, 'content-disposition')
    const contentType = headerValue(requestRes.headers, 'content-type')
    const serverFileName = fileNameFromDisposition(disposition)
    const filePath = await writeBinaryToFile(
      requestRes.data,
      serverFileName || preferredFileName,
      contentType
    )
    await openDocument(filePath)
  }
}

async function downloadAndOpenDocument({ primaryUrl = '', fallbackUrl = '', header = {}, fileName = '' } = {}) {
  const tried = new Set()
  const urls = [primaryUrl, fallbackUrl].filter((item) => item && !tried.has(item) && tried.add(item))
  let lastError = null

  for (const url of urls) {
    try {
      await tryOpenUrl(url, header, fileName)
      return true
    } catch (error) {
      lastError = error
    }
  }

  throw lastError || new Error('download failed')
}

module.exports = {
  downloadAndOpenDocument
}
