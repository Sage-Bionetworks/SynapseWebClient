import { test as cleanup, expect } from '@playwright/test'
import { ADMIN_STORAGE_STATE, USER_STORAGE_STATE } from '../playwright.config'
import { getEndpoint } from './helpers/http'
import {
  deleteTestUser,
  getAccessTokenFromCookie,
  getUserIdFromLocalStorage,
} from './helpers/testUser'

cleanup('cleanup', async ({ browser }) => {
  await cleanup.step('delete test user', async () => {
    const adminPage = await browser.newPage({
      storageState: ADMIN_STORAGE_STATE,
    })
    const userPage = await browser.newPage({
      storageState: USER_STORAGE_STATE,
    })

    const adminAccessToken = await getAccessTokenFromCookie(adminPage)
    const testUserId = await getUserIdFromLocalStorage(userPage)

    const result = await deleteTestUser(
      getEndpoint(),
      testUserId!,
      adminAccessToken,
    )
    expect(result).toEqual(testUserId)
  })
})
