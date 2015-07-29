package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidget implements RestrictionWidgetView.Presenter, SynapseWidgetPresenter, IsWidget {
	
	Callback entityUpdated;
	EntityBundle bundle;
	
	private AuthenticationController authenticationController;
	private JiraURLHelper jiraURLHelper;
	private GlobalApplicationState globalApplicationState;
	private RestrictionWidgetView view;
	private boolean showChangeLink, showIfProject, showFlagLink;
	private HashSet<Long> shownAccessRequirements;
	private Iterator<AccessRequirement> allArsIterator, unmetArsIterator;
	private AccessRequirement currentAR;
	private String jiraFlagLink;
	private AccessRequirementDialog accessRequirementDialog;
	
	@Inject
	public RestrictionWidget(
			RestrictionWidgetView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			JiraURLHelper jiraURLHelper,
			AccessRequirementDialog accessRestrictionDialog) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.jiraURLHelper = jiraURLHelper;
		this.accessRequirementDialog = accessRestrictionDialog;
		shownAccessRequirements = new HashSet<Long>();
		view.setPresenter(this);
	}
	
	public void configure(EntityBundle bundle, boolean showChangeLink, boolean showIfProject, boolean showFlagLink, Callback entityUpdated) {
		this.entityUpdated = entityUpdated;
		this.showChangeLink = showChangeLink;
		this.showIfProject = showIfProject;
		this.showFlagLink = showFlagLink;
		setEntityBundle(bundle);
		configureUI();
	}
	
	public void setEntityBundle(EntityBundle bundle) {
		if(bundle == null)  throw new IllegalArgumentException("Entity is required");
		this.bundle = bundle;
	}

	public String getJiraFlagUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		return jiraURLHelper.createFlagIssue(
				primaryEmail, 
				DisplayUtils.getDisplayName(userProfile), 
				bundle.getEntity().getId());
	}
	
	public boolean hasAdministrativeAccess() {
		return bundle.getPermissions().getCanChangePermissions();
	}
	
	public RESTRICTION_LEVEL getRestrictionLevel() {
		return GovernanceServiceHelper.entityRestrictionLevel(bundle.getAccessRequirements());
	}

	public boolean includeRestrictionWidget() {
		return (bundle.getEntity() instanceof FileEntity) || (bundle.getEntity() instanceof TableEntity) || (bundle.getEntity() instanceof Folder) || (showIfProject && bundle.getEntity() instanceof Project);
	}

	public String accessRequirementText() {
		return GovernanceServiceHelper.getAccessRequirementText(getAccessRequirement());
	}
	
	private AccessRequirement getAccessRequirement() {
		return currentAR;
	}
	
	public AccessRequirement selectNextAccessRequirement() {
		AccessRequirement nextAR = null;
		if (hasUnmetDownloadAccessRequirements()) {
			if (unmetArsIterator.hasNext())
				nextAR = unmetArsIterator.next();
		} else {
			if (allArsIterator.hasNext()) {
				nextAR = allArsIterator.next();
			}
		}
			
		return nextAR;
	}
	
	public void resetAccessRequirementCount() {
		shownAccessRequirements.clear();
		allArsIterator = bundle.getAccessRequirements().iterator();
		if (hasUnmetDownloadAccessRequirements()) {
			List<AccessRequirement> unmetRequirements = new ArrayList<AccessRequirement>();
			for (AccessRequirement unmetRequirement : bundle.getUnmetAccessRequirements()) {
				unmetRequirements.add(unmetRequirement);
				if (!(unmetRequirement instanceof SelfSignAccessRequirement)) {
					break;
				}
			}
			//show self sign access requirements, but block when we get to ACT
			unmetArsIterator = unmetRequirements.iterator();
		}
	}
	
	public boolean hasUnmetDownloadAccessRequirements() {
		List<AccessRequirement> unmetArs = bundle.getUnmetAccessRequirements();
		return (unmetArs != null && !unmetArs.isEmpty());
	}
	
	public boolean isCurrentAccessRequirementUnmet() {
		return bundle.getUnmetAccessRequirements().contains(currentAR);
	}

	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		return (sessionData==null ? null : sessionData.getProfile());				
	}
	
	public boolean isAnonymous() {
		return !authenticationController.isLoggedIn();
	}

	@Override
	public Widget asWidget() {
		if (!includeRestrictionWidget()) return null;
		return view.asWidget();
	}
	
	public void configureUI() {
		view.clear();
		boolean isAnonymous = isAnonymous();
		boolean hasAdministrativeAccess = false;
		
		jiraFlagLink = null;
		if (!isAnonymous) {
			hasAdministrativeAccess = hasAdministrativeAccess();
			jiraFlagLink = getJiraFlagUrl();
		}
		final RESTRICTION_LEVEL restrictionLevel = getRestrictionLevel();
		
		ClickHandler aboutLinkClickHandler = getAboutLinkClickHandler(
				hasAdministrativeAccess);
		
		switch (restrictionLevel) {
			case OPEN:
				view.showNoRestrictionsUI();
				break;
			case RESTRICTED:
			case CONTROLLED:
				view.showControlledUseUI();
				if (hasUnmetDownloadAccessRequirements())
					view.showUnmetRequirementsIcon();
				else
					view.showMetRequirementsIcon();
				break;
			default:
				throw new IllegalArgumentException(restrictionLevel.toString());
		}
		
		//show the info link if there are any restrictions, or if we are supposed to show the flag link (to allow people to flag or  admin to "change" the data access level).
		boolean isChangeLink = restrictionLevel==RESTRICTION_LEVEL.OPEN && hasAdministrativeAccess;
		boolean isRestricted = restrictionLevel!=RESTRICTION_LEVEL.OPEN;
		if ((isChangeLink && showChangeLink) || isRestricted) {
			if (isChangeLink)
				view.showChangeLink(aboutLinkClickHandler);
			else
				view.showShowLink(aboutLinkClickHandler);
		}
		
		if (showFlagLink) {
			if (isAnonymous)
				view.showAnonymousFlagUI();
			else
				view.showFlagUI();
		}
		
		view.setAccessRequirementDialog(accessRequirementDialog.asWidget());
	}
	
	@Override
	public void flagData() {
		view.open(jiraFlagLink);
	}
	
	private ClickHandler getAboutLinkClickHandler(
			final boolean hasAdministrativeAccess
			) {
		 
		return new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					//will run through all access requirements, unmet first (see selectNextAccessRequirement)
					resetAccessRequirementCount();
					setCurrentAccessRequirement(selectNextAccessRequirement());
					showNextAccessRequirement(hasAdministrativeAccess);
				}
		};
	}
	
	public void setCurrentAccessRequirement(AccessRequirement currentAR) {
		this.currentAR = currentAR;
	}
	
	public void showNextAccessRequirement(
			final boolean hasAdministrativeAccess
			) {
		
		//iterate over access requirements until we reach one that we have not yet shown (or there are none left to show).
		while(currentAR != null && shownAccessRequirements.contains(currentAR.getId())) {
			currentAR = selectNextAccessRequirement();	
		}
		
		//if there is another access requirement to show, then show it.
		if (currentAR != null) {
			shownAccessRequirements.add(currentAR.getId());
			boolean isApproved = isAnonymous() ? false : !isCurrentAccessRequirementUnmet();
			
			Callback showNextRestrictionCallback = new Callback() {
				@Override
				public void invoke() {
					showNextAccessRequirement(hasAdministrativeAccess);
				}
			};
			
			accessRequirementDialog.configure(getAccessRequirement(), bundle.getEntity().getId(), hasAdministrativeAccess, isApproved, entityUpdated, showNextRestrictionCallback);
			accessRequirementDialog.show();
		} else {
			accessRequirementDialog.hide();
			if (hasAdministrativeAccess && bundle.getAccessRequirements().isEmpty()) {
				//there are no access restrictions, and this person has administrative access.  verify data sensitivity, and if try then lockdown
				view.showVerifyDataSensitiveDialog();
			} else {
				//finished showing access requirements, refresh the bundle
				if (entityUpdated != null)
					entityUpdated.invoke();
			}
		}
	}

	@Override
	public void imposeRestrictionOkClicked() {
		Boolean isYesSelected = view.isYesHumanDataRadioSelected();
		Boolean isNoSelected = view.isNoHumanDataRadioSelected();
		
		if (isNoSelected != null && isNoSelected) {
			//this should not be possible, since the impose restrictions button is not enabled when the No radio button is selected!
			view.showErrorMessage("Please contact the Synapse Access and Compliance Team (ACT) to discuss imposing restrictions, at act@sagebase.org");
		} else if ((isYesSelected == null || !isYesSelected) && (isNoSelected == null || !isNoSelected)) {
			//no selection
			view.showErrorMessage("You must make a selection before continuing.");
		} else {
			view.showLoading();
			//the access requirement dialog knows how to impose the restriction
			accessRequirementDialog.imposeRestriction(bundle.getEntity().getId(), entityUpdated);
		}
	}

	@Override
	public void imposeRestrictionCancelClicked() {
		view.setImposeRestrictionModalVisible(false);
	}

	@Override
	public void anonymousFlagModalOkClicked() {
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
	}

	@Override
	public void reportIssueClicked() {
		view.showFlagModal();
	}
	
	@Override
	public void anonymousReportIssueClicked() {
		view.showAnonymousFlagModal();
	}
	
	
	@Override
	public void notHumanDataClicked() {
		//disable impose restriction button
		view.setImposeRestrictionOkButtonEnabled(false);
		//and show the warning message
		view.setNotSensitiveHumanDataMessageVisible(true);		
	}
	
	@Override
	public void yesHumanDataClicked() {
		//enable impose restriction button
		view.setImposeRestrictionOkButtonEnabled(true);
		//and hide the warning message
		view.setNotSensitiveHumanDataMessageVisible(false);
	}

	/**
	 * For unit testing
	 * @param entityUpdated
	 */
	public void setEntityUpdated(Callback entityUpdated) {
		this.entityUpdated = entityUpdated;
	}
}
