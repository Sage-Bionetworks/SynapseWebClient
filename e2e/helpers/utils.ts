import { Page, expect } from '@playwright/test'

export async function waitForSrcEndpointConfig(page: Page) {
  // window only available after page has initially loaded
  await navigateToHomepageIfPageHasNotBeenLoaded(page)
  // ensure that endpoint config is set,
  // ...so API calls point to the correct stack
  await expect(async () => {
    const response = await page.evaluate('window.SRC.OVERRIDE_ENDPOINT_CONFIG')
    expect(response).not.toBeUndefined()
  }).toPass()
}

// Use after initially navigating to baseURL
// ...to give SWC time to compile, if necessary
export async function waitForInitialPageLoad(page: Page) {
  await expect(page.getByRole('heading', { name: 'Loading…' })).not.toBeVisible(
    { timeout: 2 * 60 * 1000 }, // ...wait 2 minutes if necessary
  )
  // ensure that the SRC endpoint has been set
  // ...before navigation initiates API calls
  await waitForSrcEndpointConfig(page)
}

export async function navigateToHomepageIfPageHasNotBeenLoaded(page: Page) {
  if (page.url() === 'about:blank') {
    // Navigate to homepage, so that localStorage and SRC are available
    await page.goto('/')
    await waitForInitialPageLoad(page)
  }
}
