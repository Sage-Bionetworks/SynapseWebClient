package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionTabViewImpl implements DiscussionTabView {

	@UiField
	SimplePanel simplePanel;

	Widget widget;
	public interface TabsViewImplUiBinder extends UiBinder<Widget, DiscussionTabViewImpl> {}

	public DiscussionTabViewImpl() {
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

}
