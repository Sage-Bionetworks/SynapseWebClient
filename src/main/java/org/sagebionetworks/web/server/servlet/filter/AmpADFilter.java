package org.sagebionetworks.web.server.servlet.filter;

public class AmpADFilter extends ExternalRedirectFilter {

  @Override
  protected String getTargetURL() {
    return "https://adknowledgeportal.synapse.org";
  }
}
