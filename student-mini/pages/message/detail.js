// pages/message/detail.js
const app = getApp()
const { get } = require('../../api/request')

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

function parseMessageId(id) {
  const raw = String(id || '')
  const match = raw.match(/^([a-z]+)-(.+)$/i)
  if (match) {
    return { kind: match[1].toLowerCase(), value: match[2] }
  }
  return { kind: 'notice', value: raw }
}

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
    this.setData({ loading: true, detail: null })

    try {
      const parsed = parseMessageId(this.data.id)
      if (parsed.kind === 'reminder') {
        await this.loadReminderDetail(parsed.value)
      } else {
        await this.loadNoticeDetail(parsed.value)
      }
    } catch (e) {
      console.error('load detail failed', e)
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadNoticeDetail(id) {
    const res = await get('/student/notices')
    const list = Array.isArray(res?.data) ? res.data : []
    const hit = list.find((item) => String(item.id) === String(id))
    if (!hit) {
      wx.showToast({ title: 'Notice not found', icon: 'none' })
      return
    }

    this.setData({
      detail: {
        kind: 'notice',
        id: hit.id,
        title: hit.title,
        content: hit.summary || '',
        source: 'Official notice',
        publisher: 'School',
        publishTime: formatTime(hit.publishTime),
        tag: Array.isArray(hit.tags) ? hit.tags[0] : '',
        actionRoute: '',
        originalUrl: '',
        attachments: []
      }
    })
    wx.setNavigationBarTitle({ title: 'Notice Detail' })
  },

  async loadReminderDetail(taskId) {
    const res = await get('/student/party-progress/reminders')
    const list = Array.isArray(res?.data) ? res.data : []
    const hit = list.find((item) => String(item.taskId) === String(taskId))
    if (!hit) {
      wx.showToast({ title: 'Reminder not found', icon: 'none' })
      return
    }

    this.setData({
      detail: {
        kind: 'reminder',
        id: hit.taskId != null ? hit.taskId : taskId,
        title: hit.title || 'Party Reminder',
        content: [hit.content, hit.triggerRule].filter(Boolean).join('\n\n'),
        source: 'Party Reminder',
        publisher: hit.stageName || 'Party Process',
        publishTime: formatTime(hit.remindDate),
        tag: hit.level || '',
        actionRoute: '/sub-pages/party/index',
        actionText: 'View party progress',
        originalUrl: '',
        attachments: []
      }
    })
    wx.setNavigationBarTitle({ title: 'Party Reminder Detail' })
  },

  copyOriginalLink() {
    const url = (this.data.detail || {}).originalUrl
    if (!url) return
    wx.setClipboardData({
      data: url,
      success: () => wx.showToast({ title: 'Link copied', icon: 'success' })
    })
  },

  goToInternalRoute() {
    const route = (this.data.detail || {}).actionRoute
    if (!route) return
    this.navigateByRoute(route)
  },

  navigateByRoute(route) {
    const safeRoute = String(route || '')
    const tabRoutes = ['/pages/index/index', '/pages/message/index', '/pages/profile/index']
    if (tabRoutes.includes(safeRoute)) {
      wx.switchTab({ url: safeRoute })
      return
    }
    wx.navigateTo({ url: safeRoute })
  },

  downloadAttachment(e) {
    const { url } = e.currentTarget.dataset
    if (!url) return
    const lowerUrl = String(url).toLowerCase()
    const isFileLink = /\.(pdf|doc|docx|xls|xlsx|ppt|pptx)$/i.test(lowerUrl)
    const isHttp = /^https?:\/\//.test(url)

    if (!isHttp || !isFileLink) {
      wx.setClipboardData({
        data: url,
        success: () => wx.showToast({ title: 'Attachment link copied', icon: 'success' })
      })
      return
    }
    wx.showLoading({ title: 'Downloading...' })
    wx.downloadFile({
      url,
      success: (res) => {
        wx.hideLoading()
        wx.openDocument({
          filePath: res.tempFilePath,
          showMenu: true,
          fail: () => wx.showToast({ title: 'Open failed', icon: 'none' })
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: 'Download failed', icon: 'none' })
      }
    })
  }
})
