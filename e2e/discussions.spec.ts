import { Locator, Page, expect } from '@playwright/test'
import { defaultExpectTimeout } from '../playwright.config'
import { testAuth } from './fixtures/authenticatedUserPages'
import {
  setupProjectWithPermissions,
  teardownProjectsAndFileHandles,
} from './helpers/setupTeardown'
import {
  dismissAlert,
  expectDiscussionPageLoaded,
  expectDiscussionThreadLoaded,
  getDefaultDiscussionPath,
  goToDashboardPage,
  reloadDashboardPage,
} from './helpers/testUser'
import { Project } from './helpers/types'
import { UserPrefix, userConfigs } from './helpers/userConfig'

const discussionThreadSelector = '.DiscussionThread:visible'
const discussionReplySelector = '.reply-container:visible'

// Reply editors use the React markdown editor
const getReplyTextbox = (container: Page | Locator) => {
  return container
    .locator('.MarkdownEditor')
    .getByRole('textbox', { name: 'markdown' })
}

const expectThreadTableLoaded = async (
  page: Page,
  title: string,
  author: string,
  replies: string,
  views: string,
) => {
  await testAuth.step('Thread table info is loaded', async () => {
    const threadCells = page
      .getByRole('row')
      .filter({ hasText: title })
      .getByRole('cell')

    await expect(threadCells.first(), 'Should have correct title').toHaveText(
      title,
    )
    await expect(threadCells.nth(1), 'Should have correct author').toHaveText(
      `@${author}`,
    )
    await expect(threadCells.nth(3), 'Should have correct replies').toHaveText(
      replies,
    )
    await expect(threadCells.nth(4), 'Should have correct views').toHaveText(
      views,
    )
  })
}

// The thread title in the forum table is a MUI Link without an href (it
// navigates via onClick), so it does not have the link role
const getThreadTableTitleLink = (page: Page, threadTitle: string) => {
  return page.getByRole('cell').getByText(threadTitle, { exact: true })
}

const expectThreadReplyVisible = async (
  page: Page,
  threadReply: string,
  replierPrefix: UserPrefix,
) => {
  await testAuth.step('Confirm thread reply is visible', async () => {
    const discussionReply = page.locator(discussionReplySelector)
    await expect(
      discussionReply.getByRole('link', {
        name: `@${userConfigs[replierPrefix].username}`,
      }),
    ).toBeVisible()
    await expect(discussionReply.getByText(threadReply)).toBeVisible({
      timeout: defaultExpectTimeout * 2, // allow extra time for reply to become visible
    })
  })
}

const getDiscussionParentActionButtons = (page: Page) => {
  const parentPost = page.locator(discussionThreadSelector)
  return {
    PIN: parentPost.getByRole('button', { name: /^(Pin|Unpin) thread$/ }),
    EDIT: parentPost.getByRole('button', { name: 'Edit thread' }),
    DELETE: parentPost.getByRole('button', { name: 'Delete thread' }),
    TOGGLE_FOLLOW: parentPost.getByRole('button', {
      name: /^(Follow|Unfollow) thread$/,
    }),
  }
}

const getDiscussionReplyActionButtons = (page: Page, threadReply: string) => {
  const replyPost = page
    .locator(discussionReplySelector)
    .filter({ hasText: threadReply })
  return {
    EDIT: replyPost.getByRole('button', { name: 'Edit reply' }),
    LINK: replyPost.getByRole('button', { name: 'Copy link to this reply' }),
    DELETE: replyPost.getByRole('button', { name: 'Delete reply' }),
  }
}

let userProject: Project
const fileHandleIds: string[] = []

