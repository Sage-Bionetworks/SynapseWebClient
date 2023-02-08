package org.sagebionetworks.web.server.servlet;

public interface RequestHostProvider {
  /**
   * Get the hostname in the HTTP request that triggered this call
   *
   * @return
   */
  public String getRequestHost();
}
