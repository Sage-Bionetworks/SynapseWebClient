package org.sagebionetworks.web.shared;

import java.util.List;
import org.sagebionetworks.schema.adapter.JSONEntity;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Generic class used to encapsulate a paginated list of results of objects of any type.
 * <p>
 * 
 * This class has been annotated to produce XML in addition to JSON.
 * <p>
 * 
 * @param <T> the type of result to paginate
 */
public class PaginatedResults<T extends JSONEntity> implements IsSerializable {

	private static final long serialVersionUID = 1L;

	private long totalNumberOfResults;
	private List<T> results;

	public PaginatedResults() {}


	public PaginatedResults(List<T> results, long totalNumberOfResults) {
		super();
		this.totalNumberOfResults = totalNumberOfResults;
		this.results = results;
	}


	/**
	 * @return the total number of results in the system
	 */
	public long getTotalNumberOfResults() {
		return totalNumberOfResults;
	}

	/**
	 * @param total
	 */
	public void setTotalNumberOfResults(long total) {
		this.totalNumberOfResults = total;
	}

	/**
	 * @return the list of results
	 */
	public List<T> getResults() {
		return results;
	}

	/**
	 * @param results
	 */
	public void setResults(List<T> results) {
		this.results = results;
	}


	@Override
	public String toString() {
		return "PaginatedResults [totalNumberOfResults=" + totalNumberOfResults + ", results=" + results + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((results == null) ? 0 : results.hashCode());
		result = prime * result + (int) (totalNumberOfResults ^ (totalNumberOfResults >>> 32));
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
		PaginatedResults other = (PaginatedResults) obj;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		if (totalNumberOfResults != other.totalNumberOfResults)
			return false;
		return true;
	}


}
