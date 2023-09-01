// Required environment variables:
// - ADMIN_PAT: personal access token for admin
//
// Environment variables:
// - in CI: set in repo GitHub secrets, read via env in workflow
// - locally: set in .env file, read by dotenv

import { Page, expect, test as setup } from '@playwright/test'
import { setLocalStorage } from './helpers/localStorage'
import {
  cleanupTestUser,
  createTestUser,
  getAdminPAT,
  loginTestUser,
} from './helpers/testUser'
import { userConfigs } from './helpers/userConfig'

for (const {
  testName,
  storageStatePath,
  localStorageKey,
  user,
} of userConfigs) {
  let userId: string | undefined
  let userPage: Page

  setup.describe(`Setup: ${testName}`, () => {
    setup.beforeAll(async ({ browser }) => {
      userPage = await browser.newPage()
    })

    setup('create and authenticate user', async () => {
      // This may be the first test to run and trigger SWC compilation
      // ...which may take 15+ seconds. Mark this test as slow,
      // ...so the test will be given triple timeout.
      // https://playwright.dev/docs/api/class-test#test-slow-1
      setup.slow()

      await setup.step('create test user', async () => {
        userId = await createTestUser(user, getAdminPAT(), userPage)
        expect(userId).not.toBeUndefined()

        await setLocalStorage(userPage, localStorageKey, user.username)
        await userPage.context().storageState({ path: storageStatePath })
      })

      await setup.step('authenticate test user', async () => {
        await loginTestUser(userPage, user.username, user.password)
        await userPage.context().storageState({ path: storageStatePath })
      })
    })

    setup.afterAll(async ({}, { expectedStatus, status }) => {
      // delete test user if setup wasn't successful:
      // ...cleanup isn't run until after last retry
      // ...so we delete here, so we can retry setup without leaving behind test users
      if (status !== expectedStatus && userId !== undefined) {
        await cleanupTestUser(userId, userPage)
      }

      // clean up page
      await userPage.close()
    })
  })
}
