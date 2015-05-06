package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.entity.query.SortDirection;

public enum SortOptionEnum {
	
	LATEST_ACTIVITY("Activity: Most Recent", ProjectListSortColumn.LAST_ACTIVITY, SortDirection.DESC), 
	EARLIEST_ACTIVITY("Activity: Least Recent", ProjectListSortColumn.LAST_ACTIVITY, SortDirection.ASC),
	NAME_A_Z("Name: A-Z", ProjectListSortColumn.PROJECT_NAME, SortDirection.ASC),
	NAME_Z_A("Name: Z-A", ProjectListSortColumn.PROJECT_NAME, SortDirection.DESC);
	
	public String sortText;
	public ProjectListSortColumn sortBy;
	public SortDirection sortDir;

	SortOptionEnum(String sortText, ProjectListSortColumn sortBy, SortDirection sortDir) {
		this.sortText = sortText;
		this.sortBy = sortBy;
		this.sortDir = sortDir;
	}

}
