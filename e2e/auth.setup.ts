// Required environment variables:
// - ADMIN_USERNAME: username for admin
// - ADMIN_PASSWORD: password for admin
//
// Environment variables:
// - in CI: set in repo GitHub secrets, read via env in workflow
// - locally: set in .env file, read by dotenv

import { expect, test as setup } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import { ADMIN_STORAGE_STATE, USER_STORAGE_STATE } from '../playwright.config'
import { getEndpoint } from './helpers/http'
import { setLocalStorage } from './helpers/localStorage'
import {
  USER_NAME_LOCALSTORAGE_KEY,
  createTestUser,
  getAccessTokenFromCookie,
  getAdminUserCredentials,
  loginTestUser,
} from './helpers/testUser'
import { TestUser } from './helpers/types'

setup('authenticate users', async ({ browser }) => {
  const adminAccessToken = await setup.step('authenticate admin', async () => {
    const adminPage = await browser.newPage()
    const { adminUserName, adminUserPassword } = getAdminUserCredentials()

    await loginTestUser(adminPage, adminUserName!, adminUserPassword!)
    await adminPage.context().storageState({ path: ADMIN_STORAGE_STATE })

    return getAccessTokenFromCookie(adminPage)
  })

  const { userPage, testUserName, testUserPassword } = await setup.step(
    'create test user',
    async () => {
      const userPage = await browser.newPage()

      const testUserName = 'swc-e2e-' + uuidv4()
      const testUserPassword = 'password-' + uuidv4()
      /* 
      const testUserName = 'swc-e2e-test'
      const testUserPassword = process.env.USER_PASSWORD!
      expect(testUserPassword).not.toBeUndefined() 
      */

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
        testUser,
        adminAccessToken,
      )
      expect(testUserId).not.toBeUndefined()

      await setLocalStorage(userPage, USER_NAME_LOCALSTORAGE_KEY, testUserName)
      await userPage.context().storageState({ path: USER_STORAGE_STATE })

      return { userPage, testUserName, testUserPassword }
    },
  )

  await setup.step('authenticate test user', async () => {
    await loginTestUser(userPage, testUserName, testUserPassword)
    await userPage.context().storageState({ path: USER_STORAGE_STATE })
  })
})
