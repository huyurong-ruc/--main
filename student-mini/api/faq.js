// api/faq.js
const { get, post } = require('./request')

function normalizeFaqKeyword(value = '') {
  const raw = String(value || '').trim()
  return raw
}

/**
 * 获取FAQ列表
 * 后端: GET /api/v1/knowledge/search
 * @param {Object} params - { keyword }
 */
exports.getFaqs = (params = {}) => {
  return get('/knowledge/search', {
    keyword: normalizeFaqKeyword(params.keyword)
  }, { showLoading: false })
}

exports.listFaqs = () => {
  return get('/knowledge/faqs', {}, { showLoading: false })
}

/**
 * 提交工单
 */
exports.submitTicket = (data) => {
  return post('/student/qa-tickets', {
    questionText: data?.questionText || data?.content || '',
    contact: data?.contact || ''
  })
}

/**
 * 获取我的工单列表
 */
exports.getTickets = (params) => {
  const page = params?.page != null ? Number(params.page) : 0
  const size = params?.size != null ? Number(params.size) : 10
  const status = params?.status || null
  return get('/student/qa-tickets/page', { page, size, status })
}

/**
 * 获取工单详情
 * @param {string} id - 工单ID
 */
exports.getTicketDetail = (id) => {
  return get(`/student/qa-tickets/${id}`, {}, { showLoading: false })
}
