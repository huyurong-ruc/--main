const { get } = require('./request')

const API_PREFIX = '/academic'
const TRANSCRIPT_STORAGE_KEY = 'academicTranscriptMockState'

function isMockMode() {
  try {
    const app = getApp()
    return !!(app && app.globalData && app.globalData.USE_MOCK)
  } catch (e) {
    return false
  }
}

function getStorageState() {
  try {
    return wx.getStorageSync(TRANSCRIPT_STORAGE_KEY) || {}
  } catch (e) {
    return {}
  }
}

function setStorageState(state) {
  wx.setStorageSync(TRANSCRIPT_STORAGE_KEY, state)
}

function buildMockReport(studentId, transcript = {}) {
  const updateTime = transcript.uploadTime || '2026-04-10 10:00'
  return {
    studentId,
    gpa: '3.65',
    credits: 128,
    rank: '第15名',
    warningCount: 1,
    percent: 82,
    currentCredits: 128,
    totalCredits: 156,
    updateTime,
    modules: [
      { name: '通识课程', current: 38, total: 42 },
      { name: '专业必修', current: 56, total: 64 },
      { name: '专业选修', current: 22, total: 30 },
      { name: '实践环节', current: 12, total: 20 }
    ],
    gradeDistribution: [
      { range: '90-100', count: 8, percent: 32, color: '#1677ff' },
      { range: '80-89', count: 10, percent: 40, color: '#52c41a' },
      { range: '70-79', count: 5, percent: 20, color: '#faad14' },
      { range: '60-69', count: 2, percent: 8, color: '#ff7a45' }
    ],
    gpaTrend: [
      { semester: '大一上', gpa: '3.42' },
      { semester: '大一下', gpa: '3.55' },
      { semester: '大二上', gpa: '3.61' },
      { semester: '大二下', gpa: '3.65' }
    ],
    warnings: [
      { id: '1', type: '选修学分待补足', description: '距培养方案要求仍差 8 学分，请优先补足专业选修课程。' }
    ]
  }
}

function getStudentTranscriptState(studentId) {
  const state = getStorageState()
  return state[String(studentId)] || {
    currentTranscript: null,
    historyTranscripts: [],
    report: null
  }
}

function saveStudentTranscriptState(studentId, nextState) {
  const state = getStorageState()
  state[String(studentId)] = nextState
  setStorageState(state)
}

function buildTranscriptStatus(studentId) {
  const state = getStudentTranscriptState(studentId)
  const hasCurrentTranscript = !!state.currentTranscript
  const hasHistoryTranscript = Array.isArray(state.historyTranscripts) && state.historyTranscripts.length > 0
  return {
    hasCurrentTranscript,
    hasHistoryTranscript,
    canLoadReport: hasCurrentTranscript || hasHistoryTranscript,
    currentTranscript: state.currentTranscript,
    latestHistoryTranscript: hasHistoryTranscript ? state.historyTranscripts[0] : null
  }
}

function requestSilently(url, options = {}) {
  return new Promise((resolve, reject) => {
    let app
    try {
      app = getApp()
    } catch (e) {
      reject({ success: false, message: '应用未初始化' })
      return
    }

    wx.request({
      url: `${String(app.globalData?.baseUrl || '').trim()}${url}`,
      method: options.method || 'GET',
      data: options.data,
      timeout: 30000,
      header: {
        'Content-Type': 'application/json',
        'ngrok-skip-browser-warning': 'true',
        'Authorization': app.globalData?.token ? `Bearer ${app.globalData.token}` : '',
        ...(options.header || {})
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data?.success) {
          resolve(res.data)
          return
        }
        reject({
          statusCode: res.statusCode,
          data: res.data,
          message: res.data?.message || '请求失败'
        })
      },
      fail: (err) => reject(err)
    })
  })
}

function hasAcademicReport(raw = {}) {
  const missingModules = Array.isArray(raw.missingModules) ? raw.missingModules : []
  const recommendedCourses = Array.isArray(raw.recommendedCourses) ? raw.recommendedCourses : []
  const summary = String(raw.summary || '')
  return (
    missingModules.length > 0 ||
    recommendedCourses.length > 0 ||
    Number(raw.totalRequiredCredits || 0) > 0 ||
    Number(raw.totalEarnedCredits || 0) > 0 ||
    Number(raw.totalMissingCredits || 0) > 0 ||
    !summary.includes('暂无有效审计报告')
  )
}

function normalizeTranscriptRecord(transcript = {}) {
  if (!transcript || !transcript.transcriptId) {
    return null
  }
  return {
    id: transcript.transcriptId,
    name: transcript.term ? `${transcript.term}成绩单` : '成绩单',
    size: 0,
    uploadTime: transcript.parsedAt ? String(transcript.parsedAt).replace('T', ' ').slice(0, 16) : '',
    valid: true
  }
}

function buildStatusFromRemote(studentId, transcript = null, analysis = null) {
  const transcriptRecord = normalizeTranscriptRecord(transcript)
  const canLoadReport = hasAcademicReport(analysis || {})
  const fallbackRecord = canLoadReport ? {
    id: `report-${studentId}`,
    name: '已有学业分析报告',
    size: 0,
    uploadTime: '',
    valid: true
  } : null

  return {
    hasCurrentTranscript: !!transcriptRecord,
    hasHistoryTranscript: !!transcriptRecord || canLoadReport,
    canLoadReport,
    currentTranscript: transcriptRecord || fallbackRecord,
    latestHistoryTranscript: transcriptRecord || fallbackRecord
  }
}

