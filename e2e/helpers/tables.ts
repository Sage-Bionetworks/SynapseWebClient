import { Locator, Page, expect, test } from '@playwright/test'
import { defaultExpectTimeout } from '../../playwright.config'
import { entityUrlPathname } from './entities'
import { ColumnSchemaConfig } from './tableColumnSchemaEditor'

// Types -----------------------------------------------------------------------
export type TableDataConfig = Record<string, string | number | string[]>

// Load tables -----------------------------------------------------------------
export const getTablesPath = (projectId: string) => {
  return `${entityUrlPathname(projectId)}/tables/`
}

export const expectTablesPageLoaded = async (page: Page, projectId: string) => {
  await test.step('Tables page has loaded', async () => {
    await page.waitForURL(getTablesPath(projectId))
    await expect(page.getByRole('heading', { name: 'Tables' })).toBeVisible()
    await expect(
      page.getByRole('button', { name: 'Upload a Table' }),
    ).toBeVisible()
    await expect(page.getByRole('button', { name: 'Add New...' })).toBeVisible()
  })
}

export const expectTablePageLoaded = async (
  page: Page,
  tableName: string,
  tableDescription: string,
) => {
  await test.step('table has loaded', async () => {
    await expect(
      page.getByRole('button', { name: 'Table Tools' }),
    ).toBeVisible()
    await expect(page.locator('p').filter({ hasText: tableName })).toBeVisible()
    await expect(
      page
        .locator('div')
        .filter({ hasText: new RegExp(`^${tableDescription}$`, 'i') }),
    ).toBeVisible()

    await expect(page.getByText('Loading provenance...')).not.toBeVisible()
    await expect(
      page.getByRole('progressbar').locator('visible=true'),
    ).toHaveCount(0, { timeout: defaultExpectTimeout * 2 })
  })
}

// Enter table data ------------------------------------------------------------
const getTableEditorHeader = (page: Page) => {
  return page.getByRole('heading', {
    name: 'Edit Rows',
  })
}

export const openTableEditor = async (page: Page) => {
  return await test.step('open table editor', async () => {
    await page
      .getByRole('button', { name: 'Bulk Edit Table Cell Values' })
      .click()
    await expect(getTableEditorHeader(page)).toBeVisible()
  })
}

export const getTableEditorRows = async (page: Page) => {
  return page.locator('.table.synapseViewOrTable').getByRole('row')
}

// non-list type, no restricted value
export const enterTableValue = async (
  cell: Locator,
  value: string | number | string[],
) => {
  await cell.locator('.form-control').fill(value.toString())
}

const getDecadeIndex = (year: number) => Math.floor(year / 10)
const dateStringToDate = (dateString: string) => {
  return new Date(`${dateString}T12:00:00.000Z`)
}

export const selectTableDate = async (
  page: Page,
  cell: Locator,
  dateString: string,
) => {
  await test.step('select table date', async () => {
    const date = dateStringToDate(dateString)
    const year = date.getFullYear()
    const decade = getDecadeIndex(year)
    const currentDecade = getDecadeIndex(new Date().getFullYear())
    const decadeDifference = currentDecade - decade

    const month = date.toLocaleString('default', { month: 'short' })
    const day = date.getDate()

    await cell.click()
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
      await page
        .getByText(month, { exact: true })
        .locator('visible=true')
        .click()
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
  })
}

export const selectTableRestrictedValue = async (
  cell: Locator,
  option: string,
) => {
  await cell.getByRole('combobox').selectOption(option)
}

export const enterTableListValues = async (
  page: Page,
  cell: Locator,
  values: string | number | string[],
) => {
  await test.step('enter table list values', async () => {
    const modalHeader = await test.step('open edit modal', async () => {
      await cell.click()
      const modalHeader = page.getByRole('heading', {
        name: 'Edit Values',
      })
      await expect(modalHeader).toBeVisible()
      return modalHeader
    })

    await test.step('add values', async () => {
      await page.getByRole('button', { name: 'PASTE NEW VALUES' }).click()
      await page
        .getByRole('textbox', { name: 'Paste comma or tab delimited' })
        .fill(Array.isArray(values) ? values.join(',') : values.toString())
      await page.getByRole('button', { name: 'ADD', exact: true }).click()
    })

    await test.step('save changes', async () => {
      await page.getByRole('button', { name: 'OK' }).click()
      await expect(modalHeader).not.toBeVisible()
    })
  })
}

export const saveTableDataChanges = async (page: Page) => {
  await test.step('save table data changes', async () => {
    await page.getByRole('button', { name: 'Save' }).click()
    await expect(getTableEditorHeader(page)).not.toBeVisible()
    await expect(page.getByText('Applying changes...')).not.toBeVisible()
  })
}

