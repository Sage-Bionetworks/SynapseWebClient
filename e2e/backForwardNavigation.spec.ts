import { Page, expect } from '@playwright/test'
import { testAuth } from './fixtures/authenticatedUserPages'
import { generateEntityName } from './helpers/entities'
import { BackendDestinationEnum, doPost } from './helpers/http'
import {
  setupProject,
  teardownProjectsAndFileHandles,
} from './helpers/setupTeardown'
import { getAccessTokenFromCookie } from './helpers/testUser'
import { Project } from './helpers/types'
import { waitForInitialPageLoad } from './helpers/utils'

// SWC-6153: browser Back/Forward must show the page for the URL, without reloading the app

const rootWikiText = 'Root wiki page for back/forward navigation'
const subpageTitle = 'Back forward subpage'
const subpageText = 'Wiki subpage for back/forward navigation'

let userProject: Project
let folderName: string
let folderId: string
let subpageId: string

// Set on the window once the app has loaded. A full page load clears it.
const setNoReloadMarker = async (page: Page) => {
  await page.evaluate(() => {
    ;(window as unknown as Record<string, unknown>).swcBackForwardMarker = true
  })
}

const expectNoReload = async (page: Page) => {
  expect(
    await page.evaluate(
      () => (window as unknown as Record<string, unknown>).swcBackForwardMarker,
    ),
  ).toBe(true)
}

const goBack = async (page: Page) => {
  await page.evaluate(() => window.history.back())
}

const goForward = async (page: Page) => {
  await page.evaluate(() => window.history.forward())
}

const expectWikiTab = async (page: Page) => {
  await page.waitForURL(`/Synapse:${userProject.id}/wiki/**`)
  await expect(page.getByText(rootWikiText)).toBeVisible()
  await expect(page).toHaveTitle(/ - Wiki$/)
}

const expectFilesTab = async (page: Page) => {
  await page.waitForURL(`/Synapse:${userProject.id}/files/`)
  await expect(page.getByRole('link', { name: folderName })).toBeVisible()
  await expect(page).toHaveTitle(/ - Files$/)
}

const expectFolderPage = async (page: Page) => {
  await page.waitForURL(`/Synapse:${folderId}`)
  await expect(page).toHaveTitle(new RegExp(`^${folderName} - ${folderId}`))
}

const expectSubpage = async (page: Page) => {
  await page.waitForURL(`/Synapse:${userProject.id}/wiki/${subpageId}`)
  await expect(page.getByText(subpageText)).toBeVisible()
  await expect(page.getByText(rootWikiText)).not.toBeVisible()
}

testAuth.describe('Back/Forward navigation', () => {
  testAuth.beforeAll(async ({ browser, storageStatePaths }) => {
    userProject = await setupProject(browser, 'swc-e2e-user', storageStatePaths)

    const context = await browser.newContext({
      storageState: storageStatePaths['swc-e2e-user'],
    })
    const page = await context.newPage()
    const accessToken = await getAccessTokenFromCookie(page)
    folderName = generateEntityName('folder')
    const folder = await doPost<{ id: string }>(
      page,
      '/repo/v1/entity',
      {
        name: folderName,
        parentId: userProject.id,
        concreteType: 'org.sagebionetworks.repo.model.Folder',
      },
      accessToken,
      BackendDestinationEnum.REPO_ENDPOINT,
    )
    folderId = folder.id
    const rootWiki = await doPost<{ id: string }>(
      page,
      `/repo/v1/entity/${userProject.id}/wiki2`,
      { title: userProject.name, markdown: rootWikiText },
      accessToken,
      BackendDestinationEnum.REPO_ENDPOINT,
    )
    const subpage = await doPost<{ id: string }>(
      page,
      `/repo/v1/entity/${userProject.id}/wiki2`,
      { title: subpageTitle, markdown: subpageText, parentWikiId: rootWiki.id },
      accessToken,
      BackendDestinationEnum.REPO_ENDPOINT,
    )
    subpageId = subpage.id
    await context.close()
  })

  testAuth.afterAll(async ({ browser }) => {
    testAuth.slow()
    if (userProject?.id) {
      await teardownProjectsAndFileHandles(browser, [userProject], [])
    }
  })

  testAuth('entity page tabs and folders', async ({ userPage: page }) => {
    await testAuth.step('open the project wiki', async () => {
      await page.goto(`/Synapse:${userProject.id}/wiki/`)
      await waitForInitialPageLoad(page)
      await expectWikiTab(page)
      await setNoReloadMarker(page)
    })

    await testAuth.step('open the Files tab, then a folder', async () => {
      await page
        .locator(`a[href="/Synapse:${userProject.id}/files/"]`)
        .first()
        .click()
      await expectFilesTab(page)
      await page.getByRole('link', { name: folderName }).click()
      await expectFolderPage(page)
    })

    await testAuth.step(
      'Back to the Files tab, then the Wiki tab',
      async () => {
        await goBack(page)
        await expectFilesTab(page)
        await goBack(page)
        await expectWikiTab(page)
        await expectNoReload(page)
      },
    )

    await testAuth.step(
      'Forward to the Files tab, then the folder',
      async () => {
        await goForward(page)
        await expectFilesTab(page)
        await goForward(page)
        await expectFolderPage(page)
        await expectNoReload(page)
      },
    )
  })

  testAuth('wiki subpages', async ({ userPage: page }) => {
    await testAuth.step('open the project wiki', async () => {
      await page.goto(`/Synapse:${userProject.id}/wiki/`)
      await waitForInitialPageLoad(page)
      await expectWikiTab(page)
      await setNoReloadMarker(page)
    })

    await testAuth.step('open the subpage', async () => {
      await page.getByRole('link', { name: subpageTitle }).click()
      await expectSubpage(page)
    })

    await testAuth.step(
      'Back to the root page, Forward to the subpage',
      async () => {
        await goBack(page)
        await expectWikiTab(page)
        await expect(page.getByText(subpageText)).not.toBeVisible()
        await goForward(page)
        await expectSubpage(page)
        await expectNoReload(page)
      },
    )

    await testAuth.step('Back and Forward again', async () => {
      // Showing the root page again must not have dropped the Forward entry
      await goBack(page)
      await expectWikiTab(page)
      await goForward(page)
      await expectSubpage(page)
      await expectNoReload(page)
    })
  })
})
