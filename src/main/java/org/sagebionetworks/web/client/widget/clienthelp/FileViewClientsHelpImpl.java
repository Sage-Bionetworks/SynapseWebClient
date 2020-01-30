package org.sagebionetworks.web.client.widget.clienthelp;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileViewClientsHelpImpl implements FileViewClientsHelp {
	@UiField
	SpanElement queryElement;
	@UiField
	Modal modal;
	@UiField
	TabListItem cliTabListItem;
	// @UiField
	// TabListItem pythonTabListItem;
	@UiField
	TabPane cliTabPane;
	// @UiField
	// TabPane pythonTabPane;

	Widget widget;
	String sql = null;

	public interface Binder extends UiBinder<Widget, FileViewClientsHelpImpl> {
	}

	@Inject
	public FileViewClientsHelpImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		FileClientsHelpViewImpl.setId(cliTabListItem, cliTabPane);
		// FileClientsHelpImpl.setId(pythonTabListItem, pythonTabPane);
	}

	@Override
	public void show() {
		modal.show();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setQuery(String sql) {
		this.sql = sql;
		queryElement.setInnerHTML(sql);
	}
}
