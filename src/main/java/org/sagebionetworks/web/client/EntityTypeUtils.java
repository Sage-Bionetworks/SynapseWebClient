package org.sagebionetworks.web.client;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.table.TableEntity;

public class EntityTypeUtils {

	
	public static String getEntityClassNameForEntityType(String entityType) {
		String className = FileEntity.class.getName();
		if (entityType != null) {
			if (entityType.equalsIgnoreCase(EntityType.file.name())) {
				className = FileEntity.class.getName();
			} else if (entityType.equalsIgnoreCase(EntityType.folder.name())) {
				className = Folder.class.getName();
			} else if (entityType.equalsIgnoreCase(EntityType.project.name())) {
				className = Project.class.getName();
			} else if (entityType.equalsIgnoreCase(EntityType.table.name())) {
				className = TableEntity.class.getName();
			} else if (entityType.equalsIgnoreCase(EntityType.entityview.name())) {
				className = EntityView.class.getName();
			} else if (entityType.equalsIgnoreCase(EntityType.link.name())) {
				className = Link.class.getName();
			} else if (entityType.equalsIgnoreCase(EntityType.dockerrepo.name())) {
				className = DockerRepository.class.getName();
			}
		}
		return className;
	}

	public static EntityType getEntityTypeForEntityClassName(String className) {
		// default
		EntityType type = EntityType.file;
		
		if(Link.class.getName().equals(className)) {
			type = EntityType.link;
		} else if(Folder.class.getName().equals(className)) {
			// Folder
			type = EntityType.folder;
		} else if(FileEntity.class.getName().equals(className)) {
			// File
			type = EntityType.file;			
		} else if(Project.class.getName().equals(className)) {
			// Project
			type = EntityType.project;
		} else if(TableEntity.class.getName().equals(className)) {
			// TableEntity
			type = EntityType.table;
		} else if(EntityView.class.getName().equals(className)) {
			// EntityView
			type = EntityType.entityview;
		} else if(DockerRepository.class.getName().equals(className)) {
			// Docker Repository
			type = EntityType.dockerrepo;
		}
		return type;
	}

	public static IconType getIconTypeForEntityType(String entityType) {
		return EntityTypeUtils.getIconTypeForEntityClassName(getEntityClassNameForEntityType(entityType));
	}

	public static IconType getIconTypeForEntity(Entity entity) {
		String className = entity == null ? null : entity.getClass().getName();
		return EntityTypeUtils.getIconTypeForEntityClassName(className);
	}

	public static IconType getIconTypeForEntityClassName(String className) {
		// default
		IconType icon = IconType.FILE;
		
		if(Link.class.getName().equals(className)) {
			icon = IconType.LINK;
		} else if(Folder.class.getName().equals(className)) {
			// Folder
			icon = IconType.FOLDER;
		} else if(FileEntity.class.getName().equals(className)) {
			// File
			icon = IconType.FILE;			
		} else if(Project.class.getName().equals(className)) {
			// Project
			icon = IconType.LIST_ALT;
		} else if(TableEntity.class.getName().equals(className)) {
			// TableEntity
			icon = IconType.TABLE;
		} else if(EntityView.class.getName().equals(className)) {
			// FileView
			icon = IconType.TH_LIST;
		} else if(DockerRepository.class.getName().equals(className)) {
			// DockerRepository
			// TODO: change to Docker Icon: https://github.com/wesbos/Font-Awesome-Docker-Icon
			icon = IconType.ARCHIVE;
		}
		
		return icon;
	}
}
