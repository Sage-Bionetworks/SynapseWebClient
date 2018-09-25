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
	
	private FileHandleAssociationTableView view;
	PortalGinInjector ginInjector;
	Callback accessRestrictionDetectedCallback;
	String currentlySortingColumn = null;
	SortDirection sortDir = SortDirection.DESC;
	ArrayList<FileHandleAssociationRow> rows;
	@Inject
	public FileHandleAssociationTable(
			FileHandleAssociationTableView view,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		view.setSortingListener(this);
		accessRestrictionDetectedCallback = () -> {
			view.showAccessRestrictionsDetectedUI();
		};
	}
	
	public void configure(List<FileHandleAssociation> fhas, CallbackP<Double> addToPackageSizeCallback, CallbackP<FileHandleAssociation> onRemove) {
		view.clear();
		// create a fha table row for each fha
		rows = new ArrayList<>();
		for (FileHandleAssociation fha : fhas) {
			FileHandleAssociationRow row = ginInjector.getFileHandleAssociationRow();
			row.configure(fha, accessRestrictionDetectedCallback, addToPackageSizeCallback, onRemove);
			view.addRow(row);
			rows.add(row);
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
			//reset sort direction
			sortDir = SortDirection.ASC;
		}
		view.setSort(currentlySortingColumn,  sortDir);
		// now sort the data
		Comparator<FileHandleAssociationRow> comparator = null;
		if ("File Name".equals(header)) {
			comparator = fileNameComparator;
		} else if ("Access".equals(header)) {
			comparator = accessComparator;
		} else if ("Created By".equals(header)) {
			comparator = createdByComparator;
		} else if ("Created On".equals(header)) {
			comparator = createdOnComparator;
		} else if ("Size".equals(header)) {
			comparator = fileSizeComparator;
		}
		Collections.sort(rows, comparator);
		view.clearRows();
		for (FileHandleAssociationRow row : rows) {
			view.addRow(row);
		}
	}
	Comparator<FileHandleAssociationRow> fileNameComparator = new Comparator<FileHandleAssociationRow>() {
		@Override
		public int compare(FileHandleAssociationRow row1, FileHandleAssociationRow row2) {
			return row1.getFileName().compareTo(row2.getFileName());
		}
	};
	Comparator<FileHandleAssociationRow> accessComparator = new Comparator<FileHandleAssociationRow>() {
		@Override
		public int compare(FileHandleAssociationRow row1, FileHandleAssociationRow row2) {
			return row1.getHasAccess().compareTo(row2.getHasAccess());
		}
	};
	Comparator<FileHandleAssociationRow> createdByComparator = new Comparator<FileHandleAssociationRow>() {
		@Override
		public int compare(FileHandleAssociationRow row1, FileHandleAssociationRow row2) {
			return row1.getCreatedBy().compareTo(row2.getCreatedBy());
		}
	};
	Comparator<FileHandleAssociationRow> createdOnComparator = new Comparator<FileHandleAssociationRow>() {
		@Override
		public int compare(FileHandleAssociationRow row1, FileHandleAssociationRow row2) {
			return row1.getCreatedOn().compareTo(row2.getCreatedOn());
		}
	};

	Comparator<FileHandleAssociationRow> fileSizeComparator = new Comparator<FileHandleAssociationRow>() {
		@Override
		public int compare(FileHandleAssociationRow row1, FileHandleAssociationRow row2) {
			return row1.getFileSize().compareTo(row2.getFileSize());
		}
	};
	
}
