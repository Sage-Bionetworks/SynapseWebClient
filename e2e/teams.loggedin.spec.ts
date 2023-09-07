import { Page, expect, test } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import {
  USER_STORAGE_STATE,
  USER_VALIDATED_STORAGE_STATE,
} from '../playwright.config'
import { getLocalStorage } from './helpers/localStorage'
import {
  deleteTeamInvitationMessage,
  deleteTeamInviteAcceptanceMessage,
} from './helpers/messages'
import {
  getAccessTokenFromCookie,
  getAdminPAT,
  getUserIdFromLocalStorage,
  goToDashboard,
} from './helpers/testUser'
import {
  USER_NAME_LOCALSTORAGE_KEY,
  USER_VALIDATED_NAME_LOCALSTORAGE_KEY,
} from './helpers/userConfig'

const TEAM_NAME = 'swc-e2e-team-' + uuidv4()
const INVITATION_MESSAGE = 'swc-e2e-invite'

let validatedUserPage: Page
let userPage: Page

test.describe('Teams', () => {
  test.beforeAll(async ({ browser }) => {
    validatedUserPage = await browser.newPage({
      storageState: USER_VALIDATED_STORAGE_STATE,
    })
    userPage = await browser.newPage({
      storageState: USER_STORAGE_STATE,
    })
  })
  test('should exercise team lifecycle', async ({}, testInfo) => {
    const userName = await test.step('should get user name', async () => {
      const userName = await getLocalStorage(
        userPage,
        USER_NAME_LOCALSTORAGE_KEY,
      )
      expect(userName).not.toBeNull()

      return userName!
    })

    const validatedUserName =
      await test.step('should get validated user name', async () => {
        const validatedUserName = await getLocalStorage(
          validatedUserPage,
          USER_VALIDATED_NAME_LOCALSTORAGE_KEY,
        )
        expect(validatedUserName).not.toBeNull()

        return validatedUserName!
      })

    await test.step('user should create a team with a unique name', async () => {
      await goToDashboard(userPage)
      await userPage.getByLabel('Teams').click()
      await expect(
        userPage.getByRole('heading', { name: 'Your Teams' }),
      ).toBeVisible()

      await userPage.getByRole('button', { name: 'Create a New Team' }).click()
      await userPage.getByRole('textbox').nth(1).fill(TEAM_NAME)
      await userPage.getByRole('button', { name: 'OK' }).click()
      await expect(
        userPage.getByText(`Team Created: ${TEAM_NAME}`),
      ).toBeVisible()
    })

    await test.step('user should invite validated user to team', async () => {
      await userPage.getByRole('button', { name: 'Team Actions' }).click()
      await userPage.getByRole('link', { name: 'Invite User' }).click()

      await userPage
        .getByRole('textbox', { name: 'Enter a name or email address...' })
        .type(validatedUserName)
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
      await userPage.getByRole('button', { name: 'Send Invitation(s)' }).click()

      await expect(spinner).not.toBeVisible()
      await expect(userPage.getByText('Invitation(s) Sent')).toBeVisible()
    })

    await test.step('user should view pending invitations', async () => {
      await expect(userPage.getByText('Pending Invitations')).toBeVisible({
        timeout: testInfo.timeout * 3, // add extra timeout, so backend can finish sending request
      })
      const row = userPage.getByRole('row', { name: validatedUserName })
      await expect(row).toBeVisible()
      await expect(row.getByText(INVITATION_MESSAGE)).toBeVisible()
    })

    await test.step('validated user should accept team invitation', async () => {
      await goToDashboard(validatedUserPage)

      await Promise.all([
        expect(
          validatedUserPage.getByRole('heading', { name: 'Your Teams' }),
        ).toBeVisible(),
        validatedUserPage.getByLabel('Teams').click(),
      ])

      // get row for this invitation
      // ...in case multiple tests have invited validated user at the same time
      const row = validatedUserPage.getByRole('row', { name: TEAM_NAME })
      await expect(row.getByText(INVITATION_MESSAGE)).toBeVisible()

      await row.getByRole('button', { name: 'Join' }).click()
      await expect(row).not.toBeVisible()
    })

    await test.step('validated user should view team page', async () => {
      const teamLink = validatedUserPage.getByRole('link', { name: TEAM_NAME })

      // handle case where occasionally two team links are shown
      // ...before resolving to one team link
      await expect
        .poll(async () => {
          return await teamLink.count()
        })
        .toBe(1)

      await teamLink.click()

      await expect(
        validatedUserPage.locator('h3').filter({
          hasText: TEAM_NAME,
        }),
      ).toBeVisible()
      await expect(validatedUserPage.getByText('2 team members')).toBeVisible()

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
  })

  test.afterAll(async () => {
    // delete team
    await goToDashboard(userPage)
    await userPage.getByLabel('Teams').click()
    await userPage.getByRole('link', { name: TEAM_NAME }).click()
    await userPage.getByRole('button', { name: 'Team Actions' }).click()
    await userPage.getByRole('link', { name: 'Delete Team' }).click()
    await userPage.getByRole('button', { name: 'Delete' }).click()
    await expect(userPage.getByText('Team successfully deleted')).toBeVisible()
    await expect(
      userPage.getByRole('link', { name: TEAM_NAME }),
    ).not.toBeVisible()

    // get credentials
    const adminPAT = getAdminPAT()

    const validatedUserId = await getUserIdFromLocalStorage(validatedUserPage)
    const validatedUserAccessToken = await getAccessTokenFromCookie(
      validatedUserPage,
    )

    const userUserId = await getUserIdFromLocalStorage(userPage)
    const userAccessToken = await getAccessTokenFromCookie(userPage)
    const userName = await getLocalStorage(userPage, USER_NAME_LOCALSTORAGE_KEY)
    expect(userName).not.toBeNull()

    // delete team invitation: user -> validated user
    await deleteTeamInvitationMessage(
      [validatedUserId],
      userName!,
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

    // close pages
    await validatedUserPage.close()
    await userPage.close()
  })
})
