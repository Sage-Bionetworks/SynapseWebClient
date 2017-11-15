package org.sagebionetworks.web.client.utils;

import java.util.Collection;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.PostMessageContentAccessRequirement;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.SelfSignAccessRequirementInterface;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GovernanceServiceHelper {
	
	public static final String LOCK_ACCESS_REQUIREMENT_TEXT = "Access restricted pending review by Synapse Access and Compliance Team.";

	public static AccessApproval getAccessApproval(
			String principalId, 
			AccessRequirement ar) {
		AccessApproval approval = new AccessApproval();
		approval.setAccessorId(principalId);
		approval.setRequirementId(ar.getId());
		return approval;
	}
	
	public static void signTermsOfUse(
			final String principalId,
			final AccessRequirement ar,
			final SynapseClientAsync synapseClient,
			AsyncCallback<AccessApproval> callback
			) {
		AccessApproval ew;
		ew = getAccessApproval(principalId, ar);
		synapseClient.createAccessApproval(ew, callback);
	}
	
	/**
	 * In the case of multiple AccessRequirements on an entity, the choice of the one to display 
	 * is given by this utility.  The logic is:  If there are any unmet access requirements, return
	 * the first one.  If there are no unmet access requirements, then return the first from the
	 * 'all access requirements' list.  If there are no access requirements, then return null.
	 * 
	 * @param allARs
	 * @param unfulfilledARs
	 * @return
	 */
	public static AccessRequirement selectAccessRequirement(Collection<AccessRequirement> allARs, Collection<AccessRequirement> unfulfilledARs) {
		if (allARs==null || allARs.isEmpty()) {
			 return null;
		}
		if (unfulfilledARs==null || unfulfilledARs.isEmpty()) {
			return allARs.iterator().next();
		} else {
			return unfulfilledARs.iterator().next();
		}
	}
	
	/**
	 * 
	 * @param ar
	 * @return the kind of approval needed for the given access requirement, which is based on its Java class
	 */
	public static APPROVAL_TYPE accessRequirementApprovalType(AccessRequirement ar) {
		if (ar==null) return APPROVAL_TYPE.NONE;
		if (ar instanceof TermsOfUseAccessRequirement || ar instanceof SelfSignAccessRequirement) return APPROVAL_TYPE.USER_AGREEMENT;
		if (ar instanceof ACTAccessRequirement || ar instanceof LockAccessRequirement) return APPROVAL_TYPE.ACT_APPROVAL;
		if (ar instanceof PostMessageContentAccessRequirement) return APPROVAL_TYPE.POST_MESSAGE;
		throw new IllegalArgumentException("Unexpected access requirement type "+ar.getClass());
	}
	
	/**
	 * Independent of which AccessRequirement is currently displayed for approval the UI needs to 
	 * determine the overall restriction level for the entity.  The logic is that the restriction
	 * level is the level of the most restrictive access requirement applied to the dataset.
	 * 
	 * @param allARs the list of all access restrictions on this data set
	 * @return
	 */
	public static RESTRICTION_LEVEL entityRestrictionLevel(Collection<AccessRequirement> allARs) {
		RESTRICTION_LEVEL ans = RESTRICTION_LEVEL.OPEN;
		if (allARs==null) return ans;
		for (AccessRequirement ar : allARs) {
			if (ar instanceof SelfSignAccessRequirementInterface) {
				if (ans==RESTRICTION_LEVEL.OPEN) ans=RESTRICTION_LEVEL.RESTRICTED;
			} else if (ar instanceof ACTAccessRequirement || ar instanceof LockAccessRequirement) {
				ans=RESTRICTION_LEVEL.CONTROLLED;
			} 
		}
		return ans;
	}
	
	public static RESTRICTION_LEVEL getRestrictionLevel(AccessRequirement ar) {
		RESTRICTION_LEVEL ans = RESTRICTION_LEVEL.OPEN;
		if (ar instanceof SelfSignAccessRequirementInterface) {
			if (ans==RESTRICTION_LEVEL.OPEN) ans=RESTRICTION_LEVEL.RESTRICTED;
		} else if (ar instanceof ACTAccessRequirement || ar instanceof LockAccessRequirement) {
			ans=RESTRICTION_LEVEL.CONTROLLED;
		}
		return ans;
	}
	
	public static String getAccessRequirementText(AccessRequirement ar) {
		if (ar==null || ar instanceof ManagedACTAccessRequirement || ar instanceof SelfSignAccessRequirement) return "";
		if (ar instanceof TermsOfUseAccessRequirement) {
			return ((TermsOfUseAccessRequirement)ar).getTermsOfUse();
		} else if (ar instanceof ACTAccessRequirement) {
			return ((ACTAccessRequirement)ar).getActContactInfo();
		} else if (ar instanceof LockAccessRequirement) {
			return LOCK_ACCESS_REQUIREMENT_TEXT;
		} else if (ar instanceof PostMessageContentAccessRequirement) {
			return ((PostMessageContentAccessRequirement)ar).getUrl();
		} else {
			throw new RuntimeException("Unexpected class "+ar.getClass().getName());
		}
	}
}
