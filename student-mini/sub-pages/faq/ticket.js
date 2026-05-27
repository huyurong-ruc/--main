// sub-pages/faq/ticket.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    tickets: [],
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
  
  onShow() {
    if (app.isLoggedIn()) {
      this.setData({ page: 1, hasMore: true })
      this.loadList()
    }
  },
  
  async loadList() {
    if (this.loading || !this.hasMore) return
    
    this.setData({ loading: true })
    
    try {
      const res = await get('/student/qa-tickets/page', {
        page: Math.max(this.data.page - 1, 0),
        size: this.data.pageSize
      })
      
      const list = Array.isArray(res.data?.content) ? res.data.content : []
      const mapped = list.map((item) => ({
        id: item.id,
        status: item.status,
        statusText: item.status === 'OPEN' ? '待处理' : (item.status === 'IN_PROGRESS' ? '处理中' : (item.status === 'CLOSED' ? '已关闭' : item.status)),
        content: item.summary || '',
        createTime: item.createdAt || '',
        replyCount: 0
      }))
      this.setData({
        tickets: this.data.page === 1 ? mapped : [...this.data.tickets, ...mapped],
        hasMore: mapped.length >= this.data.pageSize,
        page: this.data.page + 1
      })
    } catch (e) {
      console.error('加载工单失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 跳转详情
  goToDetail(e) {
    wx.showToast({ title: '暂未支持工单详情', icon: 'none' })
  },
  
  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadList().finally(() => wx.stopPullDownRefresh())
  },
  
  onReachBottom() {
    if (this.data.hasMore) this.loadList()
  }
})
