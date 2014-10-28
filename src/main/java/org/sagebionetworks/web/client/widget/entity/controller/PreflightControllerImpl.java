package org.sagebionetworks.web.client.widget.entity.controller;

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
	public void preflightDeleteEntity(String entityIdToDelete, Callback callback) {
		// TODO Add a real check
		callback.invoke();
	}

}
