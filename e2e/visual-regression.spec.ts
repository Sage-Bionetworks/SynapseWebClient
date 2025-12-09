import { expect, Page, test } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { testAuth } from './fixtures/authenticatedUserPages'
import { waitForInitialPageLoad } from './helpers/utils'

/**
 * Visual regression and accessibility tests to detect styling and UI differences
 * between branches or across changes.
 *
 * USAGE:
 *
 * 1. Capture baselines (before making changes):
 *    git checkout <baseline-branch>  # develop
 *    pnpm dev
 *    pnpm playwright test visual-regression --update-snapshots
 *
 * 2. Compare after making changes:
 *    git checkout <your-feature-branch>
 *    pnpm dev
 *    pnpm playwright test visual-regression
 *
 * 3. View differences:
 *    pnpm e2e:report
 *
 * NOTE: Accessibility snapshots track violations to ensure no NEW violations
 * are introduced by your changes. Existing violations are preserved.
 */

/**
 * Reusable helper to test a page for visual and accessibility regressions
 */
async function testPageVisualAndAccessibility(
  page: Page,
  url: string,
  name: string,
  options: { fullPage?: boolean; waitTime?: number } = {},
) {
  const { fullPage = true, waitTime = 500 } = options

  await page.goto(url)
  await waitForInitialPageLoad(page)
  await page.waitForTimeout(waitTime)

  // Visual regression test
  await expect(page).toHaveScreenshot(`${name}.png`, {
    fullPage,
    maxDiffPixelRatio: 0.01,
  })

  // Accessibility snapshot
  const accessibilityScanResults = await new AxeBuilder({ page }).analyze()

  const violationsJson = JSON.stringify(
    accessibilityScanResults.violations,
    null,
    2,
  )

  expect(violationsJson).toMatchSnapshot(`${name}-a11y.json`)
}

test.describe('Visual Regression & Accessibility - Unauthenticated Pages', () => {
  test('homepage', async ({ page }) => {
    await testPageVisualAndAccessibility(page, '/', 'homepage')
  })

  test('plans', async ({ page }) => {
    await testPageVisualAndAccessibility(page, '/Plans', 'plans')
  })

  test('portals popover should match baseline', async ({ page }) => {
    await page.goto('/')
    await waitForInitialPageLoad(page)

    await page.getByRole('button', { name: 'Portals' }).click()
    await page.waitForSelector('[role="presentation"]', { state: 'visible' })
    await page.waitForTimeout(500)

    await expect(page).toHaveScreenshot('portals-dropdown.png', {
      maxDiffPixelRatio: 0.01,
    })
  })

  test('applications dropdown should match baseline', async ({ page }) => {
    await page.goto('/')
    await waitForInitialPageLoad(page)

    await page.getByRole('button', { name: 'Applications' }).click()
    await page.waitForSelector('[role="presentation"]', { state: 'visible' })
    await page.waitForTimeout(500)

    await expect(page).toHaveScreenshot('applications-dropdown.png', {
      maxDiffPixelRatio: 0.01,
    })
  })
})

testAuth.describe(
  'Visual Regression & Accessibility - Authenticated Pages',
  () => {
    // To test the below, you need an admin account for the dev stack.
    testAuth('dashboard', async ({ userPage }) => {
      await testPageVisualAndAccessibility(userPage, '/Home:x', 'dashboard')
    })

    // testAuth('projects page', async ({ userPage }) => {
    //   await testPageVisualAndAccessibility(
    //     userPage,
    //     '/ProjectsHome:0',
    //     'projects',
    //   )
    // })

    // by team dropdown

    // create a new project modal

    // project

    // tables

    // teams
  },
)
