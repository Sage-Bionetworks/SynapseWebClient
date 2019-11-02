package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TeamMemberCountView extends IsWidget {
	void setSynAlert(Widget widget);

	void setCount(String count);
}
