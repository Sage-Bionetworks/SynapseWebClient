package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBar;
import org.sagebionetworks.web.shared.EntityUtil;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityTypeProvider entityTypeProvider;
	private JiraURLHelper jiraURLHelper;
	private EventBus bus;
	private GlobalApplicationState globalApplicationState;

	private EntityBundle bundle;

	@Inject
	public EntityMetadata(EntityMetadataView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalApplicationState,
			EntityTypeProvider entityTypeProvider, JiraURLHelper jiraURLHelper,
			EventBus bus) {
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.globalApplicationState = globalApplicationState;
		this.entityTypeProvider = entityTypeProvider;
		this.jiraURLHelper = jiraURLHelper;
		this.bus = bus;
	}

	@Override
	public void loadVersions(String id, int offset, int limit,
			final AsyncCallback<PaginatedResults<VersionInfo>> asyncCallback) {
		// TODO: If we ever change the offset api to actually take 0 as a valid
		// offset, then we need to remove "+1"
		synapseClient.getEntityVersions(id, offset + 1, limit,
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						PaginatedResults<VersionInfo> paginatedResults;
						try {
							paginatedResults = nodeModelCreator.createPaginatedResults(result, VersionInfo.class);
							asyncCallback.onSuccess(paginatedResults);
						} catch (JSONObjectAdapterException e) {							
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						asyncCallback.onFailure(caught);
					}
				});
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEntityBundle(EntityBundle bundle, boolean readOnly) {
		this.bundle = bundle;
		view.setEntityBundle(bundle, bundle.getPermissions().getCanEdit() && readOnly);
		boolean isLocationable = bundle.getEntity() instanceof Locationable;
		view.setDetailedMetadataVisible(!isLocationable || LocationableTitleBar.isDataPossiblyWithinLocationable(bundle, !isAnonymous()));
	}

	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getLoggedInUser();
		return (sessionData==null ? null : sessionData.getProfile());
		
	}

	@Override
	public boolean isAnonymous() {
		return getUserProfile()==null;
	}

	@Override
	public String getJiraFlagUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		return jiraURLHelper.createFlagIssue(
				userProfile.getUserName(), 
				userProfile.getDisplayName(), 
				bundle.getEntity().getId());
	}

	private String getJiraRestrictionUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		return jiraURLHelper.createAccessRestrictionIssue(
				userProfile.getUserName(), 
				userProfile.getDisplayName(), 
				bundle.getEntity().getId());
	}

	@Override
	public String getJiraRequestAccessUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		return jiraURLHelper.createRequestAccessIssue(
				userProfile.getOwnerId(), 
				userProfile.getDisplayName(), 
				userProfile.getUserName(), 
				bundle.getEntity().getId(), 
				getAccessRequirement().getId().toString());
	}

	@Override
	public boolean hasAdministrativeAccess() {
		return bundle.getPermissions().getCanChangePermissions();
	}

	@Override
	public APPROVAL_REQUIRED getRestrictionLevel() {
		if (bundle.getAccessRequirements().size()==0L) return APPROVAL_REQUIRED.NONE;
		if (isTermsOfUseAccessRequirement()) return APPROVAL_REQUIRED.LICENSE_ACCEPTANCE;
		return APPROVAL_REQUIRED.ACT_APPROVAL;
	}

	@Override
	public boolean hasFulfilledAccessRequirements() {
		return bundle.getUnmetAccessRequirements().size()==0L;
	}

	@Override
	public boolean includeRestrictionWidget() {
		return (bundle.getEntity() instanceof Locationable);
	}

	@Override
	public String accessRequirementText() {
		if (bundle.getAccessRequirements().size()==0) throw new IllegalStateException("There is no access requirement.");
		AccessRequirement ar = bundle.getAccessRequirements().get(0);
		if (ar instanceof TermsOfUseAccessRequirement) {
			return ((TermsOfUseAccessRequirement)ar).getTermsOfUse();
		} else if (ar instanceof ACTAccessRequirement) {
			return ((ACTAccessRequirement)ar).getActContactInfo();			
		} else {
			throw new IllegalStateException("Unexpected access requirement type "+ar.getClass());
		}
	}
	
	private AccessRequirement getAccessRequirement() {
		return bundle.getAccessRequirements().get(0);
	}

	@Override
	public boolean isTermsOfUseAccessRequirement() {
		if (bundle.getAccessRequirements().size()==0) throw new IllegalStateException("There is no access requirement.");
		AccessRequirement ar = getAccessRequirement();
		if (ar instanceof TermsOfUseAccessRequirement) {
			return true;		
		} else {
			return false;
		}
	}

	@Override
	public Callback accessRequirementCallback() {
		if (!isTermsOfUseAccessRequirement()) throw new IllegalStateException("not a TOU Access Requirement");
		AccessRequirement ar = getAccessRequirement();
		TermsOfUseAccessRequirement tou = (TermsOfUseAccessRequirement)ar;
		return new Callback() {
			@Override
			public void invoke() {
				// create the self-signed access approval, then update this object
				String principalId = getUserProfile().getOwnerId();
				AccessRequirement ar = getAccessRequirement();
				Callback onSuccess = new Callback() {
					@Override
					public void invoke() {
						fireEntityUpdatedEvent();
					}
				};
				CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
					@Override
					public void invoke(Throwable t) {
						view.showInfo("Error", t.getMessage());
					}
				};
				GovernanceServiceHelper.signTermsOfUse(
						principalId, 
						ar.getId(), 
						onSuccess, 
						onFailure, 
						synapseClient, 
						jsonObjectAdapter);
			}
		};
	}

	@Override
	public void fireEntityUpdatedEvent() {
		bus.fireEvent(new EntityUpdatedEvent());
	}

	
	@Override
	public Callback getImposeRestrictionsCallback() {
		return new Callback() {
			@Override
			public void invoke() {
				EntityWrapper ew = null;
				try {
					ew = EntityUtil.createLockDownDataAccessRequirementAsEntityWrapper(bundle.getEntity().getId(), jsonObjectAdapter);
				} catch (JSONObjectAdapterException e) {
					view.showInfo("Error", e.getMessage());
					return;
				}
				// from http://stackoverflow.com/questions/3907531/gwt-open-page-in-a-new-tab
				final JavaScriptObject window = DisplayUtils.newWindow("", "", "");
				synapseClient.createAccessRequirement(ew, new AsyncCallback<EntityWrapper>(){
					@Override
					public void onSuccess(EntityWrapper result) {
						fireEntityUpdatedEvent();
						DisplayUtils.setWindowTarget(window, getJiraRestrictionUrl());
				}
					@Override
					public void onFailure(Throwable caught) {
						view.showInfo("Error", caught.getMessage());
					}
				});
			}
		};
	}
	
	@Override
	public Callback getLoginCallback() {
		return new Callback() {
			public void invoke() {		
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}
		};
	}

	@Override
	public void editCurrentVersionInfo(String entityId, String version, String comment) {
		Entity entity = bundle.getEntity();
		if (entity.getId().equals(entityId) && entity instanceof Versionable) {
			final Versionable vb = (Versionable)entity;
			if (version != null && version.equals(vb.getVersionLabel()) &&
				comment != null && comment.equals(vb.getVersionComment())) {
				view.showInfo("Version Info Unchanged", "You didn't change anything about the version info.");
				return;
			}
			if (version == null || version.equals(""))
				version = null; // Null out the version field if empty so it defaults to number
			vb.setVersionLabel(version);
			vb.setVersionComment(comment);
			JSONObjectAdapter joa = jsonObjectAdapter.createNew();
			try {
				vb.writeToJSONObject(joa);
				synapseClient.updateEntity(joa.toJSONString(),
						new AsyncCallback<EntityWrapper>() {
							@Override
							public void onFailure(Throwable caught) {
								if (!DisplayUtils.handleServiceException(
										caught, globalApplicationState.getPlaceChanger(),
										authenticationController.getLoggedInUser())) {
									view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE
											+ "\n" + caught.getMessage());
								}
							}
							@Override
							public void onSuccess(EntityWrapper result) {
								view.showInfo(DisplayConstants.VERSION_INFO_UPDATED, "Updated " + vb.getName());
								fireEntityUpdatedEvent();
							}
						});
			} catch (JSONObjectAdapterException e) {
				view.showErrorMessage(DisplayConstants.ERROR_INVALID_VERSION_FORMAT);
			}
		}
	}

	@Override
	public void promoteVersion(String entityId, Long versionNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteVersion(final String entityId, final Long versionNumber) {
		synapseClient.deleteEntityVersionById(entityId, versionNumber, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught,
						globalApplicationState.getPlaceChanger(),
						authenticationController.getLoggedInUser())) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE + "\n" + caught.getMessage());
				}
			}
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Version deleted", "Version "+ versionNumber + " of " + entityId + DisplayConstants.LABEL_DELETED);
				fireEntityUpdatedEvent();
			}
		});
	}


}
