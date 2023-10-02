import { Page, expect, test } from '@playwright/test'
import { testAuth } from './fixtures/authenticatedUserPages'
import { entityUrlPathname } from './helpers/entities'
import { goToDashboard } from './helpers/testUser'
import { Project } from './helpers/types'
import { waitForInitialPageLoad } from './helpers/utils'

const PUBLIC_PROJECT: Project = {
  name: 'swc-e2e-test-project',
  id: 'syn12366772',
}

async function expectFavoritesPageLoaded(page: Page) {
  await expect(
    page.getByRole('heading', { name: 'Your Favorites' }),
  ).toBeVisible()
  await expect(page.getByPlaceholder('Favorite Name')).toBeVisible()
}

async function goToFavorites(page: Page) {
  await page.getByLabel('Favorites', { exact: true }).click()
  await expectFavoritesPageLoaded(page)
}

async function expectProjectPageLoaded(
  page: Page,
  projectName: string,
  projectId: string,
) {
  await page.waitForURL(entityUrlPathname(projectId))
  await expect(page.getByRole('heading', { name: projectName })).toBeVisible()
  await page.waitForURL(
    `${entityUrlPathname(PUBLIC_PROJECT.id)}/discussion/default`,
  )
  await expect(page.getByRole('heading', { name: 'Discussion' })).toBeVisible()
}

test.describe('Favorites', () => {
  testAuth('should be visible', async ({ userPage }) => {
    await goToDashboard(userPage)
    await goToFavorites(userPage)
  })

  testAuth(
    'should be able to favorite and unfavorite a project',
    async ({ userPage }) => {
      const addFavorite = userPage.getByLabel(
        'Click the star to add this to your favorites',
      )
      const removeFavorite = userPage.getByLabel(
        'Click the star to remove this from your favorites',
      )
      const projectLink = userPage.getByRole('link', {
        name: PUBLIC_PROJECT.name,
      })

      await testAuth.step('user goes to public project', async () => {
        await userPage.goto(entityUrlPathname(PUBLIC_PROJECT.id))
        await waitForInitialPageLoad(userPage)
        await expectProjectPageLoaded(
          userPage,
          PUBLIC_PROJECT.name,
          PUBLIC_PROJECT.id,
        )
      })

      await testAuth.step('user favorites project', async () => {
        await expect(addFavorite).toBeVisible()
        await expect(removeFavorite).not.toBeVisible()

        await addFavorite.click()

        await expect(addFavorite).not.toBeVisible()
        await expect(removeFavorite).toBeVisible()
      })

      await testAuth.step(
        'user views project on their favorites page',
        async () => {
          await goToFavorites(userPage)
          await expect(projectLink).toBeVisible()
        },
      )

      await testAuth.step(
        'user navigates to project from favorites',
        async () => {
          await projectLink.click()

          await expectProjectPageLoaded(
            userPage,
            PUBLIC_PROJECT.name,
            PUBLIC_PROJECT.id,
          )
        },
      )

      await testAuth.step('user removes project as a favorite', async () => {
        await expect(addFavorite).not.toBeVisible()
        await expect(removeFavorite).toBeVisible()

        await removeFavorite.click()

        await expect(addFavorite).toBeVisible()
        await expect(removeFavorite).not.toBeVisible()
      })

      await testAuth.step(
        'User does not have project listed on their favorites page',
        async () => {
          await goToFavorites(userPage)
          await userPage.reload()
          await expectFavoritesPageLoaded(userPage)

          await expect(projectLink).not.toBeVisible()
        },
      )
    },
  )
})
