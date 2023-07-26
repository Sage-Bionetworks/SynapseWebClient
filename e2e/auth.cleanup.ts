import { test as cleanup, expect } from '@playwright/test'
import { getEndpoint } from './helpers/http'
import {
  TEST_USER_LOCALSTORAGE_KEY,
  deleteTestUser,
  getAdminCredentials,
  getTestUserId,
} from './helpers/testUser'

cleanup('cleanup', async ({ page }) => {
  await cleanup.step('delete test user', async () => {
    const { adminUserName, adminApiKey } = getAdminCredentials()
    const testUserId = await getTestUserId(page, TEST_USER_LOCALSTORAGE_KEY)

    const result = await deleteTestUser(
      getEndpoint(),
      adminUserName!,
      adminApiKey!,
      testUserId!,
    )
    expect(result).toEqual(testUserId)
  })
})
