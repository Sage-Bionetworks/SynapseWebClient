package org.sagebionetworks.web.client.exceptions;

/**
 * Used to handle a misconfiguration in the web client. This indicates that the user reached a state
 * that should be unreachable, and that something was misconfigured to allow it to happen.
 */
public class WebClientConfigurationException extends RuntimeException {

  WebClientConfigurationException() {
    super();
  }

  public WebClientConfigurationException(String message) {
    super(message);
  }
}
