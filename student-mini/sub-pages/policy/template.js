const app = getApp()
const policyApi = require('../../api/policy')

function normalizeTemplate(item = {}) {
  const baseUrl = app.globalData?.baseUrl || ''
  const origin = baseUrl.replace(/\/api\/v1\/?$/, '')
  const rawFileUrl = item.fileUrl || item.templateFilePath || ''
  const fileUrl = /^https?:\/\//.test(rawFileUrl)
    ? rawFileUrl
    : (rawFileUrl && origin ? `${origin}${rawFileUrl}` : '')
  const downloadUrl = item.id && baseUrl
    ? `${baseUrl.replace(/\/$/, '')}/certificate-templates/${item.id}/download`
    : fileUrl

  return {
    id: String(item.id || ''),
    title: item.title || item.templateName || '未命名模板',
    description: item.description || `用于${item.certificateType || '证明'}模板下载`,
    fileSize: item.fileSize || '-',
    fileType: item.fileType || item.outputFormat || '文件',
    updatedAt: item.updatedAt || '',
    department: item.department || '学院服务平台',
    fileUrl,
    downloadUrl,
    sourceFilePath: rawFileUrl,
    templateCode: item.templateCode || '',
    certificateType: item.certificateType || '',
    templateContent: item.templateContent || '',
    mockFileName: item.mockFileName || '',
    mockFileContent: item.mockFileContent || ''
  }
}

function logTemplateDownload(stage, payload = {}) {
  console.info('[template-download]', stage, payload)
}

function escapePdfText(text = '') {
  return String(text)
    .replace(/\\/g, '\\\\')
    .replace(/\(/g, '\\(')
    .replace(/\)/g, '\\)')
}

function stringToArrayBuffer(content = '') {
  const buffer = new ArrayBuffer(content.length)
  const view = new Uint8Array(buffer)

  for (let i = 0; i < content.length; i += 1) {
    view[i] = content.charCodeAt(i) & 0xff
  }

  return buffer
}

function buildMockPdfBuffer(target = {}) {
  const lines = [
    'Mock Template File',
    `Code: ${target.templateCode || target.id || 'N/A'}`,
    `Format: ${target.fileType || 'PDF'}`,
    'Status: download ready',
    'Preview this file to verify the flow.'
  ]
  const streamLines = [
    'BT',
    '/F1 18 Tf',
    '72 760 Td',
    `(${escapePdfText(target.templateCode || 'Template Preview')}) Tj`,
    '0 -28 Td',
    '/F1 12 Tf'
  ]

  lines.forEach((line) => {
    streamLines.push(`(${escapePdfText(line)}) Tj`)
    streamLines.push('0 -18 Td')
  })
  streamLines.push('ET')

  const stream = `${streamLines.join('\n')}\n`
  const streamLength = stream.length
  const objects = [
    '1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n',
    '2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n',
    '3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n',
    '4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n',
    `5 0 obj\n<< /Length ${streamLength} >>\nstream\n${stream}endstream\nendobj\n`
  ]

  let content = '%PDF-1.4\n'
  const offsets = []
  objects.forEach((object) => {
    offsets.push(content.length)
    content += object
  })

  const xrefOffset = content.length
  content += 'xref\n0 6\n0000000000 65535 f \n'
  offsets.forEach((offset) => {
    content += `${String(offset).padStart(10, '0')} 00000 n \n`
  })
  content += `trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF\n`

  return stringToArrayBuffer(content)
}

