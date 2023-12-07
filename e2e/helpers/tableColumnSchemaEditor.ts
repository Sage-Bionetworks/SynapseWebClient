import { Locator, Page, expect, test } from '@playwright/test'

// Types -----------------------------------------------------------------------
type FacetType = 'Range' | 'Values'
export type ColumnSchemaConfig = {
  name: string
  type: string
  size?: number
  maxListLength?: number
  defaultValue?: string
  restrictValues?: string[]
  facet?: FacetType
}
type ColumnSchemaKey = keyof ColumnSchemaConfig
const columnNameToSchemaKey: Record<string, ColumnSchemaKey | string> = {
  'Column ID': 'id',
  'Column Name': 'name',
  'Column Type': 'type',
  Size: 'size',
  'Max List Length': 'maxListLength',
  'Default Value': 'defaultValue',
  'Restrict Values': 'restrictValues',
  Facet: 'facet',
}

// Set Individual Column Schema ------------------------------------------------
const enterColumnName = async (
  page: Page,
  columnIndex: number,
  columnName: string,
) => {
  await test.step('enter column name', async () => {
    const columnNameFields = page.getByPlaceholder('Column name')
    expect(columnNameFields).toHaveCount(columnIndex + 1)
    await columnNameFields.nth(columnIndex).fill(columnName)
  })
}

const selectColumnType = async (
  page: Page,
  columnIndex: number,
  columnType: string,
) => {
  await test.step('select column type', async () => {
    const columnTypeFields = page.getByLabel('Column Type')
    await columnTypeFields.nth(columnIndex).click()
    await page.getByRole('option', { name: columnType, exact: true }).click()
  })
}

const setColumnMaxSize = async (
  page: Page,
  columnIndex: number,
  maxSize: number,
) => {
  await test.step('set column size', async () => {
    const columnMaxSizeFields = page.getByLabel('Maximum Size')
    await columnMaxSizeFields.nth(columnIndex).fill(maxSize.toString())
  })
}

const setColumnMaxListLength = async (
  page: Page,
  columnIndex: number,
  maxListLength: number,
) => {
  await test.step('set column max list length', async () => {
    const columnMaxListLengthFields = page.getByLabel('Maximum List Length')
    await columnMaxListLengthFields
      .nth(columnIndex)
      .fill(maxListLength.toString())
  })
}

const setColumnDefaultValue = async (
  page: Page,
  columnIndex: number,
  defaultValue: string,
  columnType: string,
) => {
  await test.step('set column default value', async () => {
    const columnDefaultValueField = page
      .getByLabel('Default Value')
      .nth(columnIndex)
    if (columnType === 'Boolean') {
      await columnDefaultValueField.click()
      await page.getByRole('option', { name: defaultValue }).click()
    } else {
      await columnDefaultValueField.fill(defaultValue)
    }
  })
}

const enterColumnRestrictValues = async (
  page: Page,
  columnIndex: number,
  values: string[],
) => {
  await test.step('enter restrict values', async () => {
    await page.getByLabel('Restrict Values').nth(columnIndex).click()

    const editValuesHeader = page.getByText('Edit Values')
    await expect(editValuesHeader).toBeVisible()

    await page.getByRole('button', { name: 'Paste new values' }).click()
    await page
      .getByPlaceholder('Place comma or tab delimited values here')
      .fill(values.join(','))
    await page.getByRole('button', { name: 'Add', exact: true }).click()
    await page.getByRole('button', { name: 'OK' }).click()

    await expect(editValuesHeader).not.toBeVisible()
  })
}

const selectColumnFacet = async (
  page: Page,
  columnIndex: number,
  facetType: FacetType,
) => {
  await test.step('set column facet', async () => {
    const columnFacetFields = page.getByLabel('Facet Type')
    await columnFacetFields.nth(columnIndex).click()
    await page.getByRole('option', { name: facetType }).click()
  })
}

// Set Table Schema ------------------------------------------------------------
const enterColumnSchema = async (
  page: Page,
  columnConfig: ColumnSchemaConfig,
  columnIndex: number,
) => {
  if (columnConfig.name) {
    await enterColumnName(page, columnIndex, columnConfig.name)
  }
  if (columnConfig.type) {
    await selectColumnType(page, columnIndex, columnConfig.type)
  }
  if (columnConfig.size) {
    await setColumnMaxSize(page, columnIndex, columnConfig.size)
  }
  if (columnConfig.maxListLength) {
    await setColumnMaxListLength(page, columnIndex, columnConfig.maxListLength)
  }
  if (columnConfig.defaultValue) {
    await setColumnDefaultValue(
      page,
      columnIndex,
      columnConfig.defaultValue,
      columnConfig.type,
    )
  }
  if (columnConfig.restrictValues) {
    await enterColumnRestrictValues(
      page,
      columnIndex,
      columnConfig.restrictValues,
    )
  }
  if (columnConfig.facet) {
    await selectColumnFacet(page, columnIndex, columnConfig.facet)
  }
}

