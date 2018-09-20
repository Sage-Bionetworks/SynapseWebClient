package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationTableViewImpl implements FileHandleAssociationTableView, IsWidget {
	Widget w;
	interface FileHandleAssociationTableViewImplUiBinder extends UiBinder<Widget, FileHandleAssociationTableViewImpl> {}
	@UiField
	Table fhaTable;
	@UiField
	TableRow fhaTableHeader;

	private static FileHandleAssociationTableViewImplUiBinder uiBinder = GWT
			.create(FileHandleAssociationTableViewImplUiBinder.class);
	@Inject
	public FileHandleAssociationTableViewImpl() {
		w = uiBinder.createAndBindUi(this);
	}
	@Override
	public Widget asWidget() {
		return w;
	}
	@Override
	public void clear() {
		fhaTableHeader.removeFromParent();
		fhaTable.clear();
		fhaTable.add(fhaTableHeader);
	}
	@Override
	public void addRow(IsWidget w) {
		fhaTable.add(w);
	}
}
