package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListWidgetViewImpl implements EntityListWidgetView {

	public interface Binder extends UiBinder<Widget, EntityListWidgetViewImpl> {
	}

	Widget widget;
	@UiField
	Panel table;
	@UiField
	Div rows;
	@UiField
	Span emptyUI;
	@UiField
	TableHeader descriptionHeader;

	@Inject
	public EntityListWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}


	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addRow(Widget w) {
		rows.add(w);
	}

	@Override
	public void clearRows() {
		rows.clear();
	}

	@Override
	public void setEmptyUiVisible(boolean visible) {
		emptyUI.setVisible(visible);
	}

	@Override
	public void setTableVisible(boolean visible) {
		table.setVisible(visible);
	}

	@Override
	public void setDescriptionHeaderVisible(boolean visible) {
		descriptionHeader.setVisible(visible);
	}
}
