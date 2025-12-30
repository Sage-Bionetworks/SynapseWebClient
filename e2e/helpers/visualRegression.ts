import AxeBuilder from '@axe-core/playwright'
import { Page, expect } from '@playwright/test'
import { waitForInitialPageLoad } from './utils'

// ======================================================
// Constants
// ======================================================
export const LONGER_WAIT_TIME = 2000
export const SHORTER_WAIT_TIME = 500
export const PAGE_SELECTOR = '#rootPanel .rootPanel'
export const PROJECT_HEADER_SELECTOR = '.pageHeader'
export const PROJECT_TAB_PAGE_SELECTOR = '.tab-content'
export const MAX_DIFF_PIXEL_RATIO = 0.03

// ======================================================
// Helper functions
// ======================================================
export const formatScreenshotName = (
  prefix: string,
  action: string,
  suffix = 'png',
) => {
  return `${prefix}-${action.toLowerCase().replace(/\s+/g, '-')}.${suffix}`
}

export const navigateToProjectTab = async (
  page: Page,
  projectId: string,
  tab: string,
) => {
  await page.goto(`/Synapse:${projectId}/${tab}/`)
  await waitForInitialPageLoad(page)
  await page.waitForTimeout(LONGER_WAIT_TIME)
}

export async function testPageVisualAndAccessibility(
  page: Page,
  url: string,
  name: string,
  options: { fullPage?: boolean; waitTime?: number; selector?: string } = {},
) {
  const { fullPage = true, waitTime = 500, selector } = options

  const currentUrl = page.url()

  // Avoid reloading if we're already on the target page
  if (!currentUrl.endsWith(url)) {
    await page.goto(url)
    await waitForInitialPageLoad(page)
    await page.waitForTimeout(waitTime)
  }

  if (selector) {
    // Wait for the specific element and take screenshot of it
    await page.waitForSelector(selector, { timeout: 10000 })
    const element = page.locator(selector)
    await expect(element).toHaveScreenshot(`${name}.png`, {
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  } else {
    // Take full page or partial page screenshot
    await expect(page).toHaveScreenshot(`${name}.png`, {
      fullPage,
      maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
    })
  }

  // Accessibility snapshot
  const accessibilityScanResults = selector
    ? await new AxeBuilder({ page }).include(selector).analyze()
    : await new AxeBuilder({ page }).analyze()

  // Normalize dynamic CSS class names to prevent false failures between builds
  const normalizedViolations = accessibilityScanResults.violations.map(
    violation => ({
      ...violation,
      nodes: violation.nodes.map(node => ({
        ...node,
        html:
          node.html
            // normalize css class names
            ?.replace(/css-[a-z0-9]+-/g, 'css-NORMALIZED-')
            // normalize dynamic syn ids
            ?.replace(/syn\d+/g, 'syn00000000')
            ?.replace(
              /swc-e2e-project-entity-[a-f0-9-]{36}/g,
              'swc-e2e-project-entity-NORMALIZED-UUID',
            ) || node.html,
      })),
    }),
  )

  const violationsJson = JSON.stringify(normalizedViolations, null, 2)

  expect(violationsJson).toMatchSnapshot(`${name}-a11y.json`)
}

export const pageElementInteractions = async (
  userPage: Page,
  name: string,
  screenshotName: string,
  options: {
    selector?: string
    elementType?: 'button' | 'link' | 'menuitem'
    exact?: boolean
    needsEscape?: boolean
  } = {},
) => {
  const {
    selector,
    elementType = 'button',
    exact = false,
    needsEscape = elementType === 'button',
  } = options

  // If selector is provided, scope element search to that area
  const element = selector
    ? userPage.locator(selector).getByRole(elementType, {
        name: exact ? name : new RegExp(`${name}`, 'i'),
        exact,
      })
    : userPage.getByRole(elementType, {
        name: exact ? name : new RegExp(`${name}`, 'i'),
        exact,
      })

  // Use first() for links to avoid strict mode violations with multiple matches
  let targetElement = elementType === 'link' ? element.first() : element

  // Check if element exists before trying to click
  const isElementVisible = await targetElement
    .isVisible({ timeout: 3000 })
    .catch(() => false)

  // Fallback: try to find by text content if role-based search fails
  if (!isElementVisible) {
    console.log(
      `${elementType} with role not found, trying text-based search for "${name}"`,
    )

    // Try scoped search first
    const fallbackElement = selector
      ? userPage.locator(selector).locator(`text="${name}"`)
      : userPage.locator(`text="${name}"`)

    let isFallbackVisible = await fallbackElement
      .isVisible({ timeout: 1000 })
      .catch(() => false)

    // If scoped fails, try global search
    if (!isFallbackVisible && selector) {
      console.log(`Scoped search failed, trying global search for "${name}"`)
      const globalFallback = userPage.locator(`text="${name}"`)
      const globalVisible = await globalFallback
        .isVisible({ timeout: 1000 })
        .catch(() => false)

      if (globalVisible) {
        targetElement = globalFallback
        console.log(`Found "${name}" using global text-based search`)
      }
    } else if (isFallbackVisible) {
      targetElement = fallbackElement
      console.log(`Found "${name}" using scoped text-based search`)
    }
  }

  if (await targetElement.isVisible({ timeout: 12000 })) {
    // For buttons, wait for them to be enabled (they might start disabled during loading)
    if (elementType === 'button') {
      try {
        await targetElement.waitFor({ state: 'attached', timeout: 3000 })
        await expect(targetElement).toBeEnabled({ timeout: 5000 })
      } catch (error) {
        console.log(
          `${elementType} "${name}" is visible but remains disabled after waiting: ${error}`,
        )
        return false
      }
    }

    await targetElement.click()
    await userPage.waitForTimeout(1000)

    if (selector) {
      const screenshotElement = userPage.locator(selector)
      await expect(screenshotElement).toHaveScreenshot(screenshotName, {
        maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
      })
    } else {
      // Take full page screenshot
      await expect(userPage).toHaveScreenshot(screenshotName, {
        maxDiffPixelRatio: MAX_DIFF_PIXEL_RATIO,
      })
    }

    if (needsEscape) {
      await userPage.keyboard.press('Escape')
      await userPage.waitForTimeout(300)
    }

    return true
  } else {
    console.log(`${elementType} "${name}" not found or not visible`)
    return false
  }
}
