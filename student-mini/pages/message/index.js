// pages/message/index.js
const app = getApp()
const { get } = require('../../api/request')

function resolveMessageStyle(item = {}) {
  if (item.kind === 'reminder') {
    return { typeClass: 'pending', icon: '🔔' }
  }
  const tags = Array.isArray(item.tags) ? item.tags : []
  const joined = tags.join(',')
  if (joined.includes('就业') || joined.includes('实习')) {
    return { typeClass: 'personalized', icon: '💬' }
  }
  if (joined.includes('党') || joined.includes('团') || joined.includes('流程')) {
    return { typeClass: 'pending', icon: '📝' }
  }
  return { typeClass: 'feedback', icon: '💬' }
}

Page({
  data: {
    messages: []
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar && tabBar.setData) {
      tabBar.setData({ selected: 1 })
    }
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadMessages()
  },

  async loadMessages() {
    try {
      const [noticeRes, reminderRes] = await Promise.all([
        get('/student/notices'),
        get('/student/party-progress/reminders')
      ])

      const notices = Array.isArray(noticeRes?.data) ? noticeRes.data : []
      const reminders = Array.isArray(reminderRes?.data) ? reminderRes.data : []

      const noticeItems = notices.map((item) => {
        const { typeClass, icon } = resolveMessageStyle(item)
        const publishTime = item.publishTime ? String(item.publishTime).replace('T', ' ').slice(0, 16) : ''
        return {
          id: `notice-${item.id}`,
          kind: 'notice',
          title: item.title || 'Notice',
          icon,
          typeClass,
          time: publishTime,
          tag: Array.isArray(item.tags) ? item.tags[0] : '',
          content: item.summary || '',
          unread: false,
          actionRoute: '',
          actionText: ''
        }
      })

      const reminderItems = reminders
        .filter((item) => String(item.channel || '').toLowerCase() === 'miniprogram')
        .filter((item) => ['sent', 'generated'].includes(String(item.status || '').toLowerCase()))
        .map((item) => {
          const { typeClass, icon } = resolveMessageStyle({ kind: 'reminder' })
          const publishTime = item.remindDate ? String(item.remindDate).replace('T', ' ').slice(0, 16) : ''
          return {
            id: `reminder-${item.taskId != null ? item.taskId : publishTime}`,
            kind: 'reminder',
            title: item.title || 'Party Reminder',
            icon,
            typeClass,
            time: publishTime,
            tag: item.stageName || item.level || 'Party Reminder',
            content: item.content || '',
            unread: false,
            actionRoute: '/sub-pages/party/index',
            actionText: 'View Party'
          }
        })

      const nextList = [...reminderItems, ...noticeItems].sort((a, b) => String(b.time || '').localeCompare(String(a.time || '')))
      this.setData({ messages: nextList })
    } catch (e) {
      console.error('load messages failed', e)
      this.setData({ messages: [] })
    }
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/message/detail?id=${id}` })
  },

  goToAction(e) {
    const { route } = e.currentTarget.dataset
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
  }
})
