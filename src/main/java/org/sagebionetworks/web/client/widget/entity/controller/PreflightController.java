package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.utils.Callback;

/**
 * Controller is an abstraction for all of preflight checks a user must pass before doing certain operations.
 * This includes access restrictions and user certification.
 * 
 * @author jhill
 *
 */
public interface PreflightController {
	
	/**
	 * This preflight should be used before the user adds any content to an entity.
	 * 
	 * @param destinationEntityId
	 * @param callback The passed callback will be invoked if the users satisfies all pre-conditions.
	 */
	public void preflightAddContentToEntity(String destinationEntityId, Callback callback);
	
	/**
	 * This preflight should be used before the user submits anything to an evaluation.
	 * 
	 * @param evaluationId The ID of the evaluation to submit to.
	 * @param entityIdToSubmit The ID of the entity to be submitted to the evaluation.
	 * @param callback The passed callback will be invoked when the users satisfies all pre-conditions.
	 */
	public void preflightSubmitToEvaluation(String evaluationId, String entityIdToSubmit, Callback callback);
	
	/**
	 * This preflight should be used before the users deletes an entity.
	 * 
	 * @param entityIdToDelete
	 * @param callback
	 */
	public void preflightDeleteEntity(String entityIdToDelete, Callback callback);

}
