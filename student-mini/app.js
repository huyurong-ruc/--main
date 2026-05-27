// app.js
App({
  globalData: {
    userInfo: null,
    token: null,
    // 说明：经全局检索，仓库中未配置已部署的真实域名，所有文档均指向本地开发地址 http://localhost:8080
    // 真机测试前，请通过内网穿透工具（如 ngrok / cpolar / 花生壳）将本地 8080 端口暴露为公网 HTTPS 地址，
    // 然后在下方替换为实际可访问的地址，并在小程序后台配置 request 合法域名。
    // TODO: 替换为实际后端 HTTPS 地址，例如: 'https://abc123.ngrok-free.app/api/v1'
    baseUrl: 'https://camping-penny-concert.ngrok-free.dev/api/v1',
    // 本地演示默认走 mock，未启动后端/数据库时也可登录体验。
    USE_MOCK: true,
    // 云开发代理模式开关：true 时通过云函数代理请求，false 时直接请求 baseUrl
    USE_CLOUD_PROXY: false,
  },

  onLaunch() {
    // 初始化云开发（兼容处理：不支持时不阻断主流程）
    try {
      if (wx.cloud) {
        wx.cloud.init({
          env: 'cloud1-d4gd8z2wa70a64845',
          traceUser: true
        })
        console.log('[App] 云开发初始化成功')
      } else {
        console.warn('[App] 当前基础库不支持云开发')
      }
    } catch (e) {
      console.error('[App] 云开发初始化失败:', e)
    }

    // 检查登录状态（暂不自动跳转，等后端就绪）
    this.checkLoginStatus()
  },

  checkLoginStatus() {
    try {
      const token = wx.getStorageSync('token')
      const userInfo = wx.getStorageSync('userInfo')

      if (token && userInfo) {
        this.globalData.token = token
        this.globalData.userInfo = userInfo
      }
    } catch (e) {
      console.error('读取登录状态失败', e)
    }
  },

  setLoginData(token, userInfo) {
    this.globalData.token = token
    this.globalData.userInfo = userInfo
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
  },

  clearLoginData() {
    this.globalData.token = null
    this.globalData.userInfo = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
  },

  isLoggedIn() {
    return !!this.globalData.token
  },

  logout() {
    this.clearLoginData()
  }
})
