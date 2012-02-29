package org.sagebionetworks.web.client.model;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;

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
	
	public EntityBundle(Entity entity, Annotations annotations,
			UserEntityPermissions permissions, EntityPath path) {
		super();
		this.entity = entity;
		this.annotations = annotations;
		this.permissions = permissions;
		this.path = path;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result
				+ ((permissions == null) ? 0 : permissions.hashCode());
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
		return true;
	}
	@Override
	public String toString() {
		return "EntityBundle [entity=" + entity + ", annotations="
				+ annotations + ", permissions=" + permissions + ", path="
				+ path + "]";
	}

}
