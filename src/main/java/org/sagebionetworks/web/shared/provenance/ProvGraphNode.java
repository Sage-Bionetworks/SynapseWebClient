package org.sagebionetworks.web.shared.provenance;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class ProvGraphNode implements IsSerializable {

	public abstract String getId();
	
	double xPos;
	double yPos;
	
	public double getxPos() {
		return xPos;
	}
	public void setxPos(double xPos) {
		this.xPos = xPos;
	}
	public double getyPos() {
		return yPos;
	}
	public void setyPos(double yPos) {
		this.yPos = yPos;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.toString(xPos).hashCode();
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.toString(yPos).hashCode();
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ProvGraphNode other = (ProvGraphNode) obj;
		if (!Double.toString(xPos).equals(Double
				.toString(other.xPos)))
			return false;
		if (!Double.toString(yPos).equals(Double
				.toString(other.yPos)))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProvGraphNode [xPos=" + xPos + ", yPos=" + yPos + "]";
	}
}
