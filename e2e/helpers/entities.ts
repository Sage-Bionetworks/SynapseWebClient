import { Page, expect } from '@playwright/test'
import {
  Entity,
  FileEntity,
  FileHandle,
  FileUploadComplete,
} from '@sage-bionetworks/synapse-types'
import { v4 as uuidv4 } from 'uuid'
import { BackendDestinationEnum, doDelete, doGet } from './http'
import { FileType } from './types'
import { waitForSrcEndpointConfig } from './utils'

export function generateEntityName(
  entityType: 'project' | 'folder' | 'file' | 'table',
) {
  return `swc-e2e-${entityType}-entity-${uuidv4()}`
}

const entityHashBang = '#!Synapse'

export function entityUrlPathname(entityId: string) {
  return `/${entityHashBang}:${entityId}`
}

export function getEntityIdFromPathname(pathname: string) {
  if (!pathname.includes(entityHashBang)) {
    return ''
  }

  return pathname
    .replace(new RegExp(`.*${entityHashBang}:`), '')
    .replace(/\/.*/, '')
}

export async function createProject(
  projectName: string,
  accessToken: string,
  page: Page,
) {
  await waitForSrcEndpointConfig(page)
  const entity = await page.evaluate(
    async ({ projectName, accessToken }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.SynapseClient.createProject(
        projectName,
        accessToken,
      )) as Entity
    },
    { projectName, accessToken },
  )
  return entity?.id as string
}

export async function deleteProject(
  projectId: string,
  accessToken: string,
  page: Page,
) {
  await deleteEntity(projectId, true, accessToken, page)
}

async function uploadFile(
  accessToken: string,
  fileName: string,
  fileContent: string,
  fileType: FileType,
  page: Page,
) {
  await waitForSrcEndpointConfig(page)
  const fileUploadComplete = await page.evaluate(
    async ({ accessToken, fileName, fileContent, fileType }) => {
      // File interface is only available in the browser (not node.js)
      const file = new File([fileContent], fileName, { type: fileType })
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.SynapseClient.uploadFile(
        accessToken,
        fileName,
        file,
      )) as FileUploadComplete
    },
    { accessToken, fileName, fileContent, fileType },
  )
  return fileUploadComplete
}

async function createFileEntity(
  accessToken: string,
  parentId: string,
  fileName: string,
  fileHandleId: string,
  page: Page,
) {
  const fileEntityJson = {
    parentId: parentId,
    name: fileName,
    concreteType: 'org.sagebionetworks.repo.model.FileEntity',
    dataFileHandleId: fileHandleId,
  }
  await waitForSrcEndpointConfig(page)
  const entity = await page.evaluate(
    async ({ accessToken, fileEntityJson }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.SynapseClient.createEntity(
        fileEntityJson,
        accessToken,
      )) as FileEntity
    },
    { accessToken, fileEntityJson },
  )
  return entity?.id as string
}

export async function createFile(
  fileName: string,
  fileContent: string,
  fileType: FileType,
  parentId: string,
  accessToken: string,
  page: Page,
) {
  const fileUpload = await uploadFile(
    accessToken,
    fileName,
    fileContent,
    fileType,
    page,
  )
  const fileEntityId = await createFileEntity(
    accessToken,
    parentId,
    fileUpload.fileName,
    fileUpload.fileHandleId,
    page,
  )

  return {
    fileEntityId: fileEntityId,
    fileHandleId: fileUpload.fileHandleId,
  }
}

export async function getEntity<T extends Entity>(
  page: Page,
  accessToken: string | undefined = undefined,
  entityId: string,
  versionNumber?: string | number,
) {
  await waitForSrcEndpointConfig(page)
  const entity = await page.evaluate(
    async ({ accessToken, entityId, versionNumber }) => {
      // @ts-expect-error: Cannot find name 'SRC'
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
      return (await SRC.SynapseClient.getEntity(
        accessToken,
        entityId,
        versionNumber,
      )) as T
    },
    { accessToken, entityId, versionNumber },
  )
  return entity
}

export async function getEntityFileHandleId(
  page: Page,
  accessToken: string | undefined = undefined,
  entityId: string,
  versionNumber?: string | number,
) {
  const fileEntity = await getEntity<FileEntity>(
    page,
    accessToken,
    entityId,
    versionNumber,
  )
  return fileEntity.dataFileHandleId
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

// Get a FileHandle using its ID.
// Note: Only the user that created the FileHandle can access it directly.
export async function getFileHandle(
  accessToken: string,
  handleId: string,
  page: Page,
): Promise<FileHandle> {
  return await doGet(
    page,
    `/file/v1/fileHandle/${handleId}`,
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )
}

// Delete a FileHandle using its ID.
// Note: Only the user that created the FileHandle can delete it.
//   Also, a FileHandle cannot be deleted if it is associated with a FileEntity or WikiPage
export async function deleteFileHandle(
  accessToken: string,
  handleId: string,
  page: Page,
) {
  await doDelete(
    page,
    `/file/v1/fileHandle/${handleId}`,
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )
  return handleId
}

// When a FileEntity is deleted via the Synapse API with the skipTrashCan option set,
// ...the FileEntity is marked for deletion, but briefly sits in the TrashCan before it is purged
// This method will wait/retry FileHandle deletion to allow time for the FileEntity to be purged
// ...since FileHandles cannot be deleted when associated with a FileEntity
export async function deleteFileHandleWithRetry(
  accessToken: string,
  fileHandleId: string,
  page: Page,
) {
  await expect
    .poll(
      async () => {
        try {
          await deleteFileHandle(accessToken, fileHandleId, page)
          return true
        } catch (error) {
          return false
        }
      },
      {
        // retry after 5s, 10s, 15s, then every 30s until timeout
        intervals: [5_000, 10_000, 15_000, 30_000],
        timeout: 120_001,
      },
    )
    .toBe(true)
}
