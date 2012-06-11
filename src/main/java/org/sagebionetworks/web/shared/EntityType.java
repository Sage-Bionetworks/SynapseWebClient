package org.sagebionetworks.web.shared;

import java.util.List;

import org.sagebionetworks.repo.model.registry.EntityTypeMetadata;

public class EntityType {

	private String name;
	private String className;
	private String defaultParentPath;
	private EntityTypeMetadata metadata;
    private List<EntityType> validParentTypes;
    private List<EntityType> validChildTypes;   
        
	public EntityType(String name, String className,
			String defaultParentPath, EntityTypeMetadata metadata) {
		super();
		this.name = name;
		this.className = className;
		this.defaultParentPath = defaultParentPath;
		this.metadata = metadata;		
	}

	public List<EntityType> getValidParentTypes() {
		return validParentTypes;
	}

	public void setValidParentTypes(List<EntityType> validParentTypes) {
		this.validParentTypes = validParentTypes;
	}

	public List<EntityType> getValidChildTypes() {
		return validChildTypes;
	}

	public void setValidChildTypes(List<EntityType> validChildTypes) {
		this.validChildTypes = validChildTypes;
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}

	public String getDefaultParentPath() {
		return defaultParentPath;
	}

	public EntityTypeMetadata getMetadata() {
		return metadata;
	}

	public List<String> getAliases() {
		return metadata.getAliases();
	}

	@Override
	public String toString() {
		return "EntityType [name=" + name + ", className=" + className
				+ ", defaultParentPath=" + defaultParentPath + ", metadata="
				+ metadata + "]";
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
