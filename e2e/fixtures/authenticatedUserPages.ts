import { test as base, type Page } from '@playwright/test'
import {
  USER_STORAGE_STATE,
  USER_VALIDATED_STORAGE_STATE,
} from '../../playwright.config'

type AuthenticatedUserPageFixtures = {
  userPage: Page
  validatedUserPage: Page
}

export const testAuth = base.extend<AuthenticatedUserPageFixtures>({
  userPage: async ({ browser }, use) => {
    const context = await browser.newContext({
      storageState: USER_STORAGE_STATE,
    })
    const userPage = await context.newPage()
    await use(userPage)
    await context.close()
  },
  validatedUserPage: async ({ browser }, use) => {
    const context = await browser.newContext({
      storageState: USER_VALIDATED_STORAGE_STATE,
    })
    const validatedUserPage = await context.newPage()
    await use(validatedUserPage)
    await context.close()
  },
})
