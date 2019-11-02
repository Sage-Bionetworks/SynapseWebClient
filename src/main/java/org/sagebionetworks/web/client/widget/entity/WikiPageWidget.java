package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.Objects;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WikiSubpagesCollapseEvent;
import org.sagebionetworks.web.client.events.WikiSubpagesExpandEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventHandler;

/**
 * Lightweight widget used to show a wiki page (has a markdown widget and pagebrowser)
 * 
 * @author Jay
 *
 */
public class WikiPageWidget implements WikiPageWidgetView.Presenter, SynapseWidgetPresenter {

	// utility
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;
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

	// widgets
	private StuAlert stuAlert;
	private WikiHistoryWidget historyWidget;
	private MarkdownWidget markdownWidget;
	private WikiSubpagesWidget wikiSubpages;
	private SessionStorage sessionStorage;
	private AuthenticationController authController;
	private AdapterFactory adapterFactory;
	private boolean isModifiedCreatedByHistoryVisible = true;

	public interface Callback {
		public void pageUpdated();

		public void noWikiFound();
	}

	private DateTimeUtils dateTimeUtils;
	private CookieProvider cookies;

	@Inject
	public WikiPageWidget(WikiPageWidgetView view, SynapseClientAsync synapseClient, StuAlert stuAlert, WikiHistoryWidget historyWidget, MarkdownWidget markdownWidget, WikiSubpagesWidget wikiSubpages, PortalGinInjector ginInjector, SessionStorage sessionStorage, AuthenticationController authController, AdapterFactory adapterFactory, DateTimeUtils dateTimeUtils, SynapseJavascriptClient jsClient, CookieProvider cookies, EventBus eventBus) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.stuAlert = stuAlert;
		this.historyWidget = historyWidget;
		this.markdownWidget = markdownWidget;
		this.wikiSubpages = wikiSubpages;
		this.sessionStorage = sessionStorage;
		this.authController = authController;
		this.adapterFactory = adapterFactory;
		this.dateTimeUtils = dateTimeUtils;
		this.jsClient = jsClient;
		this.cookies = cookies;
		view.setPresenter(this);
		view.setSynapseAlertWidget(stuAlert.asWidget());
		view.setWikiHistoryWidget(historyWidget);
		view.setMarkdownWidget(markdownWidget);

