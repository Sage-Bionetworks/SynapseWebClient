import { expect, test } from '@playwright/test'
import { testAuth } from './fixtures/authenticatedUserPages'
import { deleteEntity, generateEntityName } from './helpers/entities'
import {
  setupProject,
  teardownProjectsAndFileHandles,
} from './helpers/setupTeardown'
import {
  ColumnSchemaConfig,
  addColumnsFromColumnSchemaConfig,
  expectTableSchemaCorrect,
  openTableColumnSchema,
} from './helpers/tableColumnSchemaEditor'
import {
  TableDataConfig,
  confirmTableDataDimensions,
  enterTableData,
  enterTableValue,
  expectTableDataCorrect,
  expectTableItemNumberCorrect,
  expectTablePageLoaded,
  expectTablesPageLoaded,
  getDefaultQuery,
  getTableEditorRows,
  getTablesPath,
  openTableEditor,
  runQuery,
  saveTableDataChanges,
  toggleQueryEditor,
} from './helpers/tables'
import {
  dismissAlert,
  getAdminPAT,
  goToDashboardPage,
  toggleIntoExperimentalMode,
} from './helpers/testUser'
import { Project } from './helpers/types'

let userProject: Project
let fileHandleIds: string[] = []
let tableEntityId: string | undefined = undefined

const noTablesText =
  'There are no Tables associated with this project. Any Tables you create in this project will appear here'

