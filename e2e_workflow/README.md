# End-to-End Testing Workflow

## Dev Setup

### Local

1. Create a .env file with the following environment variables: `ADMIN_USERNAME` and `ADMIN_PASSWORD`.
2. Build SWC: `mvn clean install`
3. Run Tests\*: `yarn e2e`

\*Note: if running tests repeatedly without changing SWC, it will be faster to run SWC in a separate terminal (`mvn gwt:run`) and then run tests (`yarn e2e`), since Playwright will use the existing server and SWC won't need to recompile before tests are run.

The test suite will create a new test user with randomly generated username / password on the dev stack for tests that require an authenticated test user. The user will be deleted after the tests finish running.

When writing a new test, it can be useful to create a static user with known username and password, so that issues can be debugged by logging into the user's account. Additionally, users can only be deleted if all associated objects have also been deleted. If a test creates an entity in the user account but fails to clean up the entity, the test user won't be deleted and will clutter the dev stack. After a failed test run, persistent entities and test user accounts can be cleaned up more easily if user credentials are known.

### CI

In your forked repository, create [an Actions secret](https://docs.github.com/en/actions/security-guides/encrypted-secrets#creating-encrypted-secrets-for-a-repository) for both `ADMIN_USERNAME` and `ADMIN_PASSWORD`. The tests will run on each push to your forked repository.

## Writing Tests

### Test Isolation

Playwright runs tests from different files and within the same file in parallel, so all tests should be able to run concurrently. Persistent changes made within a particular test should be unique, since the same backend dev stack is used for all tests.

If a test requires sequential steps, then separate each action into a separate `test.step` within the same `test`.

See more about [test isolation in Playwright](https://playwright.dev/docs/browser-contexts).

### Run Isolation

Since all tests are run against the backend dev stack, concurrent test runs are not fully isolated from one another -- all test runs write to the same server. Tests can produce side effects that persist on the dev stack. If those side effects aren't cleaned up, the same test may fail on subsequent runs.

To prevent conflicting changes, each test run creates (and deletes) a unique dev stack user, so that user-namespaced entities will not conflict. For example, consider a test where a user creates a new Table named "My Table". Even if two developers run the test suite at the same time, the table will still be created, because the table name will be unique in the context of the user created for that run.

However, entities that are not user-namespaced must be created with care. For example, Projects must have names that are unique across Synapse. So, consider a test that creates a new project named "My Project". If two developers run the test at the same time, one of the tests will fail, because the project name will not be unique. Therefore, when creating entities that are not user-namespaced, entity names should include a unique key, e.g. `${uuidv4()} New Project`. Other entities that aren't user-namespaced are users and teams.

To prevent cluttering the backend dev stack with old test run objects, tests should clean up after themselves. So, if a new Project is created, the test suite should delete the project after all tests utilizing that Project have run.

### Common Issues

- Not awaiting an auto-retrying expectation, such as `toBeVisible()` -- these assertions will retry until the assertion passes or the timeout is reached. However, they are async, so must be awaited. See full list [here](https://playwright.dev/docs/test-assertions#auto-retrying-assertions).
- Not closing pages that are manually opened -- any page that is manually opened in a test with `const page = await browser.newPage()` must be closed with `await page.close()`. Otherwise, the accumulation of open pages will consume resources and cause issues in CI.
- Opening a new browser instance in `beforeAll` or `afterAll` -- the new browser instance will consume resources and cause issues in CI and is [strongly discouraged by Playwright](https://github.com/microsoft/playwright/issues/20598#issuecomment-1420543115). Instead, use the existing browser instance, e.g. `test.beforeAll(async ({browser}) => {const page = await browser.newPage()})`.
- Not awaiting elements in the order in which they appear on the page -- network requests are slower on CI than locally. By awaiting each element as it appears, the subsequent elements are given time to appear and can help avoid timing problems which cause tests to fail.
- Not accounting for timing issues when awaiting short-lived elements -- for example, after a user clicks on a button, a loader may appear while waiting for some work to finish, then disappear when the work is done. The test may expect the loader to appear and disappear. However, depending on how quickly the work finishes, the loader may appear and disappear before Playwright has checked for its existence. Use `Promise.all` to ensure that the check for the element is fired before the button is clicked and the loader's appearance isn't missed. See more discussion [here](https://github.com/microsoft/playwright/issues/5470#issuecomment-1285640689) and an illustrated example [here](https://tally-b.medium.com/the-illustrated-guide-to-using-promise-all-in-playwright-tests-af7a98af3f32).

## Debugging

Traces are useful to review network requests, step through video of the UI, and use a pick locator to identfy selectors. However, since network requests can contain sensitive information (e.g. login credentials), traces should not be saved as artifacts on public CI. See more about traces in the [Playwright docs](https://playwright.dev/docs/trace-viewer-intro).

Playwright supports the [`DEBUG` environment variable](https://playwright.dev/docs/debug#verbose-api-logs) to output debug logs during execution, including the following options:

- `pw:webserver`: checking whether webserver is available
- `pw:browser`: launching and closing browser
- `pw:test`: setting up and tearing down tests
- `pw:api`: verbose logging of each playwright test call -- will log typed values, so can expose user credentials and should not be used on public CI

Multiple debug variables can be passed via a comma separated list, e.g. `DEBUG="pw:webserver,pw:browser" yarn e2e`.
