export const NETWORK_UNAVAILABLE_MESSAGE =
  'This site cannot be reached. Either a connection is unavailable, or your network administrator has blocked you from accessing this site.'

/**
 * Error message returned by the Synapse Backend
 */
export type SynapseError = {
  reason: string
}

export enum ErrorResponseCode {
  /* The user's password must be reset via email. */
  PASSWORD_RESET_VIA_EMAIL_REQUIRED = 'PASSWORD_RESET_VIA_EMAIL_REQUIRED',
  /* The user has not passed the certification process. */
  USER_CERTIFICATION_REQUIRED = 'USER_CERTIFICATION_REQUIRED',
  /* At least one of the columns listed in a FacetColumnRequest is not facet-able according to the table's schema. */
  INVALID_TABLE_QUERY_FACET_COLUMN_REQUEST = 'INVALID_TABLE_QUERY_FACET_COLUMN_REQUEST',
  /* The OAuth Client is not verified. */
  OAUTH_CLIENT_NOT_VERIFIED = 'OAUTH_CLIENT_NOT_VERIFIED',
  /* Two-factor authentication is required. */
  TWO_FA_REQUIRED = 'TWO_FA_REQUIRED',
}

/**
 * https://rest-docs.synapse.org/rest/org/sagebionetworks/repo/model/ErrorResponse.html
 */
export type ErrorResponse = {
  concreteType: 'org.sagebionetworks.repo.model.ErrorResponse'
  reason: string
  errorCode?: ErrorResponseCode
}

export type TwoFactorAuthErrorResponse = {
  concreteType: 'org.sagebionetworks.repo.model.auth.TwoFactorAuthErrorResponse'
  /* The id of the user that attempted to authenticate.*/
  userId: number
  /* Token included when two-factor authentication is required. If present the client will need to include this token as part of the TwoFactorAuthLoginRequest.*/
  twoFaToken: string
  /* The reason for the error*/
  reason: string
  /* A code to be used by clients to handle the error.*/
  errorCode: ErrorResponseCode.TWO_FA_REQUIRED
}

/**
 * Error message returned by the Synapse backend joined with the
 * HTTP status code.
 */
export class SynapseClientError extends Error {
  public status: number
  public reason: string
  public errorResponse?:
    | SynapseError
    | ErrorResponse
    | TwoFactorAuthErrorResponse
  public url: string

  constructor(
    status: number,
    reason: string,
    url: string,
    errorResponse?: SynapseError | ErrorResponse | TwoFactorAuthErrorResponse,
  ) {
    super(reason)
    this.status = status
    this.reason = reason
    this.url = url
    this.errorResponse = errorResponse
    // See https://www.typescriptlang.org/docs/handbook/release-notes/typescript-2-2.html#support-for-newtarget
    Object.setPrototypeOf(this, new.target.prototype)
  }
}

/**
 * Waits t number of milliseconds
 *
 * @export
 * @param {number} t milliseconds
 * @returns after t milliseconds
 */
export function delay(t: number) {
  return new Promise(resolve => {
    setTimeout(resolve.bind(null, {}), t)
  })
}

/*
  0 - no internet connection
  429 - Too Many Requests
  502 - Bad Gateway
  503 - Service Unavailable
  504 - Gateway Timeout
*/
const RETRY_STATUS_CODES = [0, 429, 502, 503, 504]
const MAX_RETRY_STATUS_CODES = [502, 503]
const MAX_RETRY = 3
/**
 * Fetches data, retrying if the HTTP status code indicates that it could be retried. Contains custom logic for
 * handling errors returned by the Synapse backend.
 * @throws SynapseClientError
 */
export const fetchWithExponentialTimeout = async <TResponse>(
  url: RequestInfo,
  options: RequestInit,
  delayMs = 1000,
): Promise<TResponse> => {
  let response
  try {
    response = await fetch(url, options)
  } catch (err) {
    console.error(err)
    throw new SynapseClientError(0, NETWORK_UNAVAILABLE_MESSAGE, url.toString())
  }

  let numOfTry = 1
  while (response.status && RETRY_STATUS_CODES.includes(response.status)) {
    await delay(delayMs)
    // Exponential backoff if we re-fetch
    delayMs = delayMs * 2
    response = await fetch(url, options)
    if (MAX_RETRY_STATUS_CODES.includes(response.status)) {
      numOfTry++
      if (numOfTry == MAX_RETRY) {
        break
      }
    }
  }

  const contentType = response.headers.get('Content-Type')
  const responseBody = await response.text()
  let responseObject: TResponse | SynapseError | string = responseBody
  try {
    // try to parse it as json
    if (contentType && contentType.includes('application/json')) {
      responseObject = JSON.parse(responseBody) as TResponse | SynapseError
    }
  } catch (error) {
    console.warn('Failed to parse response as JSON', responseBody)
  }

  if (response.ok) {
    return responseObject as TResponse
  } else if (
    responseObject !== null &&
    typeof responseObject === 'object' &&
    'reason' in responseObject
  ) {
    throw new SynapseClientError(
      response.status,
      responseObject.reason,
      url.toString(),
      responseObject,
    )
  } else {
    throw new SynapseClientError(
      response.status,
      JSON.stringify(responseObject),
      url.toString(),
    )
  }
}
