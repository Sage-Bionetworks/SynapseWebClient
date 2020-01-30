package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.inject.Inject;

/**
 * This controller delegates each request to one or more controllers that do the actual work
 * depending on the context.
 * 
 * @author jhill
 *
 */
public class PreflightControllerImpl implements PreflightController {

	CertifiedUserController certifiedUserController;

	@Inject
	public PreflightControllerImpl(CertifiedUserController certifiedUserController) {
		super();
		this.certifiedUserController = certifiedUserController;
	}

	@Override
	public void checkUploadToEntity(final EntityBundle uploadTo, final Callback callback) {
		// Access Restrictions then certified user.
		certifiedUserController.checkUploadToEntity(uploadTo, callback);
	}

	@Override
	public void checkDownloadFromEntity(EntityBundle uploadTo, Callback callback) {
		// Only access restrictions.
		callback.invoke();
	}

	@Override
	public void checkDeleteEntity(EntityBundle toDelete, Callback callback) {
		certifiedUserController.checkDeleteEntity(toDelete, callback);
	}

	@Override
	public void checkCreateEntity(EntityBundle parentBundle, String entityClassName, Callback callback) {
		// only certified user.
		certifiedUserController.checkCreateEntity(parentBundle, entityClassName, callback);;
	}

	@Override
	public void checkUpdateEntity(EntityBundle toUpdate, Callback callback) {
		// only certified user.
		certifiedUserController.checkUpdateEntity(toUpdate, callback);
	}

	@Override
	public void checkCreateEntityAndUpload(final EntityBundle parentEntity, String entityClassName, final Callback callback) {
		// Test create first
		checkCreateEntity(parentEntity, entityClassName, new Callback() {
			@Override
			public void invoke() {
				checkUploadToEntity(parentEntity, callback);
			}
		});
	}

}