testAuth.describe('Discussions', () => {
  testAuth.beforeAll(async ({ browser, storageStatePaths }) => {
    userProject = await setupProjectWithPermissions(
      browser,
      'swc-e2e-user',
      'swc-e2e-user-validated',
      storageStatePaths,
    )
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
    'should allow discussion and reply CRUD',
    async ({ userPage, validatedUserPage }) => {
      testAuth.slow()

      const threadTitle = 'The Title of the Thread'
      const threadBody = 'The body of the Thread'
      const threadReply = 'A really interesting reply to the Thread'
      const threadReplyEdited = 'An edited reply to the Thread'

      await testAuth.step('First user goes to Project', async () => {
        await goToDashboardPage(
          userPage,
          getDefaultDiscussionPath(userProject.id),
        )

        await expectDiscussionPageLoaded(userPage, userProject.id)
      })

      await testAuth.step('First user creates a new thread', async () => {
        await testAuth.step('Open thread creation modal', async () => {
          await userPage.getByRole('button', { name: 'New Thread' }).click()
          await expect(
            userPage.getByRole('heading', { name: 'New Thread' }),
          ).toBeVisible()
        })

        await testAuth.step('Enter thread information', async () => {
          const threadTitleInput = userPage.getByPlaceholder('Title')
          await threadTitleInput.fill(threadTitle)
          await expect(threadTitleInput).toHaveValue(threadTitle)

          // New thread modal uses the GWT markdown editor
          const threadTextbox = userPage
            .locator('.markdownEditor')
            .getByRole('textbox')
          await threadTextbox.fill(threadBody)
          await expect(threadTextbox).toHaveValue(threadBody)
        })

        await testAuth.step('Post new thread', async () => {
          await userPage.getByRole('button', { name: 'Post' }).click()
          await dismissAlert(userPage, 'A new thread has been created.')
        })

        await expectThreadTableLoaded(
          userPage,
          threadTitle,
          userConfigs['swc-e2e-user'].username,
          '0',
          '0',
        )
      })

      const threadId = await testAuth.step(
        'First user can view thread',
        async () => {
          const threadLink = getThreadTableTitleLink(userPage, threadTitle)

          const threadId = await testAuth.step('Go to thread', async () => {
            await threadLink.click()

            const threadId = await testAuth.step('Get threadId', async () => {
              await userPage.waitForURL(/\/discussion\/threadId=\d+$/)
              return Number(userPage.url().replace(/.*threadId=/, ''))
            })

            await expectDiscussionThreadLoaded(
              userPage,
              threadId,
              threadTitle,
              threadBody,
              userProject.id,
            )
            return threadId
          })

          await testAuth.step('Check thread info', async () => {
            const discussionThread = userPage.locator(discussionThreadSelector)
            await expect(
              discussionThread.getByText('Followers (1)'),
              'Should have one follower',
            ).toBeVisible()
            await expect(
              userPage.getByRole('link', {
                name: `@${userConfigs['swc-e2e-user'].username}`,
              }),
            ).toBeVisible()
            await expect(discussionThread.getByText('Moderator')).toBeVisible()
          })

          return threadId
        },
      )

      await testAuth.step('View count is updated', async () => {
        await userPage
          .getByRole('button', { name: /show all threads/i })
          .click()

        await expectDiscussionPageLoaded(userPage, userProject.id)

        // retry if the view count hasn't updated
        await expect(async () => {
          // reload is necessary for view count to update
          await reloadDashboardPage(userPage)
          await expectDiscussionPageLoaded(
            userPage,
            userProject.id,
            defaultExpectTimeout * 0.5, // use shorter expect timeout, so reload is tried earlier
          )
          await expectThreadTableLoaded(
            userPage,
            threadTitle,
            userConfigs['swc-e2e-user'].username,
            '0',
            '1',
          )
        }).toPass()
      })

      await testAuth.step('Second user can view discussion', async () => {
        await goToDashboardPage(
          validatedUserPage,
          getDefaultDiscussionPath(userProject.id),
        )
        await expectDiscussionPageLoaded(validatedUserPage, userProject.id)
        await expectThreadTableLoaded(
          validatedUserPage,
          threadTitle,
          userConfigs['swc-e2e-user'].username,
          '0',
          '1',
        )
      })

      await testAuth.step('Second user can view thread', async () => {
        await getThreadTableTitleLink(validatedUserPage, threadTitle).click()
        await expectDiscussionThreadLoaded(
          validatedUserPage,
          threadId,
          threadTitle,
          threadBody,
          userProject.id,
        )
      })

      const secondUserReplyPostActions = await testAuth.step(
        'Second user replies in the thread',
        async () => {
          const discussionThread = validatedUserPage.locator(
            discussionThreadSelector,
          )

          await testAuth.step('Confirm initial followers count', async () => {
            await expect(
              discussionThread.getByText('Followers (1)'),
              'Should have one follower initially',
            ).toBeVisible()
          })

          await testAuth.step('Post a reply', async () => {
            const replyInput = discussionThread.getByRole('textbox', {
              name: 'Write a reply...',
            })

            // A second reply input briefly appears when the network
            // connection is slow. Wait for the second input to be hidden.
            await expect(replyInput).toHaveCount(1)

            await replyInput.click()
            const replyTextbox = getReplyTextbox(discussionThread)
            await replyTextbox.fill(threadReply)
            await expect(replyTextbox).toHaveValue(threadReply)
            await discussionThread
              .getByRole('button', { name: 'Post', exact: true })
              .click()
          })

          await testAuth.step(
            'Confirm thread followers incremented',
            async () => {
              await expect(
                discussionThread.getByText('Followers (2)'),
                'Should have two followers',
              ).toBeVisible()
            },
          )

          await expectThreadReplyVisible(
            validatedUserPage,
            threadReply,
            'swc-e2e-user-validated',
          )

          await testAuth.step(
            'Confirm second user has expected actions available on thread title',
            async () => {
              const parentPostActions =
                getDiscussionParentActionButtons(validatedUserPage)
              await expect(parentPostActions.PIN).not.toBeVisible()
              await expect(parentPostActions.EDIT).not.toBeVisible()
              await expect(parentPostActions.DELETE).not.toBeVisible()
              await expect(parentPostActions.TOGGLE_FOLLOW).toBeVisible()
            },
          )

          const secondUserReplyPostActions = await testAuth.step(
            'Confirm second user has expected actions available on thread reply',
            async () => {
              const replyPostActions = getDiscussionReplyActionButtons(
                validatedUserPage,
                threadReply,
              )
              await expect(replyPostActions.EDIT).toBeVisible()
              await expect(replyPostActions.LINK).toBeVisible()
              await expect(replyPostActions.DELETE).not.toBeVisible()
              return replyPostActions
            },
          )
          return secondUserReplyPostActions
        },
      )

      const firstUserReplyPostActions = await testAuth.step(
        'First user views the reply',
        async () => {
          await testAuth.step('Go to discussion thread', async () => {
            await getThreadTableTitleLink(userPage, threadTitle).click()
            await expectDiscussionThreadLoaded(
              userPage,
              threadId,
              threadTitle,
              threadBody,
              userProject.id,
            )
          })

          await expectThreadReplyVisible(
            userPage,
            threadReply,
            'swc-e2e-user-validated',
          )

          await testAuth.step(
            'Confirm first user has expected actions available on thread title',
            async () => {
              const parentPostActions =
                getDiscussionParentActionButtons(userPage)
              await expect(parentPostActions.PIN).toBeVisible()
              await expect(parentPostActions.EDIT).toBeVisible()
              await expect(parentPostActions.DELETE).toBeVisible()
              await expect(parentPostActions.TOGGLE_FOLLOW).toBeVisible()
            },
          )

          const firstUserReplyPostActions = await testAuth.step(
            'Confirm first user has expected actions available on thread reply',
            async () => {
              const replyPostActions = getDiscussionReplyActionButtons(
                userPage,
                threadReply,
              )
              await expect(replyPostActions.EDIT).not.toBeVisible()
              await expect(replyPostActions.LINK).toBeVisible()
              await expect(replyPostActions.DELETE).toBeVisible()
              return replyPostActions
            },
          )

          return firstUserReplyPostActions
        },
      )

      await testAuth.step('Second user edits the reply', async () => {
        await testAuth.step('Edit reply', async () => {
          await secondUserReplyPostActions.EDIT.click()
          const editReplyDialog = validatedUserPage
            .getByRole('dialog')
            .filter({ hasText: 'Edit Reply' })
          const replyTextbox = getReplyTextbox(editReplyDialog)
          await expect(replyTextbox).toHaveValue(threadReply)
          await replyTextbox.clear()
          await replyTextbox.fill(threadReplyEdited)
          await expect(replyTextbox).toHaveValue(threadReplyEdited)
          await editReplyDialog
            .getByRole('button', { name: 'Post', exact: true })
            .click()
          await expect(editReplyDialog).not.toBeVisible()
        })

        await expectThreadReplyVisible(
          validatedUserPage,
          threadReplyEdited,
          'swc-e2e-user-validated',
        )

        await testAuth.step(
          'Confirm edited is displayed next to reply',
          async () => {
            await expect(
              validatedUserPage
                .locator(discussionReplySelector)
                .getByText(/^posted \S+ \(Edited\)$/),
            ).toBeVisible()
          },
        )
      })

      await testAuth.step('First user deletes the reply', async () => {
        await testAuth.step('Delete the reply', async () => {
          await firstUserReplyPostActions.DELETE.click()
          const confirmDialog = userPage
            .getByRole('dialog')
            .filter({ hasText: 'Confirm Deletion' })
          await expect(
            confirmDialog.getByText(
              'Are you sure you want to delete this reply?',
            ),
          ).toBeVisible()
          await confirmDialog
            .getByRole('button', { name: 'Delete', exact: true })
            .click()
          await dismissAlert(userPage, 'A reply has been deleted.')
        })

        await testAuth.step('Confirm reply is not visible', async () => {
          await expect(
            userPage.locator(discussionReplySelector),
          ).not.toBeVisible()
        })
      })

      await testAuth.step('First user deletes the thread', async () => {
        await testAuth.step('Delete thread', async () => {
          await getDiscussionParentActionButtons(userPage).DELETE.click()
          const confirmDialog = userPage
            .getByRole('dialog')
            .filter({ hasText: 'Confirm Deletion' })
          await expect(
            confirmDialog.getByText(
              'Are you sure you want to delete this thread?',
            ),
          ).toBeVisible()
          await confirmDialog
            .getByRole('button', { name: 'Delete', exact: true })
            .click()
        })

        await testAuth.step('Dismiss alert', async () => {
          await dismissAlert(userPage, 'A thread has been deleted.')
        })

        await testAuth.step(
          'Thread is shown as deleted and can be restored',
          async () => {
            const discussionThread = userPage.locator(discussionThreadSelector)
            await expect(
              discussionThread.getByRole('button', {
                name: 'Restore deleted thread',
              }),
            ).toBeVisible()
            await expect(
              getDiscussionParentActionButtons(userPage).DELETE,
            ).not.toBeVisible()
          },
        )

        await testAuth.step('Go back to all threads', async () => {
          // deleting a thread no longer redirects to the thread list
          await userPage
            .getByRole('button', { name: /show all threads/i })
            .click()
          await expectDiscussionPageLoaded(userPage, userProject.id)
        })

        await testAuth.step('Confirm thread not visible', async () => {
          await expect(userPage.getByText(threadTitle)).not.toBeVisible()
        })
      })
    },
  )
})
