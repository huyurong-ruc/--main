// pages/message/index.js
Page({
  data: {
    messages: [
      {
        id: '1',
        title: '待办通知',
        icon: '📅',
        typeClass: 'pending',
        time: '1分钟前',
        content: '您的申请已通过',
        actionText: '前往我的证明/申请',
        actionRoute: '/sub-pages/apply/list',
        unread: true
      },
      {
        id: '2',
        title: '反馈通知',
        icon: '💬',
        typeClass: 'feedback',
        time: '1分钟前',
        content: '您的反馈已得到解答',
        actionText: '前往反馈记录',
        actionRoute: '/sub-pages/feedback/history',
        unread: true
      },
      {
        id: '3',
        title: '个性化通知',
        icon: '💬',
        typeClass: 'personalized',
        time: '1分钟前',
        tag: '就业',
        content: '人大就业：2026春季双选会即将开始！',
        unread: true
      }
    ]
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar && tabBar.setData) {
      tabBar.setData({ selected: 1 })
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
