// sub-pages/feedback/history.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    history: [],
    loading: false,
    page: 1,
    pageSize: 10,
    hasMore: true
  },
  
  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadList()
  },
  
  async loadList() {
    if (this.data.loading || !this.data.hasMore) return

    // 管理端反馈处理模块尚未建设，暂时拦截并友好提示
    wx.showModal({
      title: '提示',
      content: '意见反馈功能正在持续开发中，预计后续版本上线',
      showCancel: false
    })
    this.setData({ loading: false, hasMore: false })
    return
  },
  
  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadList().finally(() => wx.stopPullDownRefresh())
  },
  
  onReachBottom() {
    if (this.data.hasMore) this.loadList()
  }
})
