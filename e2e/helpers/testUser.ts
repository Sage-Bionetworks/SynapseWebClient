import { Page, expect } from '@playwright/test'
import { BackendDestinationEnum, doDelete, doPost } from './http'
import { getLocalStorage } from './localStorage'
import { LoginResponse, TestUser } from './types'
import { waitForInitialPageLoad } from './utils'
import { deleteVerificationSubmissionIfExists } from './verification'

const BASE64_ENCODING = 'base64'
const TEST_USER_URI = '/repo/v1/admin/user'

export function getAdminPAT() {
  const adminPAT = process.env.ADMIN_PAT!
  expect(adminPAT).not.toBeUndefined()
  return adminPAT
}

function getUserIdFromJwt(token: string) {
  const payload = JSON.parse(
    Buffer.from(token.split('.')[1], BASE64_ENCODING).toString(),
  )
  return payload.sub
}

export async function createTestUser(
  testUser: TestUser,
  accessToken: string,
  page: Page,
) {
  const responseObject = (await doPost(
    page,
    TEST_USER_URI,
    testUser,
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )) as LoginResponse
  return getUserIdFromJwt(responseObject.accessToken)
}

export async function deleteTestUser(
  testUserId: string,
  accessToken: string,
  page: Page,
) {
  await doDelete(
    page,
    `${TEST_USER_URI}/${testUserId}`,
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )
  return testUserId
}

export async function cleanupTestUser(testUserId: string, userPage: Page) {
  await deleteVerificationSubmissionIfExists(
    testUserId,
    getAdminPAT(),
    userPage,
  )
  const result = await deleteTestUser(testUserId, getAdminPAT(), userPage)
  expect(result).toEqual(testUserId)
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

  const usernameInput = page.getByLabel('Username or Email Address')
  await usernameInput.fill(testUserName)
  await expect(usernameInput).toHaveValue(testUserName)

  const passwordInput = page.getByLabel('Password')
  await expect(passwordInput).toBeEmpty()
  await passwordInput.fill(testUserPassword)
  await expect(passwordInput).not.toBeEmpty()

  await page.getByRole('button', { name: 'Sign in' }).click()

  // Ensure that correct username/password were received
  const loadingButton = page.getByRole('button', { name: 'Logging you in' })
  await expect(loadingButton).toBeVisible()
  await expect(loadingButton).not.toBeVisible()
  await expect(
    page.getByText('The provided username/password combination is incorrect'),
  ).not.toBeVisible()

  // Wait for redirect
  await expect(async () => {
    expect(page.url()).not.toContain('LoginPlace')
  }).toPass()

  // Wait until the page reaches a state where all cookies are set
  await expect(page.getByLabel('Search')).toBeVisible()
  await expect(page.getByLabel('Projects')).toBeVisible()
  await expect(page.getByLabel('Your Account')).toBeVisible({
    timeout: 30 * 1000,
  })
}

export async function goToDashboard(page: Page) {
  await page.goto('/')
  await waitForInitialPageLoad(page)
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
  return id!
}

export async function acceptSiteCookies(page: Page) {
  await page.getByRole('button', { name: 'Accept and continue' }).click()
}
