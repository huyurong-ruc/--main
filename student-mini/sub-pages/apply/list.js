// sub-pages/apply/list.js
const app = getApp()
const applyApi = require('../../api/apply')
const { getApplyDisplayTitle } = require('../../api/apply-business')
const { getApplyStatusMeta, isCompletedStatus } = require('../../api/apply-status')

function parseTime(value = '') {
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0] = value
    const parsed = new Date(year, (month || 1) - 1, day || 1, hour, minute).getTime()
    return Number.isNaN(parsed) ? 0 : parsed
  }

  const text = String(value || '').trim()
  if (!text) return 0
  const normalized = text.includes('T') ? text.replace('T', ' ') : text
  const parsed = new Date(normalized.replace(/-/g, '/')).getTime()
  return Number.isNaN(parsed) ? 0 : parsed
}

function formatApplyTime(value = '') {
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day, hour = 0, minute = 0] = value
    return `${year}-${String(month || 1).padStart(2, '0')}-${String(day || 1).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  }

  const text = String(value || '').trim()
  if (!text) return ''
  const normalized = text.includes('T') ? text.replace('T', ' ') : text
  return normalized.length >= 16 ? normalized.slice(0, 16) : normalized
}

function buildIconText(title = '') {
  const text = String(title || '')
  if (text.includes('证')) return '证'
  if (text.includes('单')) return '单'
  return '申'
}

function normalizeApplyItem(item = {}, displayMeta = {}) {
  const rawTitle = displayMeta.title || item.certificateType || item.typeName || item.title || '未命名申请'
  const title = getApplyDisplayTitle(rawTitle, item.typeKey)
  const rawStatus = item.statusCode || item.status || item.statusText || ''
  const statusMeta = getApplyStatusMeta(rawStatus)
  const timeValue = displayMeta.time || item.createdAt || item.createTime || item.submittedAt || item.time || ''
  const time = formatApplyTime(timeValue)

  return {
    id: String(item.id || ''),
    title,
    iconText: buildIconText(title),
    status: statusMeta.listLabel,
    statusClass: statusMeta.listClass,
    statusTitle: statusMeta.title,
    statusDescription: statusMeta.description,
    time,
    sortTime: parseTime(timeValue),
    completed: isCompletedStatus(rawStatus)
  }
}

async function buildApplyDisplayMeta(sourceList = []) {
  const detailEntries = await Promise.allSettled(
    sourceList.map(async (item) => {
      const detailRes = await applyApi.getApplyDetail(item.id, { showLoading: false })
      const detail = detailRes?.data || {}
      return [
        String(item.id || ''),
        {
          title: getApplyDisplayTitle(detail.certificateType || item.certificateType || item.typeName || item.title || '', item.typeKey),
          time: detail.submittedAt || detail.createTime || detail.createdAt || ''
        }
      ]
    })
  )

  return detailEntries.reduce((acc, result) => {
    if (result.status === 'fulfilled') {
      const [id, meta] = result.value
      acc[id] = meta
    }
    return acc
  }, {})
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
      const profile = app.globalData.userInfo || {}
      const studentId = Number(profile.studentId || profile.id || 0)
      if (!studentId || Number.isNaN(studentId)) {
        wx.showToast({ title: '登录信息缺少学生ID', icon: 'none' })
        this.setData({ rawList: [], applyList: [] })
        return
      }

      const res = await applyApi.getApplyList(studentId)
      const sourceList = Array.isArray(res.data)
        ? res.data
        : Array.isArray(res.data?.list)
          ? res.data.list
          : []
      const displayMeta = await buildApplyDisplayMeta(sourceList)
      const rawList = sortApplyList(sourceList.map((item) => normalizeApplyItem(item, displayMeta[String(item.id || '')] || {})))
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
