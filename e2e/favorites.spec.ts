import { expect, test } from '@playwright/test'
import { testAuth } from './fixtures/authenticatedUserPages'
import { goToDashboard } from './helpers/testUser'

test.describe('Favorites', () => {
  testAuth('should be visible', async ({ userPage }) => {
    await goToDashboard(userPage)
    await userPage.getByLabel('Favorites', { exact: true }).click()
    await expect(
      userPage.getByRole('heading', { name: 'Your Favorites' }),
    ).toBeVisible()
  })
})
