import { expect, test } from '@playwright/test'
import { goToDashboard } from './helpers/testUser'

test.describe('Favorites', () => {
  test('should be visible', async ({ page }) => {
    await goToDashboard(page)
    await page.getByLabel('Favorites', { exact: true }).click()
    await expect(
      page.getByRole('heading', { name: 'Your Favorites' }),
    ).toBeVisible()
  })
})
