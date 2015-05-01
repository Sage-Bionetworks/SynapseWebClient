package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class RClientInstallWidgetViewImpl extends com.google.gwt.user.client.ui.Composite {

	public interface Binder extends UiBinder<Widget, RClientInstallWidgetViewImpl> {}

	Widget widget;
	
	@Inject
	public RClientInstallWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		initWidget(widget);
	}
	
	public Widget asWidget() {
		return widget;
	}
}
