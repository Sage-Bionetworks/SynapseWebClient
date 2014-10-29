package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
/**
 * This is currently a stub.
 * 
 * @author jhill
 *
 */
public class PreflightControllerImpl implements PreflightController {

	@Override
	public void preflightAddContentToEntity(String destinationEntityId,
			Callback callback) {
		// TODO Add a real check
		callback.invoke();
	}

	@Override
	public void preflightSubmitToEvaluation(String evaluationId,
			String entityIdToSubmit, Callback callback) {
		// TODO Add a real check
		callback.invoke();

	}

	@Override
	public void preflightDeleteEntity(EntityBundle toDelete, Callback callback) {
		// TODO Add a real check
		callback.invoke();
	}

}
