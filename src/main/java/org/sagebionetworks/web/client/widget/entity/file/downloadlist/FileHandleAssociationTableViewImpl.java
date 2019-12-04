package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import java.util.ArrayList;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeaderImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.SortingListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationTableViewImpl implements FileHandleAssociationTableView, IsWidget {
	Widget w;

	interface FileHandleAssociationTableViewImplUiBinder extends UiBinder<Widget, FileHandleAssociationTableViewImpl> {
	}

	@UiField
	Table fhaTable;
	@UiField
	TableRow fhaTableHeader;
	@UiField
	Table fhaTableData;
	@UiField
	Div accessRestrictionDetectedUI;
	@UiField
	SortableTableHeaderImpl fileNameHeader;
	@UiField
	SortableTableHeaderImpl accessHeader;
	@UiField
	SortableTableHeaderImpl createdByHeader;
	@UiField
	SortableTableHeaderImpl createdOnHeader;
	@UiField
	SortableTableHeaderImpl fileSizeHeader;
	@UiField
	TableHeader scrollBarColumnHeader;
	ArrayList<SortableTableHeaderImpl> headers = new ArrayList<>();
	private static FileHandleAssociationTableViewImplUiBinder uiBinder = GWT.create(FileHandleAssociationTableViewImplUiBinder.class);

	@Inject
	public FileHandleAssociationTableViewImpl() {
		w = uiBinder.createAndBindUi(this);
		headers.add(fileNameHeader);
		headers.add(accessHeader);
		headers.add(createdByHeader);
		headers.add(createdOnHeader);
		headers.add(fileSizeHeader);
	}

	@Override
	public void setSortingListener(SortingListener handler) {
		for (SortableTableHeaderImpl header : headers) {
			header.setSortingListener(handler);
		}
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void clear() {
		fhaTableData.clear();
		accessRestrictionDetectedUI.setVisible(false);
	}

	@Override
	public void clearRows() {
		fhaTableData.clear();
	}

	@Override
	public void addRow(IsWidget w) {
		fhaTableData.add(w);
	}

	@Override
	public void showAccessRestrictionsDetectedUI() {
		accessRestrictionDetectedUI.setVisible(true);
	}

	@Override
	public void setSort(String headerName, SortDirection sortDir) {
		// set sort icon
		for (SortableTableHeaderImpl header : headers) {
			if (headerName.equals(header.getText())) {
				org.sagebionetworks.repo.model.table.SortDirection direction = SortDirection.ASC.equals(sortDir) ? org.sagebionetworks.repo.model.table.SortDirection.ASC : org.sagebionetworks.repo.model.table.SortDirection.DESC;
				header.setSortDirection(direction);
			} else {
				header.setSortDirection(null);
			}
		}
	}

	@Override
	public void setScrollBarColumnVisible(boolean visible) {
		scrollBarColumnHeader.setVisible(visible);
	}
}
