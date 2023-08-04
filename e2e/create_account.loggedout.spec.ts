import { expect, test } from '@playwright/test'

test.describe('Create Account', () => {
  test('should show an alert when an invalid email address is used', async ({
    page,
  }) => {
    await page.goto('/')
    // Allow time for SWC compilation
    await expect(
      page.getByRole('heading', { name: 'Loading…' }),
    ).not.toBeVisible(
      { timeout: 2 * 60 * 1000 }, // ...wait 2 minutes if necessary
    )
    await page.getByRole('link', { name: 'Register Now' }).first().click()
    await page.getByRole('textbox').first().fill('test123')
    await page.getByPlaceholder('Your email address').fill('test123')
    await page.getByRole('button', { name: 'Send registration info' }).click()
    await expect(page.locator('#rootPanel').getByRole('alert')).toHaveText(
      'Email address is not valid.',
    )
  })
})
