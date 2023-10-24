import { Browser } from '@playwright/test'
import { StorageStatePaths } from '../fixtures/authenticatedUserPages'
import {
  createProject,
  deleteFileHandleWithRetry,
  deleteProject,
  generateEntityName,
} from './entities'
import { getAccessTokenFromCookie, getAdminPAT } from './testUser'
import { Project } from './types'
import { UserPrefixes } from './userConfig'

export const setupProject = async (
  browser: Browser,
  projectCreator: UserPrefixes,
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

export const teardownProjectsAndFileHandles = async (
  browser: Browser,
  projects: Project[],
  fileHandleIds: string[],
) => {
  const context = await browser.newContext()
  const page = await context.newPage()
  const accessToken = getAdminPAT()

  // delete projects
  for await (const project of projects) {
    await deleteProject(project.id, accessToken, page)
  }

  // delete fileHandles
  for await (const fileHandleId of fileHandleIds) {
    await deleteFileHandleWithRetry(accessToken, fileHandleId, page)
  }

  await context.close()
}
