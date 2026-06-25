const { get } = require('./request')

/**
 * 拉取当前登录学生的真实党团流程状态
 * 后端: GET /api/v1/student/party-flows
 */
exports.getPartyFlowState = () => get('/student/party-flows')

