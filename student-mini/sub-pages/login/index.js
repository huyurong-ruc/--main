// sub-pages/login/index.js
const app = getApp()
const api = require('../../api/auth')

function validatePassword(value = '') {
  const password = String(value)
  if (!password) {
    return { valid: false, message: '请输入密码' }
  }
  if (/\s/.test(password)) {
    return { valid: false, message: '密码不能包含空格' }
  }
  if (password.length < 6 || password.length > 20) {
    return { valid: false, message: '密码需为6-20位字符' }
  }
  return { valid: true, message: '' }
}

Page({
  data: {
    studentNo: '',
    password: '',
    passwordTouched: false,
    passwordError: '',
    canSubmit: false,
    loading: false,
    errorCount: 0,
    locked: false,
    lockTime: 0
  },
  
  onLoad() {
    // 检查是否已登录
    if (app.isLoggedIn()) {
      wx.switchTab({
        url: '/pages/index/index'
      })
    }
  },
  
  // 输入处理
  onStudentNoInput(e) {
    this.setData({ studentNo: e.detail.value })
    this.updateCanSubmit()
  },
  
  onPasswordInput(e) {
    const password = e.detail.value
    const result = validatePassword(password)
    this.setData({
      password,
      passwordTouched: true,
      passwordError: result.valid ? '' : result.message
    })
    this.updateCanSubmit()
  },

  onPasswordBlur() {
    const result = validatePassword(this.data.password)
    this.setData({
      passwordTouched: true,
      passwordError: result.valid ? '' : result.message
    })
    this.updateCanSubmit()
  },

  updateCanSubmit() {
    const { studentNo, password } = this.data
    const passwordResult = validatePassword(password)
    const canSubmit = Boolean(studentNo.trim()) && passwordResult.valid
    if (canSubmit !== this.data.canSubmit) {
      this.setData({ canSubmit })
    }
  },
  
  // 登录提交
  async handleLogin() {
    const { studentNo, password, loading, locked } = this.data
    
    if (locked) {
      wx.showToast({ title: `请${this.data.lockTime}秒后再试`, icon: 'none' })
      return
    }
    
    if (loading) return
    
    // 表单验证
    if (!studentNo.trim()) {
      wx.showToast({ title: '请输入学号', icon: 'none' })
      return
    }

    const passwordResult = validatePassword(password)
    if (!passwordResult.valid) {
      this.setData({
        passwordTouched: true,
        passwordError: passwordResult.message
      })
      this.updateCanSubmit()
      return
    }
    
    this.setData({ loading: true })
    
    try {
      // 使用后端 /auth/login 接口
      const res = await api.login({
        username: studentNo.trim(),
        password
      })

      if (!res || res.success === false) {
        throw new Error(res?.message || '登录失败，请检查账号密码')
      }

      const payload = res.data || {}
      const userId = payload.userId || (payload.userInfo && payload.userInfo.id) || ''
      const role = payload.role || (payload.userInfo && payload.userInfo.role) || 'STUDENT'

      let profile = null
      try {
        const meRes = await api.getCurrentUser()
        profile = meRes && meRes.data ? meRes.data : null
      } catch (e) {
        profile = null
      }
      
      // 保存登录数据
      app.setLoginData(payload.token, {
        id: profile?.userId || userId || studentNo.trim(),
        studentId: profile?.studentId || '',
        name: profile?.name || profile?.username || studentNo.trim(),
        studentNo: profile?.studentNo || studentNo.trim(),
        role: profile?.role || role,
        major: profile?.major || '',
        grade: profile?.grade || ''
      })
      
      wx.showToast({ title: '登录成功', icon: 'success' })

      this.setData({ errorCount: 0, locked: false, lockTime: 0 })
      
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/index/index'
        })
      }, 1000)
      
    } catch (e) {
      console.error('登录失败', e)
      wx.showToast({ title: e.message || '登录失败，请检查账号密码', icon: 'none' })
      
      // 累计失败次数（前端先行短暂锁定，避免触发后端长时间锁）
      const newCount = this.data.errorCount + 1
      this.setData({ errorCount: newCount })
      
      if (newCount >= 3) {
        this.startLock(20)
      }
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 锁定处理
  startLock(seconds) {
    this.setData({ locked: true, lockTime: seconds })
    
    const timer = setInterval(() => {
      const time = this.data.lockTime - 1
      if (time <= 0) {
        clearInterval(timer)
        this.setData({ locked: false, lockTime: 0, errorCount: 0 })
      } else {
        this.setData({ lockTime: time })
      }
    }, 1000)
  }
})
