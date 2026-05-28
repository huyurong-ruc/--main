const APPLY_TYPE_CONFIGS = [
  {
    key: 'read-cert',
    id: 'tpl-read-cert',
    title: '在读证明申请模板',
    applyTitle: '在读证明',
    description: '适用于奖学金、签证、实习等在读证明申请场景',
    fileSize: '168 KB',
    fileType: 'DOCX',
    updatedAt: '2026-04-12 10:00',
    department: '学生事务中心',
    fileUrl: 'https://example.com/templates/read-cert.docx',
    tips: [
      '在读证明默认由学生事务中心审核，适用于签证、奖学金、实习等场景。',
      '如需纸质盖章件，请在领取方式中选择“纸质盖章件”。'
    ],
    attachmentGuide: ['如申请方要求补充材料，可上传通知邮件或用章说明。'],
    formFields: [
      {
        key: 'useScenario',
        label: '使用场景',
        type: 'selector',
        required: true,
        options: ['签证办理', '奖学金申请', '实习入职', '出国交流', '其他'],
        placeholder: '请选择使用场景'
      },
      {
        key: 'recipientOrg',
        label: '接收单位',
        type: 'text',
        required: true,
        placeholder: '请输入接收证明的单位或机构名称'
      },
      {
        key: 'language',
        label: '证明语言',
        type: 'selector',
        required: true,
        options: ['中文', '英文', '中英双语'],
        placeholder: '请选择证明语言'
      },
      {
        key: 'deliveryMethod',
        label: '领取方式',
        type: 'selector',
        required: true,
        options: ['电子版即可', '纸质盖章件'],
        placeholder: '请选择领取方式'
      },
      {
        key: 'copies',
        label: '申请份数',
        type: 'number',
        required: true,
        placeholder: '请输入需要开具的份数'
      },
      {
        key: 'applicationNote',
        label: '补充说明',
        type: 'textarea',
        required: false,
        placeholder: '如有特殊盖章要求，请在此补充说明'
      }
    ],
    workflow: [
      { key: 'student-submit', name: '提交证明申请', owner: '学生端' },
      { key: 'teacher-review', name: '学院审核盖章', owner: '教师端' },
      { key: 'document-generate', name: '生成电子证明', owner: '系统' }
    ]
  },
  {
    key: 'transcript',
    id: 'tpl-transcript',
    title: '成绩单申请模板',
    applyTitle: '成绩单',
    description: '适用于升学、留学和资格审核等成绩单申请场景',
    fileSize: '224 KB',
    fileType: 'DOCX',
    updatedAt: '2026-04-10 09:30',
    department: '教务处',
    fileUrl: 'https://example.com/templates/transcript.docx',
    tips: [
      '成绩单申请默认进入教务处审核队列，适用于复试、留学和资格审核。',
      '若需绩点排名，请在申请信息中明确勾选。'
    ],
    attachmentGuide: ['如外部单位要求格式说明，可上传通知截图或盖章要求。'],
    formFields: [
      {
        key: 'useScenario',
        label: '使用场景',
        type: 'selector',
        required: true,
        options: ['研究生复试', '留学申请', '资格审核', '就业材料', '其他'],
        placeholder: '请选择使用场景'
      },
      {
        key: 'termRange',
        label: '成绩范围',
        type: 'selector',
        required: true,
        options: ['全部学期', '近一学年', '近两学年', '自定义说明'],
        placeholder: '请选择成绩范围'
      },
      {
        key: 'language',
        label: '成绩单语言',
        type: 'selector',
        required: true,
        options: ['中文', '英文', '中英双语'],
        placeholder: '请选择成绩单语言'
      },
      {
        key: 'needRanking',
        label: '是否附带排名',
        type: 'selector',
        required: true,
        options: ['无需排名', '附带专业排名', '附带年级排名'],
        placeholder: '请选择是否附带排名'
      },
      {
        key: 'recipientOrg',
        label: '接收单位',
        type: 'text',
        required: true,
        placeholder: '请输入接收单位或招生机构名称'
      },
      {
        key: 'applicationNote',
        label: '补充说明',
        type: 'textarea',
        required: false,
        placeholder: '如需说明特定学期、课程范围或格式要求，请在此补充'
      }
    ],
    workflow: [
      { key: 'student-submit', name: '提交成绩单申请', owner: '学生端' },
      { key: 'teacher-review', name: '教务审核盖章', owner: '教师端' },
      { key: 'document-generate', name: '生成成绩单文件', owner: '系统' }
    ]
  },
  {
    key: 'teacher-qualification',
    id: 'tpl-teacher-qualification',
    title: '教师资格证申请模板',
    applyTitle: '教师资格证申请',
    description: '适用于教师资格认定材料准备与信息填报',
    fileSize: '196 KB',
    fileType: 'DOCX',
    updatedAt: '2026-04-08 15:20',
    department: '学院教务办公室',
    fileUrl: 'https://example.com/templates/teacher-qualification.docx',
    tips: [
      '教师资格证申请需先完成学生端材料补充，再进入老师审核。',
      '建议至少上传身份证明与资格认定相关材料后再提交。'
    ],
    attachmentGuide: [
      '建议上传身份证明、普通话证书、成绩单或认定通知等相关材料。',
      '附件越完整，越能直接进入老师审核队列。'
    ],
    formFields: [
      {
        key: 'applyStage',
        label: '申请阶段',
        type: 'selector',
        required: true,
        options: ['认定报名', '材料补充', '盖章证明开具'],
        placeholder: '请选择申请阶段'
      },
      {
        key: 'subject',
        label: '申请学科',
        type: 'text',
        required: true,
        placeholder: '请输入教师资格认定学科，如语文、数学'
      },
      {
        key: 'applicationArea',
        label: '认定地区',
        type: 'text',
        required: true,
        placeholder: '请输入申请认定地区或机构'
      },
      {
        key: 'contactPhone',
        label: '联系电话',
        type: 'number',
        required: true,
        placeholder: '请输入11位手机号'
      },
      {
        key: 'materialReady',
        label: '材料准备情况',
        type: 'selector',
        required: true,
        options: ['核心材料已备齐', '仍需继续补充'],
        placeholder: '请选择材料准备情况'
      },
      {
        key: 'applicationNote',
        label: '补充说明',
        type: 'textarea',
        required: false,
        placeholder: '可说明缺失材料、预计补充时间或特殊盖章要求'
      }
    ],
    workflow: [
      { key: 'student-submit', name: '填写教师资格申请信息', owner: '学生端' },
      { key: 'student-material', name: '补充资格认定材料', owner: '学生端' },
      { key: 'teacher-review', name: '学院审核认定材料', owner: '教师端' },
      { key: 'document-generate', name: '生成盖章证明', owner: '系统' }
    ]
  }
]

