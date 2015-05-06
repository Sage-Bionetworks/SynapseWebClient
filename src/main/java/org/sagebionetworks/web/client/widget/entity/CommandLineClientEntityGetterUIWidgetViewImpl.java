package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class CommandLineClientEntityGetterUIWidgetViewImpl extends com.google.gwt.user.client.ui.Composite {

	public interface Binder extends UiBinder<Widget, CommandLineClientEntityGetterUIWidgetViewImpl> {}
	
	@UiField
	SpanElement synid1;
	
	@Inject
	public CommandLineClientEntityGetterUIWidgetViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public void configure(String id) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		synid1.setInnerText(safeId);
	}
	
	public Widget asWidget() {
		return this;
	}
}