test.describe('Tables', () => {
  testAuth.beforeAll(async ({ browser, storageStatePaths }) => {
    userProject = await setupProject(browser, 'swc-e2e-user', storageStatePaths)
  })

  testAuth.afterAll(async ({ browser }) => {
    test.slow()
    if (tableEntityId) {
      const page = await browser.newPage()
      await deleteEntity(tableEntityId, true, getAdminPAT(), page)
      await page.close()
    }
    if (userProject.id) {
      await teardownProjectsAndFileHandles(
        browser,
        [userProject],
        fileHandleIds,
      )
    }
  })

  testAuth('should allow table CRUD', async ({ userPage }) => {
    test.slow()

    const tableName = generateEntityName('table')
    const tableDescription = 'table test description'

    const initialColumnsSchemaConfig: ColumnSchemaConfig[] = [
      { name: 'item', type: 'String', size: 20 },
      { name: 'quantity', type: 'Integer', defaultValue: '500' },
      // { name: 'on_sale', type: 'Boolean', defaultValue: 'false' }, - currently broken, SWC-6630
      // { name: 'size', type: 'Integer', restrictValues: '1,2,3,4' }, - currently broken, SWC-6622
      { name: 'stock_date', type: 'Date', facet: 'Range' },
      {
        name: 'color',
        type: 'String',
        size: 50,
        /* 
        // TODO: uncomment when SWC-6627 fixed
        // able to enter restrictValues via Playwright test in chromium, 
        // ...but not in firefox/webkit
        restrictValues: [ 
          'blue',
          'green',
          'indigo',
          'orange',
          'red',
          'violet',
          'yellow',
        ], */
      },
      { name: 'search_terms', type: 'String List', maxListLength: 10 },
    ]

    const initialTableData: TableDataConfig[] = [
      {
        item: 'beanie',
        quantity: 125,
        stock_date: '2012-04-28',
        color: 'blue',
        search_terms: ['fleece', 'hand wash'],
      },
      {
        item: 'hat',
        quantity: 250,
        stock_date: '2023-12-21',
        color: 'yellow',
        search_terms: ['recycled', 'five-panel', 'cotton', 'machine wash'],
      },
      {
        item: 'socks',
        quantity: 475,
        stock_date: '2022-10-02',
        color: 'green',
        search_terms: ['wool', 'machine wash'],
      },
    ]

    const updatedColumnsSchemaConfig = [
      ...initialColumnsSchemaConfig,
      { name: 'reorder_quantity', type: 'Integer', defaultValue: '25' },
    ]
    const updatedTableData = initialTableData.map((data, index) => ({
      ...data,
      reorder_quantity: '25',
    }))

    await test.step('User goes to tables page', async () => {
      await goToDashboardPage(userPage, getTablesPath(userProject.id))
      await expectTablesPageLoaded(userPage, userProject.id)
      await expect(userPage.getByText(noTablesText)).toBeVisible()
    })

    // TODO: remove this step after react-based table column schema editor is
    // released from experimental mode
    await test.step('Toggle into Experimental Mode to use react-based table column schema editor', async () => {
      await toggleIntoExperimentalMode(userPage)
      await expectTablesPageLoaded(userPage, userProject.id)
      await expect(userPage.getByText(noTablesText)).toBeVisible()
    })

    await test.step('User creates a table', async () => {
      const createTableHeader =
        await test.step('open table creation modal', async () => {
          await userPage.getByRole('button', { name: 'Add New...' }).click()
          await userPage.getByRole('menuitem', { name: 'Add Table' }).click()
          const createTableHeader = userPage.getByRole('heading', {
            name: 'Create Table',
          })
          await expect(createTableHeader).toBeVisible()
          return createTableHeader
        })

      await test.step('enter table name and description', async () => {
        await userPage
          .locator('div')
          .filter({ hasText: /^Name$/ })
          .getByRole('textbox')
          .fill(tableName)
        await userPage
          .locator('div')
          .filter({ hasText: /^Description$/ })
          .locator('textarea')
          .fill(tableDescription)
        await userPage.getByRole('button', { name: 'Next' }).click()
      })

      await test.step('create table schema', async () => {
        await addColumnsFromColumnSchemaConfig(
          userPage,
          initialColumnsSchemaConfig,
        )
      })

      await test.step('finish creating table', async () => {
        await userPage.getByRole('button', { name: 'Finish' }).click()
        await expect(
          userPage.getByText('Updating the table schema...'),
        ).toBeVisible()
        await expect(createTableHeader).not.toBeVisible()
      })
    })

    await test.step('Table has been created with expected schema', async () => {
      await expectTablePageLoaded(userPage, tableName, tableDescription)

      await test.step('get table entity id', async () => {
        tableEntityId =
          (await userPage
            .getByRole('row')
            .filter({ hasText: 'SynId' })
            .getByRole('cell')
            .nth(1)
            .textContent()) || undefined
        expect(tableEntityId).not.toBeUndefined()
      })

      await test.step('table is currently empty', async () => {
        await expect(
          userPage.getByRole('heading', { name: 'Items (0)' }),
        ).toBeVisible()
        await expect(
          userPage.getByText('This table is currently empty'),
        ).toBeVisible()
      })

      await expectTableSchemaCorrect(userPage, initialColumnsSchemaConfig)
    })

    await test.step('User adds data to the table', async () => {
      await openTableEditor(userPage)

      await enterTableData(
        userPage,
        initialColumnsSchemaConfig,
        initialTableData,
      )

      await test.step('column type is enforced', async () => {
        const rowIndex = 2
        const columnIndex = initialColumnsSchemaConfig.findIndex(
          config => config.name === 'quantity',
        )

        const cell =
          await test.step('change value to the incorrect type', async () => {
            const tableRows = await getTableEditorRows(userPage)
            const cell = tableRows
              .nth(rowIndex + 1) // shift down for column headers
              .getByRole('cell')
              .nth(columnIndex + 1) // shift right for checkboxes
            await enterTableValue(cell, 'STRING')
            return cell
          })

        await test.step('saving table triggers an error', async () => {
          await userPage.getByRole('button', { name: 'Save' }).click()
          await expect(cell).toHaveText('Value must be an integer.')
          await expect(
            userPage.getByText('See the error(s) above.'),
          ).toBeVisible()
        })

        await test.step('fix error', async () => {
          await enterTableValue(cell, initialTableData[rowIndex].quantity)
        })
      })

      await saveTableDataChanges(userPage)

      await expectTablePageLoaded(userPage, tableName, tableDescription)

      await expectTableItemNumberCorrect(userPage, initialTableData)
      await expectTableDataCorrect(
        userPage,
        initialColumnsSchemaConfig,
        initialTableData,
      )
    })

    await test.step('User edits table schema', async () => {
      await openTableColumnSchema(userPage)
      const tableSchemaEditorHeader =
        await test.step('open table column schema editor modal', async () => {
          await userPage.getByRole('button', { name: 'Edit Schema' }).click()
          const tableSchemaEditorHeader = userPage.getByRole('heading', {
            name: 'Edit Columns',
          })
          await expect(tableSchemaEditorHeader).toBeVisible()
          return tableSchemaEditorHeader
        })

      await test.step('add new columns', async () => {
        // wait for previously entered column schemas to appear
        // ...so addColumnsFromColumnSchemaConfig calculates correct starting column index
        await expect(
          userPage.getByPlaceholder('Column name'),
          'should load previously entered column schemas',
        ).toHaveCount(initialColumnsSchemaConfig.length)

        await addColumnsFromColumnSchemaConfig(
          userPage,
          updatedColumnsSchemaConfig,
        )
      })

      await test.step('save changes to table column schema', async () => {
        await userPage.getByRole('button', { name: 'Save' }).click()
        await expect(
          userPage.getByText('Updating the table schema...'),
        ).toBeVisible()

        await expect(tableSchemaEditorHeader).not.toBeVisible()
        await expectTablePageLoaded(userPage, tableName, tableDescription)

        await dismissAlert(
          userPage,
          'You made changes to the columns in this Table',
        )
      })

      await expectTableSchemaCorrect(userPage, updatedColumnsSchemaConfig)

      await test.step('default value is automatically populated in newly created column', async () => {
        await expectTableItemNumberCorrect(userPage, updatedTableData)
        await expectTableDataCorrect(
          userPage,
          updatedColumnsSchemaConfig,
          updatedTableData,
        )
      })
    })

    await test.step('User queries the table', async () => {
      const defaultQuery = getDefaultQuery(tableEntityId!)
      await toggleQueryEditor(userPage)

      await test.step('current query matches table', async () => {
        const queryInput = userPage.getByPlaceholder('Enter Query')
        await expect(queryInput).toHaveValue(defaultQuery)
      })

      await test.step('query table to select a single column', async () => {
        const column = updatedColumnsSchemaConfig[0].name
        await runQuery(userPage, `SELECT ${column} FROM ${tableEntityId}`)

        await test.step('confirm query results', async () => {
          await confirmTableDataDimensions(userPage, updatedTableData.length, 1)
          await expectTableDataCorrect(
            userPage,
            [updatedColumnsSchemaConfig[0]],
            updatedTableData.map(data => ({ [column]: data[column] })),
          )
        })
      })

      await test.step('query table to select rows where column greater than a value', async () => {
        const value = 200
        const column = 'quantity'
        await runQuery(
          userPage,
          `SELECT * FROM ${tableEntityId} WHERE ${column} > ${value}`,
        )

        await test.step('confirm query results', async () => {
          const expectedData = updatedTableData.filter(
            data => data[column] > value,
          )
          await confirmTableDataDimensions(
            userPage,
            expectedData.length,
            updatedColumnsSchemaConfig.length,
          )
          await expectTableDataCorrect(
            userPage,
            updatedColumnsSchemaConfig,
            expectedData,
          )
        })
      })

      await test.step('return table to default setup', async () => {
        await runQuery(userPage, defaultQuery, true)
        await expectTableDataCorrect(
          userPage,
          updatedColumnsSchemaConfig,
          updatedTableData,
        )
        await toggleQueryEditor(userPage)
      })
    })

    await test.step('User updates data in the table', async () => {
      const columnName = 'quantity'
      const rowIndex = updatedTableData.length - 1
      const columnIndex = updatedColumnsSchemaConfig.findIndex(
        config => config.name === columnName,
      )
      const newValue = '900'

      await openTableEditor(userPage)

      await test.step('edit row value', async () => {
        const tableRows = await getTableEditorRows(userPage)
        const cell = tableRows
          .nth(rowIndex + 1) // shift down for headers
          .getByRole('cell')
          .nth(columnIndex + 1) // shift right for checkboxes
        await enterTableValue(cell, newValue)
      })

      await saveTableDataChanges(userPage)
      await expectTablePageLoaded(userPage, tableName, tableDescription)

      await test.step('updated data is displayed', async () => {
        updatedTableData[rowIndex][columnName] = newValue
        await expectTableItemNumberCorrect(userPage, updatedTableData)
        await expectTableDataCorrect(
          userPage,
          updatedColumnsSchemaConfig,
          updatedTableData,
        )
      })
    })

    await test.step('User deletes the table', async () => {
      await test.step('delete table', async () => {
        await userPage.getByRole('button', { name: 'Table Tools' }).click()
        await userPage.getByRole('menuitem', { name: 'Delete Table' }).click()
      })

      await test.step('confirm deletion', async () => {
        await expect(
          userPage.getByRole('heading', { name: 'Confirm Deletion' }),
        ).toBeVisible()
        await expect(
          userPage.getByText(
            `Are you sure you want to delete Table "${tableName}"?`,
          ),
        ).toBeVisible()
        await userPage.getByRole('button', { name: 'Delete' }).click()
        await dismissAlert(userPage, 'The Table was successfully deleted')
      })

      await test.step('no tables are shown on Tables tab', async () => {
        await userPage.waitForURL(getTablesPath(userProject.id))
        await expect(userPage.getByText(noTablesText)).toBeVisible()
      })
    })
  })
})