export const addColumnsFromColumnSchemaConfig = async (
  page: Page,
  columnConfigs: ColumnSchemaConfig[],
  columnStartingIndex: number = 0,
) => {
  await test.step('add columns from column schema config', async () => {
    const addColumnButton = page.getByRole('button', {
      name: 'Add Column',
    })

    for (
      let columnIndex = columnStartingIndex;
      columnIndex < columnConfigs.length;
      columnIndex++
    ) {
      const columnConfig = columnConfigs[columnIndex]
      await test.step(`add column: ${columnConfig.name}`, async () => {
        await addColumnButton.click()
        await enterColumnSchema(page, columnConfig, columnIndex)
      })
    }
  })
}

// Validate Table Column Schema ------------------------------------------------
const expectColumnSchemaKeyMatchesConfig = async (
  columnSchemaConfig: ColumnSchemaConfig,
  columnSchemaKey: ColumnSchemaKey,
  schemaCell: Locator,
) => {
  if (columnSchemaConfig[columnSchemaKey]) {
    await test.step(`confirm column schema key: ${columnSchemaKey}`, async () => {
      let expectedValues = columnSchemaConfig[columnSchemaKey]!

      // handle display of types that contain spaces
      // e.g. 'String List' -> 'StringList'
      if (columnSchemaKey === 'type') {
        expectedValues = expectedValues.toString().replace(' ', '')
      }

      // handle display of restrict values
      if (
        columnSchemaKey === 'restrictValues' &&
        Array.isArray(expectedValues)
      ) {
        expectedValues = expectedValues.join(', ')
      }

      await expect(schemaCell).toHaveText(expectedValues.toString())
    })
  }
}

const expectColumnSchemaMatchesConfig = async (
  columnSchemaConfig: ColumnSchemaConfig,
  schemaCells: Locator,
  columnSchemaIndexMap: Record<ColumnSchemaKey, number>,
) => {
  await test.step(`confirm column schema: ${columnSchemaConfig.name}`, async () => {
    for (const columnSchemaKey in columnSchemaConfig) {
      const columnSchemaIndex = columnSchemaIndexMap[columnSchemaKey]

      await expectColumnSchemaKeyMatchesConfig(
        columnSchemaConfig,
        columnSchemaKey as ColumnSchemaKey,
        schemaCells.nth(columnSchemaIndex),
      )
    }
  })
}

const expectAllColumnSchemasMatchConfig = async (
  columnsSchemaConfig: ColumnSchemaConfig[],
  schemaRows: Locator,
  columnSchemaIndexMap: Record<ColumnSchemaKey, number>,
) => {
  for (let i = 0; i < columnsSchemaConfig.length; i++) {
    // skip the first row, which contains the table headers
    const schemaCells = schemaRows.nth(i + 1).getByRole('cell')
    await expectColumnSchemaMatchesConfig(
      columnsSchemaConfig[i],
      schemaCells,
      columnSchemaIndexMap,
    )
  }
}

export const openTableColumnSchema = async (page: Page) => {
  await test.step('open table column schema', async () => {
    await page.getByRole('button', { name: 'Show Table Schema' }).click()
    await expect(page.getByText('Schema', { exact: true })).toBeVisible()
  })
}

const getTableColumnSchemaRows = async (page: Page) => {
  return page.getByRole('table').filter({ hasText: 'Column' }).getByRole('row')
}

export const expectTableSchemaCorrect = async (
  page: Page,
  columnsSchemaConfig: ColumnSchemaConfig[],
) => {
  await test.step('table has expected schema', async () => {
    await openTableColumnSchema(page)
    const schemaRows = await getTableColumnSchemaRows(page)

    const columnSchemaIndexMap =
      await test.step('confirm schema column names', async () => {
        const columnNamesExpected = Object.keys(columnNameToSchemaKey)
        let columnSchemaIndexMap: Partial<Record<ColumnSchemaKey, number>> = {}
        const columnNames = schemaRows.nth(0).getByRole('cell')
        for (let i = 0; i < columnNamesExpected.length; i++) {
          // the initial column is empty in UI, so start from 1
          const columnName = await columnNames.nth(i + 1).textContent()
          expect(columnName).toBe(columnNamesExpected[i])
          columnSchemaIndexMap[columnNameToSchemaKey[columnName!]] = i + 1
        }
        return columnSchemaIndexMap as Record<ColumnSchemaKey, number>
      })

    await test.step('confirm schema rows', async () => {
      await expectAllColumnSchemasMatchConfig(
        columnsSchemaConfig,
        schemaRows,
        columnSchemaIndexMap,
      )
    })
  })
}
