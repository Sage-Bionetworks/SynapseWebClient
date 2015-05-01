package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class RClientModalWidgetViewImpl extends com.google.gwt.user.client.ui.FlowPanel implements HasWidgets {

	public interface Binder extends UiBinder<Widget, RClientModalWidgetViewImpl> {}

	Widget widget;
	
	@UiField(provided=true)
	RClientGetEntityWidgetViewImpl getEntity;
	
	@UiField(provided=true)
	RClientInstallWidgetViewImpl install;
	
	@Inject
	public RClientModalWidgetViewImpl(Binder uiBinder,
			RClientGetEntityWidgetViewImpl getEntity,
			RClientInstallWidgetViewImpl install) {
		this.getEntity = getEntity;
		this.install = install;
		widget = uiBinder.createAndBindUi(this);
	}
	
	public void configure(String id, Long versionNumber) {
		getEntity.configure(id, versionNumber);
	}
	
	public Widget asWidget() {
		return widget;
	}
}
