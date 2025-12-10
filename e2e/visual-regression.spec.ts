import { expect, Page, test } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { testAuth } from './fixtures/authenticatedUserPages'
import { getUserIdFromLocalStorage } from './helpers/testUser'
import { waitForInitialPageLoad } from './helpers/utils'

/**
 *
 * Visual regression and accessibility tests to detect styling and UI differences
 * between branches.
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

const longerWaitTime = 2000

test.describe('Visual Regression & Accessibility - Unauthenticated Pages', () => {
  test('homepage', async ({ page }) => {
    await testPageVisualAndAccessibility(page, '/', 'homepage')
  })

  test('plans', async ({ page }) => {
    await testPageVisualAndAccessibility(page, '/Plans:default', 'plans')
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
      await testPageVisualAndAccessibility(
        userPage,
        '/Home:x',
        'authenticated-homepage',
      )
    })

    testAuth('projects page', async ({ userPage }) => {
      const userId = await getUserIdFromLocalStorage(userPage)
      await testPageVisualAndAccessibility(
        userPage,
        `/Profile:${userId}/projects/all`,
        'projects',
        { waitTime: longerWaitTime },
      )
    })

    // Helper to capture screenshots of various button interactions
    const pageButtonInteractions = async (
      userPage: Page,
      name: string,
      screenshotName: string,
    ) => {
      const button = userPage.getByRole('button', {
        name: `${name}`,
        exact: true,
      })

      // Check if button exists before trying to click
      if (await button.isVisible({ timeout: 3000 })) {
        await button.click()
        await userPage.waitForTimeout(500)

        await expect(userPage).toHaveScreenshot(screenshotName, {
          maxDiffPixelRatio: 0.01,
        })

        await userPage.keyboard.press('Escape')
        await userPage.waitForTimeout(300)
        return true
      } else {
        console.log(`Button "${name}" not found or not visible`)
        return false
      }
    }

    testAuth('projects page interactions', async ({ userPage }) => {
      const userId = await getUserIdFromLocalStorage(userPage)
      await userPage.goto(`/Profile:${userId}/projects/all`)
      await waitForInitialPageLoad(userPage)
      await userPage.waitForTimeout(longerWaitTime)

      const pageButtons = [
        'Created by me',
        'Favorites', // clicking the wrong fav rn, in nav bar
        'Shared with me',
        'By Team',
        'Create a new project',
      ]

      for (const buttonName of pageButtons) {
        const screenshotName = `projects-${buttonName.toLowerCase().replace(/\s+/g, '-')}-clicked.png`

        await pageButtonInteractions(userPage, buttonName, screenshotName)
      }
    })

    testAuth('favorites page', async ({ userPage }) => {
      const userId = await getUserIdFromLocalStorage(userPage)
      await testPageVisualAndAccessibility(
        userPage,
        `/Profile:${userId}/favorites`,
        'favorites-page',
        { waitTime: longerWaitTime },
      )
    })

    testAuth('teams page', async ({ userPage }) => {
      const userId = await getUserIdFromLocalStorage(userPage)
      await testPageVisualAndAccessibility(
        userPage,
        `/Profile:${userId}/teams`,
        'teams-page',
        { waitTime: longerWaitTime },
      )
    })

    testAuth('challenges page', async ({ userPage }) => {
      const userId = await getUserIdFromLocalStorage(userPage)
      await testPageVisualAndAccessibility(
        userPage,
        `/Profile:${userId}/challenges`,
        'challenges-page',
        { waitTime: longerWaitTime },
      )
    })

    testAuth('download cart page', async ({ userPage }) => {
      await testPageVisualAndAccessibility(
        userPage,
        '/DownloadCart:0',
        'download-cart-page',
      )
    })

    testAuth('trash can page', async ({ userPage }) => {
      await testPageVisualAndAccessibility(
        userPage,
        '/Trash:0',
        'trash-can-page',
      )
    })

    testAuth('search page', async ({ userPage }) => {
      const searchQuery = {
        queryTerm: [],
        booleanQuery: [],
        facetOptions: [
          { name: 'EntityType', maxResultCount: 300, sortType: 'COUNT' },
          { name: 'Consortium', maxResultCount: 300, sortType: 'COUNT' },
          { name: 'ModifiedOn', maxResultCount: 300, sortType: 'COUNT' },
          { name: 'ModifiedBy', maxResultCount: 300, sortType: 'COUNT' },
          { name: 'CreatedOn', maxResultCount: 300, sortType: 'COUNT' },
          { name: 'Tissue', maxResultCount: 300, sortType: 'COUNT' },
          { name: 'CreatedBy', maxResultCount: 300, sortType: 'COUNT' },
        ],
        size: 30,
      }
      const searchUrl = `/Search:${encodeURIComponent(JSON.stringify(searchQuery))}`

      await testPageVisualAndAccessibility(userPage, searchUrl, 'search-page', {
        waitTime: longerWaitTime,
      })
    })

    testAuth('help button', async ({ userPage }) => {
      await userPage.goto('/projects/all')
      await waitForInitialPageLoad(userPage)

      await userPage.getByRole('button', { name: 'Help' }).click()
      await userPage.waitForSelector('[role="button"]', { state: 'visible' })
      await userPage.waitForTimeout(500)

      await expect(userPage).toHaveScreenshot('help-sidebar.png', {
        maxDiffPixelRatio: 0.01,
      })
    })

    testAuth('your account button', async ({ userPage }) => {
      await userPage.goto('/projects/all')
      await waitForInitialPageLoad(userPage)

      await userPage.getByRole('button', { name: 'Your Account' }).click()
      await userPage.waitForSelector('[role="button"]', { state: 'visible' })
      await userPage.waitForTimeout(500)

      await expect(userPage).toHaveScreenshot('your-account-sidebar.png', {
        maxDiffPixelRatio: 0.01,
      })
    })
  },
)
