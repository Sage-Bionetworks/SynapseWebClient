package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to show a wiki page (has a markdown widget and pagebrowser)
 * 
 * @author Jay
 *
 */
public class WikiPageWidget implements WikiPageWidgetView.Presenter, SynapseWidgetPresenter {

	// utility
	private SynapseClientAsync synapseClient;
	private WikiPageWidgetView view; 

	// callback
	private Callback callback;
	private CallbackP<String> wikiReloadHandler;

	// state
	private boolean isCurrentVersion;
	private Long versionInView;
	private WikiPageKey wikiKey;
	private Boolean canEdit;
	private WikiPage currentPage;
	private boolean showSubpages;
	
	// widgets
	private StuAlert stuAlert;
	private WikiHistoryWidget historyWidget;
	private MarkdownWidget markdownWidget;
	private Breadcrumb breadcrumb;
	private WikiSubpagesWidget wikiSubpages;
	private ModifiedCreatedByWidget modifiedCreatedBy;
	private SessionStorage sessionStorage;
	private AuthenticationController authController;
	private AdapterFactory adapterFactory;
	
	public interface Callback{
		public void pageUpdated();
		public void noWikiFound();
	}

	@Inject
	public WikiPageWidget(WikiPageWidgetView view,
			SynapseClientAsync synapseClient,
			StuAlert stuAlert, WikiHistoryWidget historyWidget,
			MarkdownWidget markdownWidget, Breadcrumb breadcrumb,
			WikiSubpagesWidget wikiSubpages, PortalGinInjector ginInjector,
			ModifiedCreatedByWidget modifiedCreatedBy,
			SessionStorage sessionStorage,
			AuthenticationController authController,
			AdapterFactory adapterFactory
			) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.stuAlert = stuAlert;
		this.historyWidget = historyWidget;
		this.markdownWidget = markdownWidget;
		this.wikiSubpages = wikiSubpages;
		this.breadcrumb = breadcrumb;
		this.modifiedCreatedBy = modifiedCreatedBy;
		this.stuAlert = stuAlert;
		this.sessionStorage = sessionStorage;
		this.authController = authController;
		this.adapterFactory = adapterFactory;
		view.setPresenter(this);
		view.setSynapseAlertWidget(stuAlert.asWidget());
		view.setWikiHistoryWidget(historyWidget);
		view.setMarkdownWidget(markdownWidget);
		view.setBreadcrumbWidget(breadcrumb);
		view.setModifiedCreatedBy(modifiedCreatedBy);
	}
	
	public void clear(){
		view.clear();
		view.setLoadingVisible(false);
		markdownWidget.clear();
		breadcrumb.clear();
		wikiSubpages.clearState();
		view.setWikiHeadingText("");
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void addStyleName(String style) {
		view.addStyleName(style);
	}

	public void showWikiHistory(boolean isVisible) {
		view.setWikiHistoryVisible(isVisible);
	}
	
	public void configure(final WikiPageKey wikiKey, final Boolean canEdit,
			final Callback callback, final boolean showSubpages) {
		clear();
		view.setMainPanelVisible(true);
		view.setLoadingVisible(true);
		// migrate fields to passed parameters?
		this.canEdit = canEdit;
		this.wikiKey = wikiKey;
		this.showSubpages = showSubpages;
		this.isCurrentVersion = true;
		this.versionInView = null;
		this.stuAlert.clear();
		// set up callback
		if (callback != null) {
			this.callback = callback;
		} else {
			this.callback = new Callback() {
				@Override
				public void pageUpdated() {
				}
				@Override
				public void noWikiFound() {
				}
			};
		}
		view.setWikiSubpagesWidgetVisible(showSubpages);
		if (showSubpages) {
			configureWikiSubpagesWidget();	
		}
		reloadWikiPage();
	}
	
	
	
	public void checkUseCachedWikiPage(final WikiPage cachedWikiPage) {
		synapseClient.getV2WikiPage(wikiKey, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage v2WikiPage) {
					if (v2WikiPage.getEtag().equals(cachedWikiPage.getEtag())) {
						setWikiPage(cachedWikiPage);
					} else {
						// cached wiki page is old, ask for the updated version
						getV2WikiPageAsV1();
					}
			}
			@Override
			public void onFailure(Throwable caught) {
				handleGetV2WikiPageAsV1Failure(caught);
			}
		});
	}
	
	public void getV2WikiPageAsV1() {
		synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage result) {
				cacheWikiPage(result);
				setWikiPage(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				handleGetV2WikiPageAsV1Failure(caught);
			}
		});
	}
	
	public void setWikiPage(WikiPage result) {
		try {
			view.setDiffVersionAlertVisible(false);
			view.setModifiedCreatedByHistoryPanelVisible(true);
			isCurrentVersion = true;
			final boolean isRootWiki = result.getParentWikiId() == null;
			updateCurrentPage(result);
			setOwnerObjectName(new CallbackP<String>() {
				@Override
				public void invoke(String param) {
					configureBreadcrumbs(isRootWiki, param);
				}
			});
			view.scrollWikiHeadingIntoView();
			view.setLoadingVisible(false);
		} catch (Exception e) {
			handleGetV2WikiPageAsV1Failure(e);
		}

	}
	
	public String getSessionCacheKey(WikiPageKey wikiKey) {
		String currentUserId = authController.getCurrentUserPrincipalId();
		return currentUserId + "_" + wikiKey.getOwnerObjectId() + "_" + wikiKey.getOwnerObjectType() + "_" + wikiKey.getWikiPageId() + WebConstants.WIKIPAGE_SUFFIX;
	}
	
	public WikiPage getWikiPageFromCache(WikiPageKey wikiKey) {
		WikiPage wikiPage = null;
		String key = getSessionCacheKey(wikiKey); 
		String wikiPageJson = sessionStorage.getItem(key);
		if (wikiPageJson != null) {
			try {
				wikiPage = new WikiPage(adapterFactory.createNew(wikiPageJson));
			} catch (JSONObjectAdapterException e) {
				//if any problems occur, try to get the wiki page with a rpc
			}	

		}
		return wikiPage;
	}
	
	public void cacheWikiPage(WikiPage wikiPage) {
		String key = getSessionCacheKey(wikiKey); 
		JSONObjectAdapter adapter = adapterFactory.createNew();
		try {
			wikiPage.writeToJSONObject(adapter);
			sessionStorage.setItem(key, adapter.toJSONString());
		} catch (JSONObjectAdapterException e) {
		}

	}
	
	@Override
	public void resetWikiMarkdown(String markdown) {
		view.setMarkdownVisible(true);
		if(!isCurrentVersion) {
			markdownWidget.configure(markdown, wikiKey, versionInView);
			view.setDiffVersionAlertVisible(true);
			if (canEdit) {
				view.setRestoreButtonVisible(true);
			}
		} else {
			markdownWidget.configure(markdown, wikiKey, null);
		}
	}
	
	@Override
	public void configureWikiSubpagesWidget() {
		//check configuration of wikiKey
		view.setWikiSubpagesContainers(wikiSubpages);
		wikiSubpages.configure(wikiKey, null, true, new CallbackP<WikiPageKey>() {
			@Override
			public void invoke(WikiPageKey param) {
				wikiKey = param;
				reloadWikiPage();
				if (wikiReloadHandler != null) {
					wikiReloadHandler.invoke(wikiKey.getWikiPageId());
				}
			}});
		view.setWikiSubpagesWidget(wikiSubpages);
	}
	
	@Override
	public void configureHistoryWidget(boolean canEdit) {
		// Configure the history widget and built the history table
		ActionHandler actionHandler = new ActionHandler() {
			@Override
			public void previewClicked(Long versionToPreview,
					Long currentVersion) {
				showPreview(versionToPreview, currentVersion);
			}
			@Override
			public void restoreClicked(Long versionToRestore) {
				versionInView = versionToRestore;
				restoreConfirmed();
			}
		};
		historyWidget.configure(wikiKey, canEdit, actionHandler);
		view.setWikiHistoryWidget(historyWidget);
	}
	
	@Override
	public void configureBreadcrumbs(boolean isRootWiki, String ownerObjectName) {
		if (!isRootWiki) {
			List<LinkData> links = new ArrayList<LinkData>();
			Place ownerObjectPlace = new Synapse(wikiKey.getOwnerObjectId());
			links.add(new LinkData(ownerObjectName, ownerObjectPlace));
			breadcrumb.configure(links, currentPage.getTitle());
			view.setBreadcrumbsVisible(true);
		} else {
			view.setBreadcrumbsVisible(false);
		}
	}
	
	public void configureWikiTitle(boolean isRootWiki, String title) {
		if (!isRootWiki) {
			view.setWikiHeadingText(title);
		} else {
			view.setWikiHeadingText("");
		}
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
						onFailure(new NotFoundException());
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.setMainPanelVisible(false);
					stuAlert.handleException(caught);
				}
			});
		} else if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.EVALUATION.toString()) 
				|| wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ACCESS_REQUIREMENT.toString())) {
			callback.invoke("");
		} 
	}

	
	private void refresh() {
		view.setMainPanelVisible(true);
		configure(wikiKey, canEdit, callback, showSubpages);
	}

	@Override
	public void showPreview(final Long versionToPreview, Long currentVersion) {
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
						else {
							stuAlert.handleException(caught);
						}
					}

					@Override
					public void onSuccess(WikiPage result) {
						try {
							currentPage = result;
							wikiKey.setWikiPageId(currentPage.getId());
							resetWikiMarkdown(currentPage.getMarkdown());
						} catch (Exception e) {
							onFailure(e);
						}
					}
				});
			}
		});
	}

	@Override
	public void showRestoreWarning(final Long versionToRestore) {
		org.sagebionetworks.web.client.utils.Callback okCallback = new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
				versionInView = versionToRestore;
				restoreConfirmed();
			}	
		};
		org.sagebionetworks.web.client.utils.Callback cancelCallback = new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
			}	
		};
		view.showPopup(DisplayConstants.RESTORING_WIKI_VERSION_WARNING_TITLE, DisplayConstants.RESTORING_WIKI_VERSION_WARNING_MESSAGE, 
				MessagePopup.WARNING, okCallback, cancelCallback);
	}
	
	@Override
	public void restoreConfirmed() {
		// User has confirmed. Restore and refresh the page to see the update.
		synapseClient.restoreV2WikiPage(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), wikiKey.getWikiPageId(), versionInView, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage result) {
				refresh();
				view.setDiffVersionAlertVisible(false);
			}
			@Override
			public void onFailure(Throwable caught) {
				stuAlert.handleException(caught);
			}
		});
	}
	
	private void updateCurrentPage(WikiPage result) {
		currentPage = result;
		boolean isRootWiki = currentPage.getParentWikiId() == null;
		wikiKey.setWikiPageId(currentPage.getId());
		resetWikiMarkdown(currentPage.getMarkdown());
		configureWikiTitle(isRootWiki, currentPage.getTitle());
		configureHistoryWidget(canEdit);
		modifiedCreatedBy.configure(result.getCreatedOn(), result.getCreatedBy(), result.getModifiedOn(), result.getModifiedBy());
	}
	
	@Override
	public void reloadWikiPage() {
		stuAlert.clear();
		//get the wiki page
		WikiPage wikiPage = getWikiPageFromCache(wikiKey);
		if (wikiPage != null) {
			checkUseCachedWikiPage(wikiPage);
		} else {
			getV2WikiPageAsV1();
		}
	}

	/* private methods */

	private void handleGetV2WikiPageAsV1Failure(Throwable caught) {
		view.setLoadingVisible(false);
		view.setModifiedCreatedByHistoryPanelVisible(false);
		// if it is because of a missing root (and we have edit permission),
		// then the pages browser should have a Create Wiki button
		if (caught instanceof NotFoundException && callback != null) {
			callback.noWikiFound();
		}
		if (showSubpages) {
			view.setMarkdownVisible(false);
			view.setWikiHistoryVisible(false);
		}
		if (caught instanceof NotFoundException) {
			if (canEdit) {
				view.setNoWikiCanEditMessageVisible(true);
			}else {
				view.setNoWikiCannotEditMessageVisible(true);
			}
		} else {
			view.setMainPanelVisible(false);
			stuAlert.handleException(caught);
		}
	}

	public void setWikiReloadHandler(CallbackP<String> wikiReloadHandler) {
		this.wikiReloadHandler = wikiReloadHandler;
	}

	@Override
	public void restoreClicked() {
		showRestoreWarning(versionInView);
	}

	// For testing only
	public void setWikiPageKey(WikiPageKey wikiKey) {
		this.wikiKey = wikiKey;
	}
	
	public void setCurrentPage(WikiPage currentPage) {
		this.currentPage = currentPage;
	}
	
	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	public void setModifiedCreatedByVisible(boolean isVisible) {
		modifiedCreatedBy.setVisible(isVisible);
	}
	
}
