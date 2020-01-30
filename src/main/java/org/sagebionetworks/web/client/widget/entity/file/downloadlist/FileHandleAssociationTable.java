package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.v2.results.SortingListener;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationTable implements IsWidget, SortingListener {

	public static final String FILE_NAME_COLUMN_NAME = "File Name";
	public static final String ACCESS_COLUMN_NAME = "Access";
	public static final String CREATED_BY_COLUMN_NAME = "Created By";
	public static final String CREATED_ON_COLUMN_NAME = "Created On";
	public static final String SIZE_COLUMN_NAME = "Size";

	private FileHandleAssociationTableView view;
	PortalGinInjector ginInjector;
	Callback accessRestrictionDetectedCallback;
	String currentlySortingColumn = null;
	SortDirection sortDir = SortDirection.DESC;
	ArrayList<FileHandleAssociationRow> rows;

	@Inject
	public FileHandleAssociationTable(FileHandleAssociationTableView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		view.setSortingListener(this);
		accessRestrictionDetectedCallback = () -> {
			view.showAccessRestrictionsDetectedUI();
		};
	}

	public void configure(List<FileHandleAssociation> fhas, CallbackP<Double> addToPackageSizeCallback, CallbackP<FileHandleAssociation> onRemove) {
		view.clear();
		view.setScrollBarColumnVisible(fhas.size() > 9);
		// create a fha table row for each fha
		rows = new ArrayList<>();
		for (FileHandleAssociation fha : fhas) {
			FileHandleAssociationRow row = ginInjector.getFileHandleAssociationRow();
			row.configure(fha, accessRestrictionDetectedCallback, addToPackageSizeCallback, onRemove);
			view.addRow(row);
			rows.add(row);
		}
		// if previously sorted, apply to new data
		if (currentlySortingColumn != null) {
			// toggle sort direction so that it will toggle back to the correct sort
			sortDir = SortDirection.ASC.equals(sortDir) ? SortDirection.DESC : SortDirection.ASC;
			onToggleSort(currentlySortingColumn);
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onToggleSort(String header) {
		if (header.equals(currentlySortingColumn)) {
			// toggle sort direction
			sortDir = SortDirection.ASC.equals(sortDir) ? SortDirection.DESC : SortDirection.ASC;
		} else {
			// new sort column
			currentlySortingColumn = header;
			// reset sort direction
			sortDir = SortDirection.DESC;
		}
		view.setSort(currentlySortingColumn, sortDir);
		// now sort the data
		Comparator<FileHandleAssociationRow> comparator = null;
		if (FILE_NAME_COLUMN_NAME.equals(header)) {
			comparator = SortDirection.ASC.equals(sortDir) ? fileNameAscComparator : fileNameDescComparator;
		} else if (ACCESS_COLUMN_NAME.equals(header)) {
			comparator = SortDirection.ASC.equals(sortDir) ? accessAscComparator : accessDescComparator;
		} else if (CREATED_BY_COLUMN_NAME.equals(header)) {
			comparator = SortDirection.ASC.equals(sortDir) ? createdByAscComparator : createdByDescComparator;
		} else if (CREATED_ON_COLUMN_NAME.equals(header)) {
			comparator = SortDirection.ASC.equals(sortDir) ? createdOnAscComparator : createdOnDescComparator;
		} else if (SIZE_COLUMN_NAME.equals(header)) {
			comparator = SortDirection.ASC.equals(sortDir) ? fileSizeAscComparator : fileSizeDescComparator;
		}
		Collections.sort(rows, comparator);
		view.clearRows();
		for (FileHandleAssociationRow row : rows) {
			view.addRow(row);
		}
	}

	Comparator<FileHandleAssociationRow> fileNameAscComparator = (row1, row2) -> {
		if (row1.getFileName() == null) {
			return -1;
		}
		if (row2.getFileName() == null) {
			return 1;
		}
		return row1.getFileName().toUpperCase().compareTo(row2.getFileName().toUpperCase());
	};
	Comparator<FileHandleAssociationRow> fileNameDescComparator = (row1, row2) -> {
		if (row2.getFileName() == null) {
			return -1;
		}
		if (row1.getFileName() == null) {
			return 1;
		}
		return row2.getFileName().toUpperCase().compareTo(row1.getFileName().toUpperCase());
	};
	Comparator<FileHandleAssociationRow> accessAscComparator = (row1, row2) -> {
		if (row1.getHasAccess() == null) {
			return -1;
		}
		if (row2.getHasAccess() == null) {
			return 1;
		}

		return row1.getHasAccess().compareTo(row2.getHasAccess());
	};
	Comparator<FileHandleAssociationRow> accessDescComparator = (row1, row2) -> {
		if (row2.getHasAccess() == null) {
			return -1;
		}
		if (row1.getHasAccess() == null) {
			return 1;
		}
		return row2.getHasAccess().compareTo(row1.getHasAccess());
	};
	Comparator<FileHandleAssociationRow> createdByAscComparator = (row1, row2) -> {
		if (row1.getCreatedBy() == null) {
			return -1;
		}
		if (row2.getCreatedBy() == null) {
			return 1;
		}
		return row1.getCreatedBy().toUpperCase().compareTo(row2.getCreatedBy().toUpperCase());
	};
	Comparator<FileHandleAssociationRow> createdByDescComparator = (row1, row2) -> {
		if (row2.getCreatedBy() == null) {
			return -1;
		}
		if (row1.getCreatedBy() == null) {
			return 1;
		}
		return row2.getCreatedBy().toUpperCase().compareTo(row1.getCreatedBy().toUpperCase());
	};
	Comparator<FileHandleAssociationRow> createdOnAscComparator = (row1, row2) -> {
		if (row1.getCreatedOn() == null) {
			return -1;
		}
		if (row2.getCreatedOn() == null) {
			return 1;
		}
		return row1.getCreatedOn().compareTo(row2.getCreatedOn());
	};
	Comparator<FileHandleAssociationRow> createdOnDescComparator = (row1, row2) -> {
		if (row2.getCreatedOn() == null) {
			return -1;
		}
		if (row1.getCreatedOn() == null) {
			return 1;
		}
		return row2.getCreatedOn().compareTo(row1.getCreatedOn());
	};
	Comparator<FileHandleAssociationRow> fileSizeAscComparator = (row1, row2) -> {
		if (row1.getFileSize() == null) {
			return -1;
		}
		if (row2.getFileSize() == null) {
			return 1;
		}
		return row1.getFileSize().compareTo(row2.getFileSize());
	};
	Comparator<FileHandleAssociationRow> fileSizeDescComparator = (row1, row2) -> {
		if (row2.getFileSize() == null) {
			return -1;
		}
		if (row1.getFileSize() == null) {
			return 1;
		}
		return row2.getFileSize().compareTo(row1.getFileSize());
	};
}
