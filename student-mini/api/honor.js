const { get } = require('./request')

const RECIPIENT_TYPE_ALIASES = {
  PERSONAL: 'PERSONAL',
  COLLECTIVE: 'COLLECTIVE',
  personal: 'PERSONAL',
  collective: 'COLLECTIVE',
  个人: 'PERSONAL',
  集体: 'COLLECTIVE'
}

function normalizeRecipientType(value) {
  const rawValue = typeof value === 'string' ? value.trim() : ''
  if (!rawValue || rawValue === '全部' || rawValue.toLowerCase() === 'all') {
    return undefined
  }
  return RECIPIENT_TYPE_ALIASES[rawValue] || undefined
}

const listHonors = (params = {}) => {
  const requestParams = {}

  if (params.page !== undefined) requestParams.page = params.page
  if (params.size !== undefined) requestParams.size = params.size

  const keyword = typeof params.keyword === 'string' ? params.keyword.trim() : ''
  if (keyword) requestParams.keyword = keyword

  const awardYear = Number(params.awardYear)
  if (!Number.isNaN(awardYear) && awardYear > 0) {
    requestParams.awardYear = awardYear
  }

  const honorCategory = typeof params.honorCategory === 'string' ? params.honorCategory.trim() : ''
  if (honorCategory) requestParams.honorCategory = honorCategory

  const recipientType = normalizeRecipientType(params.recipientType)
  if (recipientType) {
    requestParams.recipientType = recipientType
  }

  return get('/student/honors/page', requestParams, { showLoading: false })
}

const getHonorDetail = (id) => {
  return get(`/student/honors/${id}`)
}

const listHonorRecipients = (id) => {
  return get(`/student/honors/${id}/recipients`, {}, { showLoading: false })
}

const getHonorRecipient = (id) => {
  return get(`/student/honors/recipients/${id}`)
}

module.exports = {
  listHonors,
  getHonorDetail,
  listHonorRecipients,
  getHonorRecipient,
  normalizeRecipientType
}
