package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class CommandLineClientInstallWidgetViewImpl extends com.google.gwt.user.client.ui.Composite {

	public interface Binder extends UiBinder<Widget, CommandLineClientInstallWidgetViewImpl> {}

	Widget widget;
	
	@Inject
	public CommandLineClientInstallWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		initWidget(widget);
	}
	
	public Widget asWidget() {
		return widget;
	}
}
