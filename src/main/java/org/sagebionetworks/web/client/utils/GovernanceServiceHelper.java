package org.sagebionetworks.web.client.utils;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.PostMessageContentAccessRequirement;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.SynapseClientAsync;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GovernanceServiceHelper {

	public static final String LOCK_ACCESS_REQUIREMENT_TEXT = "Access restricted pending review by Synapse Access and Compliance Team.";

	public static AccessApproval getAccessApproval(String principalId, AccessRequirement ar) {
		AccessApproval approval = new AccessApproval();
		approval.setAccessorId(principalId);
		approval.setRequirementId(ar.getId());
		return approval;
	}

	public static void signTermsOfUse(final String principalId, final AccessRequirement ar, final SynapseClientAsync synapseClient, AsyncCallback<AccessApproval> callback) {
		AccessApproval ew;
		ew = getAccessApproval(principalId, ar);
		synapseClient.createAccessApproval(ew, callback);
	}

	public static String getAccessRequirementText(AccessRequirement ar) {
		if (ar == null || ar instanceof ManagedACTAccessRequirement || ar instanceof SelfSignAccessRequirement)
			return "";
		if (ar instanceof TermsOfUseAccessRequirement) {
			return ((TermsOfUseAccessRequirement) ar).getTermsOfUse();
		} else if (ar instanceof ACTAccessRequirement) {
			return ((ACTAccessRequirement) ar).getActContactInfo();
		} else if (ar instanceof LockAccessRequirement) {
			return LOCK_ACCESS_REQUIREMENT_TEXT;
		} else if (ar instanceof PostMessageContentAccessRequirement) {
			return ((PostMessageContentAccessRequirement) ar).getUrl();
		} else {
			throw new RuntimeException("Unexpected class " + ar.getClass().getName());
		}
	}
}
