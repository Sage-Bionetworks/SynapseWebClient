package org.sagebionetworks.web.client.widget.entity;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class CommandLineClientModalWidgetViewImpl extends com.google.gwt.user.client.ui.FlowPanel implements HasWidgets {

	public interface Binder extends UiBinder<Widget, CommandLineClientModalWidgetViewImpl> {}

	Widget widget;
	
	@UiField(provided=true)
	CommandLineClientGetEntityWidgetViewImpl getEntity;
	
	@UiField(provided=true)
	CommandLineClientInstallWidgetViewImpl install;
	
	@Inject
	public CommandLineClientModalWidgetViewImpl(Binder uiBinder,
			CommandLineClientGetEntityWidgetViewImpl getEntity,
			CommandLineClientInstallWidgetViewImpl install) {
		this.getEntity = getEntity;
		this.install = install;
		widget = uiBinder.createAndBindUi(this);
	}
	
	public void configure(String id) {
		getEntity.configure(id);
	}
	
	public Widget asWidget() {
		return widget;
	}

}
