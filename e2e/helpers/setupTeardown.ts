import { Browser } from '@playwright/test'
import { ACCESS_TYPE } from '@sage-bionetworks/synapse-types/dist/ACCESS_TYPE.js'
import { StorageStatePaths } from '../fixtures/authenticatedUserPages'
import { addUserToEntityACL } from './acl'
import {
  createProject,
  deleteFileHandleWithRetry,
  deleteProject,
  generateEntityName,
} from './entities'
import {
  getAccessTokenFromCookie,
  getAdminPAT,
  getUserIdFromAccessTokenCookie,
} from './testUser'
import { Project } from './types'
import { UserPrefix } from './userConfig'

export const setupProject = async (
  browser: Browser,
  projectCreator: UserPrefix,
  storageStatePaths: StorageStatePaths,
) => {
  const context = await browser.newContext({
    storageState: storageStatePaths[projectCreator],
  })
  const page = await context.newPage()
  const accessToken = await getAccessTokenFromCookie(page)

  const projectName = generateEntityName('project')
  const projectId = await createProject(projectName, accessToken, page)
  await context.close()

  return { name: projectName, id: projectId } as Project
}

export const setupProjectWithPermissions = async (
  browser: Browser,
  projectCreator: UserPrefix,
  projectAccessor: UserPrefix,
  storageStatePaths: StorageStatePaths,
  accessorPermissions: ACCESS_TYPE[] = [ACCESS_TYPE.DOWNLOAD, ACCESS_TYPE.READ],
) => {
  const project = await setupProject(browser, projectCreator, storageStatePaths)

  const context = await browser.newContext({
    storageState: storageStatePaths[projectAccessor],
  })
  const page = await context.newPage()
  const userId = await getUserIdFromAccessTokenCookie(page)
  await addUserToEntityACL(
    page,
    project.id,
    Number(userId),
    getAdminPAT(),
    accessorPermissions,
  )
  await context.close()

  return project
}

export const teardownProjectsAndFileHandles = async (
  browser: Browser,
  projects: Project[],
  fileHandleIds: string[],
) => {
  const context = await browser.newContext()
  const page = await context.newPage()
  const accessToken = getAdminPAT()

  // delete projects
  for (const project of projects) {
    await deleteProject(project.id, accessToken, page)
  }

  // delete fileHandles concurrently: each retries for up to 2 minutes while the
  // deleted entities are purged, so deleting them one at a time can exceed the
  // hook timeout when there are several
  await Promise.all(
    fileHandleIds.map(fileHandleId =>
      deleteFileHandleWithRetry(accessToken, fileHandleId, page),
    ),
  )

  await context.close()
}
