# End-to-End Testing Workflow

## Dev Setup

### Local

1. Install Docker, if not already installed.
2. Create a new admin user on the backend dev stack named: `swc-e2e-admin-{your-name}`, then create a new PAT with View, Download, and Modify permissions.
3. Create a `.env` file with the following environment variables: `ADMIN_PAT`.
4. Build SWC: `yarn build`
5. Serve SWC via Tomcat Docker container: `yarn docker:start`
6. Run Tests: `yarn e2e`. Tests can be run multiple times against the same Docker container.
7. When finished testing, stop and remove Docker container: `yarn docker:stop`

Notes:

- Test user creation:
  - The test suite will create a new test user with randomly generated username / password on the backend dev stack for tests that require an authenticated test user. The user will be deleted after the tests finish running.
  - However, test users can only be deleted if all associated objects have also been deleted. If a test creates an entity in the user account but fails to clean up the entity, the test user won't be deleted and will clutter the dev stack. After a failed test run, persistent entities and test user accounts can be cleaned up more easily if user credentials are known.
  - _Note_: All objects that are visible in the UI should be cleaned up. A best effort should be made to clean up test users. However, test users that have created a table will not be deleted, due to [this bug](https://sagebionetworks.jira.com/browse/PLFM-8179). We have decided it is acceptable to leave test users on the dev stack if unable to fully delete all associated objects.
- Writing new tests:
  - Review a simliar [backend integration test](https://github.com/Sage-Bionetworks/Synapse-Repository-Services/tree/develop/integration-test/src/test/java/org/sagebionetworks) for the order in which to clean up associated objects.
  - Create static test users with known username and password, so that issues can be debugged by logging into the user accounts. See comments in `e2e/helpers/userConfig.ts`.
  - Start by running the new test against one browser with one worker, trace on, and no retries: `yarn e2e --project=firefox --workers=1 --retries=0 --trace=on e2e/{new_test}.spec.ts`.
  - Before pushing changes to CI, run tests with the same configuration as CI by adding `CI=true` to the `.env` file.
- Running tests without installing Docker:
  - Ensure that your maven settings file (usually located at `~/.m2/settings.xml`) points at the backend development stack. See endpoint parameters [in this guide](https://sagebionetworks.jira.com/wiki/spaces/SWC/pages/15597754/Developer+Bootstrap).
  - Run `mvn clean install` followed by `mvn gwt:run` instead of steps 2 and 3.
  - If you would like to run tests repeatedly without changing SWC, it will be faster to run SWC in a separate terminal (`mvn gwt:run`) and then run tests (`yarn e2e`), since Playwright will use the existing server and SWC won't need to recompile before tests are run.

### CI

Playwright reports are configured and saved differently based on where the e2e workflow runs. In forked repos, reports are saved as GitHub artifacts, which are publically accessible in public repositories. Since traces can include credentials for the backend dev stack, these reports do not include traces. In the `Sage-Bionetworks`-owned repo, Playwright reports are saved to a private S3 bucket so the reports can include traces.

#### Forked Repositories

In your forked repository, ensure that [Actions are enabled](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/enabling-features-for-your-repository/managing-github-actions-settings-for-a-repository), then create [an Actions secret](https://docs.github.com/en/actions/security-guides/encrypted-secrets#creating-encrypted-secrets-for-a-repository) for `ADMIN_PAT`. The tests will run on each push to your forked repository. The Playwright report will be saved as a GitHub Artifact.

The GitHub UI or CLI can be used to view the reports:

- GitHub UI
  - Navigate to the Action run summary page.
  - Download the report named "html-report--attempt-{number}". _Note:_ only the report from the latest attempt will be available.
  - Unzip the file and move the "index.html" file into the `playwright-report` directory in SWC.
  - Run `yarn e2e:report` to view the HTML report in the browser.
- GitHub CLI
  - Install [GitHub CLI](https://cli.github.com/), if necessary.
  - Install [jq](https://jqlang.github.io/jq/download/), if necessary.
  - [Authenticate](https://cli.github.com/manual/gh_auth_login) with a GitHub host, if necessary.
  - Run `e2e_workflow/view_github_report.sh` with the following arguments: GitHub repo owner name and the GitHub run ID of the report to view. The HTML report will open in the browser.

#### Sage Repository

In the `Sage-Bionetworks`-owned repository, the tests will run on push to `develop` and `release-**` branches. The Playwright blob report from each shard will be saved in the `s3://e2e-reports-bucket-bucket-1p1qz6p48t4uy` S3 bucket. Each report will be named as `report-{ shard-number }.zip` and be located within a subdirectory named `SynapseWebClient-${ github-run-id }-${ run-attempt-number }`.

The AWS console or CLI can be used to view the reports:

- AWS console
  - Authenticate with AWS console SSO with the `org-sagebase-synapsedev` account.
  - Navigate to the S3 bucket: `s3://e2e-reports-bucket-bucket-1p1qz6p48t4uy`
  - Download the shard reports locally.
  - Move the files into the `blob-report` directory in SWC.
  - Run `yarn e2e:report:blob` to merge the shard reports and open the resulting HTML report in the browser.
- AWS CLI
  - Install [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html), if necessary.
  - Configure access credentials for AWS CLI SSO with the `org-sagebase-synapsedev` account.
  - Run `e2e_workflow/view_s3_report.sh` with the following arguments: AWS SSO profile name, the GitHub run ID of the report to view, and optionally, the run attempt number. The HTML report will open in the browser.

## Writing Tests

### Test Isolation

Playwright runs tests from different files and within the same file in parallel, so all tests should be able to run concurrently. Persistent changes made within a particular test should be unique, since the same backend dev stack is used for all tests.

If a test requires sequential steps, then separate each action into a separate `test.step` within the same `test`.

See more about [test isolation in Playwright](https://playwright.dev/docs/browser-contexts).

### Run Isolation

Since all tests are run against the backend dev stack, concurrent test runs are not fully isolated from one another -- all test runs write to the same server. Tests can produce side effects that persist on the dev stack. If those side effects aren't cleaned up, the same test may fail on subsequent runs.

To prevent conflicting changes, each test run creates (and deletes) a unique dev stack user\*, so that user-namespaced entities will not conflict. For example, consider a test where a user creates a new Table named "My Table". Even if two developers run the test suite at the same time, the table will still be created, because the table name will be unique in the context of the user created for that run.

However, entities that are not user-namespaced must be created with care. For example, Projects must have names that are unique across Synapse. So, consider a test that creates a new project named "My Project". If two developers run the test at the same time, one of the tests will fail, because the project name will not be unique. Therefore, when creating entities that are not user-namespaced, entity names should include a unique key, e.g. `${uuidv4()} New Project`. Other entities that aren't user-namespaced are users and teams.

To prevent cluttering the backend dev stack with old test run objects, tests should clean up after themselves. So, if a new Project is created, the test suite should delete the project after all tests utilizing that Project have run.

\*Note: to allow tests to be run in parallel within a run, worker fixtures create test users that are also unique per worker and browser name.

## Debugging

Traces are useful to review network requests, step through video of the UI, and use a pick locator to identfy selectors. However, since network requests can contain sensitive information (e.g. login credentials), traces should not be saved as artifacts on public CI. See more about traces in the [Playwright docs](https://playwright.dev/docs/trace-viewer-intro). However, traces can be saved in private GitHub repositories. See the [example snippet](#saving-traces) below.

Playwright supports the [`DEBUG` environment variable](https://playwright.dev/docs/debug#verbose-api-logs) to output debug logs during execution, including the following options:

- `pw:webserver`: checking whether webserver is available
- `pw:browser`: launching and closing browser
- `pw:test`: setting up and tearing down tests
- `pw:api`: verbose logging of each playwright test call -- will log typed values, so can expose user credentials and should not be used on public CI

Multiple debug variables can be passed via a comma separated list, e.g. `DEBUG="pw:webserver,pw:browser" yarn e2e`.

### Common Issues

- Not awaiting an auto-retrying expectation, such as `toBeVisible()` -- these assertions will retry until the assertion passes or the timeout is reached. However, they are async, so must be awaited. See full list [here](https://playwright.dev/docs/test-assertions#auto-retrying-assertions).
- Not awaiting elements in the order in which they appear on the page -- network requests may be slower on CI than locally. By awaiting each element as it appears, the subsequent elements are given time to appear and can help avoid timing problems which cause tests to be flaky.
- Not accounting for timing issues when awaiting short-lived elements -- for example, after a user clicks on a button, a loader may appear while waiting for some work to finish, then disappear when the work is done. The test may expect the loader to appear and disappear. However, depending on how quickly the work finishes, the loader may appear and disappear before Playwright has checked for its existence. Use `Promise.all` to ensure that the check for the element is fired before the button is clicked and the loader's appearance isn't missed. See more discussion [here](https://github.com/microsoft/playwright/issues/5470#issuecomment-1285640689) and an illustrated example [here](https://tally-b.medium.com/the-illustrated-guide-to-using-promise-all-in-playwright-tests-af7a98af3f32).
- Not ensuring that values entered into input fields are set in state before submitting a form -- Playwright acts as a very fast user and tests may be faster in CI than locally. If a form is submitted before an input value has been set in state, then the form will be submitted without a value for that input. After entering a value in an input, check that the input has the correct value (`await expect(input).toHaveValue('expectedValue')`) before submitting the form (`await submitButton.click()`).

### Troubleshooting Flaky Tests in CI

Occasionally, tests will consistently pass locally, but intermittently fail in CI. The following are techniques for troubleshooting the cause of the flakiness. See [this guide](https://ray.run/blog/detecting-and-handling-flaky-tests-in-playwright) for more ideas.

#### Network speed

CI machines may have slower network speeds than local machines. Help the test fail locally by emulating slower network speeds via the Chrome DevTools Protocol:

```typescript
const page = await browser.newPage()
const cdpSession = await page.context().newCDPSession(page)

// set network conditions, as per:
//   https://github.com/microsoft/playwright/issues/6038#issuecomment-812521882
const networkConditions = {
  'Slow 3G': {
    download: ((500 * 1000) / 8) * 0.8,
    upload: ((500 * 1000) / 8) * 0.8,
    latency: 400 * 5,
  },
  'Fast 3G': {
    download: ((1.6 * 1000 * 1000) / 8) * 0.9,
    upload: ((750 * 1000) / 8) * 0.9,
    latency: 150 * 3.75,
  },
}
await cdpSession.send('Network.emulateNetworkConditions', {
  downloadThroughput: networkConditions['Fast 3G'].download,
  uploadThroughput: networkConditions['Fast 3G'].upload,
  latency: networkConditions['Fast 3G'].latency,
  offline: false,
})

// or set cpu throttling rate, as per:
//   https://dev.to/codux/flaky-tests-and-how-to-deal-with-them-2id2
await cdpSession.send('Emulation.setCPUThrottlingRate', { rate: 3 })
```

Alternately, manually run the test with network throttling (e.g. Slow 3G) applied to the browser. This can be useful for identifying states that occur when there is a slow network connection.

#### Repeats

A flaky test in CI can be difficult to reproduce locally. However, running the test many times in a row (via Playwright's `--repeat-each` flag) can help reveal the flake.

#### Hardware Resources

A developer's local machine may have more powerful hardware resources compared to GitHub Actions workflow runners. If a VM is overbooked, tests may fail. Alternately, the remote machine may be more powerful than the local machine.

Check whether resources are consumed that don't need to be:

- Is a new browser instance launched in a test or a test hook? The new browser instance will consume resources and is [strongly discouraged by Playwright](https://github.com/microsoft/playwright/issues/20598#issuecomment-1420543115). Instead, use the existing browser instance, e.g. `test.beforeAll(async ({browser}) => {const page = await browser.newPage()})`.
- Is a new page or a new browser instance opened manually, but not closed when the test finishes? Playwright will handle opening/closing the initial browser instance and the page provided for each test, but manually allocated resources must be cleaned up by the user. Otherwise, they will consume resources for the duration of the test suite. For example, a page opened with `const page = await browser.newPage()` must be closed with `await page.close()`. Similarly, a browser instance launched with `const browser = await chromium.launch()` must be closed with `await browser.close()`.

Check whether increasing the VM size resolves the flakiness, by changing the runner in the GitHub Action workflow. See standard GitHub runner hardware resources [here](https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners#supported-runners-and-hardware-resources) and larger runners [here](https://docs.github.com/en/actions/using-github-hosted-runners/about-larger-runners#machine-specs-for-larger-runners).

If CI is faster than the local machine, look for race conditions. For example, a value is entered into a form input, then a button is clicked to submit the form. If the machine is very fast, the form may be submitted before the input value is set in state, which can result in sending an empty value for that input. To resolve, ensure that input values are set before submitting a form:

```TypeScript
const inputValue = 'test'
const input = page.getByLabel('Input Name')
await input.fill(inputValue)
await expect(input).toHaveValue(inputValue)
await page.getByRole('button', { name: 'submit' }).click()
```

#### Saving Traces

Traces can be invaluable for troubleshooting a failure in CI. However, traces may contain sensitive information. So instead of saving traces in public CI, follow these steps:

1. Create a **private** GitHub repository
2. Create the appropriate Actions secrets, as described [here](#ci)
3. Copy the current workflow from `.github/workflows/build-test-e2e.yml` to the new repo (along with any composite workflows)
4. Change the trigger so the workflow can be [manually triggered](https://docs.github.com/en/actions/using-workflows/events-that-trigger-workflows#workflow_dispatch):

```yml
on: workflow_dispatch
```

5. Change the checkout step to point at the branch in your forked repo, e.g.:

```yml
- uses: actions/checkout@v3
  with:
    repository: hallieswan/SynapseWebClient
    ref: SWC-6514
```

6. Change the `Run Playwright tests` step so that traces are on and/or DEBUG variables are set, e.g.:

```yml
- name: Run Playwright tests
  env:
    ADMIN_PAT: ${{ secrets.ADMIN_PAT }}
  run: DEBUG="pw:api" yarn playwright test --trace on
```

7. Manually [trigger](https://docs.github.com/en/actions/using-workflows/manually-running-a-workflow) the workflow.
8. Download the playwright-report artifact and [view the report](https://playwright.dev/docs/ci-intro#viewing-the-html-report), which will contain traces.
