package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RClientEntityGetterUIWidgetViewImpl extends com.google.gwt.user.client.ui.Composite {

	public interface Binder extends UiBinder<Widget, RClientEntityGetterUIWidgetViewImpl> {}
	
	@UiField
	SpanElement synid1;	
	
	@UiField
	SpanElement synid2;	
	
	@UiField
	SpanElement synid3;	
	
	@UiField
	SpanElement synid4;	

	
	@Inject
	public RClientEntityGetterUIWidgetViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public void configure(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		synid1.setInnerText(safeId);
		synid2.setInnerText(safeId);
		synid3.setInnerText(safeId);
		synid4.setInnerText(safeId);
	}
	
	public Widget asWidget() {
		return this;
	}
}
