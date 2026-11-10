import { Page, expect } from '@playwright/test'
import path from 'path'
import { defaultExpectTimeout } from '../playwright.config'
import { testAuth } from './fixtures/authenticatedUserPages'
import {
  createFile,
  entityUrlPathname,
  generateEntityName,
  getEntityFileHandleId,
  getEntityIdFromPathname,
} from './helpers/entities'
import {
  setupProject,
  teardownProjectsAndFileHandles,
} from './helpers/setupTeardown'
import {
  dismissAlert,
  getAccessTokenFromCookie,
  getAdminPAT,
  goToDashboardPage,
  reloadDashboardPage,
} from './helpers/testUser'
import { Project } from './helpers/types'
import { userConfigs } from './helpers/userConfig'

const expectFilePageLoaded = async (
  fileName: string,
  fileEntityId: string,
  page: Page,
) => {
  await testAuth.step('file page is loaded', async () => {
    await page.waitForURL(entityUrlPathname(fileEntityId))
    await expect(page.getByText(`Discussion about ${fileName}`)).toBeVisible()
    await expect(
      page.getByRole('heading', { level: 2, name: fileName }),
    ).toBeVisible()

    await expect(page.getByText('Loading provenance...')).not.toBeVisible()
    // SynID is shown in the title bar and the "About this File" sidebar;
    // hidden copies may remain in previously visited (cached) views
    await expect(
      page.getByText(fileEntityId, { exact: true }).filter({ visible: true }),
    ).not.toHaveCount(0)
  })
}

const openFileSharingSettings = async (page: Page) => {
  await testAuth.step('First user can open File Sharing Settings', async () => {
    await page.getByRole('button', { name: 'Tools', exact: true }).click()
    await page.getByRole('menuitem', { name: 'File Sharing Settings' }).click()
    const heading = page.getByRole('heading', { name: 'File Sharing Settings' })
    await expect(heading).toBeVisible()
    // The dialog can open under the pointer (left at the menu item position);
    // hovering a principal link opens a user card that covers the dialog buttons
    await heading.hover()
  })
}

const expectNoAccessPage = async (
  page: Page,
  expectTimeout: number = defaultExpectTimeout,
) => {
  await expect(
    page.getByText('You don’t have permission to view this.'),
  ).toBeVisible({ timeout: expectTimeout })
  await expect(
    page.getByText(
      'This account has not been granted access to view this resource.',
    ),
  ).toBeVisible({ timeout: expectTimeout })
}

// Each principal in the File Sharing Settings ACL editor is listed as a row
const getSharingSettingsPrincipalRow = (page: Page, userName: string) =>
  page
    .getByRole('dialog', { name: 'File Sharing Settings' })
    .getByRole('row')
    .filter({ hasText: `@${userName}` })

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
        getSharingSettingsPrincipalRow(firstUserPage, firstUserName),
      ).toBeVisible()
    },
  )

  await testAuth.step(
    `Second user is ${
      secondUserVisible ? '' : 'not'
    } listed in File Sharing settings`,
    async () => {
      const secondUserRow = getSharingSettingsPrincipalRow(
        firstUserPage,
        secondUserName,
      )
      if (secondUserVisible) {
        await expect(secondUserRow).toBeVisible()
      } else {
        await expect(secondUserRow).not.toBeVisible()
      }
    },
  )
}

const saveFileSharingSettings = async (page: Page) => {
  const saveButton = page.getByRole('button', { name: 'Save' })
  await saveButton.click()
  await expect(saveButton).not.toBeVisible()

  // Modal is closed after settings are saved
  await expect(
    page.getByRole('heading', { name: 'File Sharing Settings' }),
  ).not.toBeVisible()

  await dismissAlert(page, 'Permissions were successfully saved to Synapse')
}

