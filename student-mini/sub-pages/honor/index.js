const app = getApp()
const { listHonors, normalizeRecipientType } = require('../../api/honor')

const recipientTypeText = {
  PERSONAL: '个人',
  COLLECTIVE: '集体'
}

Page({
  data: {
    keyword: '',
    filters: [
      { label: '全部', value: '' },
      { label: '个人', value: 'PERSONAL' },
      { label: '集体', value: 'COLLECTIVE' }
    ],
    activeType: '',
    honors: [],
    page: 0,
    size: 10,
    totalElements: 0,
    hasMore: true,
    loading: false,
    errorMessage: ''
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadHonors(true)
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearchConfirm() {
    this.loadHonors(true)
  },

  clearSearch() {
    this.setData({ keyword: '' })
    this.loadHonors(true)
  },

  onFilterTap(e) {
    const value = normalizeRecipientType(e.currentTarget.dataset.value) || ''
    if (value === this.data.activeType) return
    this.setData({ activeType: value })
    this.loadHonors(true)
  },

  async loadHonors(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    const nextPage = reset ? 0 : this.data.page
    this.setData({ loading: true })

    try {
      const res = await listHonors({
        page: nextPage,
        size: this.data.size,
        keyword: this.data.keyword || undefined,
        recipientType: normalizeRecipientType(this.data.activeType)
      })
      const pageData = res?.data || {}
      const rawList = Array.isArray(pageData.content)
        ? pageData.content
        : (Array.isArray(pageData.list) ? pageData.list : [])
      const content = rawList.map((item) => this.normalizeHonor(item))
      const totalPages = Number(pageData.totalPages || 0)
      const totalElements = Number(pageData.totalElements || 0)
      this.setData({
        honors: reset ? content : this.data.honors.concat(content),
        page: nextPage + 1,
        totalElements: totalElements || content.length,
        hasMore: totalPages > 0 ? nextPage + 1 < totalPages : content.length >= this.data.size,
        errorMessage: ''
      })
    } catch (e) {
      console.error('加载荣誉展示失败', e)
      const message = e?.message || e?.data?.message || e?.msg || '荣誉展示内容加载失败'
      if (reset) {
        this.setData({ honors: [], hasMore: false, totalElements: 0, errorMessage: message })
      }
    } finally {
      this.setData({ loading: false })
      wx.stopPullDownRefresh()
    }
  },

  normalizeHonor(item) {
    const typeText = recipientTypeText[item.recipientType] || '展示'
    const awardYear = item.awardYear ? `${item.awardYear}年` : ''
    const honorCategory = item.honorCategory || '荣誉项目'
    const recipientCount = Number(item.recipientCount || 0)
    return {
      ...item,
      typeText,
      displayTitle: item.title || `${awardYear}${honorCategory}`,
      summary: item.description || '点击查看获奖者、简介与先进事迹',
      countText: `${recipientCount}位${typeText}获奖者`
    }
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/honor/detail?id=${id}` })
  },

  onPullDownRefresh() {
    this.loadHonors(true)
  },

  onReachBottom() {
    this.loadHonors(false)
  }
})