function getApplyTypeOptions() {
  return APPLY_TYPE_CONFIGS
}

function findApplyTypeById(id = '') {
  return APPLY_TYPE_CONFIGS.find((item) => item.id === id) || null
}

function findApplyTypeByTitle(title = '') {
  const normalized = String(title || '').trim()
  return APPLY_TYPE_CONFIGS.find((item) =>
    item.title === normalized ||
    item.applyTitle === normalized
  ) || null
}

function findApplyTypeByKey(key = '') {
  return APPLY_TYPE_CONFIGS.find((item) => item.key === key) || null
}

function normalizeApplyTitleText(title = '') {
  const rawTitle = String(title || '').trim()
  if (!rawTitle) return ''
  if (rawTitle.includes('成绩单')) return '成绩单申请'
  return rawTitle
    .replace(/申请模板/g, '')
    .replace(/模板/g, '')
    .trim()
}

function getApplyDisplayTitle(input = '', typeKey = '') {
  const config = typeof input === 'object' && input
    ? input
    : findApplyTypeByKey(typeKey) || findApplyTypeByTitle(String(input || ''))

  if (config) {
    if (config.key === 'transcript') {
      return '成绩单申请'
    }
    return config.applyTitle || normalizeApplyTitleText(config.title || '')
  }

  return normalizeApplyTitleText(input)
}

