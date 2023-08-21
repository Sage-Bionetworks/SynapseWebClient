import { test as cleanup, expect } from '@playwright/test'
import { ADMIN_STORAGE_STATE, USER_STORAGE_STATE } from '../playwright.config'
import { getEndpoint } from './helpers/http'
import {
  deleteTestUser,
  getAccessTokenFromCookie,
  getUserIdFromLocalStorage,
  goToDashboard,
  logoutTestUser,
} from './helpers/testUser'

cleanup('cleanup', async ({ browser }) => {
  const { adminPage, userPage } = await cleanup.step(
    'create pages',
    async () => {
      const adminPage = await browser.newPage({
        storageState: ADMIN_STORAGE_STATE,
      })
      const userPage = await browser.newPage({
        storageState: USER_STORAGE_STATE,
      })

      return { adminPage, userPage }
    },
  )

  const { adminAccessToken, testUserId } = await cleanup.step(
    'get credentials',
    async () => {
      const adminAccessToken = await getAccessTokenFromCookie(adminPage)
      const testUserId = await getUserIdFromLocalStorage(userPage)

      return { adminAccessToken, testUserId }
    },
  )

  await cleanup.step('logout users', async () => {
    await goToDashboard(userPage)
    await logoutTestUser(userPage)

    await goToDashboard(adminPage)
    await logoutTestUser(adminPage)
  })

  await cleanup.step('delete test user', async () => {
    const result = await deleteTestUser(
      getEndpoint(),
      testUserId!,
      adminAccessToken,
    )
    expect(result).toEqual(testUserId)
  })

  await cleanup.step('close pages', async () => {
    await adminPage.close()
    await userPage.close()
  })
})
