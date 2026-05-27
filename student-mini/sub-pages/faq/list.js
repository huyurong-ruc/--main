const app = getApp()
const { getFaqs } = require('../../api/faq')

function normalizeFaq(item = {}) {
  return {
    ...item,
    question: item.question || item.title || '未命名问题',
    answer: item.answer || item.content || '暂无回答'
  }
}

Page({
  data: {
    keyword: '',
    faqList: [],
    loading: false,
    page: 0,
    pageSize: 10,
    hasMore: true
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadList(true)
  },

  onShow() {
    if (app.isLoggedIn()) {
      this.loadList(true)
    }
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearchConfirm() {
    this.loadList(true)
  },

  clearSearch() {
    this.setData({ keyword: '' })
    this.loadList(true)
  },

  async loadList(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    const nextPage = reset ? 0 : this.data.page
    this.setData({ loading: true })

    try {
      const res = await getFaqs({
        page: nextPage,
        pageSize: this.data.pageSize,
        keyword: this.data.keyword || undefined
      })

      const pageData = res.data || {}
      const list = (pageData.content || pageData.list || []).map(normalizeFaq)

      this.setData({
        faqList: reset ? list : this.data.faqList.concat(list),
        page: nextPage + 1,
        hasMore: nextPage + 1 < (pageData.totalPages || 0) || list.length >= this.data.pageSize
      })
    } catch (e) {
      console.error('加载FAQ失败', e)
      if (reset) {
        this.setData({ faqList: [], hasMore: false })
      }
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
      wx.stopPullDownRefresh()
    }
  },

  toggleItem(e) {
    const { index } = e.currentTarget.dataset
    const key = `faqList[${index}].expanded`
    this.setData({ [key]: !this.data.faqList[index].expanded })
  },

  goToSubmit() {
    wx.navigateTo({ url: '/sub-pages/faq/submit' })
  },

  onPullDownRefresh() {
    this.loadList(true)
  },

  onReachBottom() {
    this.loadList(false)
  }
})
