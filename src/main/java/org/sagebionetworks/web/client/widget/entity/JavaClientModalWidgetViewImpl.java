package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class JavaClientModalWidgetViewImpl  extends com.google.gwt.user.client.ui.FlowPanel implements HasWidgets {

	public interface Binder extends UiBinder<Widget, JavaClientModalWidgetViewImpl> {}

	Widget widget;
	
	@UiField(provided=true)
	JavaClientGetEntityWidgetViewImpl getEntity;
	
	@UiField(provided=true)
	JavaClientInstallWidgetViewImpl install;
	
	@Inject
	public JavaClientModalWidgetViewImpl(Binder uiBinder,
			JavaClientInstallWidgetViewImpl install,
			JavaClientGetEntityWidgetViewImpl getEntity) {
		this.install = install;
		this.getEntity = getEntity;
		widget = uiBinder.createAndBindUi(this);
	}
	
	public void configure(String id) {
		getEntity.configure(id);
	}
	
	public Widget asWidget() {
		return widget;
	}
}
