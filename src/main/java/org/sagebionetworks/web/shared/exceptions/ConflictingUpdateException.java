package org.sagebionetworks.web.shared.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.sagebionetworks.repo.model.ErrorResponseCode;

public class ConflictingUpdateException
  extends RestServiceException
  implements IsSerializable {

  private static final long serialVersionUID = 1L;

  public ConflictingUpdateException() {
    super();
  }

  public ConflictingUpdateException(String message) {
    super(message);
  }

  public ConflictingUpdateException(String message, ErrorResponseCode code) {
    super(message, code);
  }
}
