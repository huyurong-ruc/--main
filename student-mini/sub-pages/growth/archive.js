const app = getApp()
const growthApi = require('../../api/growth')
const { getModuleMeta } = require('../../utils/growth')

function maskMobile(value = '') {
  const text = String(value).trim()
  if (!text) return ''
  return text.replace(/(\d{3})\d+(\d{4})/, '$1****$2')
}

function maskIdCard(value = '') {
  const text = String(value).trim()
  if (!text) return ''
  if (text.length <= 8) return text
  return `${text.slice(0, 4)}********${text.slice(-4)}`
}

function buildAwardTypeSummary(records = []) {
  const summaryMap = {}
  records.forEach((record) => {
    const awardType = ((record.rawFields || {}).awardType || '').trim()
    if (!awardType) return
    summaryMap[awardType] = (summaryMap[awardType] || 0) + 1
  })
  return Object.keys(summaryMap).map((key) => `${key} ${summaryMap[key]}条`)
}

function normalizeModule(module = {}) {
  const meta = getModuleMeta(module.moduleCode)
  const records = module.records || []
  const editable = module.editMode === 'SELF'
  const recordCount = typeof module.recordCount === 'number' ? module.recordCount : records.length
  return {
    ...module,
    moduleName: module.moduleName || meta.title || module.moduleCode,
    iconText: meta.iconText || '档',
    description: meta.description || '',
    emptyText: meta.emptyText || '暂无记录',
    editable,
    editModeLabel: module.editModeLabel || (editable ? '自主修改' : '禁止修改'),
    recordCount,
    previewRecords: records.slice(0, 1),
    awardTypeSummary: module.moduleCode === 'award-support' ? buildAwardTypeSummary(records) : []
  }
}

Page({
  data: {
    userInfo: null,
    avatarText: '同',
    profileCard: {},
    profileItems: [],
    identityTags: [],
    modules: [],
    moduleCount: 0,
    activeModuleCount: 0,
    totalRecordCount: 0,
    loading: false
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.setData({ userInfo: app.globalData.userInfo || null })
    this.hasLoaded = false
    this.loadArchive()
  },

  onShow() {
    this.setData({ userInfo: app.globalData.userInfo || null })
    if (this.hasLoaded) {
      this.loadArchive()
    } else {
      this.hasLoaded = true
    }
  },

  async loadArchive() {
    this.setData({ loading: true })
    try {
      const res = await growthApi.getGrowthArchive()
      const archive = res.data || {}
      const profile = archive.profile || {}
      const rawMobile = profile.mobile || profile.phone || profile.mobilePhone || ''
      const rawIdCard = profile.idCardNo || profile.idCard || profile.identityNo || ''
      const profileItems = [
        { label: '姓名', value: profile.name || '' },
        { label: '学号', value: profile.studentNo || '' },
        { label: '性别', value: profile.gender || profile.sex || '' },
        { label: '学院', value: profile.collegeName || '' },
        { label: '专业', value: profile.major || '' },
        { label: '年级', value: profile.grade || '' },
        { label: '班级', value: profile.className || '' },
        { label: '培养层次', value: profile.degreeLevel || '' },
        { label: '学籍状态', value: profile.status || '' },
        { label: '政治面貌', value: profile.politicalStatus || '' },
        { label: '入学时间', value: profile.enrollDate || profile.admissionDate || '' },
        { label: '手机号', value: maskMobile(rawMobile) },
        { label: '身份证号', value: maskIdCard(rawIdCard) },
        { label: '邮箱', value: profile.email || '' }
      ].filter((item) => item.value)
      const identityTags = [
        profile.collegeName,
        profile.major,
        profile.degreeLevel,
        profile.grade,
        profile.className
      ].filter(Boolean)
      const modules = (archive.modules || []).map(normalizeModule)
      const totalRecordCount = modules.reduce((sum, item) => sum + (item.recordCount || 0), 0)
      const activeModuleCount = modules.filter((item) => (item.recordCount || 0) > 0).length
      const displayName = profile.name || ((app.globalData.userInfo || {}).name) || '同学'
      this.setData({
        avatarText: displayName.slice(0, 1),
        profileCard: profile,
        profileItems,
        identityTags,
        modules,
        moduleCount: modules.length,
        activeModuleCount,
        totalRecordCount
      })
    } catch (e) {
      console.error('加载个人档案失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  goToModule(e) {
    const moduleCode = e.currentTarget.dataset.moduleCode
    if (!moduleCode) return
    wx.navigateTo({ url: `/sub-pages/growth/manage?moduleCode=${moduleCode}` })
  },

  goToAdd(e) {
    const moduleCode = e.currentTarget.dataset.moduleCode
    if (!moduleCode) return
    wx.navigateTo({ url: `/sub-pages/growth/edit?moduleCode=${moduleCode}` })
  }
})
