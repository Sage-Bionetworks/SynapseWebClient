package org.sagebionetworks.web.shared.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.sagebionetworks.repo.model.ErrorResponseCode;

public class RestServiceException extends Exception implements IsSerializable {

  private String message;
  private ErrorResponseCode code;

  public RestServiceException() {}

  public RestServiceException(String message) {
    this(message, null);
  }

  public RestServiceException(String message, ErrorResponseCode code) {
    this.message = message;
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public ErrorResponseCode getErrorResponseCode() {
    return code;
  }
}
