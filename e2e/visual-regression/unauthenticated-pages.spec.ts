import test, { expect } from '@playwright/test'
import { waitForInitialPageLoad } from 'e2e/helpers/utils'
import {
  MAX_DIFF_PIXEL_RATIO,
  SHORTER_WAIT_TIME,
  testPageVisualAndAccessibility,
} from 'e2e/helpers/visualRegression.js'

test.describe('Unauthenticated Pages', () => {
  test('homepage', async ({ page }) => {
    await testPageVisualAndAccessibility(page, '/', 'homepage')
  })

  test('plans', async ({ page }) => {
    await testPageVisualAndAccessibility(page, '/Plans:default', 'plans')
  })

  test('portals popover', async ({ page }) => {
    await page.goto('/')
    await waitForInitialPageLoad(page)

    await page.getByRole('button', { name: 'Portals' }).click()
    await page.waitForSelector('[role="presentation"]', { state: 'visible' })
    await page.waitForTimeout(SHORTER_WAIT_TIME)

    await expect(page).toHaveScreenshot('portals-dropdown.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  })

  test('applications popover', async ({ page }) => {
    await page.goto('/')
    await waitForInitialPageLoad(page)

    await page.getByRole('button', { name: 'Applications' }).click()
    await page.waitForSelector('[role="presentation"]', { state: 'visible' })
    await page.waitForTimeout(SHORTER_WAIT_TIME)

    await expect(page).toHaveScreenshot('applications-dropdown.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  })
})
