import { Page } from '@playwright/test'
import { navigateToHomepageIfPageHasNotBeenLoaded } from './utils'

export async function setLocalStorage(page: Page, key: string, value: string) {
  await navigateToHomepageIfPageHasNotBeenLoaded(page)
  await page.evaluate(
    ([key, value]) => {
      localStorage.setItem(key, value)
    },
    [key, value],
  )
}

export async function getLocalStorage(page: Page, key: string) {
  await navigateToHomepageIfPageHasNotBeenLoaded(page)
  const value = await page.evaluate(
    ([key]) => {
      return localStorage.getItem(key)
    },
    [key],
  )
  return value
}
