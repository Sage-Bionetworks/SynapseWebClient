import { Page } from '@playwright/test'
import {
  BackendDestinationEnum,
  doDelete,
  waitForSrcEndpointConfig,
} from './http'

export async function getVerificationSubmissionId(
  userId: string,
  accessToken: string,
  page: Page,
) {
  await waitForSrcEndpointConfig(page)
  const bundle = await page.evaluate(
    async ({ userId, accessToken }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      const mask = await SRC.SynapseConstants
        .USER_BUNDLE_MASK_VERIFICATION_SUBMISSION
      // @ts-expect-error: Cannot find name 'SRC'
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
  const url = `/repo/v1/verificationSubmission/${verificationSubmissionId}`
  await doDelete(page, url, accessToken, BackendDestinationEnum.REPO_ENDPOINT)
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
