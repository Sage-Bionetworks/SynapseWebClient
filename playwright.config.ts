import { defineConfig, devices } from '@playwright/test'
import dotenv from 'dotenv'

export const USER_STORAGE_STATE = 'playwright/.auth/user.json'
export const USER_VALIDATED_STORAGE_STATE =
  'playwright/.auth/userValidated.json'

const baseURL = 'http://127.0.0.1:8888'

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 *
 * For local development only. In CI, GitHub Action will set env using values in GitHub Secrets.
 */
dotenv.config()

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './e2e',
  /* Timeout to allow portal enough time to compile when running locally */
  timeout: 5 * 60 * 1000,
  /* Increase expectation timeout on CI */
  expect: { timeout: process.env.CI ? 30 * 1000 : 5 * 1000 },
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Limit the number of failures on CI to save resources */
  maxFailures: process.env.CI ? 10 : undefined,
  /* Retries */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  // Concise 'dot' for CI, default 'list' when running locally
  reporter: process.env.CI ? [['list'], ['html']] : 'html',
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    baseURL: baseURL,

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    // Until reports are saved in a secure location, do not collect traces on CI, since login network requests include user credentials
    trace: process.env.CI ? 'off' : 'retain-on-failure',
  },

  /* Configure projects for major browsers */
  projects: [
    // Setup project
    { name: 'setup', testMatch: /.*\.setup\.ts/, teardown: 'cleanup' },

    // Tests that don't require authentication
    {
      name: 'chromium - logged out',
      use: {
        ...devices['Desktop Chrome'],
      },
      testMatch: /.*\.loggedout\.spec.ts/,
    },

    // Tests that require authentication
    {
      name: 'chromium - logged in',
      use: {
        ...devices['Desktop Chrome'],
        // Use prepared auth state.
        storageState: USER_STORAGE_STATE,
      },
      testMatch: /.*\.loggedin\.spec.ts/,
      dependencies: ['setup'],
    },

    // Clean up project
    {
      name: 'cleanup',
      use: { storageState: USER_STORAGE_STATE },
      testMatch: /.*\.cleanup\.ts/,
    },

    /* {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },

    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    }, */

    /* Test against mobile viewports. */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },

    /* Test against branded browsers. */
    // {
    //   name: 'Microsoft Edge',
    //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
    // },
    // {
    //   name: 'Google Chrome',
    //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
    // },
  ],
  webServer: {
    command: process.env.CI ? '' : 'mvn gwt:run',
    url: baseURL,
    reuseExistingServer: true, // on CI, will use the tomcat server
  },
})
