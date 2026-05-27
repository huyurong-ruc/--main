// pages/message/detail.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    id: '',
    detail: null,
    loading: true
  },
  
  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    
    if (options.id) {
      this.setData({ id: options.id })
      this.loadDetail()
    }
  },
  
  async loadDetail() {
    this.setData({ loading: true, detail: null })
    
    try {
      // 使用后端 /student/notices/{id} 接口
      const res = await get(`/student/notices/${this.data.id}`)
      const detail = (res && res.data) || null
      this.setData({ detail })
      if (detail && detail.title) {
        wx.setNavigationBarTitle({ title: '通知详情' })
      }
    } catch (e) {
      console.error('加载详情失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },

  copyOriginalLink() {
    const url = (this.data.detail || {}).originalUrl
    if (!url) return
    wx.setClipboardData({
      data: url,
      success: () => wx.showToast({ title: '链接已复制', icon: 'success' })
    })
  },

  goToInternalRoute() {
    const route = (this.data.detail || {}).actionRoute
    if (!route) return
    this.navigateByRoute(route)
  },

  navigateByRoute(route) {
    const safeRoute = String(route || '')
    const tabRoutes = ['/pages/index/index', '/pages/message/index', '/pages/profile/index']
    if (tabRoutes.includes(safeRoute)) {
      wx.switchTab({ url: safeRoute })
      return
    }
    wx.navigateTo({ url: safeRoute })
  },

  downloadAttachment(e) {
    const { url, name } = e.currentTarget.dataset
    if (!url) return
    const lowerUrl = String(url).toLowerCase()
    const isFileLink = /\.(pdf|doc|docx|xls|xlsx|ppt|pptx)$/i.test(lowerUrl)
    const isHttp = /^https?:\/\//.test(url)

    if (!isHttp || !isFileLink) {
      wx.setClipboardData({
        data: url,
        success: () => wx.showToast({ title: '附件链接已复制', icon: 'success' })
      })
      return
    }
    wx.showLoading({ title: '下载中...' })
    wx.downloadFile({
      url,
      success: (res) => {
        wx.hideLoading()
        wx.openDocument({
          filePath: res.tempFilePath,
          showMenu: true,
          fail: () => wx.showToast({ title: '打开失败', icon: 'none' })
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '下载失败', icon: 'none' })
      }
    })
  }
})
