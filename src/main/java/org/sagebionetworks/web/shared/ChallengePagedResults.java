package org.sagebionetworks.web.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ChallengePagedResults implements IsSerializable {
	private Long totalNumberOfResults;
	private List<ChallengeBundle> results;
	
	/**
	 * Default constructor is required
	 */
	public ChallengePagedResults() {
	}
	
	
	public ChallengePagedResults(List<ChallengeBundle> results, Long totalNumberOfResults) {
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

	public List<ChallengeBundle> getResults() {
		return results;
	}

	public void setResults(List<ChallengeBundle> results) {
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
		ChallengePagedResults other = (ChallengePagedResults) obj;
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
		return "ChallengePagedResults [totalNumberOfResults=" + totalNumberOfResults + ", results=" + results + "]";
	}
	
	
	
}
