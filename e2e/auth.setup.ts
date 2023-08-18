// Required environment variables:
// - ADMIN_USERNAME: username for admin
// - ADMIN_PASSWORD: password for admin
//
// Environment variables:
// - in CI: set in repo GitHub secrets, read via env in workflow
// - locally: set in .env file, read by dotenv

import { Page, expect, test as setup } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import { ADMIN_STORAGE_STATE, USER_STORAGE_STATE } from '../playwright.config'
import { getEndpoint } from './helpers/http'
import { setLocalStorage } from './helpers/localStorage'
import {
  USER_NAME_LOCALSTORAGE_KEY,
  createTestUser,
  deleteTestUser,
  getAccessTokenFromCookie,
  getAdminUserCredentials,
  loginTestUser,
} from './helpers/testUser'
import { TestUser } from './helpers/types'

let testUserId: string | undefined
let adminAccessToken: string | undefined
let adminPage: Page
let userPage: Page

setup.describe('Setup', () => {
  setup.beforeAll(async ({ browser }) => {
    adminPage = await browser.newPage()
    userPage = await browser.newPage()
  })

  setup('authenticate users', async () => {
    setup.slow()

    await setup.step('authenticate admin', async () => {
      const { adminUserName, adminUserPassword } = getAdminUserCredentials()

      await loginTestUser(adminPage, adminUserName!, adminUserPassword!)
      await adminPage.context().storageState({ path: ADMIN_STORAGE_STATE })

      adminAccessToken = await getAccessTokenFromCookie(adminPage)
    })

    const { testUserName, testUserPassword } = await setup.step(
      'create test user',
      async () => {
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

        testUserId = await createTestUser(
          getEndpoint(),
          testUser,
          adminAccessToken,
        )
        expect(testUserId).not.toBeUndefined()

        await setLocalStorage(
          userPage,
          USER_NAME_LOCALSTORAGE_KEY,
          testUserName,
        )
        await userPage.context().storageState({ path: USER_STORAGE_STATE })

        return { testUserName, testUserPassword }
      },
    )

    await setup.step('authenticate test user', async () => {
      await loginTestUser(userPage, testUserName, testUserPassword)
      await userPage.context().storageState({ path: USER_STORAGE_STATE })
    })
  })

  setup.afterAll(async ({}, { expectedStatus, status }) => {
    // clean up pages
    await adminPage.close()
    await userPage.close()

    // delete test user if setup wasn't successful:
    // ...cleanup isn't run until after last retry
    // ...so we delete here, so we can retry setup without leaving behind test users
    if (status !== expectedStatus && testUserId !== undefined) {
      const result = await deleteTestUser(
        getEndpoint(),
        testUserId!,
        adminAccessToken,
      )
      expect(result).toEqual(testUserId)
    }
  })
})
