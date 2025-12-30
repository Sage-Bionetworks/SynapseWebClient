import { expect } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import { testAuth } from 'e2e/fixtures/authenticatedUserPages'
import { generateEntityName } from 'e2e/helpers/entities'
import { setupProjectWithPermissions } from 'e2e/helpers/setupTeardown'
import { Project } from 'e2e/helpers/types'
import {
  ColumnSchemaConfig,
  addColumnsFromColumnSchemaConfig,
} from 'e2e/helpers/tableColumnSchemaEditor'
import {
  openTableEditor,
  getTableEditorRows,
  enterTableValue,
} from 'e2e/helpers/tables'
import {
  getUserIdFromLocalStorage,
  toggleIntoExperimentalMode,
} from 'e2e/helpers/testUser'
import { waitForInitialPageLoad } from 'e2e/helpers/utils'
import {
  testPageVisualAndAccessibility,
  LONGER_WAIT_TIME,
  MAX_DIFF_PIXEL_RATIO,
  formatScreenshotName,
  pageElementInteractions,
  PROJECT_TAB_PAGE_SELECTOR,
  PROJECT_HEADER_SELECTOR,
  navigateToProjectTab,
  PAGE_SELECTOR,
  SHORTER_WAIT_TIME,
} from 'e2e/helpers/visualRegression'
import { defaultExpectTimeout } from 'playwright.config'

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
 *    pnpm playwright test authenticated-pages --update-snapshots
 *
 * 2. Compare after making changes:
 *    git checkout <your-feature-branch>
 *    pnpm dev
 *    pnpm playwright test authenticated-pages
 *
 * 3. View differences:
 *    pnpm e2e:report or pnpm exec playwright show-report
 *
 * NOTE: Accessibility snapshots track violations to ensure no NEW violations
 * are introduced by your changes. Existing violations are preserved.
 */

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
    // to fix
    const url = '/Home:x'
    await userPage.goto(url)
    await waitForInitialPageLoad(userPage)
    await userPage.waitForTimeout(LONGER_WAIT_TIME)

    //       .MuiBox-root h1:nth-of-type(2) {

    // print h1s on page

    await userPage.addStyleTag({
      content: `
        /* Hide the second h1 in the dashboard */
      .MuiBox-root h1:nth-of-type(2) {
        visibility: hidden !important;
      }
      `,
    })

    await testPageVisualAndAccessibility(
      userPage,
      url,
      'authenticated-homepage',
    )
  })

  testAuth('all projects page', async ({ userPage }) => {
    const userId = await getUserIdFromLocalStorage(userPage)

    // Navigate to the page and wait for it to load
    await userPage.goto(`/Profile:${userId}/projects/all`)
    await waitForInitialPageLoad(userPage)
    await userPage.waitForTimeout(LONGER_WAIT_TIME)

    // Wait for the specific element
    const selector = '#rootPanel .rootPanel .ProfileViewImpl'
    await userPage.waitForSelector(selector, { timeout: 10000 })

    // Hide dynamic project names to prevent false failures
    await userPage.addStyleTag({
      content: `
        /* Hide project names that vary between test runs */
        .gwt-Anchor {
          visibility: hidden !important;
        }

        /* Hide project links with data-project-id */
        a[data-project-id] {
          visibility: hidden !important;
        }

        /* Hide date spans in project list */
        .right.hidden-xs span {
          visibility: hidden !important;
        }

        /* Hide favorites icons in project lists - target the specific star link */
        a.favoriteIcon.textDecorationNone,
        a[aria-label*="Click the star to"] i.fa.syn-star,
        a[aria-label*="Click the star to"] i.fa.syn-star-o,
        .fa-star, .fa-star-o {
          visibility: hidden !important;
        }
      `,
    })

    // Wait for styles to be applied
    await userPage.waitForTimeout(SHORTER_WAIT_TIME)

    // Take screenshot after hiding dynamic content
    // Normalize project title and SynID for visual regression
    await userPage.evaluate(() => {
      // Normalize project title
      const title = document.querySelector('h3.pageHeaderTitle')
      if (title) title.textContent = 'PROJECT_TITLE'
      // Normalize SynID (span after 'Project SynID' label)
      const synIdLabel = Array.from(
        document.querySelectorAll('span.boldText'),
      ).find(el => el.textContent?.includes('Project SynID'))
      if (synIdLabel) {
        const synIdSpan = synIdLabel.parentElement?.querySelector(
          'span:not(.boldText):not(.HelpWidget)',
        )
        if (synIdSpan) synIdSpan.textContent = 'syn00000000'
      }
    })
    const element = userPage.locator(selector)
    await expect(element).toHaveScreenshot('all-projects-page.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })

    // Test button interactions - wait for buttons to be available
    const pageButtons = [
      'Created by me',
      'Favorites',
      'Shared directly with me',
      'Create a New Project',
    ]

    // Wait for at least one of the buttons to be available
    try {
      await userPage.waitForSelector('button', { timeout: 10000 })
      await userPage.waitForTimeout(1000) // Give extra time for all buttons to load
    } catch (e) {
      console.log(
        e,
        'No buttons found on projects page, skipping button interactions',
      )
    }

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
        selector: PROJECT_TAB_PAGE_SELECTOR,
      },
    )

    await userPage.addStyleTag({
      content: `
        /* Hide dynamic project title that varies between test runs */
        .projectTitle {
          visibility: hidden !important;
        }

      `,
    })

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

    await userPage.waitForSelector('a[href*="/wiki/"], a[href*="/files/"]', {
      timeout: 15000,
    })

    await userPage.waitForTimeout(1000)

    for (const tabName of projectTabs) {
      const screenshotName = `project-page-${tabName.toLowerCase()}-tab.png`

      // Try to find the tab link with more flexible timing
      try {
        await pageElementInteractions(userPage, tabName, screenshotName, {
          selector: PROJECT_TAB_PAGE_SELECTOR,
          elementType: 'link',
          exact: true,
          needsEscape: false,
        })
      } catch (error) {
        console.log(`Tab "${tabName}" interaction failed:`, error)
      }
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
        await navigateToProjectTab(userPage, userProject.id, 'wiki')

        await userPage.waitForTimeout(1500)

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PROJECT_TAB_PAGE_SELECTOR,
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

        await userPage.addStyleTag({
          content: `
            /* Hide breadcrumbs that contain dynamic usernames and timestamps */
            nav.MuiBreadcrumbs-root {
              visibility: hidden !important;
            }
          `,
        })

        // Wait for files tab to fully load and stabilize in its final state
        await userPage.waitForTimeout(LONGER_WAIT_TIME)

        // Wait for either the file table or empty message to appear
        try {
          await userPage.waitForSelector(
            'table, .gwt-Label:has-text("There is currently no content here")',
            { timeout: 3000 },
          )
        } catch {
          // Continue regardless of which state appears
        }

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PROJECT_TAB_PAGE_SELECTOR,
        })
      },
    )
  }

  // Test complete file upload workflow with screenshots
  testAuth('project files tab - complete file upload', async ({ userPage }) => {
    // Navigate to files tab
    await navigateToProjectTab(userPage, userProject.id, 'files')

    await userPage.addStyleTag({
      content: `
        /* Hide breadcrumbs that contain dynamic usernames and timestamps */
        nav.MuiBreadcrumbs-root {
          visibility: hidden !important;
        }
      `,
    })

    // Open file upload modal
    await userPage
      .getByRole('button', { name: 'Upload or Link to a File' })
      .click()

    await expect(
      userPage.getByRole('tab', { name: 'Upload File' }),
    ).toBeVisible()

    // Wait for modal content to fully load
    await expect(
      userPage.getByText('Click to upload or drag and drop'),
    ).toBeVisible()
    await userPage.waitForTimeout(SHORTER_WAIT_TIME)

    await expect(userPage.locator(PROJECT_TAB_PAGE_SELECTOR)).toHaveScreenshot(
      'project-page-files-tab-upload-modal.png',
    )

    // Select file to upload
    await expect(
      userPage.getByText(
        /All uploaded files will be stored in Synapse storage/i,
      ),
    ).toBeVisible()

    const fileChooserPromise = userPage.waitForEvent('filechooser')
    await userPage.getByText('Click to upload').click()
    await userPage
      .getByRole('menu')
      .getByRole('menuitem')
      .filter({ hasText: 'Files' })
      .click()

    const fileChooser = await fileChooserPromise
    const filePath = 'data/test_file.csv'
    await fileChooser.setFiles(`${import.meta.dirname}/../${filePath}`)

    // File selected, ready to upload
    await userPage.waitForTimeout(SHORTER_WAIT_TIME)
    await expect(userPage.locator(PROJECT_TAB_PAGE_SELECTOR)).toHaveScreenshot(
      'project-page-files-tab-uploading-file.png',
    )

    // Wait for upload completion
    await expect(userPage.getByText('Uploaded 1 Item')).toBeVisible()
    await userPage.waitForTimeout(SHORTER_WAIT_TIME)
    await expect(userPage.locator(PROJECT_TAB_PAGE_SELECTOR)).toHaveScreenshot(
      'project-page-files-tab-upload-complete.png',
    )

    // Close modal
    await userPage.getByRole('button', { name: 'Finish' }).click()
    await expect(
      userPage.getByRole('heading', { name: 'Upload or Link to File' }),
    ).not.toBeVisible()

    // View uploaded file in file list
    const fileLink = userPage.getByRole('link', { name: 'test_file.csv' })
    await expect(fileLink).toBeVisible({
      timeout: defaultExpectTimeout * 3,
    })

    await userPage.addStyleTag({
      content: `
        p.MuiTypography-body1 {
          visibility: hidden !important;
        }

        /* Hide Modified On timestamp columns (4th td in file table rows) */
        tr td:nth-child(4) {
          visibility: hidden !important;
        }
      `,
    })

    await userPage.waitForTimeout(SHORTER_WAIT_TIME)

    await expect(userPage.locator(PROJECT_TAB_PAGE_SELECTOR)).toHaveScreenshot(
      'project-page-files-tab-file-in-list.png',
    )

    // Open file details page
    await fileLink.click()
    await userPage.waitForTimeout(LONGER_WAIT_TIME)
    await expect(
      userPage.getByText('Discussion about test_file.csv'),
    ).toBeVisible()
    await expect(userPage.getByText('Loading provenance...')).not.toBeVisible()

    // Normalize dynamic syn ID in file details page
    await userPage.evaluate(() => {
      const spans = document.querySelectorAll('span.MuiTypography-smallText1')
      spans.forEach(span => {
        if (
          span.textContent?.startsWith('syn') &&
          span.textContent?.match(/^syn\d+$/)
        ) {
          span.textContent = 'syn00000000'
        }
      })
    })

    await expect(userPage.locator(PROJECT_TAB_PAGE_SELECTOR)).toHaveScreenshot(
      'project-page-file-details.png',
    )
  })

  testAuth('project datasets tab actions', async ({ userPage }) => {
    await navigateToProjectTab(userPage, userProject.id, 'datasets')

    await pageElementInteractions(
      userPage,
      'Add New...',
      'project-page-datasets-tab-add-new.png',
      {
        selector: PROJECT_TAB_PAGE_SELECTOR,
        needsEscape: false,
      },
    )

    await userPage.waitForTimeout(SHORTER_WAIT_TIME)

    const datasetsDropdownActions = ['Add Dataset', 'Add Dataset Collection']

    for (const actionName of datasetsDropdownActions) {
      const screenshotName = `project-page-datasets-tab-${actionName.toLowerCase().replace(/\s+/g, '-')}.png`

      await pageElementInteractions(userPage, actionName, screenshotName, {
        selector: PROJECT_TAB_PAGE_SELECTOR,
        elementType: 'menuitem',
        exact: true,
        needsEscape: true,
      })

      await userPage.getByRole('button', { name: 'Add New...' }).click()
      await userPage.waitForTimeout(LONGER_WAIT_TIME)
    }
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
          selector: PROJECT_TAB_PAGE_SELECTOR,
        })
      },
    )
  }

  // Test complete table creation workflow with screenshots
  testAuth(
    'project tables tab - complete table creation',
    async ({ userPage }) => {
      await navigateToProjectTab(userPage, userProject.id, 'tables')

      // Toggle into experimental mode for React-based table column schema editor
      await toggleIntoExperimentalMode(userPage)
      await navigateToProjectTab(userPage, userProject.id, 'tables')

      // Hide dynamic content in tables list view (Modified On, Created On, syn ID)
      await userPage.evaluate(() => {
        // Hide Modified On and Created On timestamp columns (date format)
        const allTdCells = document.querySelectorAll('td')
        allTdCells.forEach(cell => {
          if (cell.textContent?.match(/\d{1,2}\/\d{1,2}\/\d{4}/)) {
            ;(cell as HTMLElement).style.visibility = 'hidden'
          }
        })

        // Hide syn ID values in the ID column
        const allElements = document.querySelectorAll('*')
        allElements.forEach(element => {
          if (
            element.textContent?.startsWith('syn') &&
            element.textContent?.match(/^syn\d+$/)
          ) {
            ;(element as HTMLElement).style.visibility = 'hidden'
          }
        })
      })

      const tableName = generateEntityName('table')
      const tableDescription = 'Visual regression test table'

      const columnsSchemaConfig: ColumnSchemaConfig[] = [
        { name: 'item', type: 'String', size: 20 },
        { name: 'quantity', type: 'Integer', defaultValue: '100' },
        { name: 'in_stock', type: 'Boolean', defaultValue: 'true' },
        { name: 'date_added', type: 'Date' },
      ]

      // Open table creation modal
      await userPage.getByRole('button', { name: 'Add Table or View' }).click()
      const dialog = userPage.getByRole('dialog')
      await expect(dialog).toBeVisible()
      await userPage.waitForTimeout(SHORTER_WAIT_TIME)

      await expect(dialog).toHaveScreenshot(
        'project-page-tables-tab-create-modal.png',
      )

      // Select "Table" option
      await dialog.getByRole('menuitem', { name: 'Table' }).click()
      await userPage.waitForTimeout(SHORTER_WAIT_TIME)

      await expect(dialog).toHaveScreenshot(
        'project-page-tables-tab-schema-editor-empty.png',
      )

      // Add columns to schema
      await addColumnsFromColumnSchemaConfig(userPage, columnsSchemaConfig)
      await userPage.waitForTimeout(SHORTER_WAIT_TIME)

      await expect(dialog).toHaveScreenshot(
        'project-page-tables-tab-schema-editor-with-columns.png',
      )

      // Table name and description step
      await userPage.getByRole('button', { name: 'Next' }).click()
      await userPage.waitForTimeout(SHORTER_WAIT_TIME)

      await expect(dialog).toHaveScreenshot(
        'project-page-tables-tab-name-description-step.png',
      )

      await userPage.getByLabel('Name').fill(tableName)
      await userPage.getByLabel('Description').fill(tableDescription)
      await userPage.waitForTimeout(SHORTER_WAIT_TIME)

      // Create table
      await userPage.getByRole('button', { name: 'Finish' }).click()

      // Wait for table page to load
      await expect(
        userPage.getByRole('heading', { name: tableName }),
      ).toBeVisible({ timeout: defaultExpectTimeout * 3 })
      await expect(
        userPage.getByText('Loading provenance...'),
      ).not.toBeVisible()
      await userPage.waitForTimeout(LONGER_WAIT_TIME)

      await testAuth.step('table is currently empty', async () => {
        await expect(
          userPage.getByRole('heading', { name: 'Items (0)' }),
        ).toBeVisible()
        await expect(
          userPage.getByText('This table is currently empty'),
        ).toBeVisible()
      })

      await userPage.addStyleTag({
        content: `
          /* Hide table name that varies between test runs */
          .MuiTypography-headline2 {
            visibility: hidden !important;
          }

          /* Hide breadcrumbs with dynamic usernames and timestamps */
          nav.MuiBreadcrumbs-root {
            visibility: hidden !important;
          }

          /* Hide favorite button to prevent flakiness */
          button[aria-label="Add to Favorites"],
          span[aria-label="Add to Favorites"] {
            visibility: hidden !important;
          }
        `,
      })

      // Hide dynamic SynID values
      await userPage.evaluate(() => {
        const spans = document.querySelectorAll('span.MuiTypography-smallText1')
        spans.forEach(span => {
          if (span.textContent?.startsWith('syn')) {
            ;(span as HTMLElement).style.visibility = 'hidden'
          }
        })
      })

      // Wait for styles to be applied and page to re-render
      await userPage.waitForTimeout(1000)

      await expect(
        userPage.locator(PROJECT_TAB_PAGE_SELECTOR),
      ).toHaveScreenshot('project-page-table-created-empty.png', {
        maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
      })

      // Wait for edit button to be ready and visible
      const editButton = userPage.getByRole('button', {
        name: 'Bulk Edit Table Cell Values',
      })
      await expect(editButton).toBeVisible({ timeout: defaultExpectTimeout })
      await userPage.waitForTimeout(1000)

      await userPage.addStyleTag({
        content: `
          /* Hide last updated timestamp that varies between test runs */
          .flexcontainer-column.flexcontainer-align-items-flex-end span.em {
            visibility: hidden !important;
          }
        `,
      })

      await openTableEditor(userPage)

      await expect(
        userPage.locator(PROJECT_TAB_PAGE_SELECTOR),
      ).toHaveScreenshot('project-page-table-editor-opened.png', {
        maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
      })

      await userPage.getByRole('button', { name: 'Add Row' }).click()

      const tableRows = getTableEditorRows(userPage)
      const firstDataRow = tableRows.nth(1) // Skip header row

      const dateCell = firstDataRow.getByRole('cell').nth(4)
      await dateCell.click()
      await userPage.waitForTimeout(SHORTER_WAIT_TIME)

      await enterTableValue(dateCell, '2024-01-15')
      await userPage.waitForTimeout(SHORTER_WAIT_TIME)

      const editorDialog = userPage
        .locator('#editRowsModal .modal-dialog')
        .first()

      await expect(editorDialog).toHaveScreenshot(
        'project-page-table-date-entered.png',
        {
          maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
        },
      )
    },
  )

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

        // Wait for discussion tab content to be fully loaded
        try {
          await userPage.waitForSelector(
            'button:has-text("New Thread"), button:has-text("Discussion Tools")',
            {
              timeout: 10000,
            },
          )
          await userPage.waitForTimeout(1000)
        } catch {
          console.log('Discussion tab buttons not found, continuing with test')
        }

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PROJECT_TAB_PAGE_SELECTOR,
        })
      },
    )
  }

  // Test Discussion Tools dropdown items
  testAuth('project discussion tab dropdown actions', async ({ userPage }) => {
    await navigateToProjectTab(userPage, userProject.id, 'discussion')

    // Wait for discussion tab content to be fully loaded
    try {
      await userPage.waitForSelector('button:has-text("Discussion Tools")', {
        timeout: 10000,
      })
      await userPage.waitForTimeout(1000)
    } catch {
      console.log('Discussion Tools button not found, skipping dropdown test')
      return
    }

    // Click Discussion Tools button to open dropdown
    await pageElementInteractions(
      userPage,
      'Discussion Tools',
      'project-page-discussion-tab-discussion-tools-clicked.png',
      {
        selector: PROJECT_TAB_PAGE_SELECTOR,
        needsEscape: false,
      },
    )

    await userPage.waitForSelector(
      'role=menuitem[name="Show Deleted Threads"]',
      { timeout: 5000 },
    )

    await pageElementInteractions(
      userPage,
      'Show Deleted Threads',
      'project-page-discussion-tab-show-deleted-threads.png',
      {
        selector: PAGE_SELECTOR,
      },
    )

    try {
      await userPage.waitForSelector('text="Deleted Threads"', {
        timeout: 3000,
      })
    } catch {
      // If "Deleted Threads" text doesn't appear, just continue
      await userPage.waitForTimeout(1000)
    }
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
        await navigateToProjectTab(userPage, userProject.id, 'docker')

        await pageElementInteractions(userPage, actionName, screenshotName, {
          selector: PROJECT_TAB_PAGE_SELECTOR,
        })
      },
    )
  }

  testAuth.afterAll(async ({ browser }) => {
    if (userProject?.id) {
      const { teardownProjectsAndFileHandles } = await import(
        '../helpers/setupTeardown'
      )
      await teardownProjectsAndFileHandles(browser, [userProject], [])
    }
  })

  testAuth('favorites page', async ({ userPage }) => {
    const userId = await getUserIdFromLocalStorage(userPage)

    await userPage.goto(`/Profile:${userId}/favorites`)
    await waitForInitialPageLoad(userPage)
    await userPage.waitForTimeout(LONGER_WAIT_TIME)

    await userPage.addStyleTag({
      content: `
        /* Hide favorites icons in project lists */
        button[aria-label="Add to Favorites"],
        span[aria-label="Add to Favorites"],
        .MuiIconButton-root[aria-label*="favorite"],
        .fa-star, .fa-star-o {
          visibility: hidden !important;
        }

        /* Hide dynamic project names and links */
        a[data-project-id],
        .gwt-Anchor {
          visibility: hidden !important;
        }

        /* Hide dynamic dates and timestamps */
        .right.hidden-xs span,
        .SRC-cardContent .SRC-cardText.SRC-cardDescription {
          visibility: hidden !important;
        }
      `,
    })

    await expect(userPage).toHaveScreenshot('favorites-page.png', {
      fullPage: true,
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
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

    // Fill in team name
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
      {
        needsEscape: false,
      },
    )

    const dropdownActions = [
      'Invite User',
      'Edit Team',
      'Delete Team',
      'Leave Team',
    ]

    for (const actionName of dropdownActions) {
      const screenshotName = `teams-page-${actionName.toLowerCase().replace(/\s+/g, '-')}.png`

      await pageElementInteractions(userPage, actionName, screenshotName, {
        elementType: 'link',
        needsEscape: true,
      })

      await userPage.getByRole('button', { name: 'Team Actions' }).click()
      await userPage.waitForTimeout(LONGER_WAIT_TIME)
    }
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
    await userPage.goto('/DownloadCart:0')
    await waitForInitialPageLoad(userPage)

    await userPage.addStyleTag({
      content: '.fileCount { visibility: hidden !important; }',
    })

    // to fix

    await testPageVisualAndAccessibility(
      userPage,
      '/DownloadCart:0',
      'download-cart-page',
      {
        selector: PAGE_SELECTOR,
      },
    )

    const downloadListScreenhotName = formatScreenshotName(
      'download-cart-page',
      'download-list-clicked',
    )

    const downloadListButton = userPage.locator(
      'button:has-text("Download List")',
    )

    await downloadListButton.click()
    await userPage.waitForTimeout(SHORTER_WAIT_TIME)

    await userPage.addStyleTag({
      content: '.fileCount { visibility: hidden !important; }',
    })

    await expect(userPage).toHaveScreenshot(downloadListScreenhotName, {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  })

  testAuth('trash can page', async ({ userPage }) => {
    await testPageVisualAndAccessibility(
      userPage,
      '/Trash:0',
      'trash-can-page',
      { selector: PAGE_SELECTOR, waitTime: LONGER_WAIT_TIME },
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
      waitTime: LONGER_WAIT_TIME,
      selector: PAGE_SELECTOR,
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
    await userPage.waitForTimeout(SHORTER_WAIT_TIME)

    await expect(userPage).toHaveScreenshot('help-sidebar.png', {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  })

  testAuth('your account button', async ({ userPage }) => {
    await userPage.goto('/projects/all')
    await waitForInitialPageLoad(userPage)

    await userPage.getByRole('button', { name: 'Your Account' }).click()
    await userPage.waitForSelector('[role="button"]', { state: 'visible' })
    await userPage.waitForTimeout(SHORTER_WAIT_TIME)

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

      await userPage.goto('/projects/all')
      await waitForInitialPageLoad(userPage)

      await userPage.getByRole('button', { name: 'Your Account' }).click()
      await userPage.waitForTimeout(LONGER_WAIT_TIME)

      await pageElementInteractions(userPage, linkName, screenshotName, {
        elementType: 'link',
        exact: true,
      })
    }
  })
})
