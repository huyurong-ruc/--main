// sub-pages/apply/list.js
const app = getApp()
const applyApi = require('../../api/apply')
const { getApplyStatusMeta, isCompletedStatus } = require('../../api/apply-status')

function parseTime(value = '') {
  const text = String(value || '').trim()
  if (!text) return 0
  const parsed = new Date(text.replace(/-/g, '/')).getTime()
  return Number.isNaN(parsed) ? 0 : parsed
}

function buildIconText(title = '') {
  const text = String(title || '')
  if (text.includes('证')) return '证'
  if (text.includes('单')) return '单'
  return '申'
}

function normalizeApplyItem(item = {}) {
  const title = item.certificateType || item.typeName || item.title || '未命名申请'
  const rawStatus = item.statusCode || item.status || item.statusText || ''
  const statusMeta = getApplyStatusMeta(rawStatus)
  const time = String(item.createdAt || item.createTime || item.time || '')

  return {
    id: String(item.id || ''),
    title,
    iconText: buildIconText(title),
    status: statusMeta.listLabel,
    statusClass: statusMeta.listClass,
    statusTitle: statusMeta.title,
    statusDescription: statusMeta.description,
    time: time ? time.slice(0, 16) : '',
    sortTime: parseTime(time),
    completed: isCompletedStatus(rawStatus)
  }
}

function sortApplyList(list = []) {
  return [...list].sort((a, b) => {
    if (a.completed !== b.completed) {
      return a.completed ? 1 : -1
    }
    return b.sortTime - a.sortTime
  })
}

Page({
  data: {
    rawList: [],
    applyList: [],
    keyword: '',
    searchInputFocus: false,
    showSuggestions: false,
    suggestionList: [],
    loading: false,
    emptyText: '暂无申请记录'
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
  },

  onShow() {
    if (!app.isLoggedIn()) return
    this.loadApplyList()
  },

  onPullDownRefresh() {
    this.loadApplyList({ stopRefresh: true })
  },

  onUnload() {
    if (this.searchBlurTimer) {
      clearTimeout(this.searchBlurTimer)
    }
  },

  async loadApplyList(options = {}) {
    this.setData({ loading: true })

    try {
      const res = await applyApi.getApplyList()
      const sourceList = Array.isArray(res.data)
        ? res.data
        : Array.isArray(res.data?.list)
          ? res.data.list
          : []
      const rawList = sortApplyList(sourceList.map(normalizeApplyItem))
      this.setData({ rawList })
      this.applyFilter(this.data.keyword)
    } catch (e) {
      console.error('加载申请列表失败', e)
      const fallbackList = sortApplyList([
        normalizeApplyItem({ id: '3', title: '教师资格证申请', status: 'ACTION_REQUIRED', time: '2026-03-25 09:20' }),
        normalizeApplyItem({ id: '2', title: '成绩单', status: 'IN_REVIEW', time: '2026-04-08 11:15' }),
        normalizeApplyItem({ id: '1', title: '在读证明', status: '已通过', time: '2026-04-01 10:30' })
      ])
      this.setData({ rawList: fallbackList })
      this.applyFilter(this.data.keyword)
      wx.showToast({ title: '加载失败，已展示本地数据', icon: 'none' })
    } finally {
      this.setData({ loading: false })
      if (options.stopRefresh) {
        wx.stopPullDownRefresh()
      }
    }
  },

  applyFilter(keyword = '') {
    const nextKeyword = String(keyword || '').trim()
    const source = this.data.rawList || []
    const filtered = nextKeyword
      ? source.filter((item) => {
          const searchText = `${item.title} ${item.status} ${item.time}`.toLowerCase()
          return searchText.includes(nextKeyword.toLowerCase())
        })
      : source

    const suggestionList = (nextKeyword ? source.filter((item) => item.title.includes(nextKeyword)) : source)
      .slice(0, 6)
      .map((item) => ({
        id: item.id,
        title: item.title,
        status: item.status
      }))

    this.setData({
      keyword: nextKeyword,
      applyList: sortApplyList(filtered),
      suggestionList,
      emptyText: nextKeyword ? '未找到相关申请' : '暂无申请记录'
    })
  },

  focusSearch() {
    if (this.searchBlurTimer) {
      clearTimeout(this.searchBlurTimer)
    }
    this.setData({
      searchInputFocus: true,
      showSuggestions: true
    })
    this.applyFilter(this.data.keyword)
  },

  onSearchFocus() {
    this.focusSearch()
  },

  onSearchBlur() {
    this.searchBlurTimer = setTimeout(() => {
      this.setData({
        searchInputFocus: false,
        showSuggestions: false
      })
    }, 120)
  },

  onSearchInput(e) {
    const keyword = e.detail.value || ''
    if (this.searchBlurTimer) {
      clearTimeout(this.searchBlurTimer)
    }
    this.setData({ showSuggestions: true })
    this.applyFilter(keyword)
  },

  onSearchConfirm(e) {
    const keyword = e.detail.value || this.data.keyword
    this.applyFilter(keyword)
    this.setData({
      keyword,
      showSuggestions: false,
      searchInputFocus: false
    })
  },

  clearSearch() {
    if (this.searchBlurTimer) {
      clearTimeout(this.searchBlurTimer)
    }
    this.applyFilter('')
    this.setData({
      searchInputFocus: true,
      showSuggestions: true
    })
  },

  selectSuggestion(e) {
    const { keyword } = e.currentTarget.dataset
    if (this.searchBlurTimer) {
      clearTimeout(this.searchBlurTimer)
    }
    this.applyFilter(keyword)
    this.setData({
      keyword,
      showSuggestions: false,
      searchInputFocus: false
    })
  },

  showStatusHelp(e) {
    const { title, description } = e.currentTarget.dataset
    wx.showModal({
      title: title || '状态说明',
      content: description || '暂无状态说明',
      showCancel: false,
      confirmText: '我知道了'
    })
  },

  goToCreate() {
    wx.navigateTo({ url: '/sub-pages/apply/new' })
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/apply/detail?id=${id}` })
  }
})
