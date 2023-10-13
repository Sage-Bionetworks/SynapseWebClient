import { Page, expect, test } from '@playwright/test'
import { testAuth } from './fixtures/authenticatedUserPages'
import {
  createFile,
  createProject,
  deleteFileHandleWithRetry,
  deleteProject,
  entityUrlPathname,
  generateEntityName,
} from './helpers/entities'
import { getAccessTokenFromCookie, getAdminPAT } from './helpers/testUser'
import { Project } from './helpers/types'
import { userConfigs } from './helpers/userConfig'
import { waitForInitialPageLoad } from './helpers/utils'

const expectFilePageLoaded = async (
  fileName: string,
  fileEntityId: string,
  page: Page,
) => {
  await page.waitForURL(entityUrlPathname(fileEntityId))
  await expect(page.getByText(`Discussion about ${fileName}`)).toBeVisible()

  await expect(page.getByText('Loading provenance...')).not.toBeVisible()
  await expect(page.getByText(fileEntityId, { exact: true })).toBeVisible()
}

const openFileSharingSettings = async (page: Page) => {
  await testAuth.step('First user can open File Sharing Settings', async () => {
    await page.getByRole('button', { name: 'File Tools' }).click()
    await page.getByRole('menuitem', { name: 'File Sharing Settings' }).click()
    await expect(
      page.getByRole('heading', { name: 'File Sharing Settings' }),
    ).toBeVisible()
  })
}

const expectNoAccessPage = async (page: Page) => {
  await expect(
    page.getByRole('heading', {
      name: 'Sorry, no access to this page.',
    }),
  ).toBeVisible()
  await expect(
    page.getByText('You are not authorized to access the page requested.'),
  ).toBeVisible()
}

const confirmSharingSettings = async (
  firstUserPage: Page,
  firstUserName: string,
  secondUserName: string,
  secondUserVisible: boolean,
) => {
  await testAuth.step(
    'First user is listed in File Sharing settings',
    async () => {
      await expect(
        firstUserPage.getByRole('cell', {
          name: firstUserName,
        }),
      ).toBeVisible()
    },
  )

  await testAuth.step(
    `Second user is ${
      secondUserVisible ? '' : 'not'
    } listed in File Sharing settings`,
    async () => {
      const secondUserCell = firstUserPage.getByRole('cell', {
        name: secondUserName,
      })
      if (secondUserVisible) {
        await expect(secondUserCell).toBeVisible()
      } else {
        await expect(secondUserCell).not.toBeVisible()
      }
    },
  )
}

const confirmAndClosePermissionsSavedAlert = async (page: Page) => {
  const permissionsAlert = page.getByRole('alert').filter({
    has: page.getByText('Permissions were successfully saved to Synapse'),
  })
  await expect(permissionsAlert).toBeVisible()
  await permissionsAlert.getByRole('button').click()
  await expect(permissionsAlert).not.toBeVisible()
}

const saveFileSharingSettings = async (page: Page) => {
  const saveButton = page.getByRole('button', { name: 'Save' })
  await saveButton.click()
  await expect(saveButton).not.toBeVisible()

  // Modal is closed after settings are saved
  await expect(
    page.getByRole('heading', { name: 'File Sharing Settings' }),
  ).not.toBeVisible()

  await confirmAndClosePermissionsSavedAlert(page)
}

let userProject: Project
let fileHandleIds: string[] = []

