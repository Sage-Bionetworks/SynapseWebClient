package org.sagebionetworks.web.client.widget.table.modal.fileview;

// Table, or a Project View, or a combination View composed of Files/Folders/Tables

import static org.sagebionetworks.web.shared.WebConstants.FILE;
import static org.sagebionetworks.web.shared.WebConstants.FOLDER;
import static org.sagebionetworks.web.shared.WebConstants.PROJECT;
import static org.sagebionetworks.web.shared.WebConstants.TABLE;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewTypeMask;
import org.sagebionetworks.web.client.DisplayConstants;


public enum TableType {
	table(null, DisplayConstants.TABLE),
	submission_view(null, DisplayConstants.SUBMISSION_VIEW),
	projects(PROJECT, DisplayConstants.PROJECT_VIEW),
	tables(TABLE, DisplayConstants.VIEW),
	folders(FOLDER, DisplayConstants.VIEW),
	folders_tables(FOLDER | TABLE, DisplayConstants.VIEW),
	files(FILE, DisplayConstants.FILE_VIEW),
	files_tables(FILE | TABLE, DisplayConstants.VIEW),
	files_folders(FILE | FOLDER, DisplayConstants.VIEW),
	files_folders_tables(FILE | FOLDER | TABLE, DisplayConstants.VIEW),
	dataset(FILE, DisplayConstants.DATASET);

	private Integer viewTypeMask;
	private String displayName;

	TableType(Integer viewTypeMask, String displayName) {
		this.viewTypeMask = viewTypeMask;
		this.displayName = displayName;
	}

	public Integer getViewTypeMask() {
		return viewTypeMask;
	}

	public boolean isIncludeFiles() {
		if (viewTypeMask == null) {
			return false;
		}
		int fileBit = viewTypeMask.intValue() & FILE;
		return fileBit > 0;
	}

	public boolean isIncludeFolders() {
		if (viewTypeMask == null) {
			return false;
		}

		int fileBit = viewTypeMask.intValue() & FOLDER;
		return fileBit > 0;
	}

	public boolean isIncludeTables() {
		if (viewTypeMask == null) {
			return false;
		}

		int fileBit = viewTypeMask.intValue() & TABLE;
		return fileBit > 0;
	}

	public static TableType getTableType(boolean isFileSelected, boolean isFolderSelected, boolean isTableSelected) {
		int viewTypeMask = 0;
		if (isFileSelected) {
			viewTypeMask = FILE;
		}
		if (isFolderSelected) {
			viewTypeMask = viewTypeMask | FOLDER;
		}
		if (isTableSelected) {
			viewTypeMask = viewTypeMask | TABLE;
		}
		return getTableType(new Long(viewTypeMask));
	}

	public static TableType getTableType(Long viewTypeMask) {
		if (viewTypeMask == null) {
			return TableType.table;
		}

		int viewTypeMaskInt = viewTypeMask.intValue();
		for (TableType type : TableType.values()) {
			if (type.getViewTypeMask() == null) {
				continue;
			}
			if (viewTypeMaskInt == type.getViewTypeMask()) {
				return type;
			}
		}
		return null;
	}

	public static TableType getTableType(Entity entity) {
		if (entity instanceof TableEntity) {
			return TableType.table;
		} else if (entity instanceof Dataset) {
			return TableType.dataset;
		} else if (entity instanceof SubmissionView) {
			return TableType.submission_view;
		} else if (entity instanceof EntityView) {
			EntityView view = (EntityView) entity;

			Long typeMask = view.getViewTypeMask();
			if (typeMask == null) {
				typeMask = ViewTypeMask.getMaskForDepricatedType(view.getType());
			}
			return getTableType(typeMask);
		}
		return null;
	}

	public String getDisplayName() {
		return this.displayName;
	}
}
