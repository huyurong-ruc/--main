const MODULE_SEARCH_INDEX = [
  {
    id: 'service-apply',
    type: 'service',
    name: '证明申请',
    aliases: ['盖章申请', '证明', '在读证明', '成绩单', '教师资格证申请', '教师资格', '盖章', '申请进度'],
    keywords: ['办证明', '开证明', '电子证明', '成绩单盖章', '认定材料', '提交申请', '审核进度'],
    description: '在线办理在读证明、成绩单、教师资格证等各类证明申请，并查看审核进度。',
    guide: '选择证明类型，填写业务字段并上传材料后提交；可在列表页查看待处理、待审核和已通过状态。',
    entryLabel: '进入证明申请',
    category: '办事服务',
    route: '/sub-pages/apply/list',
    routeType: 'navigateTo'
  },
  {
    id: 'service-party',
    type: 'service',
    name: '党团流程',
    aliases: ['入党流程', '入团流程', '党团', '思想汇报', '积极分子', '发展对象', '转正'],
    keywords: ['党课培训', '政治审查', '支部大会', '入团申请', '入党申请', '流程进度'],
    description: '查看入团、入党流程节点、预计时间和当前进度状态。',
    guide: '入团流程全部完成后才开放入党流程；学生端仅可查看，状态由管理端教师审核更新。',
    entryLabel: '查看党团流程',
    category: '办事服务',
    route: '/sub-pages/party/index',
    routeType: 'navigateTo'
  },
  {
    id: 'service-academic',
    type: 'service',
    name: '学业分析',
    aliases: ['成绩分析', '培养方案', '成绩单上传', '学业报告', '课程分析', '毕业要求'],
    keywords: ['上传成绩单', '对比报告', '培养方案对比', '成绩诊断', '课程完成情况'],
    description: '上传成绩单并生成学业分析报告，查看培养方案对比与课程完成情况。',
    guide: '先上传当前或历史成绩单，再进入报告页查看课程达成、风险提醒和建议。',
    entryLabel: '进入学业分析',
    category: '功能模块',
    route: '/sub-pages/academic/index',
    routeType: 'navigateTo'
  },
  {
    id: 'service-policy',
    type: 'service',
    name: '政策库',
    aliases: ['政策查询', '规章制度', '办事指南', '知识库', '政策通知'],
    keywords: ['学生管理条例', '奖助政策', '管理办法', '制度文件', '政策文件'],
    description: '集中查询学生事务相关政策文件、制度办法和办事指南。',
    guide: '支持按政策名称、部门和时间检索，并可进入详情页查看完整政策内容。',
    entryLabel: '进入政策库',
    category: '功能模块',
    route: '/sub-pages/policy/list',
    routeType: 'navigateTo'
  },
  {
    id: 'service-template',
    type: 'service',
    name: '模板下载',
    aliases: ['表格模板', '申请表', '模板', '文档下载', '表单模板'],
    keywords: ['活动预算表', '学生证补办申请表', '下载模板', '模板文件', '文档模板'],
    description: '下载常用办事模板、申请表和业务文档，支持按模板类型快速查看。',
    guide: '进入模板页后可查看模板说明、更新时间和下载入口。',
    entryLabel: '进入模板下载',
    category: '功能模块',
    route: '/sub-pages/policy/template',
    routeType: 'navigateTo'
  },
  {
    id: 'service-notice',
    type: 'service',
    name: '通知聚合',
    aliases: ['消息中心', '通知中心', '站内通知', '消息', '待办通知', '反馈通知'],
    keywords: ['通知详情', '个性化通知', '系统消息', '站内消息'],
    description: '查看待办通知、反馈通知和个性化消息，支持进入通知详情页。',
    guide: '重要通知会按分类聚合展示，点击后可查看详情并跳转至相关业务页面。',
    entryLabel: '进入通知聚合',
    category: '功能模块',
    route: '/pages/message/index',
    routeType: 'switchTab'
  },
  {
    id: 'service-faq',
    type: 'service',
    name: '常见问题',
    aliases: ['FAQ', '问题反馈', '办事问答', '问题咨询', '问答'],
    keywords: ['常见问题列表', '提交问题', '工单', '问题历史'],
    description: '查看常见办事问题、问答说明，也可提交咨询或反馈工单。',
    guide: '可先检索常见问题；如无结果，再进入提问或反馈页面补充问题详情。',
    entryLabel: '进入常见问题',
    category: '功能模块',
    route: '/sub-pages/faq/list',
    routeType: 'navigateTo'
  },
  {
    id: 'service-archive',
    type: 'service',
    name: '我的档案',
    aliases: ['个人档案', '成长档案', '个人信息', '档案模块', '档案管理'],
    keywords: ['档案编辑', '成长记录', '综合信息', '个人资料'],
    description: '查看个人成长档案、资料信息和各模块归档内容。',
    guide: '进入档案页后可查看各主题模块内容，并在管理页维护个人成长记录。',
    entryLabel: '进入我的档案',
    category: '功能模块',
    route: '/sub-pages/growth/archive',
    routeType: 'navigateTo'
  },
  {
    id: 'service-feedback',
    type: 'service',
    name: '意见反馈',
    aliases: ['反馈', '问题反馈', '工单反馈', '历史反馈'],
    keywords: ['提交反馈', '反馈历史', '问题建议', '意见建议'],
    description: '提交意见反馈并查看历史记录，适用于功能问题和使用建议反馈。',
    guide: '可进入反馈表单提交问题，也可在历史记录中查看处理进度。',
    entryLabel: '进入意见反馈',
    category: '办事服务',
    route: '/sub-pages/feedback/form',
    routeType: 'navigateTo'
  },
  {
    id: 'service-settings',
    type: 'service',
    name: '设置中心',
    aliases: ['设置', '通知设置', '账号设置', '隐私设置'],
    keywords: ['消息开关', '缓存清理', '用户协议', '隐私政策'],
    description: '管理通知开关、缓存清理与协议设置等个人偏好配置。',
    guide: '进入设置页后可调整通知偏好、清理缓存并查看协议说明。',
    entryLabel: '进入设置中心',
    category: '功能模块',
    route: '/sub-pages/settings/index',
    routeType: 'navigateTo'
  }
]

function getModuleSearchIndex() {
  return MODULE_SEARCH_INDEX
}

module.exports = {
  MODULE_SEARCH_INDEX,
  getModuleSearchIndex
}