// Validate table data ---------------------------------------------------------
export const expectTableColumnHeaders = async (
  tableRows: Locator,
  columnsSchemaConfig: ColumnSchemaConfig[],
  columnIndexOffset: number,
) => {
  await test.step('confirm headers match config', async () => {
    const headerCells = tableRows.first().getByRole('cell')
    for (
      let columnIndex = 0;
      columnIndex < columnsSchemaConfig.length;
      columnIndex++
    ) {
      await expect(headerCells.nth(columnIndex + columnIndexOffset)).toHaveText(
        columnsSchemaConfig[columnIndex].name,
      )
    }
  })
}

const expectTableRowCorrect = async (
  tableRows: Locator,
  rowIndex: number,
  columnsSchemaConfig: ColumnSchemaConfig[],
  tableData: TableDataConfig,
) => {
  await test.step(`confirm row ${rowIndex}`, async () => {
    for (
      let colIndex = 0;
      colIndex < Object.keys(tableData).length;
      colIndex++
    ) {
      const columnName = await tableRows
        .first()
        .getByRole('cell')
        .nth(colIndex)
        .textContent()
      await test.step(`confirm ${columnName}`, async () => {
        const expectedText =
          await test.step('format expected text', async () => {
            let expectedText = tableData[columnName!]

            // handle lists
            if (Array.isArray(expectedText)) {
              expectedText = expectedText.join(', ')
            }

            // handle dates
            const columnSchema = columnsSchemaConfig.find(
              config => config.name === columnName!,
            )
            if (columnSchema?.type === 'Date') {
              const date = dateStringToDate(expectedText.toString())
              expectedText = `${
                date.getMonth() + 1
              }/${date.getDate()}/${date.getFullYear()}`
            }

            return expectedText
          })

        const tableCell =
          await test.step('get table cell to check', async () => {
            return tableRows.nth(rowIndex).getByRole('cell').nth(colIndex)
          })

        await test.step('expand cell, if needed', async () => {
          const expandButton = tableCell.locator(
            '.ExpandableTableData__expandButton',
          )
          if (await expandButton.isVisible()) {
            await expandButton.click()
          }
        })

        await test.step('confirm cell contains expected text', async () => {
          // use toContainText rather than toHaveText
          // ...to handle Date values, which also display time when date was selected
          await expect(tableCell).toContainText(expectedText.toString())
        })
      })
    }
  })
}

export const expectTableRowsCorrect = async (
  page: Page,
  columnsSchemaConfig: ColumnSchemaConfig[],
  tableData: TableDataConfig[],
) => {
  await test.step('rows contain correct data', async () => {
    const tableRows = page.getByTestId('SynapseTable').getByRole('row')
    await expectTableColumnHeaders(tableRows, columnsSchemaConfig, 0)

    for (let dataIndex = 0; dataIndex < tableData.length; dataIndex++) {
      await expectTableRowCorrect(
        tableRows,
        dataIndex + 1,
        columnsSchemaConfig,
        tableData[dataIndex],
      )
    }
  })
}

export const expectTableItemNumberCorrect = async (
  page: Page,
  tableData: TableDataConfig[],
) => {
  await test.step('confirm table items number', async () => {
    await expect(
      page.getByRole('heading', {
        name: `Items (${tableData.length})`,
      }),
    ).toBeVisible()
  })
}

export const expectTableDataCorrect = async (
  page: Page,
  columnsSchemaConfig: ColumnSchemaConfig[],
  tableData: TableDataConfig[],
) => {
  await test.step('data is displayed', async () => {
    await expectTableRowsCorrect(page, columnsSchemaConfig, tableData)
  })
}

// Table Queries ---------------------------------------------------------------
export const toggleQueryEditor = async (page: Page) => {
  await test.step('toggle query editor', async () => {
    await page.getByLabel('Show / Hide the Advanced Query Editor').click()
  })
}

export const getDefaultQuery = (tableEntityId: string) => {
  return `SELECT * FROM ${tableEntityId}`
}

export const runQuery = async (
  page: Page,
  query: string,
  isCached: boolean = false,
) => {
  await test.step('run query', async () => {
    const queryInput = page.getByPlaceholder('Enter Query')
    await queryInput.fill(query)
    await queryInput.focus()
    await queryInput.press('Enter')

    await expect(queryInput).toHaveValue(query)

    if (!isCached) {
      await page.waitForResponse(
        async response =>
          response.url().includes('/repo/v1/asynchronous/job/') &&
          response.status() === 200 &&
          (await response.json()).responseBody?.queryResult &&
          (await response.json()).jobState === 'COMPLETE' &&
          (await response.json()).requestBody.query.sql === query,
      )
    }
  })
}
