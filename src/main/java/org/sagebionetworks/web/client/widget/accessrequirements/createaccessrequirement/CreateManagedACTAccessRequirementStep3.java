package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.team.UserTeamBadge;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Second page of creating an access requirement (ACT)
 * 
 * @author Jay
 *
 */
public class CreateManagedACTAccessRequirementStep3 implements ModalPage, CreateManagedACTAccessRequirementStep3View.Presenter {
	CreateManagedACTAccessRequirementStep3View view;
	ModalPresenter modalPresenter;
	ManagedACTAccessRequirement accessRequirement;
	SynapseClientAsync synapseClient;
	String originalReviewerPrincipalId, reviewerPrincipalId;
	UserTeamBadge reviewerUserTeamBadge;

	@Inject
	public CreateManagedACTAccessRequirementStep3(CreateManagedACTAccessRequirementStep3View view, SynapseClientAsync synapseClient, SynapseSuggestBox suggestBox, UserGroupSuggestionProvider provider, UserTeamBadge reviewerUserTeamBadge) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.reviewerUserTeamBadge = reviewerUserTeamBadge;
		view.setPresenter(this);
		suggestBox.setSuggestionProvider(provider);
		suggestBox.setTypeFilter(TypeFilter.ALL);
		view.setReviewerSearchBox(suggestBox.asWidget());
		
		suggestBox.addItemSelectedHandler(new CallbackP<UserGroupSuggestion>() {
			public void invoke(UserGroupSuggestion suggestion) {
				onSynapseSuggestSelected(suggestion);
			};
		});
	}

	public void onSynapseSuggestSelected(final UserGroupSuggestion suggestion) {
		reviewerPrincipalId = suggestion.getId();
		reviewerUserTeamBadge.configure(reviewerPrincipalId);
		view.setReviewerUIVisible(true);
	}

	/**
	 * Configure this widget before use.
	 * 
	 */
	public void configure(ManagedACTAccessRequirement accessRequirement) {
		this.accessRequirement = accessRequirement;
		// get the AR ACL
	}

	@Override
	public void onPrimary() {
		// update access requirement ACL
		modalPresenter.setLoading(true);
		
		// if user/team is set, then create or update the ACL.  if empty, then either do nothing or delete the ACL.
		if (reviewerPrincipalId == null) {
			if (originalReviewerPrincipalId != null) {
				// delete
			}
		} else {
			// reviewer principal ID is set
			if (originalReviewerPrincipalId == null) {
				// create
			} else if (!reviewerPrincipalId.equals(originalReviewerPrincipalId)) {
				// update
			}
		}
		modalPresenter.onFinished();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter modalPresenter) {
		this.modalPresenter = modalPresenter;
		modalPresenter.setTitle("People with permission in this Access Requirement");
		modalPresenter.setPrimaryButtonText(DisplayConstants.FINISH);
	}
}
