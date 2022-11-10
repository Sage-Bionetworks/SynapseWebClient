package org.sagebionetworks.web.server.servlet;

public interface TokenProvider {
  /**
   * Get the user's Synapse access token.
   *
   * @return
   */
  public String getToken();
}
