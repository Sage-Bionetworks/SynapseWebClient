import { Page, expect, test } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import { defaultExpectTimeout } from '../playwright.config'
import { testAuth } from './fixtures/authenticatedUserPages'
import {
  deleteTeamInvitationMessage,
  deleteTeamInviteAcceptanceMessage,
} from './helpers/messages'
import {
  deleteTeam,
  getTeamIdFromPathname,
  teamHashBang,
} from './helpers/teams'
import {
  dismissAlert,
  getAccessTokenFromCookie,
  getAdminPAT,
  getUserIdFromLocalStorage,
  goToDashboard,
} from './helpers/testUser'
import {
  userConfigs,
  userPrefix,
  userValidatedPrefix,
} from './helpers/userConfig'

const expectMyTeamsPageLoaded = async (page: Page) => {
  await expect(page.getByRole('heading', { name: 'Your Teams' })).toBeVisible()
  await expect(
    page.getByRole('button', { name: 'Search All Teams' }),
  ).toBeVisible()
  await expect(
    page.getByRole('button', {
      name: 'Create a New Team',
    }),
  ).toBeVisible()
  await expect(page.getByPlaceholder('Team name')).toBeVisible()
}

const expectTeamPageLoaded = async (page: Page, teamName: string) => {
  await expect(page.locator('h3').filter({ hasText: teamName })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Team Actions' })).toBeVisible()
  await expect(page.getByPlaceholder('Find team members')).toBeVisible()
  await expect(page.getByRole('link', { name: teamName })).toBeVisible()
}

const TEAM_NAME = 'swc-e2e-team-' + uuidv4()
const INVITATION_MESSAGE = 'swc-e2e-invite'
let teamId: string | undefined = undefined

// Run multiple describes in parallel, but run tests inside each describe in order
// ...tests within describe expect afterAll to be run with the same users, i.e. on the same worker
// https://playwright.dev/docs/api/class-test#test-describe-configure
test.describe.configure({ mode: 'serial' })

test.describe('Teams', () => {
  testAuth(
    'should exercise team lifecycle',
    async ({ userPage, validatedUserPage }, testInfo) => {
      test.slow()

      const { userName, validatedUserName } = await testAuth.step(
        'should get user names',
        async () => {
          const userName = userConfigs[userPrefix].username
          const validatedUserName = userConfigs[userValidatedPrefix].username
          return { userName, validatedUserName }
        },
      )

      await testAuth.step(
        'user should create a team with a unique name',
        async () => {
          await goToDashboard(userPage)
          await userPage.getByLabel('Teams').click()
          await expectMyTeamsPageLoaded(userPage)

          await userPage
            .getByRole('button', { name: 'Create a New Team' })
            .click()
          const teamNameInput = userPage.getByRole('textbox').nth(1)
          await teamNameInput.fill(TEAM_NAME)
          await expect(teamNameInput).toHaveValue(TEAM_NAME)

          await userPage.getByRole('button', { name: 'OK' }).click()

          await testAuth.step('user should get teamId', async () => {
            await userPage.waitForURL(`/${teamHashBang}:**`)
            teamId = getTeamIdFromPathname(userPage.url())
          })

          await dismissAlert(userPage, `Team Created: ${TEAM_NAME}`)
          await expectTeamPageLoaded(userPage, TEAM_NAME)
          await expect(userPage.getByText('1 team members')).toBeVisible()
        },
      )

      await testAuth.step(
        'user should invite validated user to team',
        async () => {
          await userPage.getByRole('button', { name: 'Team Actions' }).click()
          await userPage.getByRole('link', { name: 'Invite User' }).click()

          await userPage
            .getByRole('textbox', { name: 'Enter a name or email address...' })
            .pressSequentially(validatedUserName)
          await userPage
            .getByRole('menuitem', { name: `@${validatedUserName}` })
            .click()
          const loadInvitedUser = userPage.getByText('Loading...')
          await expect(loadInvitedUser).toBeVisible()
          await expect(loadInvitedUser).not.toBeVisible()

          const inviteMessageBox = userPage.getByLabel('Invitation Message')
          await inviteMessageBox.click()
          await inviteMessageBox.fill(INVITATION_MESSAGE)
          await expect(inviteMessageBox).toHaveValue(INVITATION_MESSAGE)

          const spinner = userPage.locator('.modal-body > .spinner')
          await userPage
            .getByRole('button', { name: 'Send Invitation(s)' })
            .click()

          await expect(spinner).not.toBeVisible()
          await dismissAlert(userPage, 'Invitation(s) Sent')
        },
      )

      await test.step('user should view pending invitations', async () => {
        await expect(userPage.getByText('Pending Invitations')).toBeVisible({
          timeout: defaultExpectTimeout * 3, // add extra timeout, so backend can finish sending request
        })
        const row = userPage.getByRole('row', { name: validatedUserName })
        await expect(row).toBeVisible()
        await expect(row.getByText(INVITATION_MESSAGE)).toBeVisible()
      })

      await test.step('validated user should accept team invitation', async () => {
        await goToDashboard(validatedUserPage)

        await validatedUserPage.getByLabel('Teams').click()
        await expectMyTeamsPageLoaded(validatedUserPage)

        // get row for this invitation
        // ...in case multiple tests have invited validated user at the same time
        const row = validatedUserPage.getByRole('row', { name: TEAM_NAME })
        await expect(row.getByText(INVITATION_MESSAGE)).toBeVisible()
        await expect(row.getByRole('button', { name: 'Cancel' })).toBeVisible()

        await row.getByRole('button', { name: 'Join' }).click()
        await expect(row).not.toBeVisible()
      })

      await testAuth.step('validated user should view team page', async () => {
        const teamLink = validatedUserPage.getByRole('link', {
          name: TEAM_NAME,
        })

        // handle case where occasionally two team links are shown
        // ...before resolving to one team link
        await expect
          .poll(async () => {
            await expectMyTeamsPageLoaded(validatedUserPage)
            const count = await teamLink.count()
            // if no team links appear after joining the team
            // ...try reloading the page to see if the link subsequently appears
            if (count === 0) {
              await validatedUserPage.reload()
            }
            return count
          })
          .toBe(1)

        const responsePromise = validatedUserPage.waitForResponse(
          response =>
            response.url().includes('synapseclient') &&
            (response.request().postData()?.includes('getTeamMemberCount') ||
              false),
          { timeout: defaultExpectTimeout * 3 }, // allow time for the response to return
        )
        await teamLink.click()

        await expectTeamPageLoaded(validatedUserPage, TEAM_NAME)

        await responsePromise
        await expect(
          validatedUserPage.getByText('2 team members'),
        ).toBeVisible()

        await expect(
          validatedUserPage.getByRole('heading', { name: 'Managers' }),
        ).toBeVisible()
        await expect(
          validatedUserPage.getByRole('link', { name: userName, exact: true }),
        ).toBeVisible()

        await expect(
          validatedUserPage.getByRole('heading', { name: 'Members' }),
        ).toBeVisible()
        await expect(
          validatedUserPage.getByRole('link', {
            name: validatedUserName,
            exact: true,
          }),
        ).toBeVisible()
      })

      await testAuth.step('user deletes team', async () => {
        await userPage.getByRole('button', { name: 'Team Actions' }).click()
        await userPage.getByRole('link', { name: 'Delete Team' }).click()
        await userPage.getByRole('button', { name: 'Delete' }).click()

        await dismissAlert(userPage, 'Team successfully deleted')
        await expect(
          userPage.getByRole('link', { name: TEAM_NAME }),
        ).not.toBeVisible()
        teamId = undefined
      })
    },
  )

  testAuth.afterAll(async ({ userPage, validatedUserPage }) => {
    // get credentials
    const adminPAT = getAdminPAT()

    const validatedUserId = await getUserIdFromLocalStorage(validatedUserPage)
    const validatedUserAccessToken = await getAccessTokenFromCookie(
      validatedUserPage,
    )

    const userUserId = await getUserIdFromLocalStorage(userPage)
    const userAccessToken = await getAccessTokenFromCookie(userPage)
    const userName = userConfigs[userPrefix].username
    expect(userName).not.toBeNull()

    // delete team if it was created but not cleaned up during the test
    if (teamId) {
      await deleteTeam(teamId, userAccessToken, userPage)
    }

    // delete team invitation: user -> validated user
    await deleteTeamInvitationMessage(
      [validatedUserId],
      userName,
      TEAM_NAME,
      userAccessToken,
      adminPAT,
      userPage,
    )

    // delete team acceptance: validated user -> user
    await deleteTeamInviteAcceptanceMessage(
      [userUserId!],
      validatedUserAccessToken,
      adminPAT,
      validatedUserPage,
    )
  })
})
