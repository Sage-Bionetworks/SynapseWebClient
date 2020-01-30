package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.List;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadCSVFinishPageViewImpl implements UploadCSVFinishPageView {

	public interface Binder extends UiBinder<Widget, UploadCSVFinishPageViewImpl> {
	}

	@UiField
	TextBox tableName;
	@UiField
	Table table;
	@UiField
	TBody tableBody;
	@UiField
	SimplePanel trackerPanel;

	Widget widget;

	@Inject
	public UploadCSVFinishPageViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}


	@Override
	public void setTrackerVisible(boolean visible) {
		trackerPanel.setVisible(visible);
	}

	@Override
	public void setTableName(String fileName) {
		this.tableName.setValue(fileName);
	}

	@Override
	public String getTableName() {
		return this.tableName.getValue();
	}

	@Override
	public void setColumnEditor(List<ColumnModelTableRow> editors) {
		tableBody.clear();
		for (ColumnModelTableRow row : editors) {
			tableBody.add(row);
		}
	}

	@Override
	public void addTrackerWidget(IsWidget jobTrackingWidget) {
		this.trackerPanel.add(jobTrackingWidget);
	}

}
