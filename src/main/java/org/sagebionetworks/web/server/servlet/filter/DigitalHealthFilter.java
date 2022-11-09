package org.sagebionetworks.web.server.servlet.filter;

public class DigitalHealthFilter extends ExternalRedirectFilter {

  @Override
  protected String getTargetURL() {
    return "https://dhealth.synapse.org/";
  }
}
