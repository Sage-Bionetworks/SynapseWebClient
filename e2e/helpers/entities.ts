import { Page } from '@playwright/test'
import { BackendDestinationEnum, doDelete } from './http'
import { waitForSrcEndpointConfig } from './utils'

export async function createProject(
  projectName: string,
  accessToken: string,
  page: Page,
) {
  await waitForSrcEndpointConfig(page)
  const entity = await page.evaluate(
    async ({ projectName, accessToken }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      return await SRC.SynapseClient.createProject(projectName, accessToken)
    },
    { projectName, accessToken },
  )
  return entity?.id as string
}

// https://rest-docs.synapse.org/rest/DELETE/entity/id.html
// Note: not using SRC.deleteEntity, because it does not expose the skipTrashCan option
export async function deleteEntity(
  entityId: string,
  skipTrashCan: boolean,
  accessToken: string,
  page: Page,
) {
  const url = `/repo/v1/entity/${entityId}?skipTrashCan=${skipTrashCan}`
  await doDelete(page, url, accessToken, BackendDestinationEnum.REPO_ENDPOINT)
}
