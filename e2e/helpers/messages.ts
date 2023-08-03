import { doDelete, doGet, getEndpoint } from './http'
import {
  FileHandle,
  MessageBundle,
  MessageToUser,
  PaginatedResults,
} from './types'

// Retrieves the current authenticated user's outbox.
export async function getUserOutbox(accessToken: string) {
  return (await doGet(
    getEndpoint(),
    '/repo/v1/message/outbox',
    accessToken,
  )) as PaginatedResults<MessageToUser>
}

// Retrieves the current authenticated user's inbox.
// It may take several seconds for a message to appear in the inbox after creation.
async function getUserInbox(accessToken: string) {
  return (await doGet(
    getEndpoint(),
    '/repo/v1/message/inbox',
    accessToken,
  )) as PaginatedResults<MessageBundle>
}

// Deletes a message. Only accessible to administrators.
async function deleteUserMessage(accessToken: string, messageId: string) {
  await doDelete(
    getEndpoint(),
    `/repo/v1/admin/message/${messageId}`,
    accessToken,
  )
  return messageId
}

// Get a FileHandle using its ID.
// Note: Only the user that created the FileHandle can access it directly.
async function getFileHandle(accessToken: string, handleId: string) {
  return (await doGet(
    getEndpoint(),
    `/file/v1/fileHandle/${handleId}`,
    accessToken,
  )) as FileHandle
}

// Delete a FileHandle using its ID.
// Note: Only the user that created the FileHandle can delete it.
//   Also, a FileHandle cannot be deleted if it is associated with a FileEntity or WikiPage
async function deleteFileHandle(accessToken: string, handleId: string) {
  await doDelete(getEndpoint(), `/file/v1/fileHandle/${handleId}`, accessToken)
  return handleId
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
) {
  const messages = (await getUserOutbox(userAccessToken)).results.filter(
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
  await deleteUserMessage(adminAccessToken, message.id)
  await deleteFileHandle(userAccessToken, message.fileHandleId)
}

export async function deleteTeamInvitationMessage(
  recipients: string[],
  inviterUserName: string,
  teamName: string,
  inviterAccessToken: string,
  adminAccessToken: string,
) {
  await deleteUserOutboxMessageAndAssociatedFile(
    recipients,
    `${inviterUserName} has invited you to join the ${teamName} team`,
    inviterAccessToken,
    adminAccessToken,
  )
}

export async function deleteTeamInviteAcceptanceMessage(
  recipients: string[],
  accepterAccessToken: string,
  adminAccessToken: string,
) {
  await deleteUserOutboxMessageAndAssociatedFile(
    recipients,
    'New Member Has Joined the Team',
    accepterAccessToken,
    adminAccessToken,
  )
}
