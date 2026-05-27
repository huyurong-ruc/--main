// sub-pages/settings/index.js
const app = getApp()

const NOTIFICATION_SETTINGS_KEY = 'notificationSettings'
const DEFAULT_NOTIFICATIONS = {
  system: true,
  activity: true,
  message: true
}

function normalizeNotifications(source = {}) {
  return {
    system: source.system !== false,
    activity: source.activity !== false,
    message: source.message !== false
  }
}

Page({
  data: {
    cacheSize: '0 KB',
    notifications: { ...DEFAULT_NOTIFICATIONS },
    version: '1.0.0'
  },
  
  onLoad() {
    this.loadNotificationSettings()
  },

  onShow() {
    this.loadNotificationSettings()
    this.calculateCacheSize()
  },

  loadNotificationSettings() {
    try {
      const stored = wx.getStorageSync(NOTIFICATION_SETTINGS_KEY)
      const notifications = normalizeNotifications(stored && typeof stored === 'object' ? stored : DEFAULT_NOTIFICATIONS)
      this.setData({ notifications })
      app.globalData.notificationSettings = notifications
    } catch (e) {
      console.error('读取通知设置失败', e)
      this.setData({ notifications: { ...DEFAULT_NOTIFICATIONS } })
    }
  },

  saveNotificationSettings(notifications) {
    const nextNotifications = normalizeNotifications(notifications)
    try {
      wx.setStorageSync(NOTIFICATION_SETTINGS_KEY, nextNotifications)
      app.globalData.notificationSettings = nextNotifications
      return true
    } catch (e) {
      console.error('保存通知设置失败', e)
      return false
    }
  },
  
  // 计算缓存大小
  async calculateCacheSize() {
    try {
      const info = await wx.getStorageInfo()
      const sizeInKB = info.currentSize
      let cacheSize = ''
      if (sizeInKB < 1024) {
        cacheSize = sizeInKB + ' KB'
      } else {
        cacheSize = (sizeInKB / 1024).toFixed(2) + ' MB'
      }
      this.setData({ cacheSize })
    } catch (e) {
      console.error('获取缓存信息失败', e)
    }
  },
  
  // 清除缓存
  handleClearCache() {
    wx.showModal({
      title: '提示',
      content: '确定要清除缓存吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '清除中...' })
          const preservedValues = {
            token: wx.getStorageSync('token'),
            userInfo: wx.getStorageSync('userInfo'),
            notificationSettings: wx.getStorageSync(NOTIFICATION_SETTINGS_KEY)
          }

          wx.clearStorage({
            success: () => {
              try {
                if (preservedValues.token) {
                  wx.setStorageSync('token', preservedValues.token)
                }
                if (preservedValues.userInfo) {
                  wx.setStorageSync('userInfo', preservedValues.userInfo)
                }
                if (preservedValues.notificationSettings) {
                  wx.setStorageSync(NOTIFICATION_SETTINGS_KEY, preservedValues.notificationSettings)
                }
              } catch (e) {
                console.error('恢复关键缓存失败', e)
              }

              wx.hideLoading()
              this.loadNotificationSettings()
              this.calculateCacheSize()
              wx.showToast({ title: '清除成功', icon: 'success' })
            },
            fail: () => {
              wx.hideLoading()
              wx.showToast({ title: '清除失败', icon: 'none' })
            }
          })
        }
      }
    })
  },
  
  // 开关通知
  onNotificationChange(e) {
    const type = e.currentTarget.dataset.type
    const value = !!e.detail.value
    const notifications = {
      ...this.data.notifications,
      [type]: value
    }

    this.setData({ notifications })
    const saved = this.saveNotificationSettings(notifications)
    
    wx.showToast({ 
      title: saved ? (value ? '已开启' : '已关闭') : '保存失败',
      icon: 'none',
      duration: 1000
    })
  },
  
  // 检查更新
  handleCheckUpdate() {
    wx.showToast({ 
      title: '当前已是最新版本', 
      icon: 'success',
      duration: 1500
    })
  },
  
  // 跳转隐私政策
  goToPrivacy() {
    wx.navigateTo({ url: '/sub-pages/agreement/index?title=隐私政策' })
  },
  
  // 跳转用户协议
  goToAgreement() {
    wx.navigateTo({ url: '/sub-pages/agreement/index?title=用户协议' })
  }
})
