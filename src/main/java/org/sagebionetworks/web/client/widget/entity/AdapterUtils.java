package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;


/**
 * Utility for getting data in and out of adapters.
 * 
 * @author jmhill
 *
 */
public class AdapterUtils {
	/**
	 * If an EntityWrapper of a valid type (Project, Folder, FileEntity) is given, will
	 * return an instance of that Entity. Otherwise, will return null.
	 * @return Instance of the entity if it is valid,
	 * 		   otherwise returns null.
	 */
	public static Entity getEntityForBadgeInfo(AdapterFactory adapterFactory, String className, String jsonEntity) throws JSONObjectAdapterException {
		if (Project.class.getName().equals(className)) {
			return new Project(adapterFactory.createNew(jsonEntity));
		} else if (Folder.class.getName().equals(className)) {
			return  new Folder(adapterFactory.createNew(jsonEntity));
		} else if (FileEntity.class.getName().equals(className)) {
			return  new FileEntity(adapterFactory.createNew(jsonEntity));
		} else {
			return null;
		}
	}
}