const uploadFile = async (
  page: Page,
  filePath: string,
  uploadType: 'initialUpload' | 'newVersion',
) => {
  await testAuth.step('open file upload modal', async () => {
    const uploadButtonText =
      uploadType === 'initialUpload' ? 'Upload/Link File' : 'Upload'
    await page
      .getByRole('button', { name: uploadButtonText, exact: true })
      .click()

    await expect(page.getByRole('tab', { name: 'Upload File' })).toBeVisible()
    await expect(page.getByRole('tab', { name: 'Link to URL' })).toBeVisible()
    await expect(page.getByText('Click to upload')).toBeVisible()
  })

  await testAuth.step('choose file', async () => {
    // Ensure that upload type is specified before attempting upload
    // to prevent upload error: "Unsupported external upload type specified: undefined"
    await expect(
      page.getByText(/All uploaded files will be stored in Synapse storage/i),
    ).toBeVisible()

    const fileChooserPromise = page.waitForEvent('filechooser')
    await page.getByText('Click to upload').click()
    if (uploadType === 'initialUpload') {
      await page
        .getByRole('menu')
        .getByRole('menuitem')
        .filter({ hasText: 'Files' })
        .click()
    }
    const fileChooser = await fileChooserPromise
    await fileChooser.setFiles(path.join(import.meta.dirname, filePath))
  })

  await testAuth.step(
    'wait for file upload to finish and close modal',
    async () => {
      await expect(page.getByText('Uploaded 1 Item')).toBeVisible()
      await page.getByRole('button', { name: 'Finish' }).click()
      await expect(
        page.getByRole('heading', { name: 'Upload or Link to File' }),
      ).not.toBeVisible()
    },
  )

  await testAuth.step('ensure there was not an upload error', async () => {
    const uploadError = page.getByRole('heading', { name: 'Upload Error' })
    if (await uploadError.isVisible()) {
      const uploadErrorText = await page.getByRole('dialog').textContent()
      throw new Error(`Upload Error: ${uploadErrorText}`)
    }
  })
}

// MD5 is listed in the "About this File" sidebar on the file page
const getFileMD5Locator = (page: Page) =>
  page
    .getByText('MD5', { exact: true })
    .filter({ visible: true })
    .locator('xpath=following-sibling::*[1]')

const getFileMD5 = async (page: Page) => {
  return await testAuth.step('get file MD5', async () => {
    const md5 = getFileMD5Locator(page)
    await expect(md5).toHaveText(/^[0-9a-f]{32}$/)
    return md5.textContent()
  })
}

let userProject: Project
const fileHandleIds: string[] = []

