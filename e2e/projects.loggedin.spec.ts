import { Page, expect, test } from '@playwright/test'
import { v4 as uuidv4 } from 'uuid'
import { USER_STORAGE_STATE } from '../playwright.config'
import { goToDashboard } from './helpers/testUser'

const PROJECT_NAME = 'swc-e2e-project-' + uuidv4()

async function createProject(page: Page, projectName: string) {
  await page.getByLabel('Projects', { exact: true }).click()
  await page.getByLabel('Create a New Project').click()
  await page.getByLabel('Project Name').type(projectName)
  await page.getByRole('button', { name: 'Save' }).click()
}

test.describe('Projects', () => {
  test('should create a project', async ({ page }) => {
    await test.step('should create a project with a unique name', async () => {
      await goToDashboard(page)
      await createProject(page, PROJECT_NAME)
      await expect(
        page.getByRole('heading', { name: PROJECT_NAME }),
      ).toBeVisible()
    })

    await test.step('should show an alert when creating a project with an existing name', async () => {
      await createProject(page, PROJECT_NAME)
      await expect(
        page.getByText(/an entity with the name.*already exists/i),
      ).toBeVisible()
    })
  })

  test.afterAll(async ({ browser }) => {
    const page = await browser.newPage({ storageState: USER_STORAGE_STATE })

    // delete project
    await goToDashboard(page)
    await expect(
      page.getByRole('heading', { name: 'Your Projects' }),
    ).toBeVisible()

    await page.getByRole('button', { name: 'Created by me' }).click()
    await page.getByRole('link', { name: PROJECT_NAME }).click()
    await page.getByRole('button', { name: 'Project Tools' }).click()
    await page.getByRole('menuitem', { name: 'Delete Project' }).click()

    await expect(
      page.getByRole('heading', { name: 'Confirm Deletion' }),
    ).toBeVisible()
    await expect(
      page.getByText(
        `Are you sure you want to delete Project "${PROJECT_NAME}"?`,
      ),
    ).toBeVisible()

    await page.getByRole('button', { name: 'Delete', exact: true }).click()

    await expect(
      page.getByText('The Project was successfully deleted.'),
    ).toBeVisible()

    await page.getByRole('button', { name: 'Created by me' }).click()
    await expect(page.getByText(PROJECT_NAME)).not.toBeVisible()

    // clean up
    await page.close()
  })
})
