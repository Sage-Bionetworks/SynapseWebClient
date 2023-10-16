import { expect, test } from '@playwright/test'
import { testAuth } from './fixtures/authenticatedUserPages'
import { goToDashboard } from './helpers/testUser'

test.describe('Account Settings', () => {
  testAuth('should show certification status', async ({ userPage }) => {
    await goToDashboard(userPage)

    await userPage.getByLabel('Your Account').click()
    await userPage.getByRole('link', { name: 'Account Settings' }).click()
    await expect(
      userPage.getByRole('heading', { name: 'Account Settings' }),
    ).toBeVisible()

    await expect(
      userPage.getByRole('button', { name: 'Passed Certification Quiz' }),
    ).toBeVisible()
  })
})
