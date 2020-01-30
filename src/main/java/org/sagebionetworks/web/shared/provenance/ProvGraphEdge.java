package org.sagebionetworks.web.shared.provenance;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProvGraphEdge implements IsSerializable {

	private ProvGraphNode source;
	private ProvGraphNode sink;

	public ProvGraphEdge() {}

	public ProvGraphEdge(ProvGraphNode source, ProvGraphNode sink) {
		this.source = source;
		this.sink = sink;
	}

	public ProvGraphNode getSource() {
		return source;
	}

	public ProvGraphNode getSink() {
		return sink;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sink == null) ? 0 : sink.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		ProvGraphEdge other = (ProvGraphEdge) obj;
		if (sink == null) {
			if (other.sink != null)
				return false;
		} else if (!sink.equals(other.sink))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProvGraphEdge [source=" + source + ", sink=" + sink + "]";
	}

}
