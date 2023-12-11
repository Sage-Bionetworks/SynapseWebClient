import { Page, test } from '@playwright/test'

const getDecadeIndex = (year: number) => Math.floor(year / 10)
export const selectDateUsingGoogUiDatePicker = async (
  page: Page,
  year: number,
  month: string,
  day: number,
) => {
  const decade = getDecadeIndex(year)
  const currentDecade = getDecadeIndex(new Date().getFullYear())
  const decadeDifference = currentDecade - decade

  const switchButton = page.locator('.switch:visible')

  await test.step('select year', async () => {
    await switchButton.click()
    await switchButton.click()

    if (decadeDifference !== 0) {
      await test.step('change decade', async () => {
        const arrowSelector =
          decadeDifference > 0 ? '.prev:visible' : '.next:visible'
        for (let i = 0; i < decadeDifference; i++) {
          await page.locator(arrowSelector).click()
        }
      })
    }

    await page
      .getByText(year.toString(), { exact: true })
      .locator('visible=true')
      .click()
  })

  await test.step('select month', async () => {
    await page.getByText(month, { exact: true }).locator('visible=true').click()
  })

  await test.step('select day', async () => {
    let dayLocator = page.getByRole('cell', {
      name: day.toString(),
      exact: true,
    })

    // handle case where day may also be visible in previous or next month
    // e.g. 1 or 30
    if ((await dayLocator.count()) > 1) {
      dayLocator = day > 15 ? dayLocator.nth(1) : dayLocator.first()
    }
    await dayLocator.click()
  })
}
