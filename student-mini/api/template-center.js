const TEMPLATE_CENTER_ITEMS = [
  {
    id: '1',
    templateCode: 'CERT_001',
    templateName: '在读证明模板',
    certificateType: '在读证明',
    description: '用于学生在读状态证明',
    templateContent: '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业{{gradeYear}}级学生，当前学籍状态为在读。',
    templateFilePath: '/templates/cert/study-certificate.pdf',
    outputFormat: 'PDF',
    department: '学生事务中心',
    fileSize: '128 KB',
    updatedAt: '2026-04-18 10:00',
    mockFileName: 'study-certificate-template.txt',
    mockFileContent: [
      '在读证明模板',
      '',
      '用途：学生在读状态证明',
      '适用部门：学生事务中心',
      '',
      '模板正文：',
      '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业{{gradeYear}}级学生，当前学籍状态为在读。'
    ].join('\n')
  },
  {
    id: '2',
    templateCode: 'CERT_002',
    templateName: '党员身份证明模板',
    certificateType: '党员身份证明',
    description: '用于党员身份证明',
    templateContent: '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业学生，该生于{{joinDate}}加入中国共产党，当前党组织关系在我院。',
    templateFilePath: '/templates/cert/party-member-certificate.pdf',
    outputFormat: 'PDF',
    department: '党委组织部',
    fileSize: '136 KB',
    updatedAt: '2026-04-18 10:10',
    mockFileName: 'party-member-certificate-template.txt',
    mockFileContent: [
      '党员身份证明模板',
      '',
      '用途：党员身份证明',
      '适用部门：党委组织部',
      '',
      '模板正文：',
      '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业学生，该生于{{joinDate}}加入中国共产党，当前党组织关系在我院。'
    ].join('\n')
  },
  {
    id: '3',
    templateCode: 'CERT_003',
    templateName: '困难认定证明模板',
    certificateType: '困难认定证明',
    description: '用于学生困难认定证明',
    templateContent: '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业{{gradeYear}}级学生，经学院认定，该生家庭经济困难等级为{{difficultyLevel}}。',
    templateFilePath: '/templates/cert/difficulty-certificate.pdf',
    outputFormat: 'PDF',
    department: '学生资助中心',
    fileSize: '132 KB',
    updatedAt: '2026-04-18 10:20',
    mockFileName: 'difficulty-certificate-template.txt',
    mockFileContent: [
      '困难认定证明模板',
      '',
      '用途：学生困难认定证明',
      '适用部门：学生资助中心',
      '',
      '模板正文：',
      '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业{{gradeYear}}级学生，经学院认定，该生家庭经济困难等级为{{difficultyLevel}}。'
    ].join('\n')
  },
  {
    id: '4',
    templateCode: 'CERT_004',
    templateName: '成绩单模板',
    certificateType: '成绩单',
    description: '用于学生成绩证明',
    templateContent: '兹证明{{studentName}}同学（学号：{{studentNo}}）在我院{{majorName}}专业学习期间，各科成绩如下：{{grades}}',
    templateFilePath: '/templates/cert/transcript.pdf',
    outputFormat: 'PDF',
    department: '教务处',
    fileSize: '156 KB',
    updatedAt: '2026-04-18 10:30',
    mockFileName: 'transcript-template.txt',
    mockFileContent: [
      '成绩单模板',
      '',
      '用途：学生成绩证明',
      '适用部门：教务处',
      '',
      '模板正文：',
      '兹证明{{studentName}}同学（学号：{{studentNo}}）在我院{{majorName}}专业学习期间，各科成绩如下：{{grades}}'
    ].join('\n')
  },
  {
    id: '5',
    templateCode: 'CERT_005',
    templateName: '实习证明模板',
    certificateType: '实习证明',
    description: '用于学生实习证明',
    templateContent: '兹证明{{studentName}}同学（学号：{{studentNo}}）于{{startDate}}至{{endDate}}在{{companyName}}实习，实习岗位为{{position}}。',
    templateFilePath: '/templates/cert/internship-certificate.pdf',
    outputFormat: 'PDF',
    department: '就业指导中心',
    fileSize: '144 KB',
    updatedAt: '2026-04-18 10:40',
    mockFileName: 'internship-certificate-template.txt',
    mockFileContent: [
      '实习证明模板',
      '',
      '用途：学生实习证明',
      '适用部门：就业指导中心',
      '',
      '模板正文：',
      '兹证明{{studentName}}同学（学号：{{studentNo}}）于{{startDate}}至{{endDate}}在{{companyName}}实习，实习岗位为{{position}}。'
    ].join('\n')
  }
]

function getTemplateCenterItems() {
  return TEMPLATE_CENTER_ITEMS.map((item) => ({ ...item }))
}

function findTemplateCenterItemById(id = '') {
  return TEMPLATE_CENTER_ITEMS.find((item) => item.id === String(id)) || null
}

module.exports = {
  getTemplateCenterItems,
  findTemplateCenterItemById
}
