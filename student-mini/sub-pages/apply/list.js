const app = getApp()
const { getApplyList } = require('../../api/apply')

const TAB_STATUS = ['submitted', 'draft']

function normalizeApply(item = {}) {
  const statusMap = {
    submitted: { class: 'submitted', text: '已提交' },
    draft: { class: 'draft', text: '草稿' },
    approved: { class: 'approved', text: '已通过' },
    rejected: { class: 'rejected', text: '已驳回' },
    processing: { class: 'processing', text: '处理中' }
  }
  const statusMeta = statusMap[item.status] || statusMap.submitted
  return {
    ...item,
    typeName: item.typeName || item.type || '未知类型',
    statusClass: statusMeta.class,
    statusText: item.statusText || statusMeta.text,
    purpose: item.purpose || item.content || '未填写用途',
    createTime: item.createTime || item.createdAt || '-'
  }
}

Page({
  data: {
    activeTab: 0,
    tabs: ['我的申请', '草稿箱'],
    list: [],
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

  onTabChange(e) {
    const index = parseInt(e.currentTarget.dataset.index, 10)
    if (index === this.data.activeTab) return
    this.setData({
      activeTab: index,
      page: 0,
      hasMore: true,
      list: []
    })
    this.loadList(true)
  },

  async loadList(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    const nextPage = reset ? 0 : this.data.page
    this.setData({ loading: true })

    try {
      const res = await getApplyList({
        status: TAB_STATUS[this.data.activeTab],
        page: nextPage,
        pageSize: this.data.pageSize
      })

      const pageData = res.data || {}
      const content = (pageData.content || pageData.list || []).map(normalizeApply)

      this.setData({
        list: reset ? content : this.data.list.concat(content),
        page: nextPage + 1,
        hasMore: nextPage + 1 < (pageData.totalPages || 0) || content.length >= this.data.pageSize,
        totalElements: pageData.totalElements || content.length
      })
    } catch (e) {
      console.error('加载申请列表失败', e)
      if (reset) {
        this.setData({ list: [], hasMore: false, totalElements: 0 })
      }
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
      wx.stopPullDownRefresh()
    }
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/apply/detail?id=${id}` })
  },

  goToNew() {
    wx.navigateTo({ url: '/sub-pages/apply/new' })
  },

  onPullDownRefresh() {
    this.loadList(true)
  },

  onReachBottom() {
    this.loadList(false)
  }
})
