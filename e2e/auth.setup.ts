// Required environment variables:
// - DEV_ADMIN_USERNAME: username of admin who can create users on backend dev stack
// - DEV_ADMIN_API_KEY: api key for admin who can create users on backend dev stack
//
// Environment variables:
// - in CI: set in repo GitHub secrets, read via env in workflow
// - locally: set in .env file, read by dotenv

import { expect, test as setup } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import { STORAGE_STATE } from '../playwright.config'
import { getEndpoint } from './helpers/http'
import {
  TEST_USER_LOCALSTORAGE_KEY,
  TestUser,
  createTestUser,
  getAdminCredentials,
  loginTestUser,
  setTestUserId,
} from './helpers/testUser'

setup('authenticate', async ({ page }) => {
  const { testUserName, testUserPassword } = await setup.step(
    'create test user',
    async () => {
      const { adminUserName, adminApiKey } = getAdminCredentials()

      const testUserName = 'swc-e2e-' + uuidv4()
      const testUserPassword = 'password-' + uuidv4()
      const testUserEmail = `${testUserName}@test.com`
      const testUserIsValidated = false

      const testUser: TestUser = {
        username: testUserName,
        email: testUserEmail,
        password: testUserPassword,
        tou: true,
        validatedUser: testUserIsValidated,
      }

      const testUserId = await createTestUser(
        getEndpoint(),
        adminUserName!,
        adminApiKey!,
        testUser,
      )

      expect(testUserId).not.toBeUndefined()
      console.log(`Create user: ${testUserName}, id: ${testUserId}`)

      // Save user id in context, so the test user can be deleted during clean up
      await setTestUserId(page, TEST_USER_LOCALSTORAGE_KEY, testUserId)
      await page.context().storageState({ path: STORAGE_STATE })

      return { testUserName, testUserPassword }
    },
  )

  await setup.step('authenticate test user', async () => {
    await loginTestUser(page, testUserName, testUserPassword)
    // Save user authentication state in context, so login doesn't have to be repeated
    await page.context().storageState({ path: STORAGE_STATE })
  })
})