function buildFormDefaults(config) {
  return (config?.formFields || []).reduce((acc, field) => {
    acc[field.key] = ''
    return acc
  }, {})
}

function getFieldLabel(config, key = '') {
  const target = (config?.formFields || []).find((item) => item.key === key)
  return target ? target.label : key
}

function buildFieldDisplays(config, fieldValues = {}) {
  return (config?.formFields || [])
    .map((field) => {
      const value = fieldValues[field.key]
      return {
        key: field.key,
        label: field.label,
        value: value == null ? '' : String(value)
      }
    })
    .filter((item) => item.value)
}

function validateApplyForm(config, formValues = {}) {
  const fields = config?.formFields || []
  for (const field of fields) {
    const raw = formValues[field.key]
    const value = raw == null ? '' : String(raw).trim()
    if (field.required && !value) {
      return { valid: false, message: `请填写${field.label}` }
    }

    if (field.key === 'copies' && value) {
      const copies = Number(value)
      if (!Number.isInteger(copies) || copies <= 0 || copies > 10) {
        return { valid: false, message: '申请份数需为1-10之间的整数' }
      }
    }

    if (field.key === 'contactPhone' && value && !/^1\d{10}$/.test(value)) {
      return { valid: false, message: '联系电话需为11位手机号' }
    }
  }

  return { valid: true, message: '' }
}

function resolveApplyInitialStatus(config, payload = {}) {
  const fieldValues = payload.fieldValues || {}
  const attachments = Array.isArray(payload.attachments) ? payload.attachments : []

  if (config?.key === 'teacher-qualification') {
    const ready = String(fieldValues.materialReady || '').includes('已备齐')
    return ready && attachments.length >= 2 ? 'IN_REVIEW' : 'ACTION_REQUIRED'
  }

  return 'IN_REVIEW'
}

function getApplyPendingSummary(config, statusCode = '', fieldValues = {}) {
  if (!config) {
    return {
      currentNodeName: '待处理',
      pendingActionText: '请查看申请详情'
    }
  }

  if (statusCode === 'ACTION_REQUIRED') {
    if (config.key === 'teacher-qualification') {
      return {
        currentNodeName: '待补充资格材料',
        pendingActionText: '请补充教师资格认定相关材料后再提交老师审核'
      }
    }

    return {
      currentNodeName: '待补充申请信息',
      pendingActionText: '请完善当前证明申请所需信息'
    }
  }

  if (statusCode === 'IN_REVIEW') {
    return {
      currentNodeName: '老师审核中',
      pendingActionText: `${config.department}正在审核并准备盖章`
    }
  }

  if (statusCode === 'APPROVED') {
    return {
      currentNodeName: '证明已生成',
      pendingActionText: '申请已通过，可前往详情页下载文件'
    }
  }

  if (statusCode === 'REJECTED') {
    return {
      currentNodeName: '申请已驳回',
      pendingActionText: '请根据老师意见修改后重新提交'
    }
  }

  return {
    currentNodeName: '申请已撤回',
    pendingActionText: '当前申请已终止'
  }
}