test.describe('Files', () => {
  testAuth.beforeAll(async ({ browser, storageStatePaths }) => {
    const userContext = await browser.newContext({
      storageState: storageStatePaths['swc-e2e-user'],
    })
    const userPage = await userContext.newPage()
    const userAccessToken = await getAccessTokenFromCookie(userPage)

    // create project
    const userProjectName = generateEntityName('project')
    const userProjectId = await createProject(
      userProjectName,
      userAccessToken,
      userPage,
    )
    userProject = { name: userProjectName, id: userProjectId }

    await userContext.close()
  })

  testAuth.afterAll(async ({ browser }) => {
    const context = await browser.newContext()
    const page = await context.newPage()
    const accessToken = getAdminPAT()

    // delete project
    if (userProject.id) {
      await deleteProject(userProject.id, accessToken, page)
    }

    // delete fileHandles
    for await (const fileHandleId of fileHandleIds) {
      await deleteFileHandleWithRetry(accessToken, fileHandleId, page)
    }

    await context.close()
  })

  testAuth(
    'File sharing',
    async ({ userPage, validatedUserPage, browserName }, testInfo) => {
      if (browserName === 'webkit') {
        test.info().annotations.push({
          type: 'very slow',
          description: `webkit in CI only. May be related to the InviteWidget. 
            Evaluate if timeout can be removed after addressing 
            https://sagebionetworks.jira.com/browse/SWC-6569.`,
        })
        test.setTimeout(testInfo.timeout * 5)
      }

      const userName = userConfigs['swc-e2e-user'].username
      const validatedUserName = userConfigs['swc-e2e-user-validated'].username

      const { fileEntityId, fileName } = await testAuth.step(
        'Create a file',
        async () => {
          const userAccessToken = await getAccessTokenFromCookie(userPage)
          const fileName = `${generateEntityName('file')}.txt`
          const { fileEntityId, fileHandleId } = await createFile(
            fileName,
            'Created for SWC e2e testing.',
            'text/txt',
            userProject.id,
            userAccessToken,
            userPage,
          )
          fileHandleIds.push(fileHandleId)
          return { fileEntityId, fileName }
        },
      )

      await testAuth.step('First user can view a private file', async () => {
        await userPage.goto(`${entityUrlPathname(userProject.id)}/files`)
        await waitForInitialPageLoad(userPage)

        const fileLink = userPage.getByRole('link', { name: fileName })
        await expect(fileLink).toBeVisible()

        await fileLink.click()
        await expectFilePageLoaded(fileName, fileEntityId, userPage)
      })

      await openFileSharingSettings(userPage)
      await confirmSharingSettings(userPage, userName, validatedUserName, false)

      await testAuth.step('Second user cannot access the file', async () => {
        await validatedUserPage.goto(entityUrlPathname(fileEntityId))
        await waitForInitialPageLoad(validatedUserPage)

        await expectNoAccessPage(validatedUserPage)
      })

      await testAuth.step(
        'First user enables Local Sharing Settings for the File',
        async () => {
          await userPage
            .getByRole('button', { name: 'Create Local Sharing Settings' })
            .click()
          await expect(
            userPage.getByRole('heading', { name: 'Add More People' }),
          ).toBeVisible()
        },
      )

      await testAuth.step(
        'First user shares the file with second user',
        async () => {
          const userSuggestBox = userPage.getByRole('textbox', {
            name: 'Enter name...',
          })
          await userSuggestBox.fill(validatedUserName)
          await userSuggestBox.press('Shift')

          await userPage
            .getByRole('menuitem', { name: `@${validatedUserName}` })
            .click({ timeout: testInfo.timeout * 3 })

          await expect(
            userPage.getByRole('cell', { name: validatedUserName }),
          ).toBeVisible({ timeout: testInfo.timeout * 3 })

          // Don't send message, so we don't have to clean up the message
          await userPage.getByText('Notify people via email').click()

          await saveFileSharingSettings(userPage)
        },
      )

      await openFileSharingSettings(userPage)
      await confirmSharingSettings(userPage, userName, validatedUserName, true)

      await testAuth.step('Second user accesses the file', async () => {
        await validatedUserPage.reload()
        await expectFilePageLoaded(fileName, fileEntityId, validatedUserPage)
      })

      await testAuth.step('Second user downloads the file', async () => {
        await validatedUserPage
          .getByRole('button', { name: 'Download Options' })
          .click()
        const downloadPromise = validatedUserPage.waitForEvent('download')
        await validatedUserPage
          .getByRole('menuitem', { name: 'Download File' })
          .click()
        const download = await downloadPromise
        expect(download.suggestedFilename()).toBe(fileName)
      })

      await testAuth.step(
        'First user removes local sharing settings',
        async () => {
          await userPage
            .getByRole('button', { name: 'Delete Local Sharing Settings' })
            .click()

          await expect(
            userPage.getByRole('button', {
              name: 'Create Local Sharing Settings',
            }),
          ).toBeVisible({ timeout: testInfo.timeout * 3 })

          await saveFileSharingSettings(userPage)
        },
      )

      await openFileSharingSettings(userPage)
      await confirmSharingSettings(userPage, userName, validatedUserName, false)

      await testAuth.step('Second user cannot access the file', async () => {
        await validatedUserPage.reload()
        await expectNoAccessPage(validatedUserPage)
      })
    },
  )
})
