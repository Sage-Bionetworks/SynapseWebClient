package org.sagebionetworks.web.client.widget.clienthelp;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileClientsHelpImpl implements FileClientsHelp {
	@UiField
	SpanElement id1;
	@UiField
	SpanElement id2;
	@UiField
	SpanElement id3;
	@UiField
	SpanElement id4;
	@UiField
	SpanElement id5;
	@UiField
	SpanElement id6;
	Widget widget;
	String entityId = null;
	public interface Binder extends UiBinder<Widget, FileClientsHelpImpl> {}

	@Inject
	public FileClientsHelpImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void configure(String entityId) {
		this.entityId = entityId;
		id1.setInnerHTML(entityId);
		id2.setInnerHTML(entityId);
		id3.setInnerHTML(entityId);
		id4.setInnerHTML(entityId);
		id5.setInnerHTML(entityId);
		id6.setInnerHTML(entityId);
	}
}
