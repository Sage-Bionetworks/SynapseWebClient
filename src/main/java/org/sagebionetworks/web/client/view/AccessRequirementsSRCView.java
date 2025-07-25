package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;

public interface AccessRequirementsSRCView extends IsWidget {
  public void configure(
    RestrictableObjectType type,
    RestrictableObjectDescriptor subject
  );
}
