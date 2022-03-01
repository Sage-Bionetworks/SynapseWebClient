package org.sagebionetworks.web.client;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;

public class EntityTypeUtils {


	public static final String SUBMISSION_VIEW_DISPLAY_NAME = "Submissions";
	public static final String ENTITY_VIEW_DISPLAY_NAME = "View";
	public static final String MATERIALIZED_VIEW_DISPLAY_NAME = "Materialized View";
	public static final String DATASET_DISPLAY_NAME = "Dataset";
	public static final String TABLE_ENTITY_DISPLAY_NAME = "Table";
	public static final String UNKNOWN_TABLE_TYPE = "Unknown Table Type";

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
			} else if (entityType.equalsIgnoreCase(EntityType.submissionview.name())) {
				className = SubmissionView.class.getName();
			} else if (entityType.equalsIgnoreCase(EntityType.materializedview.name())) {
				className = MaterializedView.class.getName();
			} else if (entityType.equalsIgnoreCase(EntityType.dataset.name())) {
				className = Dataset.class.getName();
			}
		}
		return className;
	}

	public static EntityType getEntityType(EntityHeader header) {
		return getEntityTypeForEntityClassName(header.getType());
	}

	public static EntityType getEntityTypeForEntityClassName(String className) {
		// default
		EntityType type = EntityType.file;

		if (Link.class.getName().equals(className)) {
			type = EntityType.link;
		} else if (Folder.class.getName().equals(className)) {
			// Folder
			type = EntityType.folder;
		} else if (FileEntity.class.getName().equals(className)) {
			// File
			type = EntityType.file;
		} else if (Project.class.getName().equals(className)) {
			// Project
			type = EntityType.project;
		} else if (TableEntity.class.getName().equals(className)) {
			// TableEntity
			type = EntityType.table;
		} else if (EntityView.class.getName().equals(className)) {
			// EntityView
			type = EntityType.entityview;
		} else if (DockerRepository.class.getName().equals(className)) {
			// Docker Repository
			type = EntityType.dockerrepo;
		} else if (SubmissionView.class.getName().equals(className)) {
			// Submission View
			type = EntityType.submissionview;
		} else if (MaterializedView.class.getName().equals(className)) {
			// Materialized View
			type = EntityType.materializedview;
		} else if (Dataset.class.getName().equals(className)) {
			type = EntityType.dataset;
		}
		return type;
	}

	public static EntityType getEntityType(Entity entity) {
		String className = entity == null ? null : entity.getClass().getName();
		return EntityTypeUtils.getEntityTypeForEntityClassName(className);
	}

	/**
	 * @deprecated use {@link org.sagebionetworks.web.client.widget.EntityTypeIcon}
	 * @param className
	 * @return
	 */
	@Deprecated
	public static IconType getIconTypeForEntityClassName(String className) {
		// default
		IconType icon = IconType.FILE;

		if (Link.class.getName().equals(className)) {
			icon = IconType.LINK;
		} else if (Folder.class.getName().equals(className)) {
			// Folder
			icon = IconType.FOLDER;
		} else if (FileEntity.class.getName().equals(className)) {
			// File
			icon = IconType.FILE;
		} else if (Project.class.getName().equals(className)) {
			// Project
			icon = IconType.LIST_ALT;
		} else if (TableEntity.class.getName().equals(className)) {
			// TableEntity
			icon = IconType.TABLE;
		} else if (Dataset.class.getName().equals(className)) {
			// Dataset
			icon = IconType.TABLE;
		} else if (EntityView.class.getName().equals(className)) {
			// FileView
			icon = IconType.TH_LIST;
		} else if (SubmissionView.class.getName().equals(className)) {
			// Submission View
			icon = IconType.SERVER;
		} else if (DockerRepository.class.getName().equals(className)) {
			// DockerRepository
			// TODO: change to Docker Icon: https://github.com/wesbos/Font-Awesome-Docker-Icon
			icon = IconType.ARCHIVE;
		} else if (Dataset.class.getName().equals(className)) {
			icon = IconType.TH;
		}

		return icon;
	}

	public static String getFriendlyTableTypeName(String className) {
		String friendlyName = UNKNOWN_TABLE_TYPE;
		if (TableEntity.class.getName().equals(className)) {
			friendlyName = TABLE_ENTITY_DISPLAY_NAME;
		} else if (Dataset.class.getName().equals(className)) {
			friendlyName = DATASET_DISPLAY_NAME;
		} else if (EntityView.class.getName().equals(className)) {
			friendlyName = ENTITY_VIEW_DISPLAY_NAME;
		} else if (MaterializedView.class.getName().equals(className)) {
			friendlyName = MATERIALIZED_VIEW_DISPLAY_NAME;
		} else if (SubmissionView.class.getName().equals(className)) {
			friendlyName = SUBMISSION_VIEW_DISPLAY_NAME;
		}
		return friendlyName;
	}

	/**
	 * Gets the display name of an Entity. For Views, this method can be specific to the type of view based on the mask.
	 * @param entity
	 * @return
	 */
	public static String getFriendlyEntityTypeName(Entity entity) {
		if (entity instanceof Table) {
			return TableType.getTableType(entity).getDisplayName();
		} else {
			return org.sagebionetworks.repo.model.EntityTypeUtils.getDisplayName(
					org.sagebionetworks.repo.model.EntityTypeUtils.getEntityTypeForClass(entity.getClass())
			);
		}
	}

}
