package org.sagebionetworks.web.client.utils;

import java.util.Collection;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.PostMessageContentAccessApproval;
import org.sagebionetworks.repo.model.PostMessageContentAccessRequirement;
import org.sagebionetworks.repo.model.SelfSignAccessApproval;
import org.sagebionetworks.repo.model.TermsOfUseAccessApproval;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GovernanceServiceHelper {
	
	public static AccessApproval getAccessApproval(
			String principalId, 
			AccessRequirement ar, 
			JSONObjectAdapter jsonObjectAdapter) throws JSONObjectAdapterException {
		SelfSignAccessApproval approval;
		
		if (ar instanceof TermsOfUseAccessRequirement) {
			//Terms of Use
			approval = new TermsOfUseAccessApproval();
		} else if (ar instanceof PostMessageContentAccessRequirement) {
			//Post Message
			approval = new PostMessageContentAccessApproval();
		} else {
			throw new IllegalArgumentException("Unexpected access requirement type "+ar.getClass());
		}

		approval.setAccessorId(principalId);
		approval.setRequirementId(ar.getId());
		return approval;
	}
	
	public static void signTermsOfUse(
			final String principalId,
			final AccessRequirement ar,
			final Callback onSuccess,
			final CallbackP<Throwable> onFailure,
			final SynapseClientAsync synapseClient,
			final JSONObjectAdapter jsonObjectAdapter
			) {
		AccessApproval ew;
		try {
			ew = getAccessApproval(principalId, ar, jsonObjectAdapter);
		} catch (JSONObjectAdapterException e) {
			onFailure.invoke(e);
			return;
		}
		synapseClient.createAccessApproval(ew, new AsyncCallback<AccessApproval>(){
			@Override
			public void onSuccess(AccessApproval result) {
				onSuccess.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				onFailure.invoke(caught);
			}			
		});
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
		if (ar instanceof TermsOfUseAccessRequirement) return APPROVAL_TYPE.USER_AGREEMENT;
		if (ar instanceof ACTAccessRequirement) return APPROVAL_TYPE.ACT_APPROVAL;
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
			if (ar instanceof TermsOfUseAccessRequirement) {
				if (ans==RESTRICTION_LEVEL.OPEN) ans=RESTRICTION_LEVEL.RESTRICTED;
			} else if (ar instanceof ACTAccessRequirement) {
				ans=RESTRICTION_LEVEL.CONTROLLED;
			} 
		}
		return ans;
	}
	
	public static RESTRICTION_LEVEL getRestrictionLevel(AccessRequirement ar) {
		RESTRICTION_LEVEL ans = RESTRICTION_LEVEL.OPEN;
		if (ar instanceof TermsOfUseAccessRequirement) {
			if (ans==RESTRICTION_LEVEL.OPEN) ans=RESTRICTION_LEVEL.RESTRICTED;
		} else if (ar instanceof ACTAccessRequirement) {
			ans=RESTRICTION_LEVEL.CONTROLLED;
		}
		return ans;
	}
	
	public static String getAccessRequirementText(AccessRequirement ar) {
		if (ar==null) return "";
		if (ar instanceof TermsOfUseAccessRequirement) {
			return ((TermsOfUseAccessRequirement)ar).getTermsOfUse();
		} else if (ar instanceof ACTAccessRequirement) {
			return ((ACTAccessRequirement)ar).getActContactInfo();
		} else {
			throw new RuntimeException("Unexpected class "+ar.getClass().getName());
		}
	}
}
