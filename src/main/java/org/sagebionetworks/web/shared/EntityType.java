package org.sagebionetworks.web.shared;


public class EntityType {

	private String name;
	private String className;
        
	public EntityType(String name, String className) {
		super();
		this.name = name;
		this.className = className;	
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
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
		EntityType other = (EntityType) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		return true;
	}
    
}
