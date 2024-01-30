import { defineConfig, devices } from '@playwright/test'
import dotenv from 'dotenv'

export const baseURL = 'http://127.0.0.1:8888'

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 *
 * For local development only. In CI, GitHub Action will set env using values in GitHub Secrets.
 */
dotenv.config()

// FIXME: expect timeout not available in testInfo fixture in v1.39
// change tests to use testInfo fixture once expect settings are exposed,
// see: https://github.com/microsoft/playwright/issues/27915
export const defaultExpectTimeout = process.env.CI ? 60 * 1000 : 5 * 1000
export const defaultTestTimeout = 2 * 60 * 1000

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './e2e',
  /* Timeout to allow portal enough time to compile when running locally */
  timeout: defaultTestTimeout,
  /* Increase expectation timeout on CI */
  expect: { timeout: defaultExpectTimeout },
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Limit the number of failures on CI to save resources */
  maxFailures: process.env.CI ? 10 : undefined,
  /* Retries */
  retries: process.env.CI ? 1 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : 2,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  // Concise 'dot' for CI, default 'list' when running locally
  reporter: process.env.CI ? [['list'], ['blob']] : 'html',
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    baseURL: baseURL,

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    // Until reports are saved in a secure location, do not collect traces on CI, since login network requests include user credentials
    // Collect the trace only on the first retry to speed up slow webkit runs. See https://github.com/microsoft/playwright/issues/18119#issuecomment-1370734489
    trace: process.env.CI ? 'off' : 'on-first-retry',
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
      testMatch: /.*\.spec.ts/,
    },

    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
      testMatch: /.*\.spec.ts/,
    },

    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
      testMatch: /.*\.spec.ts/,
    },

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
    command: process.env.CI
      ? 'echo SWC not available at baseURL'
      : 'mvn gwt:run',
    url: baseURL,
    reuseExistingServer: true, // on CI, will use the tomcat server
    stdout: 'pipe',
  },
})
