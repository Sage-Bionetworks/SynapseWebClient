import { expect, test } from '@playwright/test'

test.describe('Projects', () => {
  test('should show alert when creating a new project with an existing name', async ({
    page,
  }) => {
    await page.goto('/')
    await page
      .getByRole('link', { name: 'View Your Dashboard' })
      .first()
      .click()
    await page.getByLabel('Projects').click()
    await page.getByLabel('Create a New Project').click()
    await page.getByLabel('Project Name').type('new project')
    await page.getByRole('button', { name: 'Save' }).click()
    await expect(
      page.getByText(/an entity with the name.*already exists/i),
    ).toBeVisible()
  })
})
