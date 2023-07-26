import { expect, test } from '@playwright/test'

test.describe('Favorites', () => {
  test('should be visible', async ({ page }) => {
    await page.goto('/')
    await page
      .getByRole('link', { name: 'View Your Dashboard' })
      .first()
      .click()
    await page.getByLabel('Favorites', { exact: true }).click()
    await expect(
      page.getByRole('heading', { name: 'Your Favorites' }),
    ).toBeVisible()
  })
})