function buildWorkflowNodes(config, statusCode = '', item = {}) {
  const workflow = config?.workflow || []
  const submitTime = item.createdAt || ''

  if (config?.key === 'teacher-qualification') {
    if (statusCode === 'ACTION_REQUIRED') {
      return [
        {
          id: 'student-submit',
          name: workflow[0]?.name || '填写教师资格申请信息',
          owner: '学生端',
          status: 'completed',
          statusText: '已完成',
          meaning: '基础申请信息已提交，系统已生成本次教师资格证明申请。',
          time: submitTime
        },
        {
          id: 'student-material',
          name: workflow[1]?.name || '补充资格认定材料',
          owner: '学生端',
          status: 'current',
          statusText: '待处理',
          meaning: '当前仍需由学生补充核心认定材料，完成后申请才会进入老师审核队列。',
          time: submitTime
        },
        {
          id: 'teacher-review',
          name: workflow[2]?.name || '学院审核认定材料',
          owner: '教师端',
          status: 'pending',
          statusText: '待审核',
          meaning: '材料补齐后将进入学院审核老师的盖章审核队列。',
          time: ''
        },
        {
          id: 'document-generate',
          name: workflow[3]?.name || '生成盖章证明',
          owner: '系统',
          status: 'pending',
          statusText: '待生成',
          meaning: '审核通过后系统生成盖章证明文件。',
          time: ''
        }
      ]
    }
  }

  if (statusCode === 'IN_REVIEW') {
    return [
      {
        id: 'student-submit',
        name: workflow[0]?.name || '提交申请',
        owner: '学生端',
        status: 'completed',
        statusText: '已完成',
        meaning: '学生已完成当前证明类型所需字段填写与材料上传。',
        time: submitTime
      },
      ...(config?.key === 'teacher-qualification'
        ? [{
            id: 'student-material',
            name: workflow[1]?.name || '补充资格认定材料',
            owner: '学生端',
            status: 'completed',
            statusText: '已完成',
            meaning: '教师资格认定相关材料已补齐，申请已满足审核前置条件。',
            time: submitTime
          }]
        : []),
      {
        id: 'teacher-review',
        name: config?.key === 'teacher-qualification' ? (workflow[2]?.name || '学院审核认定材料') : (workflow[1]?.name || '学院审核盖章'),
        owner: '教师端',
        status: 'current',
        statusText: '待审核',
        meaning: `${config?.department || '学院相关部门'}正在核验材料并处理盖章申请。`,
        time: submitTime
      },
      {
        id: 'document-generate',
        name: config?.key === 'teacher-qualification' ? (workflow[3]?.name || '生成盖章证明') : (workflow[2]?.name || '生成电子证明'),
        owner: '系统',
        status: 'pending',
        statusText: '待生成',
        meaning: '审核通过后系统会生成最终的证明文件。',
        time: ''
      }
    ]
  }

  if (statusCode === 'APPROVED') {
    return workflow.map((node, index) => ({
      id: node.key,
      name: node.name,
      owner: node.owner,
      status: 'completed',
      statusText: index === workflow.length - 1 ? '已生成' : '已完成',
      meaning: index === workflow.length - 1 ? '系统已生成证明文件，可下载查看。' : '该节点已按业务规则完成。',
      time: submitTime
    }))
  }

  if (statusCode === 'REJECTED') {
    return [
      {
        id: 'student-submit',
        name: workflow[0]?.name || '提交申请',
        owner: '学生端',
        status: 'completed',
        statusText: '已完成',
        meaning: '学生已完成当前申请的提交。',
        time: submitTime
      },
      {
        id: 'teacher-review',
        name: config?.key === 'teacher-qualification' ? (workflow[2]?.name || '学院审核认定材料') : (workflow[1]?.name || '学院审核盖章'),
        owner: '教师端',
        status: 'rejected',
        statusText: '已驳回',
        meaning: '审核老师已退回申请，请按驳回意见修改材料后重新提交。',
        time: submitTime
      }
    ]
  }

  return [
    {
      id: 'withdrawn',
      name: '申请已撤回',
      owner: '学生端',
      status: 'rejected',
      statusText: '已撤回',
      meaning: '当前申请已由学生主动撤回，流程终止。',
      time: submitTime
    }
  ]
}

module.exports = {
  APPLY_TYPE_CONFIGS,
  getApplyTypeOptions,
  findApplyTypeById,
  findApplyTypeByTitle,
  findApplyTypeByKey,
  getApplyDisplayTitle,
  buildFormDefaults,
  buildFieldDisplays,
  getFieldLabel,
  validateApplyForm,
  resolveApplyInitialStatus,
  getApplyPendingSummary,
  buildWorkflowNodes
}
