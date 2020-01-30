package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

public interface ProvenanceEntry extends IsWidget {

	public void setRemoveCallback(Callback removalCallback);

	public void setAnchorTarget(String targetURL);
}