testAuth.describe('Files', () => {
  testAuth.beforeAll(async ({ browser, storageStatePaths }) => {
    userProject = await setupProject(browser, 'swc-e2e-user', storageStatePaths)
  })

  testAuth.afterAll(async ({ browser }) => {
    testAuth.slow()
    if (userProject.id) {
      await teardownProjectsAndFileHandles(
        browser,
        [userProject],
        fileHandleIds,
      )
    }
  })

  testAuth(
    'File sharing',
    async ({ userPage, validatedUserPage, browserName }) => {
      testAuth.slow()

      // Use test.fixme because test.fail doesn't mark test as expected to fail
      // when there is a test timeout. Since test timeouts are expensive,
      // Playwright recommends skipping the test entirely with test.fixme.
      // See: https://github.com/microsoft/playwright/issues/16317
      testAuth.fixme(
        browserName === 'webkit' && !!process.env.CI,
        `Very slow in webkit in CI only (passes after 6min in macos-latest runner, 
          times out after 10min in Sage ubuntu-22.04-4core-16GBRAM-150GBSSD runner). 
          May be related to the InviteWidget. Evaluate if test passes after addressing 
          https://sagebionetworks.jira.com/browse/SWC-6569.`,
      )

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
        await goToDashboardPage(
          userPage,
          `${entityUrlPathname(userProject.id)}/files`,
        )

        const fileLink = userPage.getByRole('link', { name: fileName })
        await expect(fileLink).toBeVisible({
          timeout: defaultExpectTimeout * 3, // extra time for file to be created
        })

        await fileLink.click()
        await expectFilePageLoaded(fileName, fileEntityId, userPage)
      })

      await openFileSharingSettings(userPage)
      await confirmSharingSettings(userPage, userName, validatedUserName, false)

      await testAuth.step('Second user cannot access the file', async () => {
        await goToDashboardPage(
          validatedUserPage,
          entityUrlPathname(fileEntityId),
        )
        await expectNoAccessPage(validatedUserPage)
      })

      await testAuth.step(
        'First user enables Local Sharing Settings for the File',
        async () => {
          await userPage
            .getByRole('button', { name: 'Create Local Sharing Settings' })
            .click()
          await expect(
            userPage.getByRole('heading', { name: 'Add More' }),
          ).toBeVisible()
        },
      )

      await testAuth.step(
        'First user shares the file with second user',
        async () => {
          const userSuggestBox = userPage.getByRole('combobox', {
            name: 'Add a user or team',
          })
          await userSuggestBox.fill(validatedUserName)

          await userPage
            .getByRole('option', { name: `@${validatedUserName}` })
            .click({ timeout: defaultExpectTimeout * 3 })

          await expect(
            getSharingSettingsPrincipalRow(userPage, validatedUserName),
          ).toBeVisible({ timeout: defaultExpectTimeout * 3 })

          // Don't send message, so we don't have to clean up the message.
          // The checkbox's accessible name is its tooltip, so locate it via its label text
          await expect(
            userPage
              .locator('label')
              .filter({ hasText: 'Notify people via email' })
              .getByRole('checkbox'),
          ).not.toBeChecked()

          await saveFileSharingSettings(userPage)
        },
      )

      await openFileSharingSettings(userPage)
      await confirmSharingSettings(userPage, userName, validatedUserName, true)

      await testAuth.step('Second user accesses the file', async () => {
        await reloadDashboardPage(validatedUserPage)
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
          ).toBeVisible({ timeout: defaultExpectTimeout * 3 })

          await saveFileSharingSettings(userPage)
        },
      )

      await openFileSharingSettings(userPage)
      await confirmSharingSettings(userPage, userName, validatedUserName, false)

      await testAuth.step('Second user cannot access the file', async () => {
        await expect(async () => {
          await reloadDashboardPage(validatedUserPage)
          await expectNoAccessPage(
            validatedUserPage,
            defaultExpectTimeout * 0.5, // use shorter expect timeout, so reload is tried earlier
          )
        }).toPass()
      })
    },
  )

  testAuth('should create and delete a file', async ({ userPage }) => {
    testAuth.slow()

    const fileName = 'test_file.csv'
    const filePath = `data/${fileName}`
    const updatedFilePath = `data/test_file2.csv`

    await testAuth.step('go to files tab', async () => {
      await goToDashboardPage(userPage, entityUrlPathname(userProject.id))
      await expect(
        userPage.getByRole('heading', { name: userProject.name }),
      ).toBeVisible()
      await userPage
        .getByRole('link', { name: 'Files', exact: true })
        .first()
        .click()
    })

    await testAuth.step('upload file', async () => {
      await uploadFile(userPage, filePath, 'initialUpload')
    })

    const fileLink = await testAuth.step('get link to file', async () => {
      const fileLink = userPage.getByRole('link', { name: fileName })
      await expect(fileLink).toBeVisible({
        timeout: defaultExpectTimeout * 3, // extra time for file to be created
      })
      return fileLink
    })

    const { fileEntityId } = await testAuth.step(
      'get fileEntityId',
      async () => {
        const fileLinkHref = await fileLink.getAttribute('href')
        expect(fileLinkHref).not.toBeNull()

        const fileEntityId = getEntityIdFromPathname(fileLinkHref!)
        expect(fileEntityId).not.toBe('')

        return { fileEntityId }
      },
    )

    const md5v1 = await testAuth.step('view file', async () => {
      await fileLink.click()
      await expectFilePageLoaded(fileName, fileEntityId, userPage)
      return await getFileMD5(userPage)
    })

    await testAuth.step('re-upload file', async () => {
      await uploadFile(userPage, filePath, 'newVersion')
      await expectFilePageLoaded(fileName, fileEntityId, userPage)
    })

    await testAuth.step(
      'confirm re-upload did not change md5 or version',
      async () => {
        await expect(userPage.getByText('V1 (Current)')).toBeVisible()
        expect(md5v1).not.toBeNull()
        await expect(getFileMD5Locator(userPage)).toHaveText(md5v1!)
      },
    )

    await testAuth.step('upload a new file', async () => {
      await uploadFile(userPage, updatedFilePath, 'newVersion')
      await expectFilePageLoaded(fileName, fileEntityId, userPage)
    })

    await testAuth.step(
      'confirm uploading new file changed md5 and version',
      async () => {
        await expect(userPage.getByText('V2 (Current)')).toBeVisible()
        // wait for sidebar to reflect the new version before comparing
        await expect(getFileMD5Locator(userPage)).not.toHaveText(md5v1!)
        const md5v2 = await getFileMD5(userPage)
        expect(md5v1).not.toEqual(md5v2)
      },
    )

    await testAuth.step(
      'add associated fileHandleIds to cleanup list',
      async () => {
        const fileHandleIdV1 = await getEntityFileHandleId(
          userPage,
          getAdminPAT(),
          fileEntityId,
          1,
        )
        const fileHandleIdV2 = await getEntityFileHandleId(
          userPage,
          getAdminPAT(),
          fileEntityId,
          2,
        )
        fileHandleIds.push(fileHandleIdV1)
        fileHandleIds.push(fileHandleIdV2)
      },
    )

    await testAuth.step('move file to trash can', async () => {
      await testAuth.step('delete file', async () => {
        await userPage
          .getByRole('button', { name: 'Tools', exact: true })
          .click()
        await userPage.getByRole('menuitem', { name: 'Delete File' }).click()
      })

      await testAuth.step('confirm deletion', async () => {
        await expect(
          userPage.getByRole('heading', { name: 'Confirm Deletion' }),
        ).toBeVisible()
        await expect(
          userPage.getByText(
            `Are you sure you want to delete File ${fileName}?`,
          ),
        ).toBeVisible()
        await expect(
          userPage.getByRole('button', { name: 'Cancel' }),
        ).toBeVisible()

        await userPage.getByRole('button', { name: 'Delete' }).click()
      })

      await testAuth.step('confirm that file was deleted', async () => {
        await userPage.waitForURL(`${entityUrlPathname(userProject.id)}/files/`)
        await dismissAlert(userPage, 'The File was successfully deleted.')

        // The Files list is cached client-side and is not refetched after
        // the delete, so reload to verify the file is no longer listed
        await reloadDashboardPage(userPage)
        await expect(
          userPage.getByRole('heading', { name: 'Files' }),
        ).toBeVisible()
        await expect(
          userPage.getByRole('link', { name: fileName }),
        ).not.toBeVisible()
      })
    })

    await testAuth.step('remove file from trash can', async () => {
      await testAuth.step('go to trash can', async () => {
        await userPage.getByLabel('Trash Can').click()

        // reload page if trash can deferred js does not load
        await expect(async () => {
          await reloadDashboardPage(userPage)

          const trashCanHeading = userPage.getByRole('heading', {
            name: 'Trash Can',
          })
          await expect(trashCanHeading).toBeVisible({
            timeout: defaultExpectTimeout * 0.5, // decrease timeout, so reload happens more quickly
          })

          // click on heading, so tooltip on trash can nav button is hidden
          await trashCanHeading.click()
        }).toPass()
      })

      await testAuth.step('remove file from trash can', async () => {
        const fileCheckbox = userPage
          .getByRole('row')
          .filter({ hasText: fileEntityId })
          .getByRole('checkbox')
        await expect(fileCheckbox).not.toBeChecked()

        // Currently programmatically dispatching the click event
        // because the following aren't working:
        //  - await fileCheckbox.click() -> fails due to <div class="pageContent margin-top-60"> intercepting pointer events
        //  - await fileCheckbox.click({force: true}) -> fails to actually check the checkbox
        await fileCheckbox.dispatchEvent('click')

        await expect(fileCheckbox).toBeChecked()

        await userPage.getByRole('button', { name: 'Delete Selected' }).click()
      })

      await testAuth.step('confirm removal from trash can', async () => {
        await expect(
          userPage.getByText('Delete selected items from your Trash?'),
        ).toBeVisible()
        await userPage.getByRole('button', { name: 'Delete' }).click()
        await expect(userPage.locator('.spinner:visible')).toHaveCount(0)
      })

      await testAuth.step(
        'confirm that file was removed from trash can',
        async () => {
          await expect(userPage.getByText(fileName)).not.toBeVisible()
          await expect(userPage.getByText(fileEntityId)).not.toBeVisible()
          await expect(
            userPage.getByText('Your trash can is currently empty.'),
          ).toBeVisible()
        },
      )
    })
  })
})
