import { Page, expect } from '@playwright/test'

// Use after initially navigating to baseURL
// ...to give SWC time to compile, if necessary
export async function waitForInitialPageLoad(page: Page) {
  await expect(page.getByRole('heading', { name: 'Loading…' })).not.toBeVisible(
    { timeout: 2 * 60 * 1000 }, // ...wait 2 minutes if necessary
  )
}

export async function navigateToHomepageIfPageHasNotBeenLoaded(page: Page) {
  if (page.url() === 'about:blank') {
    // Navigate to homepage, so that localStorage and SRC are available
    await page.goto('/')
    await waitForInitialPageLoad(page)
  }
}
