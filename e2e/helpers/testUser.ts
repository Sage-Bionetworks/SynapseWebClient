import { Page, expect } from '@playwright/test'
import { doDelete, doPost, getUserIdFromJwt } from './http'
import { getLocalStorage } from './localStorage'
import { LoginResponse, TestUser } from './types'

const TEST_USER_URI = '/repo/v1/admin/user'
export const USER_NAME_LOCALSTORAGE_KEY = 'USER_NAME'

export function getAdminUserCredentials() {
  const adminUserName = process.env.ADMIN_USERNAME
  const adminUserPassword = process.env.ADMIN_PASSWORD
  expect(adminUserName).not.toBeUndefined()
  expect(adminUserPassword).not.toBeUndefined()
  return { adminUserName, adminUserPassword }
}

export function getAdminPAT() {
  const adminPAT = process.env.ADMIN_PAT!
  expect(adminPAT).not.toBeUndefined()
  return adminPAT
}

export async function createTestUser(
  endpoint: string,
  testUser: TestUser,
  accessToken?: string,
  adminUserName?: string,
  adminApiKey?: string,
) {
  const content = JSON.stringify(testUser)
  const responseObject = (await doPost(
    endpoint,
    TEST_USER_URI,
    content,
    accessToken,
    adminUserName,
    adminApiKey,
  )) as LoginResponse
  return getUserIdFromJwt(responseObject.accessToken)
}

export async function deleteTestUser(
  endpoint: string,
  testUserId: string,
  accessToken?: string,
  adminUserName?: string,
  adminApiKey?: string,
) {
  const uri = `${TEST_USER_URI}/${testUserId}`
  await doDelete(endpoint, uri, accessToken, adminUserName, adminApiKey)
  return testUserId
}

// Use after initially navigating to baseURL
// ...to give SWC time to compile, if necessary
export async function waitForInitialPageLoad(page: Page) {
  await expect(page.getByRole('heading', { name: 'Loading…' })).not.toBeVisible(
    { timeout: 2 * 60 * 1000 }, // ...wait 2 minutes if necessary
  )
}

export async function loginTestUser(
  page: Page,
  testUserName: string,
  testUserPassword: string,
) {
  // Perform authentication steps
  await page.goto('/')
  await waitForInitialPageLoad(page)

  await page.getByRole('link', { name: 'Log in to Synapse' }).first().click()
  await page.getByRole('button', { name: 'Sign in with your email' }).click()
  await page.getByLabel('Username or Email Address').fill(testUserName)
  await page.getByLabel('Password').fill(testUserPassword)
  await page.getByRole('button', { name: 'Sign in' }).click()

  // Wait until the page reaches a state where all cookies are set
  await expect(page.getByLabel('Search')).toBeVisible()
  await expect(page.getByLabel('Projects')).toBeVisible()
  await expect(page.getByLabel('Your Account')).toBeVisible({
    timeout: 30 * 1000,
  })
}

export async function goToDashboard(page: Page) {
  await page.goto('/')
  await page.getByRole('link', { name: 'View Your Dashboard' }).first().click()

  // wait for page to load
  await expect(page.getByLabel('Search')).toBeVisible()
  await expect(page.getByLabel('Projects')).toBeVisible()
  await expect(page.getByLabel('Your Account')).toBeVisible({
    timeout: 30 * 1000,
  })
}

export async function logoutTestUser(page: Page) {
  await page.getByLabel('Your Account').click()
  await page.getByText('Sign Out').click()
  await expect(page.getByLabel('Sign in')).toBeVisible()
}

export async function getAccessTokenFromCookie(page: Page) {
  const cookie = (await page.context().cookies()).find(
    cookie => cookie.name === 'org.sagebionetworks.security.user.login.token',
  )
  expect(cookie).not.toBeUndefined()
  return cookie!.value
}

export async function getUserIdFromLocalStorage(page: Page) {
  const id = await getLocalStorage(page, 'SESSION_MARKER')
  expect(id).not.toBeNull()
  return id
}
