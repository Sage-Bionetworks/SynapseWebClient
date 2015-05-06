package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class PythonClientModalWidgetViewImpl extends com.google.gwt.user.client.ui.FlowPanel implements HasWidgets {

	public interface Binder extends UiBinder<Widget, PythonClientModalWidgetViewImpl> {}

	
	@UiField(provided=true)
	PythonClientEntityGetterUIWidgetViewImpl getEntity;
	
	@UiField(provided=true)
	PythonClientInstallWidgetViewImpl install;
	
	@Inject
	public PythonClientModalWidgetViewImpl(Binder uiBinder,
			PythonClientEntityGetterUIWidgetViewImpl getEntity,
			PythonClientInstallWidgetViewImpl install) {
		this.getEntity = getEntity;
		this.install = install;
		uiBinder.createAndBindUi(this);
	}
	
	public void configure(String id) {
		getEntity.configure(id);
	}
	
	public Widget asWidget() {
		return this;
	}
}
