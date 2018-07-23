package org.sagebionetworks.web.client.widget.table.modal.fileview;

// Table, or a Project View, or a combination View composed of Files/Folders/Tables
import static org.sagebionetworks.web.shared.WebConstants.FILE;
import static org.sagebionetworks.web.shared.WebConstants.FOLDER;
import static org.sagebionetworks.web.shared.WebConstants.PROJECT;
import static org.sagebionetworks.web.shared.WebConstants.TABLE;

import java.util.HashSet;

import com.google.gwt.core.client.GWT;

public enum TableType {
	table(null),
	projects(PROJECT),
	tables(TABLE),
	folders(FOLDER),
	folders_tables(FOLDER|TABLE),
	files(FILE),
	files_tables(FILE|TABLE),
	files_folders(FILE|FOLDER),
	files_folders_tables(FILE|FOLDER|TABLE);

	private Integer viewTypeMask;
	
	TableType(Integer viewTypeMask) {
		this.viewTypeMask = viewTypeMask;
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
	private static final HashSet<Integer> SUPPORTED_MASKS = new HashSet<>();
	static {
		for (TableType type : TableType.values()) {
			if (type.getViewTypeMask() == null) {
				continue;
			}
			
			SUPPORTED_MASKS.add(type.getViewTypeMask());
		}
	}
	public static boolean isSupportedViewTypeMask(Long viewTypeMask) {
		if (viewTypeMask == null) {
			return false;
		}
		return SUPPORTED_MASKS.contains(viewTypeMask.intValue());
	}
	
}