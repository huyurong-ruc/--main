const app = getApp()
const policyApi = require('../../api/policy')

function normalizeTemplate(item = {}) {
  return {
    id: String(item.id || ''),
    title: item.title || '未命名模板',
    description: item.description || '暂无模板说明',
    fileSize: item.fileSize || '-',
    fileType: item.fileType || '文件',
    updatedAt: item.updatedAt || '',
    department: item.department || '学院服务平台',
    fileUrl: item.fileUrl || ''
  }
}

Page({
  data: {
    id: '',
    templates: [],
    selectedTemplate: null,
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
      const selectedTemplate = templates.find((item) => item.id === this.data.id) || null
      this.setData({ templates, selectedTemplate })
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
    wx.navigateTo({ url: `/sub-pages/policy/template?id=${id}` })
  },

  handleDownload(e) {
    const { id } = e.currentTarget.dataset
    const target = this.data.templates.find((item) => item.id === id) || this.data.selectedTemplate

    if (!target || !target.fileUrl) {
      wx.showToast({ title: '当前模板暂不支持下载', icon: 'none' })
      return
    }

    this.setData({ downloading: true })
    wx.showLoading({ title: '下载中...' })

    wx.downloadFile({
      url: target.fileUrl,
      success: (res) => {
        wx.hideLoading()
        if (res.statusCode !== 200) {
          wx.showToast({ title: '下载失败', icon: 'none' })
          return
        }
        wx.openDocument({
          filePath: res.tempFilePath,
          showMenu: true,
          fail: () => wx.showToast({ title: '打开失败', icon: 'none' })
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '下载失败', icon: 'none' })
      },
      complete: () => {
        this.setData({ downloading: false })
      }
    })
  }
})
