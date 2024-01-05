/* USERS */
export type TestUser = {
  username: string
  email: string
  password: string
  tou: boolean // true if user agrees to Synapse terms of use
  validatedUser: boolean // true if a verification approved state should be added for the user
}

// Note: many other fields are included in the payload, but only including
// field used by e2e tests here
export type JwtPayload = {
  sub: string
}

/* MESSAGES */
export type MessageToUser = {
  id: string //	The unique identifier of the message or comment
  createdBy: string //	The unique identifier of the sender of this message
  fileHandleId: string //	The S3 file handle storing the body of this message. Note: The file's mime type should be 'text/plain' or 'text/html'. If no character encoding is specified, then UTF-8 is assumed.
  createdOn: string // When this message was created
  recipients: string[] //	The unique identifiers of the intended recipients of a message
  subject: string //	Topic of this message. Optional
  inReplyTo: string //	The unique identifier of the message being replied to. Can be null
  inReplyToRoot: string //	The unique identifier of the root message being replied to
  notificationUnsubscribeEndpoint: string //	the portal prefix for one-click email unsubscription. A signed, serialized token is appended to create the complete URL. If omitted, the default endpoint will be used.
  userProfileSettingEndpoint: string //	the portal link to user profile setting page. If omitted, the default endpoint will be used.
  withUnsubscribeLink: boolean //	should the unsubscribe link be included in the email?
  withProfileSettingLink: boolean //	should the user profile setting link be included in the email?
  isNotificationMessage: boolean // A notification message is sent from a noreply email address, delivery failures are not sent back to the sender
  to: string // The email addresses in the 'to' field of the email message
  cc: string //	The email addresses in the 'cc' field of the email message
  bcc: string //	The email addresses in the 'bcc' field of the email message
}

type MessageStatusType = 'READ' | 'UNREAD' | 'ARCHIVED'

type MessageStatus = {
  messageId: string //	The unique identifier of the message.
  recipientId: string // The unique identifier of the recipient of this message.
  status: MessageStatusType // The status of the message, from the RECIPIENT'S standpoint
}

export type MessageBundle = {
  message: MessageToUser // JSON schema for a message to another user
  status: MessageStatus // JSON schema for message status from the RECIPIENT'S standpoint
}

/* ENTITIES */
export type Project = {
  name: string
  id: string
}

export type FileType = 'text/txt' | 'text/csv' | 'application/json'
