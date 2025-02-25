package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public interface StackVersionProvider {
  String get(String httpRequestHost) throws RestServiceException;
}
