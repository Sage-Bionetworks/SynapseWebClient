import { Page, expect, test } from '@playwright/test'
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

// FIXME: Discussion action icons are not buttons and don't have accessible
// names, so can't be selected by role + name or by label -- select by
// classname to differentiate between the icons
const discussionActionIconClasses = {
  PIN: '.fa-thumb-tack.imageButton',
  EDIT: '.fa-pencil.imageButton',
  DELETE: '.syn-trash-o.imageButton',
  TOGGLE_FOLLOW: '.fa-eye.imageButton',
  LINK: '.fa-link.imageButton',
}
const discussionThreadSelector = '.discussionThread:visible'
const discussionReplySelector = '.discussionReply:visible'

const getThreadTextbox = (page: Page) => {
  return page.locator('.markdownEditor').getByRole('textbox')
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

const getDiscussionParentActionButtons = async (
  page: Page,
  threadTitle: string,
) => {
  const parentPost = page
    .locator(discussionThreadSelector)
    .locator('.row')
    .filter({ hasText: threadTitle })
  return {
    PIN: parentPost.locator(discussionActionIconClasses.PIN),
    EDIT: parentPost.locator(discussionActionIconClasses.EDIT),
    DELETE: parentPost.locator(discussionActionIconClasses.DELETE),
    TOGGLE_FOLLOW: parentPost.locator(
      discussionActionIconClasses.TOGGLE_FOLLOW,
    ),
  }
}

const getDiscussionReplyActionButtons = async (
  page: Page,
  threadReply: string,
) => {
  const replyPost = page
    .locator(discussionThreadSelector)
    .locator('.row')
    .filter({ hasText: threadReply })
  return {
    EDIT: replyPost.locator(discussionActionIconClasses.EDIT),
    LINK: replyPost.locator(discussionActionIconClasses.LINK),
    DELETE: replyPost.locator(discussionActionIconClasses.DELETE),
  }
}

let userProject: Project
let fileHandleIds: string[] = []

test.describe('Discussions', () => {
  testAuth.beforeAll(async ({ browser, storageStatePaths }) => {
    userProject = await setupProjectWithPermissions(
      browser,
      'swc-e2e-user',
      'swc-e2e-user-validated',
      storageStatePaths,
    )
  })

  testAuth.afterAll(async ({ browser }) => {
    test.slow()
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
      test.slow()

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

          const threadTextbox = getThreadTextbox(userPage)
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
          const threadLink = userPage.getByRole('link', { name: threadTitle })

          const threadId = await testAuth.step('Get threadId', async () => {
            const threadLinkHref = await threadLink.getAttribute('href')
            return Number(threadLinkHref?.replace(/.*threadId=/, ''))
          })

          await testAuth.step('Go to thread', async () => {
            await threadLink.click()
            await expectDiscussionThreadLoaded(
              userPage,
              threadId,
              threadTitle,
              threadBody,
              userProject.id,
            )
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
          userPage,
          threadTitle,
          userConfigs['swc-e2e-user'].username,
          '0',
          '1',
        )
      })

      await testAuth.step('Second user can view thread', async () => {
        await validatedUserPage.getByRole('link', { name: threadTitle }).click()
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
            const threadTextbox = getThreadTextbox(validatedUserPage)
            await threadTextbox.fill(threadReply)
            await expect(threadTextbox).toHaveValue(threadReply)
            await validatedUserPage
              .getByRole('button', { name: 'Post', exact: true })
              .click()
          })

          await testAuth.step('Dismiss alert', async () => {
            await dismissAlert(validatedUserPage, 'A reply has been created.')
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
              const parentPostActions = await getDiscussionParentActionButtons(
                validatedUserPage,
                threadTitle,
              )
              await expect(parentPostActions.PIN).not.toBeVisible()
              await expect(parentPostActions.EDIT).not.toBeVisible()
              await expect(parentPostActions.DELETE).not.toBeVisible()
              await expect(parentPostActions.TOGGLE_FOLLOW).toBeVisible()
            },
          )

          const secondUserReplyPostActions = await testAuth.step(
            'Confirm second user has expected actions available on thread reply',
            async () => {
              const replyPostActions = await getDiscussionReplyActionButtons(
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
            await userPage.getByRole('link', { name: threadTitle }).click()
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
              const parentPostActions = await getDiscussionParentActionButtons(
                userPage,
                threadTitle,
              )
              await expect(parentPostActions.PIN).toBeVisible()
              await expect(parentPostActions.EDIT).toBeVisible()
              await expect(parentPostActions.DELETE).toBeVisible()
              await expect(parentPostActions.TOGGLE_FOLLOW).toBeVisible()
            },
          )

          const firstUserReplyPostActions = await testAuth.step(
            'Confirm first user has expected actions available on thread reply',
            async () => {
              const replyPostActions = await getDiscussionReplyActionButtons(
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
          const threadTextbox = getThreadTextbox(validatedUserPage)
          await threadTextbox.clear()
          await threadTextbox.fill(threadReplyEdited)
          await expect(threadTextbox).toHaveValue(threadReplyEdited)
          await validatedUserPage.getByRole('button', { name: 'Save' }).click()
        })

        await testAuth.step('Dismiss alert', async () => {
          // handle case where alert has auto-closed before the test gets to this step
          const alertText = 'A reply has been edited.'
          if (await validatedUserPage.getByText(alertText).isVisible()) {
            await dismissAlert(validatedUserPage, alertText)
          }
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
                .locator(':right-of(:text("posted")):text("Edited")'),
            ).toBeVisible()
          },
        )
      })

      await testAuth.step('First user deletes the reply', async () => {
        await testAuth.step('Delete the reply', async () => {
          await firstUserReplyPostActions.DELETE.click()
          await expect(
            userPage.getByRole('heading', { name: 'Confirm Deletion' }),
          ).toBeVisible()
          await userPage.getByRole('button', { name: 'Delete' }).click()
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
          await userPage
            .getByRole('button', { name: 'Discussion Tools' })
            .click()
          await userPage
            .getByRole('menuitem', { name: 'Delete Thread' })
            .click()
          await expect(
            userPage.getByRole('heading', { name: 'Confirm Deletion' }),
          ).toBeVisible()
          await expect(
            userPage.getByText('Are you sure you want to delete this thread?'),
          ).toBeVisible()
          await userPage.getByRole('button', { name: 'Delete' }).click()
        })

        await testAuth.step('Dismiss alert', async () => {
          await dismissAlert(userPage, 'A thread has been deleted.')
        })

        await expectDiscussionPageLoaded(userPage, userProject.id)

        await testAuth.step('Confirm thread not visible', async () => {
          await expect(userPage.getByText(threadTitle)).not.toBeVisible()
        })
      })
    },
  )
})
