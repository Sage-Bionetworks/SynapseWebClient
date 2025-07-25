package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;

public interface AccessRequirementsSRCView extends IsWidget {
  public void configure(RestrictableObjectDescriptor subject);
}
