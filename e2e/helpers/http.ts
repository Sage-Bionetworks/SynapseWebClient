export const DEFAULT_DELETE_HEADERS = {
  Accept: 'application/json; charset=UTF-8',
  'User-Agent': 'SynapseWebClient',
}
export const DEFAULT_POST_HEADERS = {
  ...DEFAULT_DELETE_HEADERS,
  'Content-Type': 'application/json; charset=UTF-8',
}

async function doFetch<TResponse>(url: RequestInfo, options: RequestInit) {
  const response = await fetch(url, options)
  const contentType = response.headers.get('Content-Type')
  const responseBody = await response.text()
  let responseObject: TResponse | string = responseBody
  try {
    // try to parse it as json
    if (contentType && contentType.includes('application/json')) {
      responseObject = JSON.parse(responseBody) as TResponse
    }
  } catch (error) {
    console.warn('Failed to parse response as JSON', responseBody)
  }

  if (response.ok) {
    return responseObject
  } else if (
    responseObject !== null &&
    typeof responseObject === 'object' &&
    'reason' in responseObject &&
    typeof responseObject.reason === 'string'
  ) {
    throw new Error(response.status + responseObject.reason + url.toString())
  } else {
    throw new Error(response.status + url.toString())
  }
}

export async function doPost<T>(
  endpoint: string,
  uri: string,
  requestContent: string,
  requestHeaders: { [key: string]: string },
) {
  const url: RequestInfo = `${endpoint}${uri}`
  const options: RequestInit = {
    body: requestContent,
    headers: requestHeaders,
    method: 'POST',
    mode: 'cors',
  }
  return await doFetch(url, options)
}

export async function doDelete<T>(
  endpoint: string,
  uri: string,
  requestHeaders: { [key: string]: string },
) {
  const url: RequestInfo = `${endpoint}${uri}`
  const options: RequestInit = {
    body: null,
    headers: requestHeaders,
    method: 'DELETE',
    mode: 'cors',
  }
  return await doFetch(url, options)
}

export function getEndpoint() {
  return 'https://repo-dev.dev.sagebase.org'
}
