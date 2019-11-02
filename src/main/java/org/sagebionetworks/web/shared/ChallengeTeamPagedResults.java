package org.sagebionetworks.web.shared;

import java.util.List;
import com.google.gwt.user.client.rpc.IsSerializable;

public class ChallengeTeamPagedResults implements IsSerializable {
	private Long totalNumberOfResults;
	private List<ChallengeTeamBundle> results;

	/**
	 * Default constructor is required
	 */
	public ChallengeTeamPagedResults() {}


	public ChallengeTeamPagedResults(List<ChallengeTeamBundle> results, Long totalNumberOfResults) {
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

	public List<ChallengeTeamBundle> getResults() {
		return results;
	}

	public void setResults(List<ChallengeTeamBundle> results) {
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
		ChallengeTeamPagedResults other = (ChallengeTeamPagedResults) obj;
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
		return "ChallengeTeamPagedResults [totalNumberOfResults=" + totalNumberOfResults + ", results=" + results + "]";
	}

}
