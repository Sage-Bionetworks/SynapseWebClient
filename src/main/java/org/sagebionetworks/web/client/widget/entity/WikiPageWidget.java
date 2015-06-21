package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

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
	private GlobalApplicationState globalApplicationState;
	private Callback callback;
	private WikiPageKey wikiKey;
	private Boolean canEdit;
	private WikiPage currentPage;
	private boolean isEmbeddedInOwnerPage;
	private WikiPageWidgetView view; 
	private AuthenticationController authenticationController;
	private boolean isCurrentVersion;
	private Long versionInView;
	private CallbackP<WikiPageKey> reloadWikiPageCallback;
	private CallbackP<String> wikiReloadHandler;
	private SynapseAlert synapseAlert;

	public interface Callback{
		public void pageUpdated();
		public void noWikiFound();
	}

	@Inject
	public WikiPageWidget(WikiPageWidgetView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			SynapseAlert synapseAlert) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseAlert = synapseAlert;
		view.setPresenter(this);
		view.setSynapseAlertWidget(synapseAlert.asWidget());
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
	
	public void configure(final WikiPageKey inWikiKey, final Boolean canEdit,
			final Callback callback, final boolean isEmbeddedInOwnerPage) {
		view.showLoading();
		this.canEdit = canEdit;
		this.wikiKey = inWikiKey;
		this.isEmbeddedInOwnerPage = isEmbeddedInOwnerPage;
		this.isCurrentVersion = true;
		this.versionInView = null;
		this.synapseAlert.clear();

		// set up callback
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
		// this callback is used to reload the wiki markdown without reloading the entire page
		reloadWikiPageCallback = new CallbackP<WikiPageKey>() {
			@Override
			public void invoke(WikiPageKey param) {
				wikiKey = param;
				reloadWikiPage();
			}
		};

		setOwnerObjectName(new CallbackP<String>() {
			@Override
			public void invoke(final String ownerObjectName) {
				//get the wiki page
				synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
					@Override
					public void onSuccess(WikiPage result) {
						try {
							currentPage = result;
							wikiKey.setWikiPageId(currentPage.getId());
							boolean isRootWiki = currentPage.getParentWikiId() == null;
							view.configure(currentPage.getMarkdown(), wikiKey, ownerObjectName, canEdit, isRootWiki, isCurrentVersion, versionInView, isEmbeddedInOwnerPage);
						} catch (Exception e) {
							onFailure(e);
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						handleGetV2WikiPageAsV1Failure(caught);
					}
				});
			}
		});
	}

	public void setOwnerObjectName(final CallbackP<String> callback) {
		if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ENTITY.toString())) {
			//lookup the entity name based on the id
			Reference ref = new Reference();
			ref.setTargetId(wikiKey.getOwnerObjectId());
			List<Reference> allRefs = new ArrayList<Reference>();
			allRefs.add(ref);
			ReferenceList list = new ReferenceList();
			list.setReferences(allRefs);
			synapseClient.getEntityHeaderBatch(list, new AsyncCallback<PaginatedResults<EntityHeader>>() {
				@Override
				public void onSuccess(PaginatedResults<EntityHeader> headers) {	
					if (headers.getTotalNumberOfResults() == 1) {
						EntityHeader theHeader = headers.getResults().get(0);
						String ownerObjectName = theHeader.getName();
						callback.invoke(ownerObjectName);
					} else {
						view.show404();
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
						view.showErrorMessage(caught.getMessage());
				}
			});
		} else if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.EVALUATION.toString()) 
				|| wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ACCESS_REQUIREMENT.toString())) {
			callback.invoke("");
		} 
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
		setOwnerObjectName(new CallbackP<String>() {
			@Override
			public void invoke(final String ownerObjectName) {
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
							view.resetWikiMarkdown(currentPage.getMarkdown(), wikiKey, isRootWiki, isCurrentVersion, versionInView);
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
		synapseClient.restoreV2WikiPage(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), wikiKey.getWikiPageId(), wikiVersion, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage result) {
				refresh();
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}

	@Override
	public CallbackP<WikiPageKey> getReloadWikiPageCallback() {
		return this.reloadWikiPageCallback;
	}

	public void reloadWikiPage() {
		synapseAlert.clear();
		synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage result) {
				try {
					currentPage = result;
					boolean isRootWiki = currentPage.getParentWikiId() == null;
					wikiKey.setWikiPageId(currentPage.getId());
					view.resetWikiMarkdown(currentPage.getMarkdown(), wikiKey, isRootWiki, true, null);
					if (wikiReloadHandler != null) {
						wikiReloadHandler.invoke(currentPage.getId());
					}
				} catch (Exception e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				handleGetV2WikiPageAsV1Failure(caught);
			}
		});
	}

	/* private methods */

	private void handleGetV2WikiPageAsV1Failure(Throwable caught) {
		// if it is because of a missing root (and we have edit permission),
		// then the pages browser should have a Create Wiki button
		if (caught instanceof NotFoundException && callback!= null) {
			callback.noWikiFound();
		}
		if (isEmbeddedInOwnerPage) {
			if (caught instanceof NotFoundException) {
				if (canEdit) {
					view.showNoteInPage(DisplayConstants.LABEL_NO_MARKDOWN);
				}else {
					view.showNoteInPage(DisplayConstants.NO_WIKI_FOUND);
				}
			} else {
				synapseAlert.handleException(caught);
				view.showSynapseAlertWidget();	
			}
		} else {
			if (caught instanceof NotFoundException) {
				view.show404();
			} else if (caught instanceof ForbiddenException) {
				view.show403();
			} else {
				synapseAlert.handleException(caught);
				view.showSynapseAlertWidget();
			}
		}
	}

	public void setWikiReloadHandler(CallbackP<String> wikiReloadHandler) {
		this.wikiReloadHandler = wikiReloadHandler;
	}
}
