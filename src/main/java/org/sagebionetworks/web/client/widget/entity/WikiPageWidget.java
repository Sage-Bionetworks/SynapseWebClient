package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to show a wiki page (has a markdown widget and pagebrowser)
 * 
 * @author Jay
 *
 */
public class WikiPageWidget implements WikiPageWidgetView.Presenter,
SynapseWidgetPresenter {
	
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private GlobalApplicationState globalApplicationState;
	private Callback callback;
	private WikiPageKey wikiKey;
	private Boolean canEdit;
	private WikiPage currentPage;
	private boolean isEmbeddedInOwnerPage;
	private AdapterFactory adapterFactory;
	private WikiPageWidgetView view; 
	AuthenticationController authenticationController;
	boolean isDescription = false;
	private boolean isCurrentVersion;
	private Long versionInView;
	
	public interface Callback{
		public void pageUpdated();
		public void noWikiFound();
	}
	
	public interface OwnerObjectNameCallback{
		public void ownerObjectNameInitialized(String ownerObjectName, boolean isDescription);
	}
	
	@Inject
	public WikiPageWidget(WikiPageWidgetView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AdapterFactory adapterFactory,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void showWikiHistory(boolean isVisible) {
		view.showWikiHistory(isVisible);
	}
	public void showCreatedBy(boolean isVisible) {
		view.showCreatedBy(isVisible);
	}
	public void showModifiedBy(boolean isVisible) {
		view.showModifiedBy(isVisible);
	}
	
	public void configure(final WikiPageKey inWikiKey, final Boolean canEdit, final Callback callback, final boolean isEmbeddedInOwnerPage) {
		view.showLoading();
		this.canEdit = canEdit;
		this.wikiKey = inWikiKey;
		this.isEmbeddedInOwnerPage = isEmbeddedInOwnerPage;
		this.isCurrentVersion = true;
		this.versionInView = null;
		//set up callback
		if (callback != null)
			this.callback = callback;
		else 
			this.callback = new Callback() {
				@Override
				public void pageUpdated() {
				}
				@Override
				public void noWikiFound() {
				}
			};
		
		setOwnerObjectName(new OwnerObjectNameCallback() {
			@Override
			public void ownerObjectNameInitialized(final String ownerObjectName, final boolean isDescription) {
				//get the wiki page
				synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
					@Override
					public void onSuccess(WikiPage result) {
						try {
							currentPage = result;
							wikiKey.setWikiPageId(currentPage.getId());
							boolean isRootWiki = currentPage.getParentWikiId() == null;
							view.configure(currentPage.getMarkdown(), wikiKey, ownerObjectName, canEdit, isRootWiki, isDescription, isCurrentVersion, versionInView, isEmbeddedInOwnerPage);
						} catch (Exception e) {
							onFailure(e);
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						//if it is because of a missing root (and we have edit permission), then the pages browser should have a Create Wiki button
						if (caught instanceof NotFoundException) {
							//show insert wiki button if user can edit and it's embedded in another entity page
							if (isEmbeddedInOwnerPage) {
								view.showWarningMessageInPage(DisplayConstants.NO_WIKI_FOUND);
							} else //otherwise, if it's not embedded in the owner page, show a 404
								view.show404();
							
							if (callback != null)
								callback.noWikiFound();
						}
						else if (caught instanceof ForbiddenException) {
							if (!isEmbeddedInOwnerPage) //if it's not embedded in the owner page, show a 403
								view.show403();
						}
						else {
							if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
								view.showWarningMessageInPage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
						}
					}
				});				
			}
		});
	}
	
	public void setOwnerObjectName(final OwnerObjectNameCallback callback) {
		if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ENTITY.toString())) {
			//lookup the entity name based on the id
			Reference ref = new Reference();
			ref.setTargetId(wikiKey.getOwnerObjectId());
			List<Reference> allRefs = new ArrayList<Reference>();
			allRefs.add(ref);
			ReferenceList list = new ReferenceList();
			list.setReferences(allRefs);		
			try {
				synapseClient.getEntityHeaderBatch(list.writeToJSONObject(adapterFactory.createNew()).toJSONString(), new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {					
						BatchResults<EntityHeader> headers;
						try {
							headers = nodeModelCreator.createBatchResults(result, EntityHeader.class);
							if (headers.getTotalNumberOfResults() == 1) {
								EntityHeader theHeader = headers.getResults().get(0);
								isDescription = !(Project.class.getName().equals(theHeader.getType()));
								String ownerObjectName = theHeader.getName();
								callback.ownerObjectNameInitialized(ownerObjectName, isDescription);
							} else {
								view.show404();
							}
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {					
						if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
							view.showErrorMessage(caught.getMessage());
					}
				});
			} catch (JSONObjectAdapterException e) {
				view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			}
		} else if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.EVALUATION.toString()) 
				|| wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ACCESS_REQUIREMENT.toString())) {
			isDescription = true;
			callback.ownerObjectNameInitialized("", isDescription);
		} 
	}
	 
	@Override
	public void createPage(final String name) {
		if (DisplayUtils.isDefined(name))
			createPage(name, null);
	}
	
	public void createPage(final String name, final org.sagebionetworks.web.client.utils.Callback onSuccess) {
		WikiPage page = new WikiPage();
		page.setParentWikiId(wikiKey.getWikiPageId());
		page.setTitle(name);
        synapseClient.createV2WikiPageWithV1(wikiKey.getOwnerObjectId(),  wikiKey.getOwnerObjectType(), page, new AsyncCallback<WikiPage>() {
            @Override
            public void onSuccess(WikiPage result) {
                view.showInfo("Page '" + name + "' Added", "");
            	if (onSuccess != null) {
            		onSuccess.invoke();
            	}
                    
                refresh();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
                    view.showErrorMessage(DisplayConstants.ERROR_PAGE_CREATION_FAILED);
            }
        });
			
	}
	
	public void clear(){
		view.clear();
	}
	private void refresh() {
		configure(wikiKey, canEdit, callback, isEmbeddedInOwnerPage);
	}

	@Override
	public void previewClicked(final Long versionToPreview, Long currentVersion) {
		isCurrentVersion = versionToPreview.equals(currentVersion);
		versionInView = versionToPreview;
		setOwnerObjectName(new OwnerObjectNameCallback() {
			@Override
			public void ownerObjectNameInitialized(final String ownerObjectName, final boolean isDescription) {
				synapseClient.getVersionOfV2WikiPageAsV1(wikiKey, versionToPreview, new AsyncCallback<WikiPage>() {

					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof NotFoundException) {
							if (callback != null)
								callback.noWikiFound();
						}
						else if (caught instanceof ForbiddenException) {
							view.show403();
						}
						else {
							if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
								view.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
						}
					}

					@Override
					public void onSuccess(WikiPage result) {
						try {
							currentPage = result;
							wikiKey.setWikiPageId(currentPage.getId());
							boolean isRootWiki = currentPage.getParentWikiId() == null;
							view.configure(currentPage.getMarkdown(), wikiKey, ownerObjectName, canEdit, isRootWiki, isDescription, isCurrentVersion, versionInView, isEmbeddedInOwnerPage);
						} catch (Exception e) {
							onFailure(e);
						}
					}
					
				});
			}
		});
	}

	@Override
	public WikiPage getWikiPage() {
		return currentPage;
	}
	
	
	@Override
	public void restoreClicked(final Long wikiVersion) {
		// User has confirmed. Restore and refresh the page to see the update.
		synapseClient.restoreV2WikiPage(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), wikiKey.getWikiPageId(), wikiVersion, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				refresh();
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
}
