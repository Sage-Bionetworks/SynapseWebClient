import { Page, expect } from '@playwright/test'
import { createHmac } from 'crypto'
import {
  DEFAULT_DELETE_HEADERS,
  DEFAULT_POST_HEADERS,
  doDelete,
  doPost,
} from './http'
import { getLocalStorage, setLocalStorage } from './localStorage'

const BASE64_ENCODING = 'base64'
const TEST_USER_URI = '/repo/v1/admin/user'
export const TEST_USER_LOCALSTORAGE_KEY = 'TEST_USER'

type LoginResponse = {
  accessToken: string // A token that authorizes subsequent requests
  acceptsTermsOfUse: boolean // BOOLEAN	Does the user accept the terms of use?
  authenticationReceipt: string // STRING	A valid receipt allows the user to skip extra security checks.
}
export type TestUser = {
  username: string
  email: string
  password: string
  tou: boolean // true if user agrees to Synapse terms of use
  validatedUser: boolean // true if a verification approved state should be added for the user
}

export function getAdminCredentials() {
  const adminUserName = process.env.DEV_ADMIN_USERNAME
  const adminApiKey = process.env.DEV_ADMIN_API_KEY
  expect(adminUserName).not.toBeUndefined()
  expect(adminApiKey).not.toBeUndefined()
  return { adminUserName, adminApiKey }
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

function addDigitalSignature(
  headers: { [key: string]: string },
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
    ...headers,
    userId: username,
    signatureTimestamp: timestamp,
    signature: signature,
  }
}

function getUserIdFromJwt(token: string) {
  const payload = JSON.parse(
    Buffer.from(token.split('.')[1], BASE64_ENCODING).toString(),
  )
  return payload.sub
}

export async function createTestUser(
  endpoint: string,
  adminUserName: string,
  adminApiKey: string,
  testUser: TestUser,
) {
  const content = JSON.stringify(testUser)
  const headers = addDigitalSignature(
    DEFAULT_POST_HEADERS,
    TEST_USER_URI,
    adminUserName!,
    adminApiKey!,
  )
  const responseObject = (await doPost(
    endpoint,
    TEST_USER_URI,
    content,
    headers,
  )) as LoginResponse
  return getUserIdFromJwt(responseObject.accessToken)
}

export async function deleteTestUser(
  endpoint: string,
  adminUserName: string,
  adminApiKey: string,
  testUserId: string,
) {
  const uri = `${TEST_USER_URI}/${testUserId}`
  const headers = addDigitalSignature(
    DEFAULT_DELETE_HEADERS,
    uri,
    adminUserName!,
    adminApiKey!,
  )
  await doDelete(endpoint, uri, headers)
  return testUserId
}

export async function loginTestUser(
  page: Page,
  testUserName: string,
  testUserPassword: string,
) {
  // Perform authentication steps
  await page.goto('/')
  await page.getByRole('link', { name: 'Log in to Synapse' }).first().click()
  await page.getByRole('button', { name: 'Sign in with your email' }).click()
  await page.getByLabel('Username or Email Address').fill(testUserName)
  await page.getByLabel('Password').fill(testUserPassword)
  await page.getByRole('button', { name: 'Sign in' }).click()

  // Wait until the page reaches a state where all cookies are set
  await expect(
    page.getByRole('heading', { name: 'Your Projects' }),
  ).toBeVisible()
}

async function navigateToHomepageIfPageHasNotBeenLoaded(page: Page) {
  if (page.url() === 'about:blank') {
    // Navigate to homepage, so that localStorage is available
    await page.goto('/')
  }
}

export async function setTestUserId(
  page: Page,
  localStorageKey: string,
  testUserId: string,
) {
  await setLocalStorage(page, localStorageKey, testUserId)
}

export async function getTestUserId(page: Page, localStorageKey: string) {
  await navigateToHomepageIfPageHasNotBeenLoaded(page)
  const userId = await getLocalStorage(page, localStorageKey)
  expect(userId).not.toBeNull()
  return userId
}
