const { get } = require('./request')

const API_PREFIX = '/student/academic'
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

const getAcademicTranscriptStatus = (studentId) => {
  if (isMockMode()) {
    return Promise.resolve({ success: true, data: buildTranscriptStatus(studentId) })
  }
  return get(`${API_PREFIX}/transcript-status/${studentId}`, {}, { showLoading: false })
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
}

/**
 * 获取学业报告详情
 * @param {number|string} studentId - 学生ID
 */
const getAcademicReport = (studentId) => {
  if (isMockMode()) {
    return getAcademicOverview(studentId)
  }
  return get(`${API_PREFIX}/analysis/${studentId}`)
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
