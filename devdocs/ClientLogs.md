# Querying client logs in CloudWatch

SWC logs browser-side errors and CSP violation reports to CloudWatch. This page shows how to find and query them.

## Running a query

1. In the AWS console, open **CloudWatch → Logs → Log Analytics** (formerly Logs Insights), in the account and region of the portal stack (`us-east-1`).
2. In the **Log group** filter, select the portal's SERVICE log group: `/aws/elasticbeanstalk/<portal environment>/SERVICE`, e.g. `/aws/elasticbeanstalk/portal-dev-614-0/SERVICE`. Search for the environment name to find it. The `…/var/log/…` groups are Beanstalk's own logs and don't contain these records.
3. Pick a time range. Keep the generated `SOURCE …` line at the top of the editor, paste one of the queries below after it, and click **Run**.

## Queries

Logs Insights can't read the JSON part of these lines directly (`filter eventName = …` returns nothing), so each query uses `parse` to pull out the fields it needs.

Recent uncaught errors:

```
fields @timestamp, @message
| filter @message like /\[org.sagebionetworks.web.ClientLog\]/
| parse @message /"eventName":"(?<eventName>[^"]*)"/
| parse @message /"exception.message":"(?<message>[^"]*)"/
| filter eventName = "swc.uncaught_error"
| sort @timestamp desc
| limit 100
```

Events per day, by name:

```
fields @message
| filter @message like /\[org.sagebionetworks.web.ClientLog\]/
| parse @message /"eventName":"(?<eventName>[^"]*)"/
| stats count(*) by eventName, bin(1d)
```

Most common error messages:

```
fields @message
| filter @message like /\[org.sagebionetworks.web.ClientLog\]/
| parse @message /"eventName":"(?<eventName>[^"]*)"/
| parse @message /"exception.message":"(?<message>[^"]*)"/
| filter eventName in ["swc.uncaught_error", "swc.upload_error"]
| stats count(*) as n by eventName, message
| sort n desc
| limit 20
```

Errors by page:

```
fields @message
| filter @message like /\[org.sagebionetworks.web.ClientLog\]/
| parse @message /"eventName":"(?<eventName>[^"]*)"/
| parse @message /"url.template":"(?<page>[^"]*)"/
| filter eventName = "swc.uncaught_error"
| stats count(*) as n by page
| sort n desc
```

Module-load failures by build:

```
fields @message
| filter @message like /swc.module_load.error/
| parse @message /"buildId":"(?<buildId>[^"]*)"/
| stats count(*) by buildId
```

CSP violations by directive and blocked URL. The browser sends either `effectiveDirective`/`blockedURL` (report-to) or `effective-directive`/`blocked-uri` (report-uri); these patterns match both:

```
fields @message
| filter @message like /"eventName":"csp.violation"/
| parse @message /"effective-?[dD]irective":"(?<directive>[^"]*)"/
| parse @message /"blocked(?:URL|-uri)":"(?<blocked>[^"]*)"/
| stats count(*) as n by directive, blocked
| sort n desc
```

The `[^"]*` patterns stop at the first quote, so a value that contains an escaped quote (`\"`) is cut short.

## How records get there

1. The browser POSTs to one of two endpoints:
   - `/Portal/log` ([LogServlet](../src/main/java/org/sagebionetworks/web/server/servlet/LogServlet.java)): client errors from [js/log.js](../js/log.js) and [js/moduleLoadDiagnostics.js](../js/moduleLoadDiagnostics.js).
   - `/Portal/csp-report` ([CSPReportServlet](../src/main/java/org/sagebionetworks/web/server/servlet/CSPReportServlet.java)): CSP violation reports, sent by the browser itself.
2. Both log one JSON record per event to the `org.sagebionetworks.web.ClientLog` logger.
3. [log4j2.xml](../src/main/resources/log4j2.xml) writes that logger to `portal.log`, through the non-blocking `ClientLogAsync` appender.
4. The CloudWatch agent ships `portal.log` to the SERVICE log group.

| `eventName`                                                                       | Sent by                                                       |
| --------------------------------------------------------------------------------- | ------------------------------------------------------------- |
| `swc.uncaught_error`                                                              | GWT uncaught exception handler (`GlobalApplicationStateImpl`) |
| `swc.upload_error`                                                                | Failed uploads (`Uploader`)                                   |
| `swc.module_load.error`, `swc.module_load.inspection`, `swc.module_load.recovery` | Lazy-chunk load failures (`moduleLoadDiagnostics.js`)         |
| `csp.violation`                                                                   | `CSPReportServlet`                                            |

`/Portal/log` rejects any other `eventName` with a 400 (`LogServlet.ALLOWED_EVENT_NAMES`). The server sets `observedTimestamp`, `user_agent.original`, `client.address` and `client.timestamp`; `client.timestamp` is the time the browser reported, kept for reference only.
