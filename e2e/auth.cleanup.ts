import { Page, test as cleanup } from '@playwright/test'
import {
  cleanupTestUser,
  getUserIdFromLocalStorage,
  goToDashboard,
  logoutTestUser,
} from './helpers/testUser'
import { userConfigs } from './helpers/userConfig'

for (const { testName, storageStatePath, user } of userConfigs) {
  let userPage: Page

  cleanup.describe(`Clean up: ${testName}`, () => {
    cleanup.beforeAll(async ({ browser }) => {
      userPage = await browser.newPage({
        storageState: storageStatePath,
      })
    })

    cleanup('clean up user', async () => {
      const { testUserId } = await cleanup.step('get credentials', async () => {
        const testUserId = await getUserIdFromLocalStorage(userPage)

        return { testUserId }
      })

      await cleanup.step('logout user', async () => {
        await goToDashboard(userPage)
        await logoutTestUser(userPage)
      })

      await cleanup.step('clean up user', async () => {
        await cleanupTestUser(testUserId, userPage)
      })
    })

    cleanup.afterAll(async () => {
      await userPage.close()
    })
  })
}
