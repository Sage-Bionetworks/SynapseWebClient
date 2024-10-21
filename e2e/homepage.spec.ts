import { expect, test } from '@playwright/test'
import { testAuth } from './fixtures/authenticatedUserPages'
import { waitForInitialPageLoad } from './helpers/utils'

test.describe('Homepage - Unauthenticated', () => {
  test('should show Log In To Synapse button when logged out', async ({
    page,
  }) => {
    await page.goto('/')
    await waitForInitialPageLoad(page)

    await expect(page.getByRole('button', { name: 'Login' })).toHaveCount(1)
    await expect(
      page.getByRole('button', { name: 'View Dashboard' }),
    ).toHaveCount(0)
  })
})

testAuth.describe('Homepage - Authenticated', () => {
  testAuth(
    'should show View Dashboard button when logged in',
    async ({ userPage }) => {
      await userPage.goto('/Home:x')
      await waitForInitialPageLoad(userPage)

      await expect(userPage.getByRole('button', { name: 'Login' })).toHaveCount(
        0,
      )
      await expect(
        userPage.getByRole('button', { name: 'View Dashboard' }),
      ).toHaveCount(1)
    },
  )
})
