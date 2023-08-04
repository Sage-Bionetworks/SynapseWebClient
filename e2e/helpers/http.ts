import { createHmac } from 'crypto'
import { fetchWithExponentialTimeout } from './srcFetch'

export const BASE64_ENCODING = 'base64'
export const DEFAULT_GETDELETE_HEADERS = {
  Accept: '*/*',
  'User-Agent': 'SynapseWebClient',
}
export const DEFAULT_POST_HEADERS = {
  ...DEFAULT_GETDELETE_HEADERS,
  Accept: 'application/json; charset=UTF-8',
  'Content-Type': 'application/json; charset=UTF-8',
}

function generateDigitalSignature(
  data: string,
  base64EncodedSecretKey: string,
) {
  const hash = createHmac(
    'sha1',
    Buffer.from(base64EncodedSecretKey, BASE64_ENCODING),
  )
    .update(data)
    .digest(BASE64_ENCODING)
  return hash
}

function getDigitalSignature(
  uri: string,
  username: string,
  base64EncodedSecretKey: string,
) {
  const timestamp = new Date().toISOString()
  const signature = generateDigitalSignature(
    `${username}${uri}${timestamp}`,
    base64EncodedSecretKey,
  )
  return {
    userId: username,
    signatureTimestamp: timestamp,
    signature: signature,
  }
}

export function getUserIdFromJwt(token: string) {
  const payload = JSON.parse(
    Buffer.from(token.split('.')[1], BASE64_ENCODING).toString(),
  )
  return payload.sub
}

function updateHeaders(
  headers: { [key: string]: string },
  uri: string,
  accessToken?: string,
  userName?: string,
  apiKey?: string,
) {
  return {
    ...headers,
    ...(accessToken && {
      'Access-Control-Request-Headers': 'authorization',
      Authorization: `Bearer ${accessToken}`,
    }),
    ...(apiKey && userName && getDigitalSignature(uri, userName, apiKey)),
  }
}

export async function doPost<T>(
  endpoint: string,
  uri: string,
  requestContent: string,
  accessToken?: string,
  userName?: string,
  apiKey?: string,
) {
  const url: RequestInfo = `${endpoint}${uri}`
  const options: RequestInit = {
    body: requestContent,
    headers: updateHeaders(
      DEFAULT_POST_HEADERS,
      uri,
      accessToken,
      userName,
      apiKey,
    ),
    method: 'POST',
    mode: 'cors',
  }
  return await fetchWithExponentialTimeout(url, options)
}

export async function doGet<T>(
  endpoint: string,
  uri: string,
  accessToken?: string,
  userName?: string,
  apiKey?: string,
) {
  const url: RequestInfo = `${endpoint}${uri}`
  const options: RequestInit = {
    body: null,
    headers: updateHeaders(
      DEFAULT_GETDELETE_HEADERS,
      uri,
      accessToken,
      userName,
      apiKey,
    ),
    method: 'GET',
    mode: 'cors',
  }
  return await fetchWithExponentialTimeout(url, options)
}

export async function doDelete<T>(
  endpoint: string,
  uri: string,
  accessToken?: string,
  userName?: string,
  apiKey?: string,
) {
  const url: RequestInfo = `${endpoint}${uri}`
  const options: RequestInit = {
    body: null,
    headers: updateHeaders(
      DEFAULT_GETDELETE_HEADERS,
      uri,
      accessToken,
      userName,
      apiKey,
    ),
    method: 'DELETE',
    mode: 'cors',
  }
  return await fetchWithExponentialTimeout(url, options)
}

export function getEndpoint() {
  return 'https://repo-dev.dev.sagebase.org'
}
