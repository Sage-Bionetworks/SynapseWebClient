import { Page, expect, test } from '@playwright/test'
import { LoginResponse } from '@sage-bionetworks/synapse-types'
import { defaultExpectTimeout } from '../../playwright.config'
import { testAuth } from '../fixtures/authenticatedUserPages'
import { entityUrlPathname } from './entities'
import { BackendDestinationEnum, doDelete, doPost } from './http'
import { getLocalStorage } from './localStorage'
import { JwtPayload, TestUser } from './types'
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
  ) as JwtPayload
  return payload.sub
}

export async function createTestUser(
  testUser: TestUser,
  accessToken: string,
  page: Page,
) {
  const responseObject = await doPost(
    page,
    TEST_USER_URI,
    testUser,
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )
  return getUserIdFromJwt((responseObject as LoginResponse).accessToken)
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

async function getSuccessfulLoginResponsePromise(page: Page) {
  return page.waitForResponse(
    response =>
      response.url().includes('/auth/v1/login2') && response.status() == 201,
    { timeout: defaultExpectTimeout * 3 }, // allow time for the response to return
  )
}

async function getAuthenticatedUserBundleResponsePromise(page: Page) {
  return page.waitForResponse(
    async response =>
      response.url().includes('/repo/v1/user/bundle?mask=127') &&
      response.status() == 200 &&
      !(await response.text()).includes('Anonymous'),
    { timeout: defaultExpectTimeout * 3 }, // allow time for the response to return
  )
}

export async function expectAuthenticatedNavDrawerLoaded(page: Page) {
  await test.step('authenticated nav drawer is loaded', async () => {
    await expect(page.getByLabel('Search')).toBeVisible()
    await expect(page.getByLabel('Projects', { exact: true })).toBeVisible()
    await expect(page.getByLabel('Your Account')).toBeVisible()
  })
}

export async function loginTestUser(
  page: Page,
  testUserName: string,
  testUserPassword: string,
) {
  // Accept cookies, so banner doesn't obscure buttons in other tests
  await acceptSiteCookies(page)

  // Login
  await page.getByRole('link', { name: 'Log in to Synapse' }).first().click()
  await page.getByRole('button', { name: 'Sign in with your email' }).click()

  const usernameInput = page.getByLabel('Username or Email Address')
  await usernameInput.fill(testUserName)
  await expect(usernameInput).toHaveValue(testUserName)

  const passwordInput = page.getByLabel('Password')
  await expect(passwordInput).toBeEmpty()
  await passwordInput.fill(testUserPassword)
  await expect(passwordInput).not.toBeEmpty()

  const successfulLoginResponsePromise = getSuccessfulLoginResponsePromise(page)
  const authenticatedUserBundleResponsePromise =
    getAuthenticatedUserBundleResponsePromise(page)
  await page.getByRole('button', { name: 'Sign in' }).click()

  // Ensure that correct username/password were received
  await expect(
    page.getByText('The provided username/password combination is incorrect'),
  ).not.toBeVisible()

  // Wait for redirect
  await successfulLoginResponsePromise
  await expect(() => {
    expect(page.url()).not.toContain('LoginPlace')
  }).toPass()

  // Wait until the page reaches a state where all cookies are set
  await authenticatedUserBundleResponsePromise
  await expectAuthenticatedNavDrawerLoaded(page)
}

export async function goToDashboard(page: Page) {
  await page.goto('/')
  const authenticatedUserBundleResponsePromise =
    getAuthenticatedUserBundleResponsePromise(page)

  // wait for page to load
  await waitForInitialPageLoad(page)
  await authenticatedUserBundleResponsePromise
  await expectAuthenticatedNavDrawerLoaded(page)
}

export async function reloadDashboardPage(page: Page) {
  await test.step('reload dashboard page', async () => {
    const authenticatedUserBundleResponsePromise =
      getAuthenticatedUserBundleResponsePromise(page)
    await page.reload()

    // wait for page to load
    await waitForInitialPageLoad(page)
    await authenticatedUserBundleResponsePromise
    await expectAuthenticatedNavDrawerLoaded(page)
  })
}

export async function goToDashboardPage(page: Page, urlPath: string) {
  const authenticatedUserBundleResponsePromise =
    getAuthenticatedUserBundleResponsePromise(page)
  await page.goto(urlPath)

  await waitForInitialPageLoad(page)
  await authenticatedUserBundleResponsePromise
  await expectAuthenticatedNavDrawerLoaded(page)
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
  await page.getByRole('button', { name: 'ALLOW ALL' }).click()
}

export const dismissAlert = async (page: Page, alertText: string) => {
  const alert = page.getByRole('alert').filter({ hasText: alertText })
  // allow extra time for alert to appear
  // ...to handle alerts triggered by potentially slow network requests, e.g. create file
  await expect(alert).toBeVisible({ timeout: defaultExpectTimeout * 3 })
  await alert.getByRole('button', { name: 'Close' }).click()
  await expect(alert).not.toBeVisible()
}

export const getDefaultDiscussionPath = (projectId: string) => {
  return `${entityUrlPathname(projectId)}/discussion/default`
}

export const expectDiscussionPageLoaded = async (
  page: Page,
  projectId: string,
  expectTimeout: number = defaultExpectTimeout,
) => {
  await testAuth.step('Default discussion page has loaded', async () => {
    await page.waitForURL(getDefaultDiscussionPath(projectId))
    await expect(page.getByRole('heading', { name: 'Discussion' })).toBeVisible(
      { timeout: expectTimeout },
    )
    await expect(page.getByRole('button', { name: 'New Thread' })).toBeVisible({
      timeout: expectTimeout,
    })
    await expect(
      page.getByRole('button', { name: 'Discussion Tools' }),
    ).toBeVisible({ timeout: expectTimeout })
    await expect(page.getByPlaceholder('Search discussions')).toBeVisible({
      timeout: expectTimeout,
    })
  })
}

export const expectDiscussionThreadLoaded = async (
  page: Page,
  threadId: number,
  threadTitle: string,
  threadBody: string,
  projectId: string,
) => {
  await testAuth.step('Discussion thread has loaded', async () => {
    await page.waitForURL(
      `${entityUrlPathname(projectId)}/discussion/threadId=${threadId}`,
    )
    await expect(
      page.getByRole('heading', { name: 'Discussion' }),
    ).toBeVisible()

    await expect(
      page.getByRole('button', { name: /show all threads/i }),
    ).toBeVisible({ timeout: defaultExpectTimeout * 3 }) // extra time for thread response to return
    await expect(
      page.getByRole('button', { name: 'Date Posted' }),
    ).toBeVisible()
    await expect(
      page.getByRole('button', { name: 'Most Recent' }),
    ).toBeVisible()

    const discussionThread = page.locator('.discussionThread:visible')
    await expect(discussionThread.getByText(threadTitle)).toBeVisible()
    await expect(discussionThread.getByText(threadBody)).toBeVisible()
  })
}

export const toggleIntoExperimentalMode = async (page: Page) => {
  await test.step('toggle into experimental mode', async () => {
    const experimentalModeLabel = page.getByText('Experimental Mode')
    const experimentalModeInput = page.getByLabel('Experimental Mode')

    await expect(experimentalModeInput).not.toBeChecked()

    // Because the input element is not visible, we need to click the label
    await experimentalModeLabel.click()

    await expect(experimentalModeInput).toBeChecked()
  })
}
