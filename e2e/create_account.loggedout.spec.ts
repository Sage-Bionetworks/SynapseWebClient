import { expect, test } from '@playwright/test'
import { waitForInitialPageLoad } from './helpers/testUser'

test.describe('Create Account', () => {
  test('should show an alert when an invalid email address is used', async ({
    page,
  }) => {
    await page.goto('/')
    await waitForInitialPageLoad(page)

    await page.getByRole('link', { name: 'Register Now' }).first().click()
    await page.getByRole('textbox').first().fill('test123')
    await page.getByPlaceholder('Your email address').fill('test123')
    await page.getByRole('button', { name: 'Send registration info' }).click()
    await expect(page.locator('#rootPanel').getByRole('alert')).toHaveText(
      'Email address is not valid.',
    )
  })
})
