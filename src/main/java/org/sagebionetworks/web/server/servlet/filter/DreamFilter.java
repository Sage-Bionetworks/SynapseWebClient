package org.sagebionetworks.web.server.servlet.filter;

public class DreamFilter extends ExternalRedirectFilter {

  @Override
  protected String getTargetURL() {
    return "http://dreamchallenges.org/";
  }
}
