// sub-pages/policy/detail.js
const app = getApp()
const policyApi = require('../../api/policy')

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
    this.setData({ loading: true })
    
    try {
      const res = await policyApi.getPolicyDetail(this.data.id)
      const raw = res.data || {}
      const answerText = raw.answer == null ? '' : String(raw.answer)
      const contentHtml = answerText
        ? answerText.replace(/\n/g, '<br/>')
        : ''
      this.setData({
        detail: {
          id: raw.id,
          title: raw.title,
          category: raw.category,
          officialUrl: raw.officialUrl,
          content: contentHtml,
          attachments: Array.isArray(raw.attachments) ? raw.attachments : []
        }
      })
    } catch (e) {
      console.error('加载详情失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 下载附件
  downloadFile(e) {
    const { id, name } = e.currentTarget.dataset
    if (!id) {
      wx.showToast({ title: '附件信息缺失', icon: 'none' })
      return
    }
    wx.showLoading({ title: '正在下载...' })
    
    const token = app.globalData.token
    const baseUrl = (app.globalData.baseUrl || '').replace(/\/$/, '')
    const url = baseUrl + `/platform/files/${id}/download`
    wx.downloadFile({
      url,
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        wx.hideLoading()
        wx.openDocument({
          filePath: res.tempFilePath,
          success: () => console.log('打开成功'),
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
