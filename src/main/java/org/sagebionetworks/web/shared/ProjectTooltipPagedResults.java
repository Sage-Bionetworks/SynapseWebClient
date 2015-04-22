package org.sagebionetworks.web.shared;

import java.util.List;

import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.UserProfile;

public class ProjectTooltipPagedResults {
	private int totalNumberOfResults;
	private List<ProjectHeader> results;
	private List<UserProfile> lastModifiedBy;

	public ProjectTooltipPagedResults() {	
	}
	
	public ProjectTooltipPagedResults(List<ProjectHeader> results, int totalNumberOfResults, List<UserProfile> lastModifiedBy) {	
		this.results = results;
		this.totalNumberOfResults = totalNumberOfResults;
		this.lastModifiedBy = lastModifiedBy;
	}
	
	public int getTotalNumberOfResults() {
		return totalNumberOfResults;
	}

	public void setTotalNumberOfResults(int totalNumberOfResults) {
		this.totalNumberOfResults = totalNumberOfResults;
	}

	public List<ProjectHeader> getProjectHeaders() {
		return results;
	}

	public void setProjectHeaders(List<ProjectHeader> results) {
		this.results = results;
	}
	
	public List<UserProfile> getModifiedByUserProfiles() {
		return lastModifiedBy;
	}
	
	public void setModiedByUserProfiles(List<UserProfile> lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	
}
