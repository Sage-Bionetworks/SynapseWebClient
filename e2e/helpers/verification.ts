import { Page, expect } from '@playwright/test'
import { navigateToHomepageIfPageHasNotBeenLoaded } from './localStorage'

export async function waitForSrcEndpointConfig(page: Page) {
  // window only available after page has initially loaded
  await navigateToHomepageIfPageHasNotBeenLoaded(page)
  // ensure that endpoint config is set,
  // ...so API calls point to the correct stack
  await expect(async () => {
    const response = await page.evaluate('window.SRC.OVERRIDE_ENDPOINT_CONFIG')
    expect(response).not.toBeUndefined()
  }).toPass()
}

export async function getVerificationSubmissionId(
  userId: string,
  accessToken: string,
  page: Page,
) {
  await waitForSrcEndpointConfig(page)
  const bundle = await page.evaluate(
    async ({ userId, accessToken }) => {
      const mask = await SRC.SynapseConstants
        .USER_BUNDLE_MASK_VERIFICATION_SUBMISSION
      return await SRC.SynapseClient.getUserBundle(userId, mask, accessToken)
    },
    { userId, accessToken },
  )
  return bundle?.verificationSubmission?.id
}

// https://rest-docs.synapse.org/rest/DELETE/verificationSubmission/id.html
export async function deleteVerificationSubmissionById(
  verificationSubmissionId: string,
  accessToken: string,
  page: Page,
) {
  await waitForSrcEndpointConfig(page)
  await page.evaluate(
    async ({ verificationSubmissionId, accessToken }) => {
      const endpoint = await SRC.SynapseEnums.BackendDestinationEnum
        .REPO_ENDPOINT
      return await SRC.HttpClient.doDelete(
        `/repo/v1/verificationSubmission/${verificationSubmissionId}`,
        accessToken,
        endpoint,
      )
    },
    { verificationSubmissionId, accessToken },
  )
}

export async function deleteVerificationSubmissionIfExists(
  userId: string,
  accessToken: string,
  page: Page,
) {
  const verificationSubmissionId = await getVerificationSubmissionId(
    userId,
    accessToken,
    page,
  )
  if (verificationSubmissionId) {
    await deleteVerificationSubmissionById(
      verificationSubmissionId,
      accessToken,
      page,
    )
  }
}
