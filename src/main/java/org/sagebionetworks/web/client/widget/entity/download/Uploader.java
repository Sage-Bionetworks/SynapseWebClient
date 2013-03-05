package org.sagebionetworks.web.client.widget.entity.download;

import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.SynapsePersistable;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.shared.EntityUtil;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Uploader implements UploaderView.Presenter, SynapseWidgetPresenter, SynapsePersistable {
	
	private UploaderView view;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private HandlerManager handlerManager;
	private Entity entity;
	private String parentEntityId;
	private List<AccessRequirement> accessRequirements;
	private EntityTypeProvider entityTypeProvider;
	private JSONObjectAdapter jsonObjectAdapter;
	private AdapterFactory adapterFactory;
	private AutoGenFactory autogenFactory;

	private SynapseClientAsync synapseClient;
	private JiraURLHelper jiraURLHelper;
	private SynapseJSNIUtils synapseJsniUtils;
	private GWTWrapper gwt;
	
	@Inject
	public Uploader(
			UploaderView view, 			
			NodeModelCreator nodeModelCreator, 
			AuthenticationController authenticationController, 
			EntityTypeProvider entityTypeProvider,
			SynapseClientAsync synapseClient,
			JiraURLHelper jiraURLHelper,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseJSNIUtils synapseJsniUtils,
			AdapterFactory adapterFactory, 
			AutoGenFactory autogenFactory,
			GWTWrapper gwt
			) {
	
		this.view = view;		
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.synapseClient = synapseClient;
		this.jiraURLHelper = jiraURLHelper;
		this.jsonObjectAdapter=jsonObjectAdapter;
		this.synapseJsniUtils = synapseJsniUtils;
		this.adapterFactory = adapterFactory;
		this.autogenFactory = autogenFactory;
		this.gwt = gwt;
		view.setPresenter(this);		
		clearHandlers();
	}		
		
	public Widget asWidget(Entity entity, List<AccessRequirement> accessRequirements) {
		this.view.setPresenter(this);
		this.entity = entity;
		this.accessRequirements = accessRequirements;
		this.view.createUploadForm(true);
		return this.view.asWidget();
	}

	public Widget asWidget(String parentEntityId, List<AccessRequirement> accessRequirements) {
		this.parentEntityId = parentEntityId;
		return asWidget((Entity)null, accessRequirements);
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
		this.entity = null;
		this.parentEntityId = null;
	}

	@Override
	public Widget asWidget() {
		return null;
	}

	public Entity getEntity() {
		return entity;
	}
	
	@Override
	public String getUploadActionUrl(boolean isRestricted) {
		boolean isFileEntity = entity == null || entity instanceof FileEntity;
		String entityParentString = entity==null && parentEntityId != null ? DisplayUtils.FILE_HANDLE_FILEENTITY_PARENT_PARAM_KEY + "=" + parentEntityId + "&": "";
		String entityIdString = entity != null ? DisplayUtils.ENTITY_PARAM_KEY + "=" + entity.getId() + "&" : "";
		String uploadUrl = isFileEntity ? 
				//new way
				synapseJsniUtils.getBaseFileHandleUrl() + "?" + DisplayUtils.IS_RESTRICTED_PARAM_KEY + "=" +isRestricted + "&" +
						DisplayUtils.FILE_HANDLE_CREATE_FILEENTITY_PARAM_KEY  + "=" + Boolean.toString(entity == null) + "&" + entityParentString + entityIdString: 
				//old way
				gwt.getModuleBaseURL() + "upload" + "?" + 
					entityIdString +
					DisplayUtils.IS_RESTRICTED_PARAM_KEY + "=" +isRestricted;
		return uploadUrl;
	}
	
	@Override
	public void setExternalFilePath(String path, final boolean isNewlyRestricted) {
		if (entity==null || entity instanceof FileEntity) {
			//new data, use the appropriate synapse call
			//if we haven't created the entity yet, do that first
			if (entity == null) {
				createNewFileEntity(path, isNewlyRestricted);
			}
			else {
				updateExternalFileEntity(entity.getId(), path, isNewlyRestricted);
			}
		}
		else {
			//old data
			String entityId = entity.getId();
			synapseClient.updateExternalLocationable(entityId, path, new AsyncCallback<EntityWrapper>() {
				
				public void onSuccess(EntityWrapper result) {
					externalLinkUpdated(result, isNewlyRestricted, entity.getClass());
				};
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
				}
			} );
		}
	}
	
	public void externalLinkUpdated(EntityWrapper result, boolean isNewlyRestricted, Class<? extends Entity> entityClass) {
		try {
			entity = nodeModelCreator.createJSONEntity(result.getEntityJson(), entityClass);
			if (isNewlyRestricted) {
				EntityWrapper arEW = null;
				try {
					arEW=EntityUtil.createLockDownDataAccessRequirementAsEntityWrapper(entity.getId(), jsonObjectAdapter);
				} catch (JSONObjectAdapterException caught) {
					view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);							
				}
				synapseClient.createAccessRequirement(arEW, new AsyncCallback<EntityWrapper>(){
					@Override
					public void onSuccess(EntityWrapper result) {
						view.showInfo(DisplayConstants.TEXT_LINK_FILE, DisplayConstants.TEXT_LINK_SUCCESS);
						// open Jira issue
						view.openNewBrowserTab(getJiraRestrictionLink());
						entityUpdated();
					}
					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
					}
				});
			} else {
				view.showInfo(DisplayConstants.TEXT_LINK_FILE, DisplayConstants.TEXT_LINK_SUCCESS);
				entityUpdated();						
			}
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
		}
	}
	
	public void updateExternalFileEntity(String entityId, String path, final boolean isNewlyRestricted) {
		try {
			synapseClient.updateExternalFile(entityId, path, new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					externalLinkUpdated(result, isNewlyRestricted, FileEntity.class);
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
				}
			});
		} catch (Throwable t) {
			view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
		}
	}
	public void createNewFileEntity(final String path, final boolean isNewlyRestricted) {
		Entity fileEntity = createNewEntity(FileEntity.class.getName(), parentEntityId);
		String entityJson;
		try {
			entityJson = fileEntity.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(entityJson, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String newId) {
					updateExternalFileEntity(newId, path, isNewlyRestricted);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ERROR_FILE_CREATION_FAILED);
				}			
			});
		} catch (JSONObjectAdapterException e) {			
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
		}
	}

	private Entity createNewEntity(String className, String parentId) {
		Entity entity = (Entity) autogenFactory.newInstance(className);
		entity.setParentId(parentId);
		entity.setEntityType(className);		
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addCancelHandler(CancelHandler handler) {
		handlerManager.addHandler(CancelEvent.getType(), handler);
	}
	
	@Override
	public void clearHandlers() {
		handlerManager = new HandlerManager(this);
	}

	@Override
	public void addPersistSuccessHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	@Override
	public void closeButtonSelected() {
		handlerManager.fireEvent(new CancelEvent());
	}

	public void entityUpdated() {
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	@Override
	public void handleSubmitResult(String resultHtml, final boolean isNewlyRestricted) {
		if(resultHtml == null) resultHtml = "";
		// response from server
		//try to parse
		UploadResult uploadResult = null;
		try{
			uploadResult = AddAttachmentDialog.getUploadResult(resultHtml);
			if (uploadResult.getUploadStatus() == UploadStatus.SUCCESS) {
				//upload result has the entity id that was created by the FileHandleServlet
				String entityId = uploadResult.getMessage();
				//get the entity, and report success
				synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
					@Override
					public void onSuccess(EntityWrapper result) {
						try {
							entity = nodeModelCreator.createEntity(result);
							uploadSuccess(isNewlyRestricted);
						} catch (JSONObjectAdapterException e) {
							view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage(caught.getMessage());
					}
				});
				
			}else {
				uploadError();
			}
		} catch (Throwable th) {};//wasn't an UplaodResult
		
		if (uploadResult == null) {
			if(!resultHtml.contains(DisplayUtils.UPLOAD_SUCCESS)) {
				uploadError();
			} else {
				uploadSuccess(isNewlyRestricted);
			}
		}
	}
	
	private void uploadError() {
		view.showErrorMessage(DisplayConstants.ERROR_UPLOAD);
		handlerManager.fireEvent(new CancelEvent());
	}
	private void uploadSuccess(boolean isNewlyRestricted) {
		view.showInfo(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, DisplayConstants.TEXT_UPLOAD_SUCCESS);
		if (isNewlyRestricted) {
			view.openNewBrowserTab(getJiraRestrictionLink());
		}
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	@Override
	public boolean isRestricted() {
		return GovernanceServiceHelper.entityRestrictionLevel(accessRequirements)!=RESTRICTION_LEVEL.OPEN;
	}
	
	
	@Override
	public String getJiraRestrictionLink() {
		UserProfile userProfile = authenticationController.getLoggedInUser().getProfile();
		if (userProfile==null) throw new NullPointerException("User profile cannot be null.");
		return jiraURLHelper.createAccessRestrictionIssue(
				userProfile.getUserName(), userProfile.getDisplayName(), entity.getId());
	}
}
