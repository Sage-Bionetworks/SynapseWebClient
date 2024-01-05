import { Page } from '@playwright/test'
import {
  ACCESS_TYPE,
  AccessControlList,
  ResourceAccess,
} from '@sage-bionetworks/synapse-types'
import { waitForSrcEndpointConfig } from './utils'

// https://rest-docs.synapse.org/rest/GET/entity/id/acl.html
async function getEntityACL(page: Page, entityId: string, accessToken: string) {
  await waitForSrcEndpointConfig(page)
  const acl = await page.evaluate(
    async ({ entityId, accessToken }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.SynapseClient.getEntityACL(
        entityId,
        accessToken,
      )) as AccessControlList
    },
    { entityId, accessToken },
  )
  return acl
}

/**
 * Update an Entity's ACL
 * Note: The caller must be granted ACCESS_TYPE.CHANGE_PERMISSIONS on the Entity to call this method.
 * https://rest-docs.synapse.org/rest/PUT/entity/id/acl.html
 */
async function updateEntityACL(
  page: Page,
  acl: AccessControlList,
  accessToken: string,
) {
  await waitForSrcEndpointConfig(page)
  const updatedACL = await page.evaluate(
    async ({ acl, accessToken }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.SynapseClient.updateEntityACL(
        acl,
        accessToken,
      )) as AccessControlList
    },
    { acl, accessToken },
  )
  return updatedACL
}

// Note: The caller must be granted ACCESS_TYPE.CHANGE_PERMISSIONS on the Entity to call this method
export async function addUserToEntityACL(
  page: Page,
  entityId: string,
  userId: number,
  accessToken: string,
  accessType: ACCESS_TYPE[] = [ACCESS_TYPE.DOWNLOAD, ACCESS_TYPE.READ],
) {
  const acl = await getEntityACL(page, entityId, accessToken)
  const newAccess: ResourceAccess = {
    principalId: userId,
    accessType: accessType,
  }
  acl.resourceAccess.push(newAccess)
  return await updateEntityACL(page, acl, accessToken)
}
