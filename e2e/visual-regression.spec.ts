import { expect, Page, test } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { v4 as uuidv4 } from 'uuid'
import { testAuth } from './fixtures/authenticatedUserPages'
import { getUserIdFromLocalStorage } from './helpers/testUser'
import { waitForInitialPageLoad } from './helpers/utils'
import { Project } from './helpers/types'
import { setupProjectWithPermissions } from './helpers/setupTeardown'

/**
 * ======================================================
 * Visual Regression & Accessibility Tests
 * ======================================================
 *
 * Visual regression testing is used to identify unintended visual and accessibility
 * differences in the UI by comparing screenshots across branches.
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

// ======================================================
// Constants
// ======================================================
const LONGER_WAIT_TIME = 2000
// const PAGE_SELECTOR = '#rootPanel .rootPanel'
const PROJECT_HEADER_SELECTOR = '.pageHeader'
const PAGE_SELECTOR = '.tab-content'
const MAX_DIFF_PIXEL_RATIO = 0.01

// ======================================================
// Shared Test Helpers
// ======================================================

const formatScreenshotName = (
  prefix: string,
  action: string,
  suffix = 'png',
) => {
  return `${prefix}-${action.toLowerCase().replace(/\s+/g, '-')}.${suffix}`
}

const navigateToProjectTab = async (
  page: Page,
  projectId: string,
  tab: string,
) => {
  await page.goto(`/Synapse:${projectId}/${tab}/`)
  await waitForInitialPageLoad(page)
  await page.waitForTimeout(LONGER_WAIT_TIME)
}

async function testPageVisualAndAccessibility(
  page: Page,
  url: string,
  name: string,
  options: { fullPage?: boolean; waitTime?: number; selector?: string } = {},
) {
  const { fullPage = true, waitTime = 500, selector } = options

  await page.goto(url)
  await waitForInitialPageLoad(page)
  await page.waitForTimeout(waitTime)

  if (selector) {
    // Wait for the specific element and take screenshot of it
    await page.waitForSelector(selector, { timeout: 10000 })
    const element = page.locator(selector)
    await expect(element).toHaveScreenshot(`${name}.png`)
  } else {
    // Take full page or partial page screenshot
    await expect(page).toHaveScreenshot(`${name}.png`, {
      fullPage,
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  }

  // Accessibility snapshot
  const accessibilityScanResults = await new AxeBuilder({ page }).analyze()

  // Normalize dynamic CSS class names to prevent false failures between builds
  const normalizedViolations = accessibilityScanResults.violations.map(
    violation => ({
      ...violation,
      nodes: violation.nodes.map(node => ({
        ...node,
        html:
          node.html?.replace(/css-[a-z0-9]+-/g, 'css-NORMALIZED-') || node.html,
      })),
    }),
  )

  const violationsJson = JSON.stringify(normalizedViolations, null, 2)

  expect(violationsJson).toMatchSnapshot(`${name}-a11y.json`)
}

const pageElementInteractions = async (
  userPage: Page,
  name: string,
  screenshotName: string,
  options: {
    selector?: string
    elementType?: 'button' | 'link' | 'menuitem'
    exact?: boolean
    needsEscape?: boolean
  } = {},
) => {
  const {
    selector,
    elementType = 'button',
    exact = false,
    needsEscape = elementType === 'button',
  } = options

  // If selector is provided, scope element search to that area to avoid navigation conflicts
  const element = selector
    ? userPage.locator(selector).getByRole(elementType, {
        name: exact ? name : new RegExp(`${name}`, 'i'),
        exact,
      })
    : userPage.getByRole(elementType, {
        name: exact ? name : new RegExp(`${name}`, 'i'),
        exact,
      })

  // Use first() for links to avoid strict mode violations with multiple matches
  const targetElement = elementType === 'link' ? element.first() : element

  // Check if element exists before trying to click
  if (await targetElement.isVisible({ timeout: 3000 })) {
    await targetElement.click()
    await userPage.waitForTimeout(500)

    if (selector) {
      const screenshotElement = userPage.locator(selector)
      await expect(screenshotElement).toHaveScreenshot(screenshotName)
    } else {
      // Take full page screenshot
      await expect(userPage).toHaveScreenshot(screenshotName, {
        maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
      })
    }

    if (needsEscape) {
      await userPage.keyboard.press('Escape')
      await userPage.waitForTimeout(300)
    }

    return true
  } else {
    console.log(`${elementType} "${name}" not found or not visible`)
    return false
  }
}

const testDropdownActions = async (
  page: Page,
  triggerButton: string,
  actions: string[],
  options: {
    selector?: string
    screenshotPrefix: string
    elementType?: 'button' | 'link' | 'menuitem'
    exact?: boolean
  },
) => {
  for (const actionName of actions) {
    // Click trigger button to open dropdown
    const triggerElement = options.selector
      ? page
          .locator(options.selector)
          .getByRole('button', { name: triggerButton })
      : page.getByRole('button', { name: triggerButton })

    await triggerElement.click()
    await page.waitForTimeout(500) // Longer wait for dropdown to appear

    // Generate proper screenshot name
    const screenshotName = `${options.screenshotPrefix}-${actionName.toLowerCase().replace(/\s+/g, '-')}.png`

    // Find and click dropdown item
    const dropdownElement = page.getByRole(options.elementType || 'link', {
      name: options.exact ? actionName : new RegExp(actionName, 'i'),
      exact: options.exact,
    })

    if (await dropdownElement.isVisible({ timeout: 3000 })) {
      await dropdownElement.click()
      await page.waitForTimeout(500)

      // Take screenshot
      if (options.selector) {
        const screenshotElement = page.locator(options.selector)
        await expect(screenshotElement).toHaveScreenshot(screenshotName)
      } else {
        await expect(page).toHaveScreenshot(screenshotName, {
          maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
        })
      }

      // Close any modals/dropdowns
      await page.keyboard.press('Escape')
      await page.waitForTimeout(300)
    } else {
      console.log(
        `${options.elementType || 'link'} "${actionName}" not found or not visible`,
      )
    }
  }
}

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
    await page.waitForTimeout(500)

    await expect(page).toHaveScreenshot('portals-dropdown.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  })

  test('applications popover', async ({ page }) => {
    await page.goto('/')
    await waitForInitialPageLoad(page)

    await page.getByRole('button', { name: 'Applications' }).click()
    await page.waitForSelector('[role="presentation"]', { state: 'visible' })
    await page.waitForTimeout(500)

    await expect(page).toHaveScreenshot('applications-dropdown.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  })
})

testAuth.describe('Authenticated Pages', () => {
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

  testAuth('all projects page', async ({ userPage }) => {
    const userId = await getUserIdFromLocalStorage(userPage)
    await testPageVisualAndAccessibility(
      userPage,
      `/Profile:${userId}/projects/all`,
      'projects',
      {
        waitTime: LONGER_WAIT_TIME,
        selector: '#rootPanel .rootPanel .ProfileViewImpl',
      },
    )

    // Hide dynamic project names to prevent false failures
    await userPage.addStyleTag({
      content: `
        /* Hide project names that vary between test runs */
        a.gwt-Anchor.displayInline[data-project-id] {
          visibility: hidden !important;
        }
      `,
    })

    // Test button interactions
    const pageButtons = [
      'Created by me',
      'Favorites',
      'Shared directly with me',
      'Create a New Project',
    ]

    for (const buttonName of pageButtons) {
      const screenshotName = formatScreenshotName(
        'all-projects',
        `${buttonName}-clicked`,
      )

      await pageElementInteractions(userPage, buttonName, screenshotName, {
        selector: '#rootPanel .rootPanel .ProfileViewImpl',
      })
    }
  })

  testAuth('project page', async ({ userPage }) => {
    await testPageVisualAndAccessibility(
      userPage,
      `/Synapse:${userProject.id}/wiki/`,
      'project-page',
      {
        waitTime: LONGER_WAIT_TIME,
        selector: PAGE_SELECTOR,
      },
    )

    // Test various header actions
    await pageElementInteractions(
      userPage,
      'Project Tools',
      'project-page-project-tools-dropdown.png',
      {
        selector: PROJECT_HEADER_SELECTOR,
      },
    )

    const projectTabs = [
      'Wiki',
      'Files',
      'Tables',
      'Discussion',
      'Docker',
      'Datasets',
    ]

    for (const tabName of projectTabs) {
      const screenshotName = `project-page-${tabName.toLowerCase()}-tab.png`

      await pageElementInteractions(userPage, tabName, screenshotName, {
        selector: PAGE_SELECTOR,
        elementType: 'link',
        exact: true,
        needsEscape: false,
      })
    }
  })

  // Test various Wiki tab actions
  const wikiTabActions = ['Edit Project Wiki', 'View Wiki Source']

  for (const actionName of wikiTabActions) {
    const screenshotName = formatScreenshotName(
      'project-page-wiki-tab',
      actionName,
    )

    testAuth(
      `project wiki tab actions - ${actionName}`,
      async ({ userPage }) => {
        // Navigate to wiki tab first
        await navigateToProjectTab(userPage, userProject.id, 'wiki')

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PAGE_SELECTOR,
        })
      },
    )
  }

  // Test various Files tab actions
  const filesTabActions = ['Upload or Link to a File', 'Add New Folder']

  for (const actionName of filesTabActions) {
    const screenshotName = formatScreenshotName(
      'project-page-files-tab',
      actionName,
    )

    testAuth(
      `project files tab actions - ${actionName}`,
      async ({ userPage }) => {
        // Navigate to files tab first
        await navigateToProjectTab(userPage, userProject.id, 'files')

        // Hide dynamic usernames to prevent false failures
        await userPage.addStyleTag({
          content: `
            /* Hide usernames that vary between test runs */
            a.SRC-userCard.UserBadge {
              visibility: hidden !important;
            }
          `,
        })

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PAGE_SELECTOR,
        })
      },
    )
  }

  testAuth(
    'project datasets tab actions - Add New...',
    async ({ userPage }) => {
      // Navigate to datasets tab first
      await navigateToProjectTab(userPage, userProject.id, 'datasets')

      await pageElementInteractions(
        userPage,
        'Add New...',
        'project-page-datasets-tab-add-new.png',
        {
          selector: PAGE_SELECTOR,
        },
      )
    },
  )

  // Test datasets dropdown actions using helper
  testAuth('project datasets tab dropdown actions', async ({ userPage }) => {
    // Navigate to datasets tab first
    await navigateToProjectTab(userPage, userProject.id, 'datasets')

    // Wait for dropdown menu to be detached if it was previously open
    await userPage
      .waitForSelector('[role="menu"]', {
        state: 'detached',
        timeout: 1000,
      })
      .catch(() => {}) // Ignore if already detached

    const datasetsDropdownActions = ['Add Dataset', 'Add Dataset Collection']
    await testDropdownActions(userPage, 'Add New...', datasetsDropdownActions, {
      selector: PAGE_SELECTOR,
      screenshotPrefix: 'project-page-datasets-tab',
      elementType: 'menuitem',
      exact: true,
    })
  })

  // Test various Tables tab actions
  const tablesTabActions = ['Upload a Table', 'Add Table or View']

  for (const actionName of tablesTabActions) {
    const screenshotName = formatScreenshotName(
      'project-page-tables-tab',
      actionName,
    )

    testAuth(
      `project tables tab actions - ${actionName}`,
      async ({ userPage }) => {
        // Navigate to tables tab first
        await navigateToProjectTab(userPage, userProject.id, 'tables')

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PAGE_SELECTOR,
        })
      },
    )
  }

  // Test various Discussion tab actions
  const discussionTabActions = ['New Thread', 'Discussion Tools']

  for (const actionName of discussionTabActions) {
    const screenshotName = formatScreenshotName(
      'project-page-discussion-tab',
      actionName,
    )

    testAuth(
      `project discussion tab actions - ${actionName}`,
      async ({ userPage }) => {
        // Navigate to discussion tab first
        await navigateToProjectTab(userPage, userProject.id, 'discussion')

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PAGE_SELECTOR,
        })
      },
    )
  }

  // Test Discussion Tools dropdown items
  testAuth('project discussion tab dropdown actions', async ({ userPage }) => {
    // Navigate to discussion tab first
    await navigateToProjectTab(userPage, userProject.id, 'discussion')

    const discussionDropdownActions = ['Show Deleted Threads']
    await testDropdownActions(
      userPage,
      'Discussion Tools',
      discussionDropdownActions,
      {
        selector: PAGE_SELECTOR,
        screenshotPrefix: 'project-page-discussion-tab',
        elementType: 'menuitem',
      },
    )
  })

  // Test various Docker tab actions
  const dockerTabActions = ['Add External Repository']

  for (const actionName of dockerTabActions) {
    const screenshotName = formatScreenshotName(
      'project-page-docker-tab',
      actionName,
    )

    testAuth(
      `project docker tab actions - ${actionName}`,
      async ({ userPage }) => {
        // Navigate to docker tab first
        await navigateToProjectTab(userPage, userProject.id, 'docker')

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PAGE_SELECTOR,
        })
      },
    )
  }

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
      { waitTime: LONGER_WAIT_TIME },
    )
  })

  testAuth('teams page', async ({ userPage }) => {
    const TEAM_NAME = 'test-team-' + uuidv4()
    const userId = await getUserIdFromLocalStorage(userPage)
    await testPageVisualAndAccessibility(
      userPage,
      `/Profile:${userId}/teams`,
      'teams-page',
      { waitTime: LONGER_WAIT_TIME },
    )

    // Hide dynamic content to prevent false failures
    await userPage.addStyleTag({
      content: `
        /* Hide dynamic content that varies between test runs */
        a.highlight-link,
        h3.pageHeaderTitle,
        .SRC-cardContent a[href^="/Profile:"],
        .SRC-emailText a.link,
        .SRC-cardContent .SRC-emailText {
          visibility: hidden !important;
        }
      `,
    })

    // Create a team first so we have something to interact with
    await pageElementInteractions(
      userPage,
      'Create a New Team',
      'teams-page-create-a-new-team-clicked.png',
      {
        needsEscape: false,
      },
    )

    // Fill in team name (using nth(1) like in teams.spec.ts)
    const teamNameInput = userPage.getByRole('textbox').nth(1)
    await teamNameInput.fill(TEAM_NAME)
    await userPage.getByRole('button', { name: 'OK' }).click()

    // after clicking OK, wait for navigation to team page
    await userPage.waitForURL('**/Team:*')
    await waitForInitialPageLoad(userPage)
    await userPage.waitForTimeout(LONGER_WAIT_TIME)

    // Screenshot of the created team page
    const screenshotName = `teams-page-test-team-created.png`
    await expect(userPage).toHaveScreenshot(screenshotName, {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })

    // click team actions dropdown button
    await pageElementInteractions(
      userPage,
      'Team Actions',
      'teams-page-test-team-clicked-team-actions-dropdown.png',
    )

    const dropdownActions = [
      'Invite User',
      'Edit Team',
      'Delete Team',
      'Leave Team',
    ]

    await testDropdownActions(userPage, 'Team Actions', dropdownActions, {
      screenshotPrefix: 'teams-page',
      elementType: 'link',
    })
  })

  testAuth('challenges page', async ({ userPage }) => {
    const userId = await getUserIdFromLocalStorage(userPage)
    await testPageVisualAndAccessibility(
      userPage,
      `/Profile:${userId}/challenges`,
      'challenges-page',
      { waitTime: LONGER_WAIT_TIME },
    )
  })

  testAuth('download cart page', async ({ userPage }) => {
    // Defaults to Download List tab
    await testPageVisualAndAccessibility(
      userPage,
      '/DownloadCart:0',
      'download-cart-page',
    )

    // Test button interactions
    const downloadCartButtons = ['Access Actions Required']

    for (const buttonName of downloadCartButtons) {
      const screenshotName = formatScreenshotName(
        'download-cart-page',
        `${buttonName}-clicked`,
      )

      await pageElementInteractions(userPage, buttonName, screenshotName, {
        selector: PAGE_SELECTOR,
      })
    }
  })

  testAuth('trash can page', async ({ userPage }) => {
    await testPageVisualAndAccessibility(userPage, '/Trash:0', 'trash-can-page')
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
      waitTime: LONGER_WAIT_TIME,
    })
    const searchInput = userPage.locator('.search-textbox')
    await searchInput.fill('test')
    await userPage.keyboard.press('Enter')
    await userPage.waitForTimeout(1000)
    await expect(userPage).toHaveScreenshot('search-page-with-query.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  })

  testAuth('help button', async ({ userPage }) => {
    await userPage.goto('/projects/all')
    await waitForInitialPageLoad(userPage)

    await userPage.getByRole('button', { name: 'Help' }).click()
    await userPage.waitForSelector('[role="button"]', { state: 'visible' })
    await userPage.waitForTimeout(500)

    await expect(userPage).toHaveScreenshot('help-sidebar.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  })

  testAuth('your account button', async ({ userPage }) => {
    const SIDEBAR_SELECTOR = '.linkList a'
    const YOUR_ACCOUNT_BUTTON = '[role="button"][aria-label="Your Account"]'

    await userPage.goto('/projects/all')
    await waitForInitialPageLoad(userPage)

    await userPage.getByRole('button', { name: 'Your Account' }).click()
    await userPage.waitForSelector('[role="button"]', { state: 'visible' })
    await userPage.waitForTimeout(500)

    // Hide dynamic username text to prevent false failures
    await userPage.addStyleTag({
      content: `
        /* Hide username text that varies between test runs */
        .header {
          visibility: hidden !important;
        }
      `,
    })

    await expect(userPage).toHaveScreenshot('your-account-sidebar.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })

    const accountSidebarLinks = ['View Profile', 'Access Requests', 'Following']

    for (const linkName of accountSidebarLinks) {
      const screenshotName = formatScreenshotName(
        'your-account-sidebar',
        `${linkName}-clicked`,
      )

      const sidebarLink = userPage
        .locator(SIDEBAR_SELECTOR)
        .filter({ hasText: linkName })

      if (await sidebarLink.isVisible({ timeout: 3000 })) {
        await sidebarLink.click()
        await userPage.waitForTimeout(500)
        await expect(userPage).toHaveScreenshot(screenshotName, {
          maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
        })

        // Reopen Your Account sidebar for next item
        const yourAccountBtn = userPage.locator(YOUR_ACCOUNT_BUTTON)
        if (await yourAccountBtn.isVisible({ timeout: 3000 })) {
          await yourAccountBtn.click()
          await userPage.waitForTimeout(500)
        }
      } else {
        console.log(`Sidebar link "${linkName}" not found`)
      }
    }
  })
})
