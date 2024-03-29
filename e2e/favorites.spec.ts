import { Page, expect } from '@playwright/test'
import { testAuth } from './fixtures/authenticatedUserPages'
import { entityUrlPathname } from './helpers/entities'
import {
  expectDiscussionPageLoaded,
  goToDashboard,
  goToDashboardPage,
  reloadDashboardPage,
} from './helpers/testUser'
import { Project } from './helpers/types'

// Public project owned by swc-e2e-admin on backend dev stack.
// In the future, consider creating/deleting a new public project within the test instead.
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
  await expect(page.getByRole('heading', { name: projectName })).toBeVisible()
  await expectDiscussionPageLoaded(page, projectId)
}

function getFavoriteStarSpiner(page: Page) {
  return page.locator('.pageHeader').locator('.spinner:visible')
}

testAuth.describe('Favorites', () => {
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
        await goToDashboardPage(userPage, entityUrlPathname(PUBLIC_PROJECT.id))
        await expectProjectPageLoaded(
          userPage,
          PUBLIC_PROJECT.name,
          PUBLIC_PROJECT.id,
        )
      })

      await testAuth.step('user favorites project', async () => {
        await expect(addFavorite).toBeVisible()
        await expect(removeFavorite).not.toBeVisible()

        const spinner = getFavoriteStarSpiner(userPage)
        await addFavorite.click()
        await expect(spinner).not.toBeVisible()

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

        const spinner = getFavoriteStarSpiner(userPage)
        await removeFavorite.click()
        await expect(spinner).not.toBeVisible()

        await expect(addFavorite).toBeVisible()
        await expect(removeFavorite).not.toBeVisible()
      })

      await testAuth.step(
        'User does not have project listed on their favorites page',
        async () => {
          await goToFavorites(userPage)

          // The page needs to be reloaded for the favorites list to update
          await reloadDashboardPage(userPage)
          await expectFavoritesPageLoaded(userPage)

          await expect(projectLink).not.toBeVisible()
        },
      )
    },
  )
})
