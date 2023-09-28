/* USERS */
export type LoginResponse = {
  accessToken: string // A token that authorizes subsequent requests
  acceptsTermsOfUse: boolean // Does the user accept the terms of use?
  authenticationReceipt: string // A valid receipt allows the user to skip extra security checks.
}

export type TestUser = {
  username: string
  email: string
  password: string
  tou: boolean // true if user agrees to Synapse terms of use
  validatedUser: boolean // true if a verification approved state should be added for the user
}

/* FILES */
export type FileHandleStatus =
  | 'AVAILABLE' //	The default status of a file handle that is considered available and linked to at least on object (For the possible associations see FileHandleAssociateType).
  | 'UNLINKED' // The file handle has been detected as not linked to any object (For the possible associations see FileHandleAssociateType), this file will eventually be archived.
  | 'ARCHIVED' // The file handle has been archived after being UNLINKED for more than 30 days. The file handle can be restored by the creator using the dedicated API. When ARCHIVED the file might not be readily accessible and restoring might take a few hours.

export type FileHandle = {
  id: string //	The ID of this FileHandle. All references to this FileHandle will use this ID. Synapse will generate this ID when the FileHandle is created.
  etag: string //	FileHandles are immutable from the perspective of the API. The only field that can be change is the previewId. When a new previewId is set, the etag will change.
  createdBy: string //	The ID Of the user that created this file.
  createdOn: string //	The date when this file was uploaded.
  modifiedOn: string //	The date when the file was modified. This is handled by the backend and cannot be modified.
  concreteType: string //	This is used to indicate the implementation of this interface. For example, an S3FileHandle should be set to: org.sagebionetworks.repo.model.file.S3FileHandle
  contentType: string //	Must be: http://en.wikipedia.org/wiki/Internet_media_type
  contentMd5: string //	The file's content MD5.
  fileName: string //	The short, user visible name for this file.
  storageLocationId: number //	The optional storage location descriptor
  contentSize: number //	The size of the file in bytes.
  status: FileHandleStatus // The status of the file handle as computed by the backend. This value cannot be changed, any file handle that is not AVAILABLE should not be used.
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

export type MessageStatusType = 'READ' | 'UNREAD' | 'ARCHIVED'

export type MessageStatus = {
  messageId: string //	The unique identifier of the message.
  recipientId: string // The unique identifier of the recipient of this message.
  status: MessageStatusType // The status of the message, from the RECIPIENT'S standpoint
}

export type MessageBundle = {
  message: MessageToUser // JSON schema for a message to another user
  status: MessageStatus // JSON schema for message status from the RECIPIENT'S standpoint
}

export type PaginatedResults<T> = {
  totalNumberOfResults: number //	Calculating the actual totalNumberOfResults is not longer supported. Therefore, for each page, the totalNumberOfResults is estimated using the current page, limit, and offset. When the page size equals the limit, the totalNumberOfResults will be offset+pageSize+ 1. Otherwise, the totalNumberOfResults will be offset+pageSize.
  results: T[] // The the id of the entity to which this reference refers
}

/* ENTITIES */
export type Project = {
  name: string
  id: string
}

export type FileUploadComplete = {
  fileHandleId: string
  fileName: string
}

export type FileType = 'text/txt' | 'text/csv' | 'application/json'
