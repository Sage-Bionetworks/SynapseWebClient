package org.sagebionetworks.web.client.widget.clienthelp;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ContainerClientsHelpImpl implements ContainerClientsHelp {
	@UiField
	SpanElement id1;
	@UiField
	SpanElement id2;
	@UiField
	Modal modal;
	@UiField
	TabListItem cliTabListItem;
	@UiField
	TabListItem pythonTabListItem;
	@UiField
	TabPane cliTabPane;
	@UiField
	TabPane pythonTabPane;
	
	Widget widget;
	String entityId = null;
	public interface Binder extends UiBinder<Widget, ContainerClientsHelpImpl> {}

	@Inject
	public ContainerClientsHelpImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		FileClientsHelpViewImpl.setId(cliTabListItem, cliTabPane);
		FileClientsHelpViewImpl.setId(pythonTabListItem, pythonTabPane);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void configureAndShow(String entityId) {
		this.entityId = entityId;
		id1.setInnerHTML(entityId);
		id2.setInnerHTML(entityId);
		modal.show();
	}
}
