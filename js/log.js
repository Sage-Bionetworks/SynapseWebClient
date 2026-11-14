/**
 * Client-side logging to the Portal's log servlet (`POST /Portal/log`).
 *
 * Also defines `window._swcLogClientError`, which GWT calls (see
 * SynapseJSNIUtilsImpl.logClientError) to log uncaught errors and upload
 * failures.
 */

const LOG_SERVLET_PATH = 'Portal/log'
// Mirrors the fallback in synapse-react-client's getEndpoint(): if GWT
// hasn't set `window.SRC_OVERRIDE_ENDPOINT_CONFIG` yet (this module can run
// before GWT bootstraps), assume the production Portal origin. Used for both
// `GET /Portal/versions` and `POST /Portal/log` below.
const PRODUCTION_PORTAL_ENDPOINT = 'https://www.synapse.org/'
const PORTAL_VERSIONS_PATH = 'Portal/versions'

function getPortalEndpoint() {
  try {
    const override =
      window.SRC_OVERRIDE_ENDPOINT_CONFIG &&
      window.SRC_OVERRIDE_ENDPOINT_CONFIG.PORTAL
    return override || PRODUCTION_PORTAL_ENDPOINT
  } catch {
    return PRODUCTION_PORTAL_ENDPOINT
  }
}

let versionsPromise = null

/**
 * Fetch and cache `{ portalVersion, repoVersion }`.
 */
function getVersions() {
  if (!versionsPromise) {
    versionsPromise = fetch(
      `${getPortalEndpoint().replace(/\/+$/, '')}/${PORTAL_VERSIONS_PATH}`,
      { mode: 'cors', credentials: 'omit', keepalive: true },
    )
      .then(res => res.text())
      .then(text => {
        const [portalVersion, repoVersion] = text.split(',')
        return { portalVersion, repoVersion }
      })
      .catch(() => ({}))
  }
  return versionsPromise
}

/**
 * Send one OpenTelemetry-shaped log record to the Portal's log servlet
 * (`POST /Portal/log`), which writes it to stdout for CloudWatch Logs. Timestamp
 * is captured before the async version lookup, so it records when the event
 * happened. `keepalive` lets the request
 * survive a page reload (e.g. the one `attemptRecovery` triggers) without
 * needing to be awaited first.
 */
export function sendLog(eventName, severityText, body, attributes) {
  try {
    const timestamp = new Date().toISOString()
    return getVersions()
      .then(versions => {
        return fetch(
          `${getPortalEndpoint().replace(/\/+$/, '')}/${LOG_SERVLET_PATH}`,
          {
            method: 'POST',
            mode: 'cors',
            credentials: 'omit',
            keepalive: true,
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              timestamp,
              severityText,
              eventName,
              body,
              attributes: {
                ...attributes,
                'synapse.repo.version': versions.repoVersion,
              },
            }),
          },
        )
      })
      .catch(() => {})
  } catch {
    return Promise.resolve()
  }
}

// '/PasswordResetSignedToken:abc…' → '/PasswordResetSignedToken:{token}'
// '/Synapse:syn123/wiki/456'       → '/Synapse:{token}'
// '/'                              → '/'
export function toUrlTemplate(href) {
  try {
    const first = new URL(href).pathname.split('/').find(Boolean) || ''
    const colon = first.indexOf(':')
    return colon === -1 ? `/${first}` : `/${first.slice(0, colon)}:{token}`
  } catch {
    return undefined
  }
}

// Called from GWT (SynapseJSNIUtilsImpl.logClientError) to send uncaught errors and upload failures to
// /Portal/log. Each distinct error is sent once per page load, and at most MAX_CLIENT_ERRORS_PER_EVENT per
// eventName per tab, so a repeating error can't flood the logs (see PLFM-9565). Counts are kept per eventName
// so one kind of error can't use up the budget for the others, and are stored in sessionStorage so reloading
// the page doesn't reset them.
const MAX_CLIENT_ERRORS_PER_EVENT = 25
const CLIENT_ERROR_COUNTS_KEY = 'swcClientErrorCounts'
const seenClientErrors = new Set()
const clientErrorCounts = loadClientErrorCounts()

function loadClientErrorCounts() {
  try {
    const counts = JSON.parse(sessionStorage.getItem(CLIENT_ERROR_COUNTS_KEY))
    if (counts && typeof counts === 'object') return counts
  } catch {
    // sessionStorage is unavailable or the value is corrupt: start from zero
  }
  return {}
}

function saveClientErrorCounts() {
  try {
    sessionStorage.setItem(
      CLIENT_ERROR_COUNTS_KEY,
      JSON.stringify(clientErrorCounts),
    )
  } catch {
    // sessionStorage is unavailable or full: counts still apply for this page load
  }
}

window._swcLogClientError = function (eventName, message, stacktrace) {
  try {
    const key = `${eventName}|${message}`
    const count = clientErrorCounts[eventName] || 0
    if (seenClientErrors.has(key) || count >= MAX_CLIENT_ERRORS_PER_EVENT) {
      return
    }
    seenClientErrors.add(key)
    clientErrorCounts[eventName] = count + 1
    saveClientErrorCounts()
    sendLog(
      eventName,
      'ERROR',
      { message },
      {
        'exception.message': message,
        'exception.stacktrace': stacktrace || undefined,
        'url.template': toUrlTemplate(window.location.href),
        'url.domain': window.location.hostname,
      },
    )
  } catch {
    // Ignore failures here: this runs inside SWC's uncaught error handler, so throwing would raise another
    // error (and could trigger another log attempt)
  }
}
