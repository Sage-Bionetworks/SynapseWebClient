package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.List;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadPreviewViewImpl implements UploadPreviewView {

	private static String MIN_WIDTH = "75px";

	public interface Binder extends UiBinder<Widget, UploadPreviewViewImpl> {
	}

	@UiField
	Text previewMessage;
	@UiField
	Table table;
	@UiField
	TableRow header;
	@UiField
	TBody body;
	@UiField
	Alert emptyResults;

	Widget widget;

	@Inject
	public UploadPreviewViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setHeaders(List<String> headers) {
		header.clear();
		body.clear();
		for (String value : headers) {
			TableHeader th = new TableHeader();
			th.setMinimumWidth(MIN_WIDTH);
			th.add(new Strong(value));
			header.add(th);
		}
	}

	@Override
	public void addRow(List<String> row) {
		TableRow tr = new TableRow();
		for (String cell : row) {
			TableData td = new TableData();
			td.add(new Text(cell));
			tr.add(td);
		}
		body.add(tr);
	}

	@Override
	public void setPreviewMessage(String message) {
		previewMessage.setText(message);
	}

	@Override
	public void showEmptyPreviewMessage(String message) {
		emptyResults.setText(message);
	}

	@Override
	public void setEmptyMessageVisible(boolean visibile) {
		emptyResults.setVisible(visibile);
	}

	@Override
	public void setTableVisible(boolean visibile) {
		this.table.setVisible(visibile);
	}

}
