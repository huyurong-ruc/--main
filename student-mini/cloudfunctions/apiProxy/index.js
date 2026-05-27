// 云函数入口文件
const cloud = require('wx-server-sdk')

cloud.init({
  // 这里会自动使用当前环境，也可以在 project.config.json 中指定
  env: cloud.DYNAMIC_CURRENT_ENV
})

/**
 * 通用API代理云函数
 * 将前端请求转发到原有后端服务器
 * 
 * 调用参数：
 * {
 *   path: '/auth/login',      // API路径（不含baseUrl）
 *   method: 'POST',           // HTTP方法
 *   data: { ... },            // 请求数据
 *   header: { ... }           // 额外请求头（可选）
 * }
 */
exports.main = async (event, context) => {
  const { path, method = 'GET', data = {}, header = {} } = event

  // ========== 配置区域：替换为你的真实后端地址 ==========
  // 注意：云函数内访问的外部域名需要在小程序后台配置为 request 合法域名
  // 并且该域名必须支持 HTTPS
  const BASE_URL = 'https://camping-penny-concert.ngrok-free.dev/api/v1'
  // ======================================================

  if (!path) {
    return {
      success: false,
      message: '缺少 path 参数',
      code: 400
    }
  }

  const url = `${BASE_URL}${path}`

  // 获取用户openid（如果需要透传给后端）
  const wxContext = cloud.getWXContext()

  try {
    const result = await new Promise((resolve, reject) => {
      const https = require('https')
      const http = require('http')
      const URL = require('url')

      const parsedUrl = URL.parse(url)
      const protocol = parsedUrl.protocol === 'https:' ? https : http

      const postData = JSON.stringify(data)

      const options = {
        hostname: parsedUrl.hostname,
        port: parsedUrl.port || (parsedUrl.protocol === 'https:' ? 443 : 80),
        path: parsedUrl.path,
        method: method.toUpperCase(),
        headers: {
          'Content-Type': 'application/json',
          'X-Cloud-Openid': wxContext.OPENID || '',
          'X-Cloud-Unionid': wxContext.UNIONID || '',
          ...header
        }
      }

      if (method.toUpperCase() !== 'GET' && data) {
        options.headers['Content-Length'] = Buffer.byteLength(postData)
      }

      const req = protocol.request(options, (res) => {
        let responseData = ''

        res.on('data', (chunk) => {
          responseData += chunk
        })

        res.on('end', () => {
          try {
            const parsedData = JSON.parse(responseData)
            resolve({
              success: true,
              statusCode: res.statusCode,
              data: parsedData
            })
          } catch (e) {
            resolve({
              success: true,
              statusCode: res.statusCode,
              data: responseData
            })
          }
        })
      })

      req.on('error', (error) => {
        console.error('[apiProxy] 请求失败:', error)
        reject({
          success: false,
          message: `代理请求失败: ${error.message}`,
          code: 500
        })
      })

      req.setTimeout(30000, () => {
        req.destroy()
        reject({
          success: false,
          message: '代理请求超时',
          code: 504
        })
      })

      if (method.toUpperCase() !== 'GET' && data) {
        req.write(postData)
      }
      req.end()
    })

    return result

  } catch (error) {
    console.error('[apiProxy] 异常:', error)
    return {
      success: false,
      message: error.message || '云函数内部错误',
      code: error.code || 500
    }
  }
}
