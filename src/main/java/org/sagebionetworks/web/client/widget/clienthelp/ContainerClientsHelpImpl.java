package org.sagebionetworks.web.client.widget.clienthelp;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
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
	@UiField
	SpanElement id3;
	@UiField
	Modal modal;
	@UiField
	TabListItem cliTabListItem;
	@UiField
	TabListItem pythonTabListItem;
	@UiField
	TabListItem rTabListItem;
	@UiField
	TabPane cliTabPane;
	@UiField
	TabPane pythonTabPane;
	@UiField
	TabPane rTabPane;


	Widget widget;
	String entityId = null;

	public interface Binder extends UiBinder<Widget, ContainerClientsHelpImpl> {
	}

	@Inject
	public ContainerClientsHelpImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		FileClientsHelpViewImpl.setId(cliTabListItem, cliTabPane);
		FileClientsHelpViewImpl.setId(pythonTabListItem, pythonTabPane);
		FileClientsHelpViewImpl.setId(rTabListItem, rTabPane);
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
		id3.setInnerHTML(entityId);
		modal.show();
	}
}
