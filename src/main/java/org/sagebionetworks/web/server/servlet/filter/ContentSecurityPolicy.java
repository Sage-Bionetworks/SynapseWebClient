package org.sagebionetworks.web.server.servlet.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * The documented list of sources that may supply script, frames, and other content to the Synapse web client: its
 * definition of acceptable mobile code for NIST SP 800-53 SC-18 (SWC-8037). ContentSecurityPolicyFilter sends the
 * policy built from this list as Content-Security-Policy-Report-Only. See devdocs/CONTENT_SECURITY_POLICY.md.
 */
public final class ContentSecurityPolicy {

  private ContentSecurityPolicy() {}

  /**
   * A source expression, the directives it is allowed in, the feature that needs it, and who operates it.
   */
  public record AllowedSource(
    String source,
    List<String> directives,
    String purpose,
    String owner
  ) {}

  public static final String DEFAULT_SRC = "default-src";
  public static final String SCRIPT_SRC = "script-src";
  public static final String STYLE_SRC = "style-src";
  public static final String FONT_SRC = "font-src";
  public static final String IMG_SRC = "img-src";
  public static final String MEDIA_SRC = "media-src";
  public static final String CONNECT_SRC = "connect-src";
  public static final String FRAME_SRC = "frame-src";
  public static final String WORKER_SRC = "worker-src";
  public static final String MANIFEST_SRC = "manifest-src";
  public static final String OBJECT_SRC = "object-src";
  public static final String BASE_URI = "base-uri";
  public static final String FORM_ACTION = "form-action";
  public static final String FRAME_ANCESTORS = "frame-ancestors";

  // Every directive the policy sets, in output order. A directive with no allowed source is 'none'.
  // object-src and frame-ancestors have none: no site may frame the web client, including the web client itself
  public static final List<String> DIRECTIVES = List.of(
    DEFAULT_SRC,
    SCRIPT_SRC,
    STYLE_SRC,
    FONT_SRC,
    IMG_SRC,
    MEDIA_SRC,
    CONNECT_SRC,
    FRAME_SRC,
    WORKER_SRC,
    MANIFEST_SRC,
    OBJECT_SRC,
    BASE_URI,
    FORM_ACTION,
    FRAME_ANCESTORS
  );

  // Placeholder for the CDN origin of the requested site (e.g. https://cdn-www.synapse.org), which build() fills in
  public static final String SYNAPSE_CDN = "{synapse-cdn}";

  public static final String SAGE = "Sage Bionetworks";
  public static final String GOOGLE = "Google";

