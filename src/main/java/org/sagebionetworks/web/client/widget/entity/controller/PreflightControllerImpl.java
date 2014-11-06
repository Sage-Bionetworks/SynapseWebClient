package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
/**
 * This is currently a stub.
 * 
 * @author jhill
 *
 */
public class PreflightControllerImpl implements PreflightController {
	
	CertifiedUserController certifiedUserController;
	AccessRestrictionController accessRestrictionController;

	@Override
	public void checkUploadToEntity(final EntityBundle uploadTo, final Callback callback) {
		// Access Restrictions then certified user.
		accessRestrictionController.checkUploadToEntity(uploadTo, new Callback() {
			@Override
			public void invoke() {
				// Access Restrictions
				certifiedUserController.checkUploadToEntity(uploadTo, callback);
			}
		});
	}

	@Override
	public void checkDownloadFromEntity(EntityBundle uploadTo, Callback callback) {
		// Only access restrictions.
		accessRestrictionController.checkDownloadFromEntity(uploadTo, callback);
	}

	@Override
	public void checkDeleteEntity(EntityBundle toDelete, Callback callback) {
		certifiedUserController.checkDeleteEntity(toDelete, callback);
	}

	@Override
	public void checkCreateEntity(EntityBundle parentBundle, String entityClassName,
			Callback callback) {
		certifiedUserController.checkCreateEntity(parentBundle, entityClassName, callback);;
	}

	@Override
	public void checkUpdateEntity(EntityBundle toUpdate, Callback callback) {
		certifiedUserController.checkUpdateEntity(toUpdate, callback);
	}

}
