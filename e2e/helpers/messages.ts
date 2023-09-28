import { Page } from '@playwright/test'
import { deleteFileHandle } from './entities'
import { BackendDestinationEnum, doDelete, doGet } from './http'
import { MessageBundle, MessageToUser, PaginatedResults } from './types'

// Retrieves the current authenticated user's outbox.
export async function getUserOutbox(accessToken: string, page: Page) {
  return (await doGet(
    page,
    '/repo/v1/message/outbox',
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )) as PaginatedResults<MessageToUser>
}

// Retrieves the current authenticated user's inbox.
// It may take several seconds for a message to appear in the inbox after creation.
export async function getUserInbox(accessToken: string, page: Page) {
  return (await doGet(
    page,
    '/repo/v1/message/inbox',
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )) as PaginatedResults<MessageBundle>
}

// Deletes a message. Only accessible to administrators.
async function deleteUserMessage(
  accessToken: string,
  messageId: string,
  page: Page,
) {
  await doDelete(
    page,
    `/repo/v1/admin/message/${messageId}`,
    accessToken,
    BackendDestinationEnum.REPO_ENDPOINT,
  )
  return messageId
}

function arraysAreEqual(arr1: string[], arr2: string[]) {
  return (
    arr1.length === arr2.length &&
    arr1.every((element, index) => element === arr2[index])
  )
}

export async function deleteUserOutboxMessageAndAssociatedFile(
  recipients: string[],
  subject: string,
  userAccessToken: string,
  adminAccessToken: string,
  page: Page,
) {
  const messages = (await getUserOutbox(userAccessToken, page)).results.filter(
    message =>
      message.subject === subject &&
      arraysAreEqual(recipients.sort(), message.recipients.sort()),
  )

  if (messages.length !== 1) {
    throw new Error(
      `Should return one message when filtering outbox with provided recipient and subject, but found ${messages.length} messages.`,
    )
  }

  const message = messages[0]
  await deleteUserMessage(adminAccessToken, message.id, page)
  await deleteFileHandle(userAccessToken, message.fileHandleId, page)
}

export async function deleteTeamInvitationMessage(
  recipients: string[],
  inviterUserName: string,
  teamName: string,
  inviterAccessToken: string,
  adminAccessToken: string,
  page: Page,
) {
  await deleteUserOutboxMessageAndAssociatedFile(
    recipients,
    `${inviterUserName} has invited you to join the ${teamName} team`,
    inviterAccessToken,
    adminAccessToken,
    page,
  )
}

export async function deleteTeamInviteAcceptanceMessage(
  recipients: string[],
  accepterAccessToken: string,
  adminAccessToken: string,
  page: Page,
) {
  await deleteUserOutboxMessageAndAssociatedFile(
    recipients,
    'New Member Has Joined the Team',
    accepterAccessToken,
    adminAccessToken,
    page,
  )
}
