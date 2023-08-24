import { Page, test as cleanup, expect } from '@playwright/test'
import { ADMIN_STORAGE_STATE, USER_STORAGE_STATE } from '../playwright.config'
import { getEndpoint } from './helpers/http'
import {
  deleteTestUser,
  getAdminPAT,
  getUserIdFromLocalStorage,
  goToDashboard,
  logoutTestUser,
} from './helpers/testUser'

let adminPage: Page
let userPage: Page

cleanup.describe('Clean up', () => {
  cleanup.beforeAll(async ({ browser }) => {
    adminPage = await browser.newPage({
      storageState: ADMIN_STORAGE_STATE,
    })
    userPage = await browser.newPage({
      storageState: USER_STORAGE_STATE,
    })
  })

  cleanup('clean up users', async () => {
    const { testUserId } = await cleanup.step('get credentials', async () => {
      const testUserId = await getUserIdFromLocalStorage(userPage)

      return { testUserId }
    })

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
        getAdminPAT(),
      )
      expect(result).toEqual(testUserId)
    })
  })

  cleanup.afterAll(async () => {
    await adminPage.close()
    await userPage.close()
  })
})