function getMockFileName(target = {}) {
  const sourceFilePath = target.sourceFilePath || ''
  const pathMatch = sourceFilePath.match(/\/([^/]+\.[a-z0-9]+)$/i)
  const fallbackExt = String(target.fileType || '').toLowerCase() === 'pdf' ? '.pdf' : '.pdf'
  const fallbackName = `${target.templateCode || 'template'}${fallbackExt}`
  const fileName = pathMatch?.[1] || fallbackName

  return fileName.replace(/[\\/:*?"<>|]/g, '_')
}

function writeMockTemplateFile(target = {}) {
  return new Promise((resolve, reject) => {
    const filePath = `${wx.env.USER_DATA_PATH}/${getMockFileName(target)}`
    const fs = wx.getFileSystemManager()
    const data = buildMockPdfBuffer(target)

    fs.writeFile({
      filePath,
      data,
      success: () => resolve(filePath),
      fail: reject
    })
  })
}

function buildDownloadHeader() {
  const header = {}

  if (app.globalData?.token) {
    header.Authorization = `Bearer ${app.globalData.token}`
  }

  const baseUrl = app.globalData?.baseUrl || ''
  if (/ngrok|\.dev/i.test(baseUrl)) {
    header['ngrok-skip-browser-warning'] = 'true'
  }

  return header
}

Page({
  data: {
    id: '',
    templates: [],
    selectedTemplate: null,
    selectedIndex: 0,
    loading: false,
    downloading: false
  },

  onLoad(options = {}) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.setData({ id: options.id || '' })
    this.loadTemplates()
  },

  onPullDownRefresh() {
    this.loadTemplates({ stopRefresh: true })
  },

  async loadTemplates(options = {}) {
    this.setData({ loading: true })

    try {
      const res = await policyApi.getTemplates()
      const templates = Array.isArray(res.data) ? res.data.map(normalizeTemplate) : []
      const selectedIndex = Math.max(templates.findIndex((item) => item.id === this.data.id), 0)
      const selectedTemplate = templates[selectedIndex] || null
      this.setData({
        templates,
        selectedIndex,
        selectedTemplate,
        id: selectedTemplate?.id || ''
      })
    } catch (e) {
      console.error('加载模板失败', e)
      wx.showToast({ title: '加载模板失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
      if (options.stopRefresh) {
        wx.stopPullDownRefresh()
      }
    }
  },

  openTemplateDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    const selectedIndex = this.data.templates.findIndex((item) => item.id === id)
    if (selectedIndex < 0) return
    this.setData({
      selectedIndex,
      selectedTemplate: this.data.templates[selectedIndex],
      id
    })
  },

  onTemplateSwiperChange(e) {
    const selectedIndex = Number(e.detail.current || 0)
    const selectedTemplate = this.data.templates[selectedIndex] || null
    this.setData({
      selectedIndex,
      selectedTemplate,
      id: selectedTemplate?.id || ''
    })
  },

  async handleDownload(e) {
    const { id } = e.currentTarget.dataset
    const target = this.data.templates.find((item) => item.id === id) || this.data.selectedTemplate
    const downloadUrl = target?.downloadUrl || target?.fileUrl || ''

    if (!target) {
      wx.showToast({ title: '模板信息缺失', icon: 'none' })
      return
    }

    if (!downloadUrl && !app.globalData.USE_MOCK) {
      wx.showToast({ title: '当前模板暂不支持下载', icon: 'none' })
      return
    }

    this.setData({ downloading: true })
    wx.showLoading({ title: '下载中...' })
    logTemplateDownload('start', {
      id: target.id,
      templateCode: target.templateCode,
      useMock: app.globalData.USE_MOCK,
      downloadUrl
    })

    if (app.globalData.USE_MOCK) {
      try {
        const filePath = await writeMockTemplateFile(target)
        logTemplateDownload('mock-file-written', { id: target.id, filePath })
        wx.openDocument({
          filePath,
          showMenu: true,
          success: () => {
            wx.hideLoading()
            this.setData({ downloading: false })
            wx.showToast({ title: '下载成功', icon: 'success' })
            logTemplateDownload('open-success', { id: target.id, filePath, mode: 'mock' })
          },
          fail: (error) => {
            wx.hideLoading()
            this.setData({ downloading: false })
            console.error('模板预览失败', error)
            wx.showToast({ title: '预览失败', icon: 'none' })
            logTemplateDownload('open-fail', { id: target.id, mode: 'mock', error })
          }
        })
      } catch (e) {
        console.error('模板下载失败', e)
        wx.hideLoading()
        this.setData({ downloading: false })
        wx.showToast({ title: '下载失败', icon: 'none' })
        logTemplateDownload('mock-write-fail', { id: target.id, error: e })
      }
      return
    }

    wx.downloadFile({
      url: downloadUrl,
      header: buildDownloadHeader(),
      success: (res) => {
        logTemplateDownload('download-response', {
          id: target.id,
          statusCode: res.statusCode,
          tempFilePath: res.tempFilePath
        })
        if (res.statusCode !== 200) {
          wx.hideLoading()
          wx.showToast({ title: '下载失败', icon: 'none' })
          return
        }
        wx.openDocument({
          filePath: res.tempFilePath,
          showMenu: true,
          success: () => {
            wx.hideLoading()
            wx.showToast({ title: '下载成功', icon: 'success' })
            logTemplateDownload('open-success', { id: target.id, filePath: res.tempFilePath, mode: 'remote' })
          },
          fail: (error) => {
            wx.hideLoading()
            console.error('模板预览失败', error)
            wx.showToast({ title: '预览失败', icon: 'none' })
            logTemplateDownload('open-fail', { id: target.id, mode: 'remote', error })
          }
        })
      },
      fail: (error) => {
        console.error('模板下载失败', error)
        wx.hideLoading()
        wx.showToast({ title: '下载失败', icon: 'none' })
        logTemplateDownload('download-fail', { id: target.id, error })
      },
      complete: () => {
        this.setData({ downloading: false })
      }
    })
  }
})
