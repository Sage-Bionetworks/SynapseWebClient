import { Page, expect, test } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import { ADMIN_STORAGE_STATE, USER_STORAGE_STATE } from '../playwright.config'
import { getLocalStorage } from './helpers/localStorage'
import {
  deleteTeamInvitationMessage,
  deleteTeamInviteAcceptanceMessage,
} from './helpers/messages'
import {
  USER_NAME_LOCALSTORAGE_KEY,
  getAccessTokenFromCookie,
  getAdminUserCredentials,
  getUserIdFromLocalStorage,
  goToDashboard,
} from './helpers/testUser'

const TEAM_NAME = 'swc-e2e-team-' + uuidv4()
const INVITATION_MESSAGE = 'swc e2e test team invitation' + uuidv4()

let adminPage: Page
let userPage: Page

test.describe('Teams', () => {
  test.beforeAll(async ({ browser }) => {
    adminPage = await browser.newPage({
      storageState: ADMIN_STORAGE_STATE,
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

      return userName
    })

    const adminUserName = await test.step('should get admin name', async () => {
      const { adminUserName } = getAdminUserCredentials()
      return adminUserName
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

    await test.step('user should invite admin to team', async () => {
      await userPage.getByRole('button', { name: 'Team Actions' }).click()
      await userPage.getByRole('link', { name: 'Invite User' }).click()

      await userPage
        .getByRole('textbox', { name: 'Enter a name or email address...' })
        .type(adminUserName!)
      await userPage
        .getByRole('menuitem', { name: `@${adminUserName}` })
        .click()
      const loadInvitedUser = userPage.getByText('Loading...')
      await expect(loadInvitedUser).toBeVisible()
      await expect(loadInvitedUser).not.toBeVisible()

      await userPage.getByLabel('Invitation Message').type(INVITATION_MESSAGE)

      const spinner = userPage.locator('.modal-body > .spinner')
      await Promise.all([
        expect(spinner).toBeVisible(),
        userPage.getByRole('button', { name: 'Send Invitation(s)' }).click(),
      ])

      await expect(spinner).not.toBeVisible()
      await expect(userPage.getByText('Invitation(s) Sent')).toBeVisible()
    })

    await test.step('user should view pending invitations', async () => {
      await expect(userPage.getByText('Pending Invitations')).toBeVisible({
        timeout: testInfo.timeout * 3, // add extra timeout, so backend can finish sending request
      })
      const row = userPage.getByRole('row', { name: adminUserName! })
      await expect(row).toBeVisible()
      await expect(row.getByText(INVITATION_MESSAGE)).toBeVisible()
    })

    await test.step('admin should accept team invitation', async () => {
      await goToDashboard(adminPage)

      await Promise.all([
        expect(
          adminPage.getByRole('heading', { name: 'Your Teams' }),
        ).toBeVisible(),
        adminPage.getByLabel('Teams').click(),
      ])
      await adminPage.waitForTimeout(2 * 1000) // allow time for responses to return

      // get row for this invitation
      // ...in case multiple test suite users have invited admin user at the same time
      const row = adminPage.getByRole('row', { name: TEAM_NAME })
      await expect(row.getByText(INVITATION_MESSAGE)).toBeVisible()

      await row.getByRole('button', { name: 'Join' }).click()
      await expect(row).not.toBeVisible()
    })

    await test.step('admin should view team page', async () => {
      const teamLink = adminPage.getByRole('link', { name: TEAM_NAME })

      // handle case where occasionally two team links are shown
      // ...before resolving to one team link
      await expect
        .poll(async () => {
          return await teamLink.count()
        })
        .toBe(1)

      await teamLink.click()
      await adminPage.waitForTimeout(2 * 1000) // allow time for responses to return

      await expect(
        adminPage.locator('h3').filter({
          hasText: TEAM_NAME,
        }),
      ).toBeVisible()
      await expect(adminPage.getByText('2 team members')).toBeVisible()

      await expect(
        adminPage.getByRole('heading', { name: 'Managers' }),
      ).toBeVisible()
      await expect(
        adminPage.getByRole('link', { name: userName! }),
      ).toBeVisible()

      await expect(
        adminPage.getByRole('heading', { name: 'Members' }),
      ).toBeVisible()
      await expect(
        adminPage.getByRole('link', { name: adminUserName! }),
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
    const adminUserId = await getUserIdFromLocalStorage(adminPage)
    const adminAccessToken = await getAccessTokenFromCookie(adminPage)

    const userUserId = await getUserIdFromLocalStorage(userPage)
    const userAccessToken = await getAccessTokenFromCookie(userPage)
    const userName = await getLocalStorage(userPage, USER_NAME_LOCALSTORAGE_KEY)
    expect(userName).not.toBeNull()

    // delete team invitation: user -> admin
    await deleteTeamInvitationMessage(
      [adminUserId!],
      userName!,
      TEAM_NAME,
      userAccessToken,
      adminAccessToken,
    )

    // delete team acceptance: admin -> user
    await deleteTeamInviteAcceptanceMessage(
      [userUserId!],
      adminAccessToken,
      adminAccessToken,
    )

    // close pages
    await adminPage.close()
    await userPage.close()
  })
})
