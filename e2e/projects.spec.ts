import { Page, expect, test } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import { testAuth } from './fixtures/authenticatedUserPages'
import { deleteProject, getEntityIdFromPathname } from './helpers/entities'
import { dismissAlert, getAdminPAT, goToDashboard } from './helpers/testUser'

const PROJECT_NAME = 'swc-e2e-project-' + uuidv4()
let projectId: string | undefined = undefined

async function createProject(page: Page, projectName: string) {
  await page.getByLabel('Projects', { exact: true }).click()
  await page.getByLabel('Create a New Project').click()

  const projectNameInput = page.getByLabel('Project Name')
  await projectNameInput.fill(projectName)
  await expect(projectNameInput).toHaveValue(projectName)

  await page.getByRole('button', { name: 'Save' }).click()
}

// Run multiple describes in parallel, but run tests inside each describe in order
// ...tests within describe expect afterAll to be run with the same users, i.e. on the same worker
// https://playwright.dev/docs/api/class-test#test-describe-configure
test.describe.configure({ mode: 'serial' })

test.describe('Projects', () => {
  testAuth('should create a project', async ({ userPage }) => {
    await testAuth.step(
      'should create a project with a unique name',
      async () => {
        await goToDashboard(userPage)
        await createProject(userPage, PROJECT_NAME)
        await expect(
          userPage.getByRole('heading', { name: PROJECT_NAME }),
        ).toBeVisible()
        await expect(
          userPage.getByRole('button', { name: 'Delete Wiki Page' }),
        ).toBeVisible()
        projectId = getEntityIdFromPathname(userPage.url())
      },
    )

    await testAuth.step(
      'should show an alert when creating a project with an existing name',
      async () => {
        await createProject(userPage, PROJECT_NAME)
        await expect(
          userPage.getByText(/an entity with the name.*already exists/i),
        ).toBeVisible()
        await userPage.getByRole('button', { name: 'Cancel' }).click()
        await expect(
          userPage.getByText('Create a new Project', { exact: true }),
        ).not.toBeVisible()
      },
    )

    await testAuth.step('should delete project', async () => {
      await userPage.getByRole('button', { name: 'Project Tools' }).click()
      await userPage.getByRole('menuitem', { name: 'Delete Project' }).click()

      await expect(
        userPage.getByRole('heading', { name: 'Confirm Deletion' }),
      ).toBeVisible()
      await expect(
        userPage.getByText(
          `Are you sure you want to delete Project "${PROJECT_NAME}"?`,
        ),
      ).toBeVisible()

      await userPage
        .getByRole('button', { name: 'Delete', exact: true })
        .click()
    })

    await testAuth.step('confirm project deleted', async () => {
      await dismissAlert(userPage, 'The Project was successfully deleted.')
      await expect(userPage.getByText(PROJECT_NAME)).not.toBeVisible()
      projectId = undefined
    })
  })

  testAuth.afterAll(async ({ browser }) => {
    // if test failed before project was deleted, then delete project here
    if (projectId) {
      const context = await browser.newContext()
      const page = await context.newPage()
      await deleteProject(projectId, getAdminPAT(), page)
      await context.close()
    }
  })
})
