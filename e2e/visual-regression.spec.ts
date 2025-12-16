import { expect, Page, test } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { testAuth } from './fixtures/authenticatedUserPages'
import { getUserIdFromLocalStorage } from './helpers/testUser'
import { waitForInitialPageLoad } from './helpers/utils'
import { Project } from './helpers/types'
import { setupProjectWithPermissions } from './helpers/setupTeardown'

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
 *    pnpm e2e:report or pnpm exec playwright show-report
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
  options: { fullPage?: boolean; waitTime?: number; selector?: string } = {},
) {
  const { fullPage = true, waitTime = 500, selector } = options
  console.log('url:', url)

  await page.goto(url)
  await waitForInitialPageLoad(page)
  await page.waitForTimeout(waitTime)

  // Visual regression test
  if (selector) {
    // Wait for the specific element and take screenshot of it
    await page.waitForSelector(selector, { timeout: 10000 })
    const element = page.locator(selector)
    await expect(element).toHaveScreenshot(`${name}.png`)
  } else {
    // Take full page or partial page screenshot
    await expect(page).toHaveScreenshot(`${name}.png`, {
      fullPage,
      maxDiffPixelRatio: 0.01,
    })
  }

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
    let userProject: Project

    testAuth.beforeAll(async ({ browser, storageStatePaths }) => {
      userProject = await setupProjectWithPermissions(
        browser,
        'swc-e2e-user',
        'swc-e2e-user-validated',
        storageStatePaths,
      )
    })

    // To test the below, you need an admin account for the dev stack.
    testAuth('dashboard', async ({ userPage }) => {
      await testPageVisualAndAccessibility(
        userPage,
        '/Home:x',
        'authenticated-homepage',
      )
    })

    // Helper to capture screenshots of various button interactions
    const pageButtonInteractions = async (
      userPage: Page,
      name: string,
      screenshotName: string,
      selector?: string,
    ) => {
      // If selector is provided, scope button search to that area to avoid navigation conflicts
      const button = selector
        ? userPage.locator(selector).getByRole('button', {
            name: new RegExp(`${name}`, 'i'),
          })
        : userPage.getByRole('button', {
            name: new RegExp(`${name}`, 'i'),
          })

      // Check if button exists before trying to click
      if (await button.isVisible({ timeout: 3000 })) {
        await button.click()
        await userPage.waitForTimeout(500)

        if (selector) {
          // Focus screenshot on the specified element
          const element = userPage.locator(selector)
          await expect(element).toHaveScreenshot(screenshotName)
        } else {
          // Take full page screenshot
          await expect(userPage).toHaveScreenshot(screenshotName, {
            maxDiffPixelRatio: 0.01,
          })
        }

        await userPage.keyboard.press('Escape')
        await userPage.waitForTimeout(300)
        return true
      } else {
        console.log(`Button "${name}" not found or not visible`)
        return false
      }
    }

    testAuth('projects page', async ({ userPage }) => {
      const userId = await getUserIdFromLocalStorage(userPage)
      await testPageVisualAndAccessibility(
        userPage,
        `/Profile:${userId}/projects/all`,
        'projects',
        {
          waitTime: longerWaitTime,
          selector: '#rootPanel .rootPanel .ProfileViewImpl',
        },
      )

      // Test button interactions
      const pageButtons = [
        'Created by me',
        'Favorites',
        'Shared directly with me',
        'Create a New Project',
      ]

      for (const buttonName of pageButtons) {
        const screenshotName = `projects-${buttonName.toLowerCase().replace(/\s+/g, '-')}-clicked.png`

        await pageButtonInteractions(
          userPage,
          buttonName,
          screenshotName,
          '#rootPanel .rootPanel .ProfileViewImpl',
        )
      }
    })

    testAuth('project page', async ({ userPage }) => {
      // Navigate to the created project page
      await testPageVisualAndAccessibility(
        userPage,
        `/Synapse:${userProject.id}/wiki/`,
        'project-page',
        {
          waitTime: longerWaitTime,
          selector: '#rootPanel .rootPanel',
        },
      )

      // Test project tab interactions
      const projectTabs = [
        'Wiki',
        'Files',
        'Datasets',
        'Tables',
        'Discussion',
        'Docker',
      ]

      for (const tabName of projectTabs) {
        const screenshotName = `project-${tabName.toLowerCase()}-tab.png`

        await pageButtonInteractions(
          userPage,
          tabName,
          screenshotName,
          '#rootPanel .rootPanel',
        )
      }

      // Test project tools dropdown if present
      const projectToolsButton = userPage
        .locator('#rootPanel .rootPanel')
        .getByRole('button', {
          name: /Project Tools/i,
        })

      if (await projectToolsButton.isVisible({ timeout: 3000 })) {
        await projectToolsButton.click()
        await userPage.waitForTimeout(500)

        const element = userPage.locator('#rootPanel .rootPanel')
        await expect(element).toHaveScreenshot('project-tools-dropdown.png')

        await userPage.keyboard.press('Escape')
        await userPage.waitForTimeout(300)
      }
    })

    testAuth.afterAll(async ({ browser }) => {
      if (userProject?.id) {
        const { teardownProjectsAndFileHandles } = await import(
          './helpers/setupTeardown'
        )
        await teardownProjectsAndFileHandles(browser, [userProject], [])
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
