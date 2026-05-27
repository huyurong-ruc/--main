// api/apply.js
const { get, post, put } = require('./request')

/**
 * 获取申请列表（电子证明申请记录）
 * 后端: GET /api/v1/student/certificates/requests
 * 状态约定:
 * - ACTION_REQUIRED: 学生仍需完成前置节点，前端展示为“待处理”
 * - IN_REVIEW: 已进入老师审核队列，前端展示为“待审核”
 * @param {Object} params - { page, pageSize }
 */
exports.getApplyList = (params) => get('/student/certificates/requests', params)

/**
 * 获取申请详情
 * 后端: GET /api/v1/certificates/requests/{requestId}/preview
 * @param {string} id - 申请ID
 */
exports.getApplyDetail = (id) => get(`/certificates/requests/${id}/preview`)

/**
 * 提交申请（电子证明申请）
 * 后端: POST /api/v1/certificates/requests
 * @param {Object} data - 申请数据
 */
exports.submitApply = (data) => post('/certificates/requests', data)

/**
 * 撤回申请
 * 后端: POST /api/v1/certificates/requests/{requestId}/action
 * @param {string} id - 申请ID
 */
exports.cancelApply = (id) => post(`/certificates/requests/${id}/action`, { action: 'withdraw' })

/**
 * 获取申请类型列表
 * 后端: GET /api/v1/knowledge/templates
 */
exports.getApplyTypes = () => get('/knowledge/templates')
