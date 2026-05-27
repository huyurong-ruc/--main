/**
 * 学生端用户状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { studentLogin, studentLogout, getCurrentStudent } from '@/api/student'

export const useStudentStore = defineStore('student', () => {
  function normalizeStudentUser(profile = {}, fallback = {}) {
    return {
      id: profile.id ?? fallback.id ?? fallback.userId ?? profile.userId ?? null,
      userId: profile.userId ?? fallback.userId ?? profile.id ?? fallback.id ?? null,
      studentId: profile.studentId ?? fallback.studentId ?? profile.id ?? fallback.userId ?? null,
      username: profile.username ?? fallback.username ?? profile.studentNo ?? fallback.studentNo ?? '',
      fullName: profile.fullName ?? profile.name ?? fallback.fullName ?? fallback.name ?? '',
      name: profile.name ?? profile.fullName ?? fallback.name ?? fallback.fullName ?? '',
      studentNo: profile.studentNo ?? fallback.studentNo ?? profile.username ?? fallback.username ?? '',
      major: profile.major ?? fallback.major ?? '',
      grade: profile.grade ?? fallback.grade ?? '',
      className: profile.className ?? fallback.className ?? '',
      collegeName: profile.collegeName ?? fallback.collegeName ?? '',
      email: profile.email ?? fallback.email ?? '',
      role: profile.role ?? fallback.role ?? 'STUDENT',
      permissions: profile.permissions ?? fallback.permissions ?? ['*']
    }
  }

  // 状态
  const token = ref(localStorage.getItem('student_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('student_user') || 'null'))
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const fullName = computed(() => userInfo.value?.fullName || '')
  const studentNo = computed(() => userInfo.value?.studentNo || '')
  const role = computed(() => userInfo.value?.role || '')

  // 初始化
  function init() {
    const savedToken = localStorage.getItem('student_token')
    const savedUser = localStorage.getItem('student_user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      userInfo.value = JSON.parse(savedUser)
    }
  }

  // 登录
  async function login(username, password) {
    loading.value = true
    
    try {
      const res = await studentLogin(username, password)
      const authData = res?.data || {}

      if (!authData.token) {
        throw new Error(res?.message || '登录失败')
      }

      token.value = authData.token
      localStorage.setItem('student_token', token.value)

      let normalizedUser = normalizeStudentUser(authData.userInfo || {}, {
        userId: authData.userId,
        studentId: authData.userId,
        username,
        studentNo: username,
        role: authData.role
      })

      try {
        const profileRes = await getCurrentStudent()
        if (profileRes?.data) {
          normalizedUser = normalizeStudentUser(profileRes.data, normalizedUser)
        }
      } catch (profileError) {
        console.warn('获取学生资料失败，回退登录返回数据:', profileError)
      }

      userInfo.value = normalizedUser
      
      // 持久化
      localStorage.setItem('student_user', JSON.stringify(normalizedUser))
      
      return { success: true, data: normalizedUser }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    } finally {
      loading.value = false
    }
  }

  // 登出
  async function logout() {
    try {
      await studentLogout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 清除本地状态
      token.value = ''
      userInfo.value = null
      
      localStorage.removeItem('student_token')
      localStorage.removeItem('student_user')
    }
  }

  // 获取当前用户信息
  async function fetchUserInfo() {
    if (!token.value) return null
    
    try {
      const res = await getCurrentStudent()
      if (res.success && res.data) {
        const normalizedUser = normalizeStudentUser(res.data, userInfo.value || {})
        userInfo.value = normalizedUser
        localStorage.setItem('student_user', JSON.stringify(normalizedUser))
        return normalizedUser
      }
      return null
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }

  // 权限检查
  function hasPermission(permission) {
    if (!userInfo.value?.permissions) return false
    return userInfo.value.permissions.includes('*') || userInfo.value.permissions.includes(permission)
  }

  return {
    // 状态
    token,
    userInfo,
    loading,
    currentUser: userInfo,
    
    // 计算属性
    isLoggedIn,
    fullName,
    studentNo,
    role,
    
    // 方法
    init,
    login,
    logout,
    fetchUserInfo,
    hasPermission
  }
})

// 兼容别名的导出
export const useUserStore = useStudentStore
