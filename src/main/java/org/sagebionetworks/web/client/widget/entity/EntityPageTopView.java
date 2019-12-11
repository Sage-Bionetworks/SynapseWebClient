package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;

public interface EntityPageTopView extends IsWidget, SynapseView {
	void setProjectMetadata(Widget w);

	void setTabs(Widget w);

	void setProjectActionMenu(Widget w);

	void setProjectLoadingVisible(boolean visible);

	void scrollToTop();

	EventBinder<EntityPageTop> getEventBinder();
}
