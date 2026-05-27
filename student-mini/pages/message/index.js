// pages/message/index.js
const app = getApp()
const { get } = require('../../api/request')

function resolveMessageStyle(item = {}) {
  const tags = Array.isArray(item.tags) ? item.tags : []
  const joined = tags.join(',')
  if (joined.includes('就业') || joined.includes('实习')) {
    return { typeClass: 'personalized', icon: '💬' }
  }
  if (joined.includes('党') || joined.includes('团') || joined.includes('流程')) {
    return { typeClass: 'pending', icon: '📅' }
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
      const res = await get('/student/notices')
      const list = Array.isArray(res?.data) ? res.data : []
      const nextList = list.map((item) => {
        const { typeClass, icon } = resolveMessageStyle(item)
        const publishTime = item.publishTime
          ? String(item.publishTime).replace('T', ' ').slice(0, 16)
          : ''
        return {
          id: String(item.id),
          title: item.title,
          icon,
          typeClass,
          time: publishTime,
          tag: Array.isArray(item.tags) ? item.tags[0] : '',
          content: item.summary || '',
          unread: false,
          actionRoute: ''
        }
      })
      this.setData({ messages: nextList })
    } catch (e) {
      console.error('加载通知失败', e)
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
