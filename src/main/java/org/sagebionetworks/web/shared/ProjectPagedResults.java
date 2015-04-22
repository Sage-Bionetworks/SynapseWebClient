package org.sagebionetworks.web.shared;

import java.io.Serializable;
import java.util.List;

import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.UserProfile;

public class ProjectPagedResults implements Serializable {
	private int totalNumberOfResults;
	private List<ProjectHeader> results;
	private List<UserProfile> lastModifiedBy;
	
	/**
	 * Default constructor is required
	 */
	public ProjectPagedResults() {
	}
	
	
	public ProjectPagedResults(List<ProjectHeader> results, int totalNumberOfResults) {
		super();
		this.totalNumberOfResults = totalNumberOfResults;
		this.results = results;
	}
	
	public ProjectPagedResults(List<ProjectHeader> results, int totalNumberOfResults, List<UserProfile> lastModifiedBy) {
		super();
		this.totalNumberOfResults = totalNumberOfResults;
		this.results = results;
		this.lastModifiedBy = lastModifiedBy;
	}
	
	public void setLastModifiedBy(List<UserProfile> lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public List<UserProfile> getLastModifiedBy() {
		return lastModifiedBy;
	}

	public int getTotalNumberOfResults() {
		return totalNumberOfResults;
	}

	public void setTotalNumberOfResults(int totalNumberOfResults) {
		this.totalNumberOfResults = totalNumberOfResults;
	}

	public List<ProjectHeader> getResults() {
		return results;
	}

	public void setResults(List<ProjectHeader> results) {
		this.results = results;
	}
	
}
