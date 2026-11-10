import { Page, expect, test } from '@playwright/test'
import { LoginResponse } from '@sage-bionetworks/synapse-types'
import { defaultExpectTimeout } from '../../playwright.config'
import { testAuth } from '../fixtures/authenticatedUserPages'
import { entityUrlPathname } from './entities'
import { BackendDestinationEnum, doDelete, doPost } from './http'
import { JwtPayload, TestUser } from './types'
import { dismissHoverCards, waitForInitialPageLoad } from './utils'
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
  await page.getByRole('button', { name: 'Login' }).first().click()
  const emailSignInButton = page.getByRole('button', {
    name: 'Sign in with your email',
  })
  const usernameInput = page.getByLabel('Username or Email Address')
  // The account site may not have hydrated when the button first renders, in which
  // case the click is dropped -- retry until the email sign-in form appears.
  await expect(async () => {
    if (await emailSignInButton.isVisible()) {
      await emailSignInButton.click()
    }
    await expect(usernameInput).toBeVisible({ timeout: 5000 })
  }).toPass({ timeout: defaultExpectTimeout * 3 })
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

export async function getUserIdFromAccessTokenCookie(page: Page) {
  return getUserIdFromJwt(await getAccessTokenFromCookie(page))
}

export async function acceptSiteCookies(page: Page) {
  const cookieBanner = page.getByText('Our site uses cookies.')
  const allowAllButton = page.getByRole('button', { name: 'ALLOW ALL' })
  await expect(allowAllButton.first()).toBeVisible()
  // The banner can be (re)mounted while the page initializes, so a click may land
  // on an instance that is discarded and the banner remains. Retry until it is gone.
  await expect(async () => {
    if (await allowAllButton.first().isVisible()) {
      await allowAllButton.first().click({ timeout: 5_000 })
    }
    await expect(cookieBanner).toHaveCount(0, { timeout: 5_000 })
  }).toPass({ timeout: defaultExpectTimeout })
  // ensure the consent cookie was actually stored, so it is persisted in storageState
  await expect(async () => {
    const cookies = await page.context().cookies()
    expect(
      cookies.find(
        cookie =>
          cookie.name ===
          'org.sagebionetworks.security.cookies.portal.preference',
      ),
    ).not.toBeUndefined()
  }).toPass()
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
    // The discussion action menu is labeled "Tools" (exact, to exclude the
    // entity-level "Project Tools" menu)
    await expect(
      page.getByRole('button', { name: 'Tools', exact: true }),
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
    // the thread is usually opened by clicking its title, so the cursor may rest
    // on the author's badge on the thread page
    await dismissHoverCards(page)
    await expect(
      page.getByRole('heading', { name: 'Discussion' }),
    ).toBeVisible()

    await expect(
      page.getByRole('button', { name: /show all threads/i }),
    ).toBeVisible({ timeout: defaultExpectTimeout * 3 }) // extra time for thread response to return

    // The thread (incl. its reply sort controls) is rendered by the React
    // DiscussionThread component
    const discussionThread = page.locator('.DiscussionThread:visible')
    await expect(
      discussionThread.getByRole('button', { name: 'Date Posted' }),
    ).toBeVisible()
    await expect(
      discussionThread.getByRole('button', { name: 'Most Recent' }),
    ).toBeVisible()
    await expect(
      discussionThread.getByRole('heading', { name: threadTitle }),
    ).toBeVisible()
    await expect(discussionThread.getByText(threadBody)).toBeVisible()
  })
}
