// pages/mine/index.js
Page({
  data: {},
  
  goToInfo() {
    wx.navigateTo({ url: '/sub-pages/growth/archive' })
  },
  
  goToSettings() {
    wx.navigateTo({ url: '/sub-pages/settings/index' })
  },
  
  goToFeedback() {
    wx.navigateTo({ url: '/sub-pages/feedback/history' })
  },
  
  clearCache() {
    wx.showModal({
      title: '提示',
      content: '确定要清除缓存吗？',
      success: (res) => {
        if (res.confirm) {
          wx.clearStorage()
          wx.showToast({ title: '清理成功', icon: 'success' })
        }
      }
    })
  }
})