function buildGradeDistribution(report = {}) {
  const earned = Number(report.totalEarnedCredits || 0)
  const missing = Number(report.totalMissingCredits || 0)
  const total = earned + missing
  if (total <= 0) {
    return []
  }
  return [
    {
      range: '已完成',
      count: earned,
      percent: Math.round((earned / total) * 100),
      color: '#52c41a'
    },
    {
      range: '待补足',
      count: missing,
      percent: Math.round((missing / total) * 100),
      color: '#faad14'
    }
  ].filter((item) => item.count > 0)
}

function normalizeAnalysisReport(raw = {}) {
  const missingModules = Array.isArray(raw.missingModules) ? raw.missingModules : []
  return {
    studentId: raw.studentId,
    gpa: raw.completionRate != null ? `${raw.completionRate}%` : '--',
    credits: raw.totalEarnedCredits ?? 0,
    rank: raw.grade || '待更新',
    warningCount: missingModules.length,
    percent: raw.completionRate ?? 0,
    currentCredits: raw.totalEarnedCredits ?? 0,
    totalCredits: raw.totalRequiredCredits ?? 0,
    updateTime: raw.dataSourceNote ? '已同步' : '-',
    modules: missingModules.map((item) => ({
      name: item.moduleName || '模块',
      current: item.earnedCredits ?? 0,
      total: item.requiredCredits ?? 0,
      percent: item.completionRate ?? 0
    })),
    gradeDistribution: buildGradeDistribution(raw),
    gpaTrend: [],
    warnings: missingModules.map((item, index) => ({
      id: String(index + 1),
      type: item.moduleName || '模块缺口',
      description: `已获 ${item.earnedCredits ?? 0}/${item.requiredCredits ?? 0} 学分，仍缺 ${item.missingCredits ?? 0} 学分${item.recommendedCourses ? `，建议课程：${item.recommendedCourses}` : ''}`
    })),
    summary: raw.summary || '',
    reviewHints: raw.reviewHints || [],
    dataSourceNote: raw.dataSourceNote || ''
  }
}

const getAcademicTranscriptStatus = (studentId) => {
  if (isMockMode()) {
    return Promise.resolve({ success: true, data: buildTranscriptStatus(studentId) })
  }
  return Promise.allSettled([
    requestSilently(`/academic/programs/transcripts/student/${studentId}`),
    get(`${API_PREFIX}/analysis/${studentId}`, {}, { showLoading: false })
  ]).then(([transcriptResult, analysisResult]) => {
    if (analysisResult.status !== 'fulfilled') {
      throw analysisResult.reason
    }

    let transcript = null
    if (transcriptResult.status === 'fulfilled') {
      transcript = transcriptResult.value?.data || null
    } else {
      const message = String(transcriptResult.reason?.message || transcriptResult.reason?.data?.message || '')
      const statusCode = transcriptResult.reason?.statusCode
      const isNotUploaded = statusCode === 400 || statusCode === 404 || message.includes('未找到成绩单')
      if (!isNotUploaded) {
        throw transcriptResult.reason
      }
    }

    return {
      success: true,
      data: buildStatusFromRemote(studentId, transcript, analysisResult.value?.data || {})
    }
  })
}

/**
 * 获取学业分析概览
 * @param {number|string} studentId - 学生ID
 */
const getAcademicOverview = (studentId) => {
  if (isMockMode()) {
    const state = getStudentTranscriptState(studentId)
    const status = buildTranscriptStatus(studentId)
    if (!status.canLoadReport) {
      return Promise.resolve({ success: true, data: null })
    }
    const report = state.report || buildMockReport(studentId, status.currentTranscript || status.latestHistoryTranscript || {})
    return Promise.resolve({ success: true, data: report })
  }
  return get(`${API_PREFIX}/analysis/${studentId}`, {}, { showLoading: false })
    .then((res) => ({
      ...res,
      data: normalizeAnalysisReport(res.data || {})
    }))
}

/**
 * 获取学业报告详情
 * @param {number|string} studentId - 学生ID
 */
const getAcademicReport = (studentId) => {
  if (isMockMode()) {
    return getAcademicOverview(studentId)
  }
  return get(`${API_PREFIX}/analysis/${studentId}`, {}, { showLoading: false })
    .then((res) => ({
      ...res,
      data: normalizeAnalysisReport(res.data || {})
    }))
}

const saveUploadedTranscript = (studentId, fileInfo = {}) => {
  const id = String(studentId)
  const state = getStudentTranscriptState(id)
  const uploadTime = fileInfo.uploadTime || new Date().toISOString().replace('T', ' ').slice(0, 16)
  const transcriptRecord = {
    name: fileInfo.name || '成绩单.pdf',
    size: fileInfo.size || 0,
    uploadTime,
    valid: true
  }

  const historyTranscripts = [transcriptRecord].concat(
    (state.historyTranscripts || []).filter((item) => item.uploadTime !== transcriptRecord.uploadTime)
  )

  const nextState = {
    currentTranscript: transcriptRecord,
    historyTranscripts,
    report: buildMockReport(id, transcriptRecord)
  }

  saveStudentTranscriptState(id, nextState)
  return Promise.resolve({ success: true, data: nextState })
}

module.exports = {
  getAcademicOverview,
  getAcademicReport,
  getAcademicTranscriptStatus,
  saveUploadedTranscript
}
