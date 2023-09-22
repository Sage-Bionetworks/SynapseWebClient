import { Page } from '@playwright/test'
import { BackendDestinationEnum, doPut } from './http'

/* Set a user's certification status. For Synapse administrators only.
https://rest-docs.synapse.org/rest/PUT/admin/user/id/certificationStatus.html */
export async function setCertifiedUserStatus(
  userId: string,
  isCertified: boolean,
  accessToken: string,
  page: Page,
) {
  const url = `/repo/v1/admin/user/${userId}/certificationStatus?isCertified=${isCertified}`
  await doPut(
    page,
    url,
    undefined,
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )
}
