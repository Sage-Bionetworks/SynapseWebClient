# End-to-End Testing Workflow

## Dev Workflow

1. Create a .env file with the following environment variables: `DEV_ADMIN_USERNAME` and `DEV_ADMIN_API_KEY`.
2. Build SWC: `mvn clean install`
3. Run Tests\*: `yarn e2e`

\*Note: if running tests repeatedly without changing SWC, it will be faster to run SWC in a separate terminal (`mvn gwt:run`) and then run tests (`yarn e2e`), since Playwright will use the existing server and SWC won't need to recompile before tests are run.

## Writing Tests

### Test Isolation

Playwright tests in different files and within the same files in parallel, so all tests should be able to run concurrently. Persistent changes made within a particular test should be unique, since the same backend dev stack is used for all tests.

If a test requires sequential steps, then separate each action into a separate `test.step` within the same `test`.

See more about [test isolation in Playwright](https://playwright.dev/docs/browser-contexts).

### Run Isolation

Since all tests are run against the backend dev stack, concurrent test runs are not fully isolated from one another -- all test runs write to the same server. Tests can produced side effects, which will persist on the dev stack. If those side effects aren't cleaned up, the same test may fail on subsequent runs.

To prevent conflicting changes, each test run creates (and deletes) a unique dev stack user, so that user-namespaced entities will not conflict. For example, consider a test where a user creates a new Table named "My Table". Even if two developers run the test suite at the same time, the table will still be created, because the table name will be unique in the context of the user created for that run.

However, entities that are not namespaced must be created with care. For example, Projects must have names that are unique across Synapse. So, consider a test that creates a new project named "My Project". If two developers run the test at the same time, one of the tests will fail, because the project name will not be unique. Therefore, when creating entities that are not user-namespaced, entity names should include a unique key, e.g. `${uuidv4()} New Project`.

To prevent cluttering the backend dev stack with old test run objects, tests should clean up after themselves. So, if a new Project is created, the test suite should delete the project after all tests utilizing that Project have run.
