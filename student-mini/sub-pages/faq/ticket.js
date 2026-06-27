// sub-pages/faq/ticket.js
const app = getApp()
const {
  clearLatestTicket,
  getTicketErrorMessage,
  getTickets,
  getTicketDetail,
  readLatestTicket,
  readTicketRefreshToken
} = require('../../api/faq')

const STATUS_TABS = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'OPEN' },
  { label: '处理中', value: 'IN_PROGRESS' },
  { label: '已关闭', value: 'CLOSED' }
]

function dedupeTickets(list = []) {
  const seen = new Set()
  return list.filter((item) => {
    const key = String(item.id || '')
    if (!key || seen.has(key)) {
      return false
    }
    seen.add(key)
    return true
  })
}

function canDisplayUnderStatus(ticket = {}, status = '') {
  if (!status) {
    return true
  }
  return String(ticket.status || '').toUpperCase() === String(status || '').toUpperCase()
}

Page({
  data: {
    statusTabs: STATUS_TABS,
    activeStatus: '',
    tickets: [],
    loading: false,
    refreshing: false,
    page: 0,
    pageSize: 10,
    hasMore: true,
    total: 0,
    errorMessage: '',
    createdId: '',
    lastRefreshToken: ''
  },
  
  onLoad(options) {
    if (options?.createdId) {
      this.setData({ createdId: String(options.createdId) })
    }
  },

  onShow() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    const refreshToken = readTicketRefreshToken()
    if (!this._hasInitialized || refreshToken !== this.data.lastRefreshToken) {
      this._hasInitialized = true
      this.setData({ lastRefreshToken: refreshToken })
      this.resetAndLoad(true)
    }
  },

  injectLatestTicket() {
    const latestTicket = readLatestTicket()
    if (!latestTicket || !latestTicket.id || !canDisplayUnderStatus(latestTicket, this.data.activeStatus)) {
      return false
    }

    const nextList = dedupeTickets([latestTicket, ...(this.data.tickets || [])])
    this.setData({
      tickets: nextList,
      total: Math.max(this.data.total, nextList.length)
    })
    return true
  },

  resetAndLoad(withOptimistic = false) {
    this.setData({
      tickets: [],
      page: 0,
      hasMore: true,
      total: 0,
      errorMessage: '',
      refreshing: true
    })

    if (withOptimistic) {
      this.injectLatestTicket()
    }

    return this.loadList({ reset: true })
  },

  async loadList({ reset = false } = {}) {
    if (this.data.loading || (!reset && !this.data.hasMore)) return

    const requestPage = reset ? 0 : this.data.page
    this.setData({ loading: true })

    try {
      const res = await getTickets({
        page: requestPage,
        size: this.data.pageSize,
        status: this.data.activeStatus || null
      })
      const pageData = res?.data || {}
      const remoteList = Array.isArray(pageData.content) ? pageData.content : []
      const latestTicket = readLatestTicket()
      const shouldKeepOptimistic = latestTicket
        && latestTicket.id
        && canDisplayUnderStatus(latestTicket, this.data.activeStatus)
        && !remoteList.some((item) => String(item.id) === String(latestTicket.id))

      const mergedRemote = shouldKeepOptimistic
        ? dedupeTickets([latestTicket, ...remoteList])
        : remoteList

      this.setData({
        tickets: reset
          ? mergedRemote
          : dedupeTickets([...(this.data.tickets || []), ...mergedRemote]),
        hasMore: requestPage + 1 < Number(pageData.totalPages || 0),
        page: requestPage + 1,
        total: Math.max(Number(pageData.totalElements || 0), shouldKeepOptimistic ? 1 : 0),
        errorMessage: ''
      })

      if (!shouldKeepOptimistic && latestTicket?.id) {
        clearLatestTicket()
      }
    } catch (e) {
      console.error('加载工单失败', e)
      const message = getTicketErrorMessage(e, '加载工单失败，请稍后重试')
      if (reset) {
        this.setData({
          tickets: this.data.tickets || [],
          errorMessage: message,
          hasMore: false
        })
      } else {
        this.setData({ errorMessage: message })
      }
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ loading: false, refreshing: false })
    }
  },

  onStatusChange(e) {
    const { status } = e.currentTarget.dataset
    if (String(status || '') === String(this.data.activeStatus || '')) {
      return
    }
    this.setData({ activeStatus: String(status || '') })
    this.resetAndLoad(true)
  },

  retryLoad() {
    this.resetAndLoad(true)
  },

  // 跳转详情：有关联 FAQ 时跳转到 FAQ 列表并定位展开；无关联时提示
  async goToDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return

    // 优先使用列表项中的 matchedFaqId；若不存在则拉取详情补充
    const ticket = (this.data.tickets || []).find((t) => String(t.id) === String(id))
    let matchedFaqId = ticket?.matchedFaqId || null

    if (!matchedFaqId) {
      try {
        const res = await getTicketDetail(id)
        matchedFaqId = res?.data?.matchedFaqId || null
      } catch (e) {
        console.error('获取工单详情失败', e)
      }
    }

    if (matchedFaqId) {
      wx.navigateTo({
        url: `/sub-pages/faq/list?focusId=${matchedFaqId}`
      })
    } else {
      wx.showToast({ title: '该工单暂无关联回答', icon: 'none' })
    }
  },
  
  onPullDownRefresh() {
    this.resetAndLoad(true).finally(() => wx.stopPullDownRefresh())
  },
  
  onReachBottom() {
    if (this.data.hasMore) this.loadList()
  }
})