  // Sources allowed on every stack
  public static final List<AllowedSource> ALLOWED_SOURCES = List.of(
    new AllowedSource(
      "'self'",
      List.of(
        DEFAULT_SRC,
        SCRIPT_SRC,
        STYLE_SRC,
        FONT_SRC,
        MEDIA_SRC,
        CONNECT_SRC,
        FRAME_SRC,
        WORKER_SRC,
        MANIFEST_SRC,
        BASE_URI,
        FORM_ACTION
      ),
      "The web client itself: Portal.html, RPC and servlet calls, MD5 web workers (/workers), manifest.json, and the same-origin iframes GWT installs its code in",
      SAGE
    ),
    new AllowedSource(
      SYNAPSE_CDN,
      List.of(SCRIPT_SRC, STYLE_SRC, FONT_SRC),
      "Static resources for www, staging and tst (HtmlInjectionFilter cdnEndpoint): GWT code, the Vite bundle, bundled libraries, CSS and fonts",
      SAGE
    ),
    new AllowedSource(
      "'unsafe-inline'",
      List.of(SCRIPT_SRC),
      "The GWT xsiframe linker installs the compiled code and runAsync fragments as inline scripts; gwtbootstrap3 injects library code with ScriptInjector.fromString; trusted users' HTML and notebook previews render in srcdoc iframes, which inherit this policy",
      SAGE
    ),
    new AllowedSource(
      "'unsafe-eval'",
      List.of(SCRIPT_SRC),
      "GWT-RPC (stream version 7) decodes responses with eval; @rjsf/validator-ajv8 compiles JSON schemas into functions; jquery.tablesorter; the plotly full bundle; the Google Maps JavaScript API",
      SAGE
    ),
    new AllowedSource(
      "'unsafe-inline'",
      List.of(STYLE_SRC),
      "MUI/Emotion, plotly and GWT <ui:style> insert <style> elements; KaTeX, sanitized wiki HTML (text-align) and GWT-built HTML use style attributes",
      SAGE
    ),
    new AllowedSource(
      "data:",
      List.of(IMG_SRC),
      "TIFF previews drawn to a canvas, data:image URLs in sanitized wiki HTML, images inlined in stylesheets",
      SAGE
    ),
    new AllowedSource(
      "blob:",
      List.of(IMG_SRC, MEDIA_SRC, FRAME_SRC, WORKER_SRC),
      "Files fetched into object URLs: upload and avatar previews, PDF and data use certificate previews; pica image resizing workers",
      SAGE
    ),
    new AllowedSource(
      "*",
      List.of(IMG_SRC),
      "Wiki markdown can show images from any host. Display-only; script-src is not affected",
      "Wiki authors"
    ),
    new AllowedSource(
      "https:",
      List.of(MEDIA_SRC),
      "Video and audio from Synapse storage (data.prod.sagebase.org) and from user-configured external storage, which can be any host",
      "Synapse users"
    ),
    new AllowedSource(
      "https:",
      List.of(CONNECT_SRC),
      "Uploads to and downloads from user-configured external object stores, which can be any host (AwsSdk, Uploader, FileDownloadHandlerWidget). Also covers the fixed APIs: repo-prod.prod.sagebase.org, data.prod.sagebase.org, api.datacite.org, www.ebi.ac.uk, citation.doi.org, api.ror.org, api.short.io, Google Analytics",
      "Synapse users"
    ),
    new AllowedSource(
      "wss://*.execute-api.us-east-1.amazonaws.com",
      List.of(CONNECT_SRC),
      "Synapse Grid websocket. The API Gateway is created per stack release, so its ID changes",
      SAGE
    ),
    new AllowedSource(
      "https://www.googletagmanager.com",
      List.of(SCRIPT_SRC, FRAME_SRC),
      "Google Tag Manager container GTM-KPW4KS62 (synapse-react-client useGoogleAnalytics) and its <noscript> iframe",
      GOOGLE
    ),
    new AllowedSource(
      "https://www.google.com",
      List.of(SCRIPT_SRC),
      "Google Ads tags loaded by the Tag Manager container",
      GOOGLE
    ),
    new AllowedSource(
      "https://googleads.g.doubleclick.net",
      List.of(SCRIPT_SRC),
      "Google Ads tags loaded by the Tag Manager container",
      GOOGLE
    ),
    new AllowedSource(
      "https://www.googleadservices.com",
      List.of(SCRIPT_SRC),
      "Google Ads conversion tags loaded by the Tag Manager container (SWC-7749)",
      GOOGLE
    ),
    new AllowedSource(
      "https://maps.googleapis.com",
      List.of(SCRIPT_SRC),
      "Google Maps JavaScript API for the Google Map wiki widget (GoogleMap)",
      GOOGLE
    ),
    new AllowedSource(
      "https://fonts.googleapis.com",
      List.of(STYLE_SRC),
      "Google Fonts stylesheets (Portal.html, synapse-react-client)",
      GOOGLE
    ),
    new AllowedSource(
      "https://fonts.gstatic.com",
      List.of(FONT_SRC),
      "Google Fonts font files",
      GOOGLE
    ),
    // Contentsquare documents *.contentsquare.net (https://docs.contentsquare.com/en/web/content-security-policy/);
    // these are the hosts the survey uses on Synapse. A host it adds later shows up as a Report-Only violation
    new AllowedSource(
      "https://t.contentsquare.net",
      List.of(SCRIPT_SRC),
      "Contentsquare tag loader and runtime for the Hotjar NPS survey, loaded by the Tag Manager container (SWC-7749)",
      "Contentsquare (Hotjar)"
    ),
    new AllowedSource(
      "https://script.hj.contentsquare.net",
      List.of(SCRIPT_SRC, FONT_SRC),
      "Hotjar NPS survey widget script and fonts (SWC-7749)",
      "Contentsquare (Hotjar)"
    ),
    new AllowedSource(
      "https://sagebionetworks.jira.com",
      List.of(SCRIPT_SRC, STYLE_SRC, FRAME_SRC),
      "Jira issue collector on access requirements (ACTAccessRequirementWidget)",
      "Atlassian"
    ),
    // Frames: video widget
    new AllowedSource(
      "https://www.youtube.com",
      List.of(FRAME_SRC),
      "Video widget YouTube embeds",
      GOOGLE
    ),
    new AllowedSource(
      "https://player.vimeo.com",
      List.of(FRAME_SRC),
      "Video widget Vimeo embeds (SWC-8150)",
      "Vimeo"
    ),
    // Frames: External Website widget (ShinySiteWidget.VALID_URL_BASES). Two of its bases are not allowed:
    // http://spark.rstudio.com/ (plain http, blocked as mixed content) and https://dce.seattlebiosoftware.com/
    // (blocked by every CSP since July 2023 without a reported problem)
    new AllowedSource(
      "https://*.synapse.org",
      List.of(FRAME_SRC),
      "External Website widget: any Synapse site",
      SAGE
    ),
    new AllowedSource(
      "https://*.sagebase.org",
      List.of(FRAME_SRC),
      "External Website widget: any Sage site",
      SAGE
    ),
    new AllowedSource(
      "https://s3.amazonaws.com/static.synapse.org/",
      List.of(FRAME_SRC),
      "External Website widget: the static.synapse.org bucket (this path only)",
      SAGE
    ),
    new AllowedSource(
      "https://docs.google.com",
      List.of(FRAME_SRC),
      "External Website widget: Google Forms",
      GOOGLE
    ),
    new AllowedSource(
      "https://calendar.google.com",
      List.of(FRAME_SRC),
      "External Website widget: Google Calendar",
      GOOGLE
    ),
    new AllowedSource(
      "https://sage-bionetworks.shinyapps.io",
      List.of(FRAME_SRC),
      "External Website widget: Sage Shiny apps",
      "Posit (shinyapps.io)"
    ),
    new AllowedSource(
      "https://sagebio.shinyapps.io",
      List.of(FRAME_SRC),
      "External Website widget: Sage Shiny apps",
      "Posit (shinyapps.io)"
    ),
    new AllowedSource(
      "https://olssol.shinyapps.io",
      List.of(FRAME_SRC),
      "External Website widget: rwebgenie Shiny app",
      "Posit (shinyapps.io)"
    ),
    new AllowedSource(
      "https://publish.smartsheet.com",
      List.of(FRAME_SRC),
      "External Website widget: published Smartsheets",
      "Smartsheet"
    ),
    new AllowedSource(
      "https://airtable.com",
      List.of(FRAME_SRC),
      "External Website widget: Airtable embeds (SWC-7772)",
      "Airtable"
    )
  );

