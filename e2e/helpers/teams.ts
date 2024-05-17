import { Page } from '@playwright/test'
import { BackendDestinationEnum, doDelete } from './http'

export const teamPlace = 'Team'

export function getTeamIdFromPathname(pathname: string) {
  if (!pathname.includes(teamPlace)) {
    return undefined
  }

  return pathname.replace(new RegExp(`.*${teamPlace}:`), '')
}

// https://rest-docs.synapse.org/rest/DELETE/team/id.html
export async function deleteTeam(
  teamId: string,
  accessToken: string,
  page: Page,
) {
  const url = `/repo/v1/team/${teamId}`
  await doDelete(page, url, accessToken, BackendDestinationEnum.REPO_ENDPOINT)
}
