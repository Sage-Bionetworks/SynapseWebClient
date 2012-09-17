package org.sagebionetworks.web.client.utils;

import org.sagebionetworks.repo.model.TermsOfUseAccessApproval;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GovernanceServiceHelper {
	public static void signTermsOfUse(
			final String principalId,
			final Long accessRequirementId,
			final Callback onSuccess,
			final CallbackP<Throwable> onFailure,
			final SynapseClientAsync synapseClient,
			final JSONObjectAdapter jsonObjectAdapter
			) {
		TermsOfUseAccessApproval agreement = new TermsOfUseAccessApproval();
		agreement.setAccessorId(principalId);
		agreement.setRequirementId(accessRequirementId);
		JSONObjectAdapter approvalJson = null;
		try {
			approvalJson = agreement.writeToJSONObject(jsonObjectAdapter.createNew());
		} catch (JSONObjectAdapterException e) {
			onFailure.invoke(e);
			return;
		}
		EntityWrapper ew = new EntityWrapper(approvalJson.toJSONString(), agreement.getClass().getName(), null);
		synapseClient.createAccessApproval(ew, new AsyncCallback<EntityWrapper>(){
			@Override
			public void onSuccess(EntityWrapper result) {
				onSuccess.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				onFailure.invoke(caught);
			}			
		});
	}
	
	/**
	 * Lock down access to the given data object, then open a link to request customized access restrictions
	 * @param principalId
	 * @param accessRequirementId
	 * @param onSuccess
	 * @param onFailure
	 * @param synapseClient
	 * @param jsonObjectAdapter
	 */
	public static void lockDownData(
			final SynapseClientAsync synapseClient,
			final JSONObjectAdapter jsonObjectAdapter,
			final String jiraRestrictionLink
			) {
			// TODO call the service that locks down the dataset
			Window.open(jiraRestrictionLink, "_blank", "");
	}
}