  // Sources added only when the server runs in development mode (StackEndpoints.IS_DEV_MODE)
  public static final List<AllowedSource> DEV_ALLOWED_SOURCES = List.of(
    new AllowedSource(
      "http://127.0.0.1:9876",
      List.of(SCRIPT_SRC),
      "GWT code server (Super Dev Mode)",
      SAGE
    ),
    new AllowedSource(
      "http://localhost:5173",
      List.of(SCRIPT_SRC, STYLE_SRC, FONT_SRC, CONNECT_SRC),
      "Vite dev server",
      SAGE
    ),
    new AllowedSource(
      "ws://localhost:5173",
      List.of(CONNECT_SRC),
      "Vite hot module reload",
      SAGE
    ),
    new AllowedSource(
      "http://localhost:8080",
      List.of(CONNECT_SRC),
      "Local repository services",
      SAGE
    ),
    new AllowedSource(
      "http://127.0.0.1:8080",
      List.of(CONNECT_SRC),
      "Local repository services",
      SAGE
    )
  );

  /**
   * Builds the policy from the allowed sources.
   * @param cdnOrigin origin that replaces SYNAPSE_CDN (e.g. https://cdn-www.synapse.org), or null if the site has no CDN
   * @param reportingEndpointName Reporting-Endpoints name for report-to
   * @param reportUri report-uri fallback for browsers that don't support report-to
   */
  public static String build(
    List<AllowedSource> sources,
    String cdnOrigin,
    String reportingEndpointName,
    String reportUri
  ) {
    Map<String, List<String>> sourcesByDirective = new LinkedHashMap<>();
    for (String directive : DIRECTIVES) {
      sourcesByDirective.put(directive, new ArrayList<>());
    }
    for (AllowedSource allowed : sources) {
      String source = allowed.source();
      if (SYNAPSE_CDN.equals(source)) {
        if (cdnOrigin == null) continue;
        source = cdnOrigin;
      }
      for (String directive : allowed.directives()) {
        List<String> directiveSources = sourcesByDirective.get(directive);
        if (directiveSources == null) {
          throw new IllegalArgumentException(
            "Unknown directive " + directive + " for " + allowed.source()
          );
        }
        directiveSources.add(source);
      }
    }
    StringJoiner policy = new StringJoiner("; ");
    sourcesByDirective.forEach((directive, directiveSources) ->
      policy.add(
        directive +
        " " +
        (directiveSources.isEmpty()
            ? "'none'"
            : String.join(" ", directiveSources))
      )
    );
    policy.add("report-to " + reportingEndpointName);
    policy.add("report-uri " + reportUri);
    return policy.toString();
  }
}
