const app = getApp()
const { getFaqs } = require('../../api/faq')

function isQaCategory(category = '') {
  const raw = String(category || '')
  const lower = raw.toLowerCase()
  return lower.includes('faq') || raw.includes('问答') || raw.includes('FAQ管理') || raw.includes('faq管理')
}

function getFaqErrorMessage(error = {}) {
  const rawMessage = String(
    error?.message || error?.data?.message || error?.msg || error?.errMsg || ''
  ).trim()

  if (!rawMessage) return 'FAQ加载失败，请稍后重试'
  if (/timeout|超时/i.test(rawMessage)) return 'FAQ请求超时，请点击重新加载'
  if (/404|不存在/.test(rawMessage)) return 'FAQ接口不存在，请联系管理员核对配置'
  if (/500|内部错误|server/i.test(rawMessage)) return 'FAQ服务异常，请稍后重试'
  return rawMessage
}

function normalizeFaq(item = {}) {
  const category = item.category || ''
  return {
    id: item.id,
    question: item.title || '未命名问题',
    answer: item.answer || '暂无问答说明',
    category,
    statusText: category || '问答',
    createdAt: '',
    expanded: false
  }
}

Page({
  data: {
    keyword: '',
    faqList: [],
    loading: false,
    page: 0,
    pageSize: 10,
    hasMore: false,
    total: 0,
    errorMessage: ''
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

    const keyword = String(this.data.keyword || '').trim()
    this.setData({ loading: true })

    try {
      const res = await getFaqs({ keyword })
      const sourceList = Array.isArray(res.data) ? res.data : []
      const list = sourceList
        .filter((item) => isQaCategory(item.category))
        .map(normalizeFaq)

      this.setData({
        faqList: list,
        page: 1,
        total: list.length,
        hasMore: false,
        errorMessage: ''
      })
    } catch (e) {
      console.error('加载FAQ失败', e)
      const message = getFaqErrorMessage(e)
      if (reset) {
        this.setData({ faqList: [], hasMore: false, total: 0, errorMessage: message })
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

  retryLoad() {
    this.loadList(true)
  },

  onPullDownRefresh() {
    this.loadList(true)
  },

  onReachBottom() {
    return
  }
})
