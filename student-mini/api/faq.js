// api/faq.js
const { get, post } = require('./request')

const QA_TICKET_REFRESH_KEY = 'qa_ticket_refresh_token'
const QA_TICKET_LATEST_KEY = 'qa_ticket_latest'

function normalizeFaqKeyword(value = '') {
  const raw = String(value || '').trim()
  return raw
}

function resolveTicketStatusMeta(status = '') {
  const normalized = String(status || '').trim().toUpperCase()
  if (normalized === 'OPEN') {
    return { value: 'OPEN', label: '待处理', className: 'open' }
  }
  if (normalized === 'IN_PROGRESS') {
    return { value: 'IN_PROGRESS', label: '处理中', className: 'progress' }
  }
  if (normalized === 'CLOSED') {
    return { value: 'CLOSED', label: '已关闭', className: 'closed' }
  }
  return { value: normalized || 'UNKNOWN', label: normalized || '未知状态', className: 'unknown' }
}

function normalizeTicketPreview(content = '') {
  const raw = String(content || '').trim()
  if (!raw) {
    return ''
  }

  // 后端 toListItem 可能截断 60 字符，导致前缀不完整，需兼容处理
  const contactPrefix = /^(联系方式[：:]?.*?(?:\n\s*\n|$))/s
  const cleaned = raw.replace(contactPrefix, '').trim()
  // 如果清理后为空（例如整段只有联系方式），回退显示原始内容
  return cleaned || raw
}

function normalizeTicketItem(item = {}) {
  const statusMeta = resolveTicketStatusMeta(item.status)
  const createdAt = item.createdAt || item.createTime || ''
  const summary = normalizeTicketPreview(item.summary || item.questionText || item.content || '')
  const messages = Array.isArray(item.messages) ? item.messages : []

  return {
    id: item.id,
    status: statusMeta.value,
    statusText: statusMeta.label,
    statusClass: statusMeta.className,
    content: summary,
    questionText: item.questionText || summary,
    createTime: createdAt,
    createdAt,
    replyCount: item.replyCount != null ? Number(item.replyCount) : messages.length,
    matchedFaqId: item.matchedFaqId || null,
    messages
  }
}

function normalizeTicketPageData(data = {}) {
  const content = Array.isArray(data.content) ? data.content.map(normalizeTicketItem) : []
  return {
    content,
    totalElements: Number(data.totalElements || content.length || 0),
    totalPages: Number(data.totalPages || 0),
    page: Number(data.page || 0),
    size: Number(data.size || content.length || 0)
  }
}

function buildTicketRefreshToken() {
  return `${Date.now()}`
}

function markTicketRefresh(ticket = null) {
  const refreshToken = buildTicketRefreshToken()
  try {
    wx.setStorageSync(QA_TICKET_REFRESH_KEY, refreshToken)
    if (ticket) {
      wx.setStorageSync(QA_TICKET_LATEST_KEY, ticket)
    }
  } catch (e) {
    console.error('写入工单刷新标记失败', e)
  }
  return refreshToken
}

function readLatestTicket() {
  try {
    return wx.getStorageSync(QA_TICKET_LATEST_KEY) || null
  } catch (e) {
    return null
  }
}

function clearLatestTicket() {
  try {
    wx.removeStorageSync(QA_TICKET_LATEST_KEY)
  } catch (e) {
    console.error('清理最新工单缓存失败', e)
  }
}

function readTicketRefreshToken() {
  try {
    return String(wx.getStorageSync(QA_TICKET_REFRESH_KEY) || '')
  } catch (e) {
    return ''
  }
}

function getTicketErrorMessage(error = {}, fallback = '工单接口请求失败，请稍后重试') {
  const rawMessage = String(
    error?.message || error?.data?.message || error?.msg || error?.errMsg || ''
  ).trim()

  if (!rawMessage) return fallback
  if (/timeout|超时/i.test(rawMessage)) return '请求超时，请稍后重试'
  if (/401|登录|未认证|token/i.test(rawMessage)) return '登录状态已失效，请重新登录'
  if (/404|不存在/i.test(rawMessage)) return '接口不存在，请联系管理员检查配置'
  if (/500|内部错误|server/i.test(rawMessage)) return '服务暂时不可用，请稍后再试'
  if (/network|网络/i.test(rawMessage)) return '网络异常，请检查网络后重试'
  return rawMessage
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
  }, { showLoading: false }).then((res) => {
    const normalized = normalizeTicketItem(res?.data || {})
    markTicketRefresh(normalized)
    return {
      ...res,
      data: normalized
    }
  })
}

/**
 * 获取我的工单列表
 */
exports.getTickets = (params) => {
  const page = params?.page != null ? Number(params.page) : 0
  const size = params?.size != null ? Number(params.size) : 10
  const query = { page, size }
  const status = params?.status
  if (status != null && String(status).trim() !== '') {
    query.status = String(status).trim().toUpperCase()
  }
  return get('/student/qa-tickets/page', query, { showLoading: false }).then((res) => ({
    ...res,
    data: normalizeTicketPageData(res?.data || {})
  }))
}

/**
 * 获取工单详情
 * @param {string} id - 工单ID
 */
exports.getTicketDetail = (id) => {
  return get(`/student/qa-tickets/${id}`, {}, { showLoading: false }).then((res) => ({
    ...res,
    data: normalizeTicketItem(res?.data || {})
  }))
}

exports.resolveTicketStatusMeta = resolveTicketStatusMeta
exports.normalizeTicketItem = normalizeTicketItem
exports.normalizeTicketPageData = normalizeTicketPageData
exports.markTicketRefresh = markTicketRefresh
exports.readLatestTicket = readLatestTicket
exports.clearLatestTicket = clearLatestTicket
exports.readTicketRefreshToken = readTicketRefreshToken
exports.getTicketErrorMessage = getTicketErrorMessage
exports.QA_TICKET_REFRESH_KEY = QA_TICKET_REFRESH_KEY
exports.QA_TICKET_LATEST_KEY = QA_TICKET_LATEST_KEY
