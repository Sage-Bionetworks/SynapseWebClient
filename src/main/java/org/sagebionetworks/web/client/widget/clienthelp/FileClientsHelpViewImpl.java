package org.sagebionetworks.web.client.widget.clienthelp;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileClientsHelpViewImpl implements FileClientsHelpView {
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
	@UiField
	SpanElement versionUI1;
	@UiField
	SpanElement versionUI2;
	@UiField
	SpanElement versionUI3;
	@UiField
	SpanElement version1;
	@UiField
	SpanElement version2;
	@UiField
	SpanElement version3;
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

	public interface Binder extends UiBinder<Widget, FileClientsHelpViewImpl> {
	}

	@Inject
	public FileClientsHelpViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		FileClientsHelpViewImpl.setId(cliTabListItem, cliTabPane);
		FileClientsHelpViewImpl.setId(pythonTabListItem, pythonTabPane);
		FileClientsHelpViewImpl.setId(rTabListItem, rTabPane);
	}

	public static void setId(TabListItem tabListItem, TabPane tabPane) {
		String id = HTMLPanel.createUniqueId();
		tabListItem.setDataTarget("#" + id);
		tabPane.setId(id);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void configureAndShow(String entityId, Long version) {
		id1.setInnerHTML(entityId);
		id2.setInnerHTML(entityId);
		id3.setInnerHTML(entityId);
		id4.setInnerHTML(entityId);
		id5.setInnerHTML(entityId);
		id6.setInnerHTML(entityId);
		String versionString = version.toString();
		version1.setInnerHTML(versionString);
		version2.setInnerHTML(versionString);
		version3.setInnerHTML(versionString);
		modal.show();
	}

	@Override
	public void setVersionVisible(boolean visible) {
		UIObject.setVisible(versionUI1, visible);
		UIObject.setVisible(versionUI2, visible);
		UIObject.setVisible(versionUI3, visible);
	}
}
