package org.sagebionetworks.web.client.model;

import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.TableBundle;

/**
 * A bundle of various parts of an entity.  This allows the client to get all the required parts in 
 * a minimum number of RPC calls.
 * 
 * @author John
 *
 */
public class EntityBundle {
	
	private Entity entity;
	private Annotations annotations;
	private UserEntityPermissions permissions;
	private EntityPath path;
	private List<AccessRequirement> accessRequirements;
	private List<AccessRequirement> unmetAccessRequirements;
	private Boolean hasChildren;
	private List<FileHandle> fileHandles;
	private TableBundle tableBundle;
	
	public EntityBundle(Entity entity, Annotations annotations,
			UserEntityPermissions permissions, EntityPath path, 
			List<AccessRequirement> accessRequirements,
		List<AccessRequirement> unmetAccessRequirements, List<FileHandle> fileHandles, TableBundle tableBundle
	) {
		super();
		this.entity = entity;
		this.annotations = annotations;
		this.permissions = permissions;
		this.path = path;
		this.accessRequirements=accessRequirements;
		this.unmetAccessRequirements=unmetAccessRequirements;
		this.fileHandles = fileHandles;
		this.tableBundle = tableBundle;
	}
	public Entity getEntity() {
		return entity;
	}
	public Annotations getAnnotations() {
		return annotations;
	}
	public UserEntityPermissions getPermissions() {
		return permissions;
	}
	public EntityPath getPath() {
		return path;
	}	
	
	public Boolean getHasChildren() {
		return hasChildren;
	}
	public void setChildCount(Boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	public void setAnnotations(Annotations annotations) {
		this.annotations = annotations;
	}
	public void setPermissions(UserEntityPermissions permissions) {
		this.permissions = permissions;
	}
	public void setPath(EntityPath path) {
		this.path = path;
	}
	public List<AccessRequirement> getAccessRequirements() {
		return accessRequirements;
	}
	public void setAccessRequirements(
			List<AccessRequirement> accessRequirements) {
		this.accessRequirements = accessRequirements;
	}
	public List<AccessRequirement> getUnmetAccessRequirements() {
		return unmetAccessRequirements;
	}
	public void setUnmetAccessRequirements(
			List<AccessRequirement> unmetAccessRequirements) {
		this.unmetAccessRequirements = unmetAccessRequirements;
	}
	
	public List<FileHandle> getFileHandles() {
		return fileHandles;
	}
	
	public void setFileHandles(List<FileHandle> fileHandles) {
		this.fileHandles = fileHandles;
	}
	
	public TableBundle getTableBundle() {
		return tableBundle;
	}
	public void setTableBundle(TableBundle tableBundle) {
		this.tableBundle = tableBundle;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((accessRequirements == null) ? 0 : accessRequirements
						.hashCode());
		result = prime * result
				+ ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result
				+ ((fileHandles == null) ? 0 : fileHandles.hashCode());
		result = prime * result
				+ ((hasChildren == null) ? 0 : hasChildren.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result
				+ ((permissions == null) ? 0 : permissions.hashCode());
		result = prime
				* result
				+ ((unmetAccessRequirements == null) ? 0
						: unmetAccessRequirements.hashCode());
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
		EntityBundle other = (EntityBundle) obj;
		if (accessRequirements == null) {
			if (other.accessRequirements != null)
				return false;
		} else if (!accessRequirements.equals(other.accessRequirements))
			return false;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (fileHandles == null) {
			if (other.fileHandles != null)
				return false;
		} else if (!fileHandles.equals(other.fileHandles))
			return false;
		if (hasChildren == null) {
			if (other.hasChildren != null)
				return false;
		} else if (!hasChildren.equals(other.hasChildren))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		if (unmetAccessRequirements == null) {
			if (other.unmetAccessRequirements != null)
				return false;
		} else if (!unmetAccessRequirements
				.equals(other.unmetAccessRequirements))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "EntityBundle [entity=" + entity + ", annotations="
				+ annotations + ", permissions=" + permissions + ", path="
				+ path + ", accessRequirements=" + accessRequirements
				+ ", unmetAccessRequirements=" + unmetAccessRequirements
				+ ", hasChildren=" + hasChildren + ", fileHandles=" + fileHandles
				+ "]";
	}
	
}
