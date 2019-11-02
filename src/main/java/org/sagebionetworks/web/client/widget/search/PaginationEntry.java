package org.sagebionetworks.web.client.widget.search;

public class PaginationEntry {

	private String label;
	private int start;
	private boolean isCurrent;


	public PaginationEntry(String label, int start, boolean isCurrent) {
		super();
		this.label = label;
		this.start = start;
		this.isCurrent = isCurrent;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public int getStart() {
		return start;
	}


	public void setStart(int start) {
		this.start = start;
	}


	public boolean isCurrent() {
		return isCurrent;
	}


	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isCurrent ? 1231 : 1237);
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + start;
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
		PaginationEntry other = (PaginationEntry) obj;
		if (isCurrent != other.isCurrent)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (start != other.start)
			return false;
		return true;
	}


}
