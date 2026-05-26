const { get } = require('./request')

const API_PREFIX = '/student/academic'

/**
 * 获取学业分析概览
 * @param {number|string} studentId - 学生ID
 */
const getAcademicOverview = (studentId) => {
  return get(`${API_PREFIX}/analysis/${studentId}`, {}, { showLoading: false })
}

/**
 * 获取学业报告详情
 * @param {number|string} studentId - 学生ID
 */
const getAcademicReport = (studentId) => {
  return get(`${API_PREFIX}/analysis/${studentId}`)
}

module.exports = {
  getAcademicOverview,
  getAcademicReport
}
