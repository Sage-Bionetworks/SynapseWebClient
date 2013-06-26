package org.sagebionetworks.web.client.widget.entity.menu;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestResourceList;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ActionMenu implements ActionMenuView.Presenter, SynapseWidgetPresenter {
	
	private ActionMenuView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private EntityBundle entityBundle;
	private EntityTypeProvider entityTypeProvider;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityEditor entityEditor;
	private AutoGenFactory entityFactory;
	private EntityUpdatedHandler entityUpdatedHandler;
	private SynapseJSNIUtils synapseJSNIUtils;
	private CookieProvider cookieProvider;
	private  NodeModelCreator nodeModelCreator;
	private Long versionNumber;
	
	public interface EvaluationsCallback {
		/**
		 * When available evaluations are returned
		 */
		public void onSuccess(List<Evaluation> evaluations);		
	}
	
	public interface SubmitterAliasesCallback {
		/**
		 * When available evaluations are returned
		 */
		public void onSuccess(List<String> evaluations);		
	}


	
	@Inject
	public ActionMenu(ActionMenuView view, NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter, EntityEditor entityEditor,
			AutoGenFactory entityFactory,
			SynapseJSNIUtils synapseJSNIUtils,
			CookieProvider cookieProvider) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.entityEditor = entityEditor;
		this.entityFactory = entityFactory;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.cookieProvider = cookieProvider;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}	
	
	public Widget asWidget(EntityBundle bundle, boolean isAdministrator, boolean canEdit, Long versionNumber) {		
		view.setPresenter(this);
		this.entityBundle = bundle; 		
		this.versionNumber = versionNumber;

		// Get EntityType
		EntityType entityType = entityTypeProvider.getEntityTypeForEntity(bundle.getEntity());
		
		view.createMenu(bundle, entityType, authenticationController, isAdministrator, canEdit, versionNumber, DisplayUtils.isInTestWebsite(cookieProvider));
		return view.asWidget();
	}
	
	@Override
	public void submitToEvaluations(List<String> evaluationIds, String submitterAlias) {
		//set up shared values across all submissions
		Entity entity = entityBundle.getEntity();
		Submission newSubmission = new Submission();
		newSubmission.setEntityId(entity.getId());
		newSubmission.setSubmitterAlias(submitterAlias);
		newSubmission.setUserId(authenticationController.getCurrentUserPrincipalId());
		if (entity instanceof Versionable) {
			newSubmission.setVersionNumber(((Versionable)entity).getVersionNumber());
		}
		if (evaluationIds.size() > 0)
			submitToEvaluations(newSubmission, entity.getEtag(), evaluationIds, 0);
	}
	
	public void submitToEvaluations(final Submission newSubmission, final String etag, final List<String> evaluationIds, final int index) {
		//and create a new submission for each evaluation
		String evalId = evaluationIds.get(index);
		newSubmission.setEvaluationId(evalId);
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			newSubmission.writeToJSONObject(adapter);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
		}
		try {
			synapseClient.createSubmission(adapter.toJSONString(), etag, new AsyncCallback<String>() {			
				@Override
				public void onSuccess(String result) {
					//result is the updated submission
					if (index == evaluationIds.size()-1) {
						view.showInfo(DisplayConstants.SUBMITTED_TITLE, DisplayConstants.SUBMITTED_TO_EVALUATION);				
					} else {
						submitToEvaluations(newSubmission, etag, evaluationIds, index+1);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
	@Override
	public void showAvailableEvaluations() {
		getAvailableEvaluations(new EvaluationsCallback() {
			@Override
			public void onSuccess(final List<Evaluation> evaluations) {
				if (evaluations == null || evaluations.size() == 0) {
					//no available evaluations, pop up an info dialog
					view.showErrorMessage(DisplayConstants.NOT_PARTICIPATING_IN_ANY_EVALUATIONS);
				} 
				else {
					getAvailableEvaluationsSubmitterAliases(new SubmitterAliasesCallback() {
						@Override
						public void onSuccess(List<String> submitterAliases) {
							//add the default team name (if set in the profile and not already in the list)
							UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
							String teamName = sessionData.getProfile().getTeamName();
							if (teamName != null && teamName.length() > 0 && !submitterAliases.contains(teamName)) {
								submitterAliases.add(teamName);
							}
							view.popupEvaluationSelector(evaluations, submitterAliases);		
						}
					});
				}
			}
		});
	}
	@Override
	public void isSubmitButtonVisible() {
		getAvailableEvaluations(new EvaluationsCallback() {
			@Override
			public void onSuccess(final List<Evaluation> evaluations) {
				if (evaluations.size() > 0) {
					view.showSubmitToChallengeButton();
				}
			}
		});
		
	}
	
	public void getAvailableEvaluations(final EvaluationsCallback callback) {
		try {
			synapseClient.getAvailableEvaluations(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String jsonString) {
					try {
						PaginatedResults<Evaluation> results = nodeModelCreator.createPaginatedResults(jsonString, Evaluation.class);
						List<Evaluation> list = results.getResults();
						callback.onSuccess(list);
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
	public void getAvailableEvaluationsSubmitterAliases(final SubmitterAliasesCallback callback) {
		try {
			synapseClient.getAvailableEvaluationsSubmitterAliases(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String jsonString) {
					try {
						RestResourceList results = nodeModelCreator.createJSONEntity(jsonString, RestResourceList.class);
						callback.onSuccess(results.getList());
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
	public void clearState() {
		view.clear();
		// remove handlers
		entityUpdatedHandler = null;
		this.entityBundle = null;		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return null;
	}
    
	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
		entityEditor.setEntityUpdatedHandler(handler);
	}

	@Override
	public void moveEntity(String newParentId) {
		final EntityType entityType = entityTypeProvider.getEntityTypeForEntity(entityBundle.getEntity());
		final String entityTypeDisplay = entityTypeProvider.getEntityDispalyName(entityType);
		
		entityBundle.getEntity().setParentId(newParentId);		
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			entityBundle.getEntity().writeToJSONObject(adapter);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
		}
		
		// update the entity
		synapseClient.createOrUpdateEntity(adapter.toJSONString(), null, false, new AsyncCallback<String>() {			
			@Override
			public void onSuccess(String result) {				
				view.showInfo(entityTypeDisplay + " Moved", "The " + entityTypeDisplay + " was successfully moved."); 
				// Reload this entity
				globalApplicationState.getPlaceChanger().goTo(new Synapse(entityBundle.getEntity().getId()));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof BadRequestException) {
					view.showErrorMessage(DisplayConstants.ERROR_CANT_MOVE_HERE);
					return;
				}
				if(caught instanceof NotFoundException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
					return;
				}
				if (caught instanceof UnauthorizedException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_AUTHORIZED);
				}
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_MOVE_FAILURE);			
				}
			}
		});
	}

	@Override
	public void deleteEntity() {
		final String parentId = entityBundle.getEntity().getParentId();
		final EntityType entityType = entityTypeProvider.getEntityTypeForEntity(entityBundle.getEntity());
		final String entityTypeDisplay = entityTypeProvider.getEntityDispalyName(entityType);
		synapseClient.deleteEntityById(entityBundle.getEntity().getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {				
				view.showInfo(entityTypeDisplay + " Deleted", "The " + entityTypeDisplay + " was successfully deleted."); 
				// Go to entity's parent
				Place gotoPlace = null;
				if(parentId != null && !Project.class.getName().equals(entityBundle.getEntity().getEntityType())) {
					gotoPlace = new Synapse(parentId);
				} else {
					gotoPlace = new Home(DisplayUtils.DEFAULT_PLACE_TOKEN);
				}
					
				globalApplicationState.getPlaceChanger().goTo(gotoPlace);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);			
				}
			}
		});
	}	
	
	@Override
	public boolean isUserLoggedIn() {
		return authenticationController.isLoggedIn();
	}

	@Override
	public void onEdit() {
		// Edit this entity.
		entityEditor.editEntity(entityBundle, false);
	}

	@Override
	public void addNewChild(EntityType type, String parentId) {
		entityEditor.addNewEntity(type, parentId);
		
	}

	@Override
	public void createLink(String selectedEntityId) {			
		Link link = (Link) entityFactory.newInstance(Link.class.getName());
		link.setParentId(selectedEntityId); // user selects where to save
		Reference ref = new Reference();
		ref.setTargetId(entityBundle.getEntity().getId());
		link.setLinksTo(ref); // links to this entity
		link.setLinksToClassName(entityBundle.getEntity().getEntityType());
		link.setName(entityBundle.getEntity().getName()); // copy name of this entity as default
		link.setEntityType(Link.class.getName());		
		
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			link.writeToJSONObject(adapter);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
		}		
		
		// create the link
		synapseClient.createOrUpdateEntity(adapter.toJSONString(), null, true, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.TEXT_LINK_SAVED, DisplayConstants.TEXT_LINK_SAVED);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof BadRequestException) {
					view.showErrorMessage(DisplayConstants.ERROR_CANT_SAVE_LINK_HERE);
					return;
				}
				if(caught instanceof NotFoundException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
					return;
				}
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
				}
			}
		});
		
	}
	
	@Override
	public void uploadToGenomespace() {
		String url = null;
		if(entityBundle.getEntity() instanceof Locationable) {
			Locationable locationable = (Locationable)entityBundle.getEntity();
			List<LocationData> locs = locationable.getLocations();
			if(locs != null && locs.size() > 0) {
				LocationData ld = locs.get(0);
				if(ld != null) {
					url = ld.getPath();
				}
				showUploadGenomeSpaceWindow(url, null);
			}
		} else if(entityBundle.getEntity() instanceof FileEntity) {
			final FileEntity fileEntity = (FileEntity)entityBundle.getEntity();
			FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
			if(fileHandle != null) {
				if (fileHandle instanceof ExternalFileHandle) {
					url = ((ExternalFileHandle) fileHandle).getExternalURL();
					showUploadGenomeSpaceWindow(url, null);
				}
				else if (fileHandle instanceof S3FileHandleInterface){
					synapseClient.getFileEntityTemporaryUrlForVersion(fileEntity.getId(), fileEntity.getVersionNumber(), new AsyncCallback<String>() {
						@Override
						public void onSuccess(String realUrl) {
							if(realUrl != null && !realUrl.equals(""))
								showUploadGenomeSpaceWindow(realUrl, fileEntity.getName());
						}
						@Override
						public void onFailure(Throwable caught) {
							if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
							view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
						}
					});							
				}
			}
		}				

	}
	
	/*
	 * Private Methods
	 */
	private void showUploadGenomeSpaceWindow(String url, String fileName) {
		if(url == null || url.equals("")) {
			view.showErrorMessage("This entity does not contain a file to upload.");
		} else {
			if(fileName != null)
				synapseJSNIUtils.uploadUrlToGenomeSpace(url, fileName);
			else
				synapseJSNIUtils.uploadUrlToGenomeSpace(url);
		}
	}
	
}
