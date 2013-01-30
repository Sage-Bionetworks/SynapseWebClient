package org.sagebionetworks.web.shared.provenance;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProvGraphNode implements IsSerializable {

	public enum Type { ACTIVITY, ENTITY, EXPAND };
	
	String id;
	double xPos;
	double yPos;
	Type type;
	
	public ProvGraphNode(String id, double xPos, double yPos, Type type) {
		super();
		this.id = id;
		this.xPos = xPos;
		this.yPos = yPos;
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
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
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		long temp;
		temp = Double.doubleToLongBits(xPos);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yPos);
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type != other.type)
			return false;
		if (Double.doubleToLongBits(xPos) != Double
				.doubleToLongBits(other.xPos))
			return false;
		if (Double.doubleToLongBits(yPos) != Double
				.doubleToLongBits(other.yPos))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ProvGraphNode [id=" + id + ", xPos=" + xPos + ", yPos=" + yPos
				+ ", type=" + type + "]";
	}
	
	
}
