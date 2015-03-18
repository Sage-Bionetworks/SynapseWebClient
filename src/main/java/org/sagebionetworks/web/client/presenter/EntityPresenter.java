package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_PATH;
import static org.sagebionetworks.web.shared.EntityBundleTransport.FILE_HANDLES;
import static org.sagebionetworks.web.shared.EntityBundleTransport.HAS_CHILDREN;
import static org.sagebionetworks.web.shared.EntityBundleTransport.PERMISSIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.TABLE_DATA;
import static org.sagebionetworks.web.shared.EntityBundleTransport.UNMET_ACCESS_REQUIREMENTS;

import java.util.List;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.shared.AccessRequirementUtils;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class EntityPresenter extends AbstractActivity implements EntityView.Presenter, Presenter<Synapse> {
		
	private Synapse place;
	private EntityView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private String entityId;
	private Long versionNumber;
	private AdapterFactory adapterFactory;
	private Synapse.EntityArea area;
	private String areaToken;
	private CookieProvider cookies;
	private SynapseJSNIUtils synapseJsniUtils;
	public static final String ENTITY_BACKGROUND_IMAGE_NAME="entity_background_image_3141592653.png";
	@Inject
	public EntityPresenter(EntityView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			AdapterFactory adapterFactory, CookieProvider cookies,
			SynapseJSNIUtils synapseJsniUtils) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		this.cookies = cookies;
		this.synapseJsniUtils = synapseJsniUtils;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Synapse place) {
		this.place = place;
		this.view.setPresenter(this);		
		
		this.entityId = place.getEntityId();
		this.versionNumber = place.getVersionNumber();
		this.area = place.getArea();
		this.areaToken = place.getAreaToken();
		refresh();
	}
	
	public void updateArea(EntityArea area, String areaToken) {
		this.area = area;
		this.areaToken = areaToken;
		place.setArea(area);
		place.setAreaToken(areaToken);
		place.setNoRestartActivity(true);
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void replaceArea(EntityArea area, String areaToken) {
		this.area = area;
		this.areaToken = areaToken;
		place.setArea(area);
		place.setAreaToken(areaToken);
		place.setNoRestartActivity(true);
		globalApplicationState.replaceCurrentPlace(place);
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
	@Override
	public void refresh() {
		view.setBackgroundImageVisible(false);
		// Hide the view panel contents until async callback completes
		view.showLoading();
		
		// We want the entity, permissions and path.
		// TODO : add REFERENCED_BY
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS | FILE_HANDLES | TABLE_DATA;
		AsyncCallback<EntityBundleTransport> callback = new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport transport) {				
				if (transport.getIsWikiBasedEntity() && !DisplayUtils.isInTestWebsite(cookies)) {
					globalApplicationState.getPlaceChanger().goTo(new Wiki(entityId, ObjectType.ENTITY.toString(), null));
				}
				else {
					EntityBundle bundle = null;
					try {
						bundle = nodeModelCreator.createEntityBundle(transport);
						
						// Redirect if Entity is a Link
						if(bundle.getEntity() instanceof Link) {
							Reference ref = ((Link)bundle.getEntity()).getLinksTo();
							entityId = null;
							if(ref != null){
								// redefine where the page is and refresh
								entityId = ref.getTargetId();
								versionNumber = ref.getTargetVersionNumber();
								refresh();
								return;
							} else {
								// show error and then allow entity bundle to go to view
								view.showErrorMessage(DisplayConstants.ERROR_NO_LINK_DEFINED);
							}
						}
						EntityHeader projectHeader = DisplayUtils.getProjectHeader(bundle.getPath()); 					
						if(projectHeader == null) view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
						if (projectHeader != null)
							loadBackgroundImage(projectHeader.getId());
						EntityPresenter.filterToDownloadARs(bundle);
						view.setEntityBundle(bundle, versionNumber, projectHeader, area, areaToken);
					} catch (JSONObjectAdapterException ex) {					
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));					
					}
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof NotFoundException) {
					view.show404();
				} else if(caught instanceof ForbiddenException && authenticationController.isLoggedIn()) {
					view.show403();
				} else if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(DisplayConstants.ERROR_UNABLE_TO_LOAD);
				}
			}			
		};
		if (versionNumber == null) {
			synapseClient.getEntityBundle(entityId, mask, callback);
		} else {
			synapseClient.getEntityBundleForVersion(entityId, versionNumber, mask, callback);
		}
	}
	
	public void loadBackgroundImage(final String projectEntityId) {
		//if an attachment is found that has a particular name, then it is set to the background.
		//get the root wiki id
		synapseClient.getRootWikiId(projectEntityId, ObjectType.ENTITY.toString(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String rootWikiId) {
				if (rootWikiId != null) {
					WikiPageKey wikiKey = new WikiPageKey(projectEntityId, ObjectType.ENTITY.toString(), rootWikiId);
					loadBackgroundImage(wikiKey);
				}
			}
			@Override
			public void onFailure(Throwable e) {
				//if anything goes wrong during image load, catch and log to console only
				synapseJsniUtils.consoleError(e.getMessage());
			}
		});
	}
	
	public void loadBackgroundImage(final WikiPageKey rootPageKey) {
		synapseClient.getWikiAttachmentHandles(rootPageKey, new AsyncCallback<FileHandleResults>() {
			@Override
			public void onSuccess(FileHandleResults fileHandleResults) {
				try {
					if (fileHandleResults != null && fileHandleResults.getList() != null && !fileHandleResults.getList().isEmpty()) {
						//look for special file name
						for (FileHandle handle : fileHandleResults.getList()) {
							if (ENTITY_BACKGROUND_IMAGE_NAME.equalsIgnoreCase(handle.getFileName())) {
								String url = DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), rootPageKey, handle.getFileName(),false);
								view.setBackgroundImageUrl(url);
								view.setBackgroundImageVisible(true);
								break;
							}
						}
					}
				} catch (Exception e) {
					//if anything goes wrong during image load, catch and log to console only
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//failed to load background image.  log in console only
				synapseJsniUtils.consoleError(caught.getMessage());
			}
		});
	}
	
	public static void filterToDownloadARs(EntityBundle bundle) {
		List<AccessRequirement> filteredList = AccessRequirementUtils.filterAccessRequirements(bundle.getAccessRequirements(), ACCESS_TYPE.DOWNLOAD);
		bundle.setAccessRequirements(filteredList);
		
		filteredList = AccessRequirementUtils.filterAccessRequirements(bundle.getUnmetDownloadAccessRequirements(), ACCESS_TYPE.DOWNLOAD);
		bundle.setUnmetDownloadAccessRequirements(filteredList);
	}
}
