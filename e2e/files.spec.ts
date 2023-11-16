import { Page, expect, test } from '@playwright/test'
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

    await expect(page.getByText('Loading provenance...')).not.toBeVisible()
    await expect(page.getByText(fileEntityId, { exact: true })).toBeVisible()
  })
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

const expectNoAccessPage = async (
  page: Page,
  expectTimeout: number = defaultExpectTimeout,
) => {
  await expect(
    page.getByRole('heading', {
      name: 'Sorry, no access to this page.',
    }),
  ).toBeVisible({ timeout: expectTimeout })
  await expect(
    page.getByText('You are not authorized to access the page requested.'),
  ).toBeVisible({ timeout: expectTimeout })
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
      uploadType === 'initialUpload'
        ? 'Upload or Link to a File'
        : 'Upload a New Version of File'
    await page.getByRole('button', { name: uploadButtonText }).click()

    await expect(page.getByRole('link', { name: 'Upload File' })).toBeVisible()
    await expect(page.getByRole('link', { name: 'Link to URL' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Browse...' })).toBeVisible()
  })

  await testAuth.step('choose file', async () => {
    // Ensure that upload type is specified before attempting upload
    // to prevent upload error: "Unsupported external upload type specified: undefined"
    await expect(
      page.getByText(/all uploaded files will be stored in synapsestorage/i),
    ).toBeVisible()

    const fileChooserPromise = page.waitForEvent('filechooser')
    await page.getByRole('button', { name: 'Browse...' }).click()
    if (uploadType === 'initialUpload') {
      await page
        .getByRole('menu')
        .getByRole('link')
        .filter({ hasText: 'Files' })
        .click()
    }
    const fileChooser = await fileChooserPromise
    await fileChooser.setFiles(path.join(__dirname, filePath))
  })

  await testAuth.step('wait for file upload modal to close', async () => {
    await expect(page.getByText('Initializing......')).not.toBeVisible()
    await expect(
      page.getByRole('heading', { name: 'Upload or Link to File' }),
    ).not.toBeVisible()
  })

  await testAuth.step('ensure there was not an upload error', async () => {
    const uploadError = page.getByRole('heading', { name: 'Upload Error' })
    if (await uploadError.isVisible()) {
      const uploadErrorText = await page.getByRole('dialog').textContent()
      throw new Error(`Upload Error: ${uploadErrorText}`)
    }
  })
}

const dismissFileUploadAlert = async (page: Page) => {
  await testAuth.step('dismiss file upload alert', async () => {
    await dismissAlert(page, 'File successfully uploaded')
  })
}

const getFileMD5 = async (page: Page) => {
  return await testAuth.step('get file MD5', async () => {
    const row = page.getByRole('row').filter({ hasText: 'MD5' })
    expect(row.getByRole('cell')).toHaveCount(2)
    return row.getByRole('cell').filter({ hasNotText: 'MD5' }).textContent()
  })
}

let userProject: Project
let fileHandleIds: string[] = []

test.describe('Files', () => {
  testAuth.beforeAll(async ({ browser, storageStatePaths }) => {
    userProject = await setupProject(browser, 'swc-e2e-user', storageStatePaths)
  })

  testAuth.afterAll(async ({ browser }) => {
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
      test.slow()

      // Use test.fixme because test.fail doesn't mark test as expected to fail
      // when there is a test timeout. Since test timeouts are expensive,
      // Playwright recommends skipping the test entirely with test.fixme.
      // See: https://github.com/microsoft/playwright/issues/16317
      test.fixme(
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
            .click({ timeout: defaultExpectTimeout * 3 })

          await expect(
            userPage.getByRole('cell', { name: validatedUserName }),
          ).toBeVisible({ timeout: defaultExpectTimeout * 3 })

          // Don't send message, so we don't have to clean up the message
          await userPage.getByText('Notify people via email').click()

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

  testAuth(
    'should create and delete a file',
    async ({ userPage, browserName }) => {
      test.slow()

      test.fail(
        browserName === 'webkit',
        `Playwright is not preserving the File's lastModified time in webkit, so 
        file upload fails because it seems like the file was modified during 
        upload. See https://github.com/microsoft/playwright/issues/27452. Fix 
        planned for Playwright v1.40.`,
      )

      const fileName = 'test_file.csv'
      const filePath = `data/${fileName}`
      const updatedFilePath = `data/test_file2.csv`

      await testAuth.step('go to files tab', async () => {
        await goToDashboardPage(userPage, entityUrlPathname(userProject.id))
        await expect(
          userPage.getByRole('heading', { name: userProject.name }),
        ).toBeVisible()
        await userPage.getByRole('link', { name: 'Files', exact: true }).click()
      })

      await testAuth.step('upload file', async () => {
        await uploadFile(userPage, filePath, 'initialUpload')
        await dismissFileUploadAlert(userPage)
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
          const md5reupload = await getFileMD5(userPage)
          expect(md5v1).toEqual(md5reupload)
          await expect(userPage.getByText('V1 (Current)')).toBeVisible()
        },
      )

      // Upload success alert intermittently appears when re-uploading the same file
      await testAuth.step(
        'dismiss file upload alert for re-uploaded file, if visible',
        async () => {
          if (
            await userPage.getByText('File successfully uploaded').isVisible()
          ) {
            await dismissFileUploadAlert(userPage)
          }
        },
      )

      await testAuth.step('upload a new file', async () => {
        await uploadFile(userPage, updatedFilePath, 'newVersion')
        await expectFilePageLoaded(fileName, fileEntityId, userPage)
        await dismissFileUploadAlert(userPage)
      })

      await testAuth.step(
        'confirm uploading new file changed md5 and version',
        async () => {
          const md5v2 = await getFileMD5(userPage)
          expect(md5v1).not.toEqual(md5v2)
          await expect(userPage.getByText('V2 (Current)')).toBeVisible()
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
          await userPage.getByRole('button', { name: 'File Tools' }).click()
          await userPage.getByRole('menuitem', { name: 'Delete File' }).click()
        })

        await testAuth.step('confirm deletion', async () => {
          await expect(
            userPage.getByRole('heading', { name: 'Confirm Deletion' }),
          ).toBeVisible()
          await expect(
            userPage.getByText(
              `Are you sure you want to delete File "${fileName}"?`,
            ),
          ).toBeVisible()
          await expect(
            userPage.getByRole('button', { name: 'Cancel' }),
          ).toBeVisible()

          await userPage.getByRole('button', { name: 'Delete' }).click()
        })

        await testAuth.step('confirm that file was deleted', async () => {
          await userPage.waitForURL(
            `${entityUrlPathname(userProject.id)}/files/`,
          )

          await expect(
            userPage.getByRole('heading', { name: 'Files' }),
          ).toBeVisible()
          await expect(
            userPage.getByRole('link', { name: fileName }),
          ).not.toBeVisible()

          await dismissAlert(userPage, 'The File was successfully deleted.')
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
          const fileCheckbox = userPage.getByRole('checkbox', {
            name: `Select ${fileEntityId}`,
          })
          await expect(fileCheckbox).not.toBeChecked()

          // Currently programmatically dispatching the click event
          // because the following aren't working:
          //  - await fileCheckbox.click() -> fails due to <div class="pageContent margin-top-60"> intercepting pointer events
          //  - await fileCheckbox.click({force: true}) -> fails to actually check the checkbox
          await fileCheckbox.dispatchEvent('click')

          await expect(fileCheckbox).toBeChecked()

          await userPage
            .getByRole('button', { name: 'Delete Selected' })
            .click()
        })

        await testAuth.step('confirm removal from trash can', async () => {
          await expect(
            userPage.getByText('Delete selected items from your Trash?'),
          ).toBeVisible()
          await userPage.getByRole('button', { name: 'Delete' }).click()
        })

        await testAuth.step(
          'confirm that file was removed from trash can',
          async () => {
            await expect(userPage.getByText(fileName)).not.toBeVisible()
            await expect(userPage.getByText(fileEntityId)).not.toBeVisible()
            await expect(
              userPage.getByText('Trash Can is currently empty.'),
            ).toBeVisible()
          },
        )
      })
    },
  )
})
