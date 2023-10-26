import { Browser } from '@playwright/test'
import { StorageStatePaths } from '../fixtures/authenticatedUserPages'
import ACCESS_TYPE from './ACCESS_TYPE'
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
  getUserIdFromLocalStorage,
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
  const userId = await getUserIdFromLocalStorage(page)
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

  // delete fileHandles
  for (const fileHandleId of fileHandleIds) {
    await deleteFileHandleWithRetry(accessToken, fileHandleId, page)
  }

  await context.close()
}
