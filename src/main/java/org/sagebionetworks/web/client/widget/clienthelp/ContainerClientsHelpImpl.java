package org.sagebionetworks.web.client.widget.clienthelp;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ContainerClientsHelpImpl implements ContainerClientsHelp {
	@UiField
	SpanElement id1;
	@UiField
	SpanElement id2;
	Widget widget;
	String entityId = null;
	public interface Binder extends UiBinder<Widget, ContainerClientsHelpImpl> {}

	@Inject
	public ContainerClientsHelpImpl(Binder binder) {
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
	}
}
