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
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
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
	private SynapseAlert synapseAlert;
	private WikiHistoryWidget historyWidget;
	private MarkdownWidget markdownWidget;
	private Breadcrumb breadcrumb;
	private WikiSubpagesWidget wikiSubpages;
	private UserBadge createdByBadge;
	private UserBadge modifiedByBadge;	
	
	private String suffix;
	
	public interface Callback{
		public void pageUpdated();
		public void noWikiFound();
	}

	@Inject
	public WikiPageWidget(WikiPageWidgetView view,
			SynapseClientAsync synapseClient,
			SynapseAlert synapseAlert, WikiHistoryWidget historyWidget,
			MarkdownWidget markdownWidget, Breadcrumb breadcrumb,
			WikiSubpagesWidget wikiSubpages, PortalGinInjector ginInjector) {
		this.view = view;
		this.synapseClient = synapseClient;
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
		view.setLoadingVisible(false);
		markdownWidget.clear();
		breadcrumb.clear();
		wikiSubpages.clearState();
		view.setCreatedModifiedVisible(false);
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
	public void showCreatedBy(boolean isVisible) {
		view.showCreatedBy(isVisible);
	}
	public void showModifiedBy(boolean isVisible) {
		view.showModifiedBy(isVisible);
	}
	
	public void configure(final WikiPageKey wikiKey, final Boolean canEdit,
			final Callback callback, final boolean showSubpages, String suffix) {
		clear();
		view.setMainPanelVisible(true);
		view.setLoadingVisible(true);
		// migrate fields to passed parameters?
		this.canEdit = canEdit;
		this.wikiKey = wikiKey;
		this.showSubpages = showSubpages;
		this.isCurrentVersion = true;
		this.versionInView = null;
		this.suffix = suffix;
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
							updateCurrentPage(result);
							boolean isRootWiki = currentPage.getParentWikiId() == null;
							configureBreadcrumbs(isRootWiki, ownerObjectName);
							view.setWikiSubpagesWidgetVisible(showSubpages);
							if (showSubpages) {
								configureWikiSubpagesWidget();	
							}
							view.setLoadingVisible(false);
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
		view.setMarkdownVisible(true);
		if(!isCurrentVersion) {
			markdownWidget.configure(markdown, wikiKey, suffix, versionInView);
			view.setDiffVersionAlertVisible(true);
			if (canEdit) {
				view.setRestoreButtonVisible(true);
			}
		} else {
			markdownWidget.configure(markdown, wikiKey, suffix, null);
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
	
	@Override
	public void configureCreatedModifiedBy() {
		view.setCreatedModifiedVisible(true);
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
					synapseAlert.handleException(caught);
				}
			});
		} else if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.EVALUATION.toString()) 
				|| wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ACCESS_REQUIREMENT.toString())) {
			callback.invoke("");
		} 
	}

	
	private void refresh() {
		view.setMainPanelVisible(true);
		configure(wikiKey, canEdit, callback, showSubpages, suffix);
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
							synapseAlert.handleException(caught);
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
				synapseAlert.handleException(caught);
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
		configureCreatedModifiedBy();
	}
	@Override
	public void reloadWikiPage() {
		synapseAlert.clear();
		synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(final WikiPage result) {
				try {
					view.setDiffVersionAlertVisible(false);
					isCurrentVersion = true;
					final boolean isRootWiki = result.getParentWikiId() == null;
					updateCurrentPage(result);
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
		view.setLoadingVisible(false);
		// if it is because of a missing root (and we have edit permission),
		// then the pages browser should have a Create Wiki button
		if (caught instanceof NotFoundException && callback != null) {
			callback.noWikiFound();
		}
		if (showSubpages) {
			view.setMarkdownVisible(false);
			view.setWikiHistoryVisible(false);			
			if (caught instanceof NotFoundException) {
				if (canEdit) {
					view.setNoWikiCanEditMessageVisible(true);
				}else {
					view.setNoWikiCannotEditMessageVisible(true);
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
	
	public void show404() {
		view.setMainPanelVisible(false);
		synapseAlert.show404();
	}
	
	public void show403() {
		view.setMainPanelVisible(false);
		synapseAlert.show403();
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
	
}
