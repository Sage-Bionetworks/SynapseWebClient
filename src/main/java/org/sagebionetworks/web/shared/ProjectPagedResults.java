package org.sagebionetworks.web.shared;

import java.io.Serializable;
import java.util.List;

import org.sagebionetworks.repo.model.ProjectHeader;

public class ProjectPagedResults implements Serializable {
	private int totalNumberOfResults;
	private List<ProjectHeader> results;

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

	


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((results == null) ? 0 : results.hashCode());
		result = prime * result + totalNumberOfResults;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectPagedResults other = (ProjectPagedResults) obj;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		if (totalNumberOfResults != other.totalNumberOfResults)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "ProjectPagedResults [totalNumberOfResults=" + totalNumberOfResults + ", results=" + results + "]";
	}
	
}
