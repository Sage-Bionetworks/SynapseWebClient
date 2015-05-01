package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class RClientInstallWidgetViewImpl extends com.google.gwt.user.client.ui.Composite {

	public interface Binder extends UiBinder<Widget, RClientInstallWidgetViewImpl> {}
	
	@Inject
	public RClientInstallWidgetViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public Widget asWidget() {
		return this;
	}
}
