import { Locator, Page, expect, test } from '@playwright/test'
import {
  AsynchronousJobStatus,
  QueryBundleRequest,
  QueryResultBundle,
} from '@sage-bionetworks/synapse-types'
import { defaultExpectTimeout } from '../../playwright.config'
import { selectDateUsingGoogUiDatePicker } from './dates'
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
    await expect(
      page.getByRole('button', { name: 'Add Table Or View' }),
    ).toBeVisible()
  })
}

export const expectTablePageLoaded = async (
  page: Page,
  tableName: string,
  tableDescription: string,
) => {
  await test.step('table has loaded', async () => {
    await expect(page.getByRole('button', { name: 'Table Tools' })).toBeVisible(
      { timeout: defaultExpectTimeout * 2 },
    )
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

export const getTableEditorRows = (page: Page) => {
  return page.locator('.table.synapseViewOrTable').getByRole('row')
}

// Enter table rows ------------------------------------------------------------
const enterTableDataRow = async (
  page: Page,
  tableCells: Locator,
  columnsSchemaConfig: ColumnSchemaConfig[],
  tableData: TableDataConfig,
) => {
  for (let index = 0; index < columnsSchemaConfig.length; index++) {
    const config = columnsSchemaConfig[index]
    const cell = tableCells.nth(index + 1) // shift right for checkboxes
    let value = tableData[config.name]

    if (config.type === 'Date') {
      await selectTableDate(page, cell, value as string)
    } else if (config.type.includes('List')) {
      await enterTableListValues(page, cell, value)
    } else if (config.restrictValues || config.type === 'Boolean') {
      if (value == undefined) {
        value = 'nothing selected'
      }
      await selectTableRestrictValues(cell, value as string)
    } else {
      await enterTableValue(cell, value)
    }
  }
}

export const enterTableData = async (
  page: Page,
  columnsSchemaConfig: ColumnSchemaConfig[],
  tableData: TableDataConfig[],
) => {
  const tableRows = getTableEditorRows(page)

  // skip initial empty column
  await expectTableColumnHeaders(tableRows, columnsSchemaConfig, 1)

  await test.step('enter test data', async () => {
    const addRowButton = page.getByRole('button', { name: 'Add Row' })
    for (let index = 0; index < tableData.length; index++) {
      const item = tableData[index].item.toString()

      await test.step(`enter test data: ${item}`, async () => {
        await addRowButton.click()

        // start from 1 so column headers in first row are skipped
        const cells = tableRows.nth(index + 1).getByRole('cell')
        await enterTableDataRow(
          page,
          cells,
          columnsSchemaConfig,
          tableData[index],
        )
      })
    }
  })
}

// Enter individual table cells ------------------------------------------------
// non-list type, no restricted value
export const enterTableValue = async (
  cell: Locator,
  value: string | number | string[],
) => {
  await cell.locator('.form-control').fill(value.toString())
}

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
    const month = date.toLocaleString('default', { month: 'short' })
    const day = date.getDate()

    await cell.click()
    await selectDateUsingGoogUiDatePicker(page, year, month, day)
  })
}

export const selectTableRestrictValues = async (
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
        .getByRole('textbox', {
          name: 'Paste comma or tab delimited values here.',
        })
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
    await expect(getTableEditorHeader(page)).not.toBeVisible({
      timeout: defaultExpectTimeout * 3, // allow time for changes to be applied
    })
    await expect(page.getByText('Applying changes...')).not.toBeVisible()
  })
}

// Validate table data ---------------------------------------------------------
const getTableRows = (page: Page) => {
  return page.getByTestId('SynapseTable').getByRole('row')
}

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
        const expectedText = await test.step('format expected text', () => {
          let expectedText = tableData[columnName!]
          const columnSchema = columnsSchemaConfig.find(
            config => config.name === columnName!,
          )

          // handle null
          if (expectedText == null) {
            if (columnSchema?.defaultValue) {
              expectedText = columnSchema.defaultValue
            } else {
              return '–'
            }
          }

          // handle lists
          if (Array.isArray(expectedText)) {
            expectedText = expectedText.join(', ')
          }

          // handle dates
          if (columnSchema?.type === 'Date') {
            const date = dateStringToDate(expectedText.toString())
            expectedText = `${
              date.getMonth() + 1
            }/${date.getDate()}/${date.getFullYear()}`
          }

          return expectedText
        })

        const tableCell = await test.step('get table cell to check', () => {
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

export const expectTableDataCorrect = async (
  page: Page,
  columnsSchemaConfig: ColumnSchemaConfig[],
  tableData: TableDataConfig[],
) => {
  await test.step('confirm table data', async () => {
    const tableRows = getTableRows(page)
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
          (
            (await response.json()) as AsynchronousJobStatus<
              QueryBundleRequest,
              QueryResultBundle
            >
          ).responseBody?.queryResult !== undefined &&
          (
            (await response.json()) as AsynchronousJobStatus<
              QueryBundleRequest,
              QueryResultBundle
            >
          ).jobState === 'COMPLETE' &&
          (
            (await response.json()) as AsynchronousJobStatus<
              QueryBundleRequest,
              QueryResultBundle
            >
          ).requestBody.query.sql === query,
      )
    }
  })
}

export const confirmTableDataDimensions = async (
  page: Page,
  nDataRows: number,
  nCols: number,
) => {
  await test.step('confirm table dimensions', async () => {
    const tableRows = getTableRows(page)
    await expect(
      tableRows.first().getByRole('cell'),
      `should have ${nCols} column(s)`,
    ).toHaveCount(nCols)
    await expect(
      tableRows,
      `should have column headers and ${nDataRows} data row(s)`,
    ).toHaveCount(nDataRows + 1)
  })
}