		view.getEventBinder().bindEventHandlers(this, eventBus);
	}

	public void clear() {
		view.clear();
		view.setLoadingVisible(false);
		markdownWidget.clear();
		view.setWikiSubpagesWidgetVisible(false);
		view.setWikiHeadingText("");
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void addStyleName(String style) {
		view.addStyleName(style);
	}

	public void configure(final WikiPageKey wikiKey, final Boolean canEdit, final Callback callback) {
		clear();
		view.setMainPanelVisible(true);
		view.setLoadingVisible(true);
		// migrate fields to passed parameters?
		this.canEdit = canEdit;
		this.wikiKey = wikiKey;
		this.isCurrentVersion = true;
		this.versionInView = null;
		this.stuAlert.clear();
		// set up callback
		if (callback != null) {
			this.callback = callback;
		} else {
			this.callback = new Callback() {
				@Override
				public void pageUpdated() {}

				@Override
				public void noWikiFound() {}
			};
		}
		reloadWikiPage();
		view.setWikiHistoryDiffToolButtonVisible(DisplayUtils.isInTestWebsite(cookies), wikiKey);
	}

	public void showSubpages(ActionMenuWidget actionMenu) {
		view.setWikiSubpagesWidgetVisible(true);
		configureWikiSubpagesWidget(actionMenu);
	}

	public void checkUseCachedWikiPage(final WikiPage cachedWikiPage) {
		jsClient.getV2WikiPage(wikiKey, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage v2WikiPage) {
				stuAlert.clear();
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
		jsClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
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
		if (!Objects.equals(result.getId(), wikiKey.getWikiPageId())) {
			// if this result is not the wiki page that we want, then ignore.
			return;
		}
		try {
			view.setDiffVersionAlertVisible(false);
			view.setModifiedCreatedByHistoryPanelVisible(isModifiedCreatedByHistoryVisible);
			isCurrentVersion = true;
			updateCurrentPage(result);
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
				// if any problems occur, try to get the wiki page with a rpc
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
		if (DisplayUtils.isDefined(markdown)) {
			view.setNoWikiCanEditMessageVisible(false);
			view.setNoWikiCannotEditMessageVisible(false);
			view.setMarkdownVisible(true);
			if (!isCurrentVersion) {
				markdownWidget.configure(markdown, wikiKey, versionInView);
				view.setDiffVersionAlertVisible(true);
				if (canEdit) {
					view.setRestoreButtonVisible(true);
				}
			} else {
				markdownWidget.configure(markdown, wikiKey, null);
			}
		} else {
			showNoWikiFoundUI();
		}
	}

	private void onWikiPageKeyChange(WikiPageKey newKey) {
		view.setLoadingVisible(true);
		markdownWidget.clear();
		view.setWikiHeadingText("");

		wikiKey = newKey;
		reloadWikiPage();
		if (wikiReloadHandler != null) {
			wikiReloadHandler.invoke(wikiKey.getWikiPageId());
		}
	}

	public void configureWikiSubpagesWidget(ActionMenuWidget actionMenu) {
		// check configuration of wikiKey
		wikiSubpages.configure(wikiKey, true, newKey -> {
			onWikiPageKeyChange(newKey);
		}, actionMenu);
		view.setWikiSubpagesWidget(wikiSubpages);
	}

	@Override
	public void configureHistoryWidget(boolean canEdit) {
		// Configure the history widget and built the history table
		ActionHandler actionHandler = new ActionHandler() {
			@Override
			public void previewClicked(Long versionToPreview, Long currentVersion) {
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

	public void configureWikiTitle(boolean isRootWiki, String title) {
		if (!isRootWiki) {
			view.setWikiHeadingText(title);
		} else {
			view.setWikiHeadingText("");
		}
	}

	private void refresh() {
		view.setMainPanelVisible(true);
		configure(wikiKey, canEdit, callback);
	}

	@Override
	public void showPreview(final Long versionToPreview, Long currentVersion) {
		isCurrentVersion = versionToPreview.equals(currentVersion);
		versionInView = versionToPreview;
		jsClient.getVersionOfV2WikiPageAsV1(wikiKey, versionToPreview, new AsyncCallback<WikiPage>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					if (callback != null)
						callback.noWikiFound();
				} else {
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
			public void invoke() {}
		};
		view.showPopup(DisplayConstants.RESTORING_WIKI_VERSION_WARNING_TITLE, DisplayConstants.RESTORING_WIKI_VERSION_WARNING_MESSAGE, MessagePopup.WARNING, okCallback, cancelCallback);
	}

	@Override
	public void restoreConfirmed() {
		// User has confirmed. Restore and refresh the page to see the update.
		synapseClient.restoreV2WikiPage(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), wikiKey.getWikiPageId(), versionInView, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage result) {
				onWikiPageKeyChange(wikiKey);
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
		view.setCreatedOn(dateTimeUtils.getDateTimeString(result.getCreatedOn()));
		view.setModifiedOn(dateTimeUtils.getDateTimeString(result.getModifiedOn()));
		view.setWikiHistoryVisible(false);
		historyWidget.clear();
	}

	@Override
	public void reloadWikiPage() {
		stuAlert.clear();
		// get the wiki page
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
		view.setMarkdownVisible(false);
		view.setWikiHistoryVisible(false);
		if (caught instanceof NotFoundException) {
			showNoWikiFoundUI();
		} else {
			view.setMainPanelVisible(false);
			stuAlert.handleException(caught);
		}
	}

	private void showNoWikiFoundUI() {
		view.setMarkdownVisible(false);
		if (canEdit) {
			view.setNoWikiCanEditMessageVisible(true);
		} else {
			view.setNoWikiCannotEditMessageVisible(true);
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

	public void setModifiedCreatedByHistoryVisible(boolean isVisible) {
		isModifiedCreatedByHistoryVisible = isVisible;
	}

	@Override
	public void showWikiHistory() {
		configureHistoryWidget(canEdit);
	}

	@EventHandler
	public void onWikiSubpagesCollapseEvent(WikiSubpagesCollapseEvent event) {
		view.collapseWikiSubpages();
	}

	@EventHandler
	public void onWikiSubpagesExpandEvent(WikiSubpagesExpandEvent event) {
		view.expandWikiSubpages();
	}
}
