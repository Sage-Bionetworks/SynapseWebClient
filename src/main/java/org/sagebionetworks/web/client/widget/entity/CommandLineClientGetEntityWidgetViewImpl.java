package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class CommandLineClientGetEntityWidgetViewImpl extends com.google.gwt.user.client.ui.Composite {

	public interface Binder extends UiBinder<Widget, CommandLineClientGetEntityWidgetViewImpl> {}

	Widget widget;
	
	@UiField
	Text download;
	
	@Inject
	public CommandLineClientGetEntityWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		initWidget(widget);
	}
	
	public void configure(String id) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		download.setText("synapse get " + safeId);
	}
	
	public Widget asWidget() {
		return widget;
	}
}
