package org.sagebionetworks.web.shared;

import java.io.Serializable;
import java.util.List;

public class TeamMemberPagedResults implements Serializable {
	private Long totalNumberOfResults;
	private List<TeamMemberBundle> results;

	/**
	 * Default constructor is required
	 */
	public TeamMemberPagedResults() {}


	public TeamMemberPagedResults(List<TeamMemberBundle> results, Long totalNumberOfResults) {
		super();
		this.totalNumberOfResults = totalNumberOfResults;
		this.results = results;
	}

	public Long getTotalNumberOfResults() {
		return totalNumberOfResults;
	}

	public void setTotalNumberOfResults(Long totalNumberOfResults) {
		this.totalNumberOfResults = totalNumberOfResults;
	}


	public List<TeamMemberBundle> getResults() {
		return results;
	}


	public void setResults(List<TeamMemberBundle> results) {
		this.results = results;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((results == null) ? 0 : results.hashCode());
		result = prime * result + ((totalNumberOfResults == null) ? 0 : totalNumberOfResults.hashCode());
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
		TeamMemberPagedResults other = (TeamMemberPagedResults) obj;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		if (totalNumberOfResults == null) {
			if (other.totalNumberOfResults != null)
				return false;
		} else if (!totalNumberOfResults.equals(other.totalNumberOfResults))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "TeamMemberPagedResults [totalNumberOfResults=" + totalNumberOfResults + ", results=" + results + "]";
	}



}
