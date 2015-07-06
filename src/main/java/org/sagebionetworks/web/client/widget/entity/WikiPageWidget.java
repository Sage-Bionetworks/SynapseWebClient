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
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

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
	private GlobalApplicationState globalApplicationState;
	private WikiPageWidgetView view; 
	private AuthenticationController authenticationController;

	// callback
	private Callback callback;
	private CallbackP<String> wikiReloadHandler;

	// state
	private boolean isCurrentVersion;
	private Long versionInView;
	private WikiPageKey wikiKey;
	private Boolean canEdit;
	private WikiPage currentPage;
	private boolean isEmbeddedInOwnerPage;
	
	// widgets
	private SynapseAlert synapseAlert;
	private WikiHistoryWidget historyWidget;
	private MarkdownWidget markdownWidget;
	private Breadcrumb breadcrumb;
	private WikiSubpagesWidget wikiSubpages;
	private UserBadge createdByBadge;
	private UserBadge modifiedByBadge;	

	public interface Callback{
		public void pageUpdated();
		public void noWikiFound();
	}

	@Inject
	public WikiPageWidget(WikiPageWidgetView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			SynapseAlert synapseAlert, WikiHistoryWidget historyWidget,
			MarkdownWidget markdownWidget, Breadcrumb breadcrumb,
			WikiSubpagesWidget wikiSubpages, PortalGinInjector ginInjector) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseAlert = synapseAlert;
		this.historyWidget = historyWidget;
		this.markdownWidget = markdownWidget;
		this.wikiSubpages = wikiSubpages;
		this.breadcrumb = breadcrumb;
		view.setPresenter(this);
		view.setSynapseAlertWidget(synapseAlert);
		view.setWikiHistoryWidget(historyWidget);
		view.setMarkdownWidget(markdownWidget);
		view.setBreadcrumbWidget(breadcrumb);
		createdByBadge = ginInjector.getUserBadgeWidget();
		modifiedByBadge = ginInjector.getUserBadgeWidget();
		view.setModifiedByBadge(modifiedByBadge);
		view.setCreatedByBadge(createdByBadge);
	}
	
	public void clear(){
		view.clear();
		view.hideLoading();
		markdownWidget.clear();
		breadcrumb.clear();
		wikiSubpages.clearState();
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
	
	public void configure(final WikiPageKey wikiKey, final Boolean canEdit,
			final Callback callback, final boolean isEmbeddedInOwnerPage) {
		clear();
		view.showLoading();
		// migrate fields to passed parameters?
		this.canEdit = canEdit;
		this.wikiKey = wikiKey;
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
			
		setOwnerObjectName(new CallbackP<String>() {
			@Override
			public void invoke(final String ownerObjectName) {
				//get the wiki page
				synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
					@Override
					public void onSuccess(WikiPage result) {
						try {
							currentPage = result;
							boolean isRootWiki = currentPage.getParentWikiId() == null;
							wikiKey.setWikiPageId(currentPage.getId());							
							resetWikiMarkdown(currentPage.getMarkdown());
							configureWikiTitle(isRootWiki, currentPage.getTitle());
							configureHistoryWidget(canEdit);
							configureBreadcrumbs(isRootWiki, ownerObjectName);
							configureWikiSubpagesWidget(isEmbeddedInOwnerPage);	
							configureCreatedModifiedBy();
							view.hideLoading();
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
	
	@Override
	public void resetWikiMarkdown(String markdown) {
		view.showMarkdown();
		if(!isCurrentVersion) {
			markdownWidget.configure(markdown, wikiKey, false, versionInView);
			view.showDiffVersionAlert();
			if (canEdit) {
				view.showRestoreButton();
			}
		} else {
			markdownWidget.configure(markdown, wikiKey, false, null);
		}
	}
	
	@Override
	public void configureWikiSubpagesWidget(boolean isEmbeddedInOwnerPage) {
		//check configuration of wikiKey
		view.setWikiSubpagesContainers(wikiSubpages);
		wikiSubpages.configure(wikiKey, null, isEmbeddedInOwnerPage, new CallbackP<WikiPageKey>() {
			@Override
			public void invoke(WikiPageKey param) {
				wikiKey = param;
				reloadWikiPage();
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
				showRestoreWarning(versionToRestore);
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
			view.showBreadcrumbs();
		} else {
			view.hideBreadcrumbs();
		}
	}
	
	public void configureWikiTitle(boolean isRootWiki, String title) {
		if (!isRootWiki) {
			view.setWikiHeadingText(title);
		} else {
			view.setWikiHeadingText("");
		}
	}	
	
	@Override
	public void configureCreatedModifiedBy() {
		view.showCreatedModified();
		modifiedByBadge.configure(currentPage.getModifiedBy());
		createdByBadge.configure(currentPage.getCreatedBy());
		// added check for testing, as Date is not instantiable/mockable
		if (currentPage.getModifiedOn() != null) {
			view.setModifiedByText(" on " + DisplayUtils.convertDataToPrettyString(currentPage.getModifiedOn()));
			view.setCreatedByText(" on " + DisplayUtils.convertDataToPrettyString(currentPage.getCreatedOn()));
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
						show404();
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

	
	private void refresh() {
		configure(wikiKey, canEdit, callback, isEmbeddedInOwnerPage);
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
						else if (caught instanceof ForbiddenException) {
							show403();
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
				view.hideDiffVersionAlert();
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public void reloadWikiPage() {
		synapseAlert.clear();
		synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(final WikiPage result) {
				try {
					view.hideDiffVersionAlert();
					isCurrentVersion = true;
					final boolean isRootWiki = result.getParentWikiId() == null;
					currentPage = result;
					wikiKey.setWikiPageId(result.getId());
					resetWikiMarkdown(result.getMarkdown());
					configureWikiTitle(isRootWiki, result.getTitle());
					configureHistoryWidget(canEdit);
					setOwnerObjectName(new CallbackP<String>() {
						@Override
						public void invoke(String param) {
							configureBreadcrumbs(isRootWiki, param);
						}
					});
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
		view.hideLoading();
		// if it is because of a missing root (and we have edit permission),
		// then the pages browser should have a Create Wiki button
		if (caught instanceof NotFoundException && callback != null) {
			callback.noWikiFound();
		}
		if (isEmbeddedInOwnerPage) {
			view.hideMarkdown();
			view.hideHistory();
			view.hideCreatedModified();
			if (caught instanceof NotFoundException) {
				if (canEdit) {
					view.showNoWikiCanEditMessage();
				}else {
					view.showNoWikiCannotEditMessage();
				}
			} else {
				synapseAlert.handleException(caught);
			}
		} else {
			if (caught instanceof NotFoundException) {
				show404();
			} else if (caught instanceof ForbiddenException) {
				show403();
			} else {
				synapseAlert.handleException(caught);
			}
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
	
	public void show404() {
		view.clear();
		synapseAlert.show404();
	}
	
	public void show403() {
		view.clear();
		synapseAlert.show403();
	}
}
