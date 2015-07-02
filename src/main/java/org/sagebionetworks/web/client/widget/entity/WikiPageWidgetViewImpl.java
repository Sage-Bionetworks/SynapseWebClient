package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to show a wiki page (has a markdown widget and pagebrowser)
 * 
 * @author Jay
 *
 */
public class WikiPageWidgetViewImpl extends FlowPanel implements WikiPageWidgetView {

	public interface Binder extends UiBinder<Widget, WikiPageWidgetViewImpl> {}
	
	WikiPageWidgetView.Presenter presenter;
	private boolean isCurrentVersion;
	private Long versionInView;
	
	@UiField
	FlowPanel wikiPagePanel;
	
	@UiField
	Alert diffVersionAlert;
	
	@UiField
	Span modifiedOnField;
	
	@UiField
	Span createdOnField;
	
	@UiField
	SimplePanel	modifiedByBadgePanel;
	
	@UiField
	SimplePanel	createdByBadgePanel;
	
	@UiField
	Button wikiHistoryButton;
	
	@UiField
	Button restoreButton;
	
	@UiField
	SimplePanel wikiHistoryPanel;
	
	@UiField
	FlowPanel wikiSubpagesPanel;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	HTMLPanel loadingPanel;
	
	@UiField
	HTMLPanel modifiedByPanel;
	
	@UiField
	HTMLPanel createdByPanel;
	
	@UiField
	SimplePanel breadcrumbPanel;
	
	@UiField
	SimplePanel markdownPanel;
	
	@UiField
	Anchor anchorToCurrentVersion;
	
	@UiField
	Collapse historyCollapse;
	
	Widget widget;

	public interface OwnerObjectNameCallback{
		public void ownerObjectNameInitialized();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Inject
	public WikiPageWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		restoreButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.restoreClicked();
				// If this button is used when viewing the current version for any reason, don't do anything
				// Otherwise, raise warning to user for confirmation before restoring
				if(!isCurrentVersion) {
					// versionInView should be set if !isCurrentVersion
					if(versionInView != null) {
						showRestorationWarning(versionInView);
					}
				}
			}
		});
		anchorToCurrentVersion.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.reloadCurrentWikiPage();
			}
		});
		wikiHistoryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!historyCollapse.isCollapsing()) {
					if (wikiHistoryButton.getIcon() == IconType.CARET_DOWN) {
						wikiHistoryButton.setIcon(IconType.CARET_UP);
					} else {
						wikiHistoryButton.setIcon(IconType.CARET_DOWN);
					}
				}
			}
		});
	}
	
	@Override
	public void clear() {
		diffVersionAlert.setVisible(false);
		//rest of clearing UI state?
	}
	
	@Override
	public void showDiffVersionAlert() {
		diffVersionAlert.setVisible(true);
	}
	
	@Override
	public void showPopup(String title, String message, MessagePopup popupType, Callback okCallback, Callback cancelCallback) {
		DisplayUtils.showPopup(message, DisplayConstants.RESTORING_WIKI_VERSION_WARNING_MESSAGE, 
				popupType, okCallback, cancelCallback);
	}

	@Override
	public void show404() {
		clear();
		add(new HTML(DisplayUtils.get404Html()));
	}

	@Override
	public void show403() {
		clear();
		add(new HTML(DisplayUtils.get403Html()));
	}

	@Override
	public void showNoteInPage(String message) {
		clear();
		add(new Italic(message));
	}
	
	@Override
	public void setWikiSubpagesWidget(IsWidget wikiSubpages) {
		wikiSubpagesPanel.clear();
		wikiSubpagesPanel.add(wikiSubpages);
	}

	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	public void showRestorationWarning(final Long wikiVersion) {
		org.sagebionetworks.web.client.utils.Callback okCallback = new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
				presenter.restoreClicked();
			}	
		};
		org.sagebionetworks.web.client.utils.Callback cancelCallback = new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
			}	
		};
		DisplayUtils.showPopup(DisplayConstants.RESTORING_WIKI_VERSION_WARNING_TITLE, DisplayConstants.RESTORING_WIKI_VERSION_WARNING_MESSAGE, 
				MessagePopup.WARNING, okCallback, cancelCallback);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showCreatedBy(boolean isVisible) {
		createdByPanel.setVisible(isVisible);
	}

	@Override
	public void showModifiedBy(boolean isVisible) {
		modifiedByPanel.setVisible(isVisible);
	}

	@Override
	public void showWikiHistory(boolean isVisible) {
		wikiHistoryPanel.setVisible(isVisible);
	}

//	@Override
//	public void resetWikiMarkdown(String markdown, final WikiPageKey wikiKey,
//			boolean isRootWiki, boolean isCurrentVersion, final Long versionInView) {
//		this.wikiKey = wikiKey;
//		this.isRootWiki = isRootWiki;
//		this.isHistoryOpen = false;
//		this.isHistoryWidgetBuilt = false;
//		this.isCurrentVersion = isCurrentVersion;
//		this.versionInView = versionInView;
//		if(!isCurrentVersion) {
//			markdownWidget.configure(markdown, wikiKey, false, versionInView);
//		} else {
//			markdownWidget.configure(markdown, wikiKey, false, null);
//		}
//		resetWikiPagePanel();
//	}

//	private void resetWikiPagePanel() {
//		wikiPagePanel.clear();
//		if(!isCurrentVersion) {
//			// Create warning that user is viewing a different version
//			Alert notice = createDifferentVersionNotice();
//			wikiPagePanel.add(notice);
//		}
//
//		wikiPagePanel.add(getBreadCrumbs());
//		SimplePanel topBarWrapper = new SimplePanel();
//		String titleString = isRootWiki ? "" : presenter.getWikiPage().getTitle();
//		Heading h2 = new Heading(HeadingSize.H2);
//		h2.setText(titleString);
//		h2.addStyleName("margin-bottom-0-imp");
//		topBarWrapper.add(h2);
//		wikiPagePanel.add(topBarWrapper);
//
//		FlowPanel mainPanel = new FlowPanel();
//		mainPanel.add(wrapWidget(markdownWidget.asWidget(), "margin-top-5"));
//		wikiPagePanel.add(mainPanel);
//
//		FlowPanel modifiedCreatedSection = createdModifiedCreatedSection();
//		wikiPagePanel.add(wrapWidget(modifiedCreatedSection, "margin-top-10 clearleft"));
//	}

	/*
	 *     +------+
	 *     | Wiki |
	 * +---------------------------------------------------------+
	 * | +---------+ +-----------------------------------------+ |
	 * | | 2       | | +-------------------------------------+ | |
	 * | |         | | | Notice                              | | |
	 * | |         | | +-------------------------------------+ | |
	 * | +---------+ |  Wiki content                           | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             | Modified/Created by                     | |
	 * |             | Show Wiki History                     3 | |
	 * | 1           +-----------------------------------------+ |
	 * +---------------------------------------------------------+
	 * 
	 * 1 - this widget
	 * 2 - WikiSubpagesWidget widget
	 * 3 - wikiPagePanel
	 */
//	private void showDefaultViewWithWiki() {
//		clear();
//		//also add the wiki subpages widget, unless explicitly instructed not to in the markdown
//		FlowPanel wikiSubpagesPanel = new FlowPanel();
//		WikiSubpagesWidget widget = ginInjector.getWikiSubpagesRenderer();
//		//subpages widget is special in that it applies styles to the markdown html panel (if there are subpages)
//		wikiSubpagesPanel.add(widget.asWidget());
//		add(wikiSubpagesPanel);
//		add(wikiPagePanel);
//		widget.configure(wikiKey, new HashMap<String, String>(), null, wikiSubpagesPanel, wikiPagePanel, isEmbeddedInOwnerPage, presenter.getReloadWikiPageCallback());
//	}
	
	@Override
	public void setWikiSubpagesContainers(WikiSubpagesWidget wikiSubpages) {
		wikiSubpages.setContainers(wikiSubpagesPanel, wikiPagePanel);
	}

//	private FlowPanel createdModifiedCreatedSection() {
		// Add created/modified information at the end
//		SafeHtmlBuilder shb = new SafeHtmlBuilder();
//		shb.appendHtmlConstant(DisplayConstants.MODIFIED_BY + " ");
//		HTML modifiedText = new HTML(shb.toSafeHtml());
//
//		shb = new SafeHtmlBuilder();
//		shb.appendHtmlConstant(" " + DisplayConstants.ON + " " + DisplayUtils.converDataToPrettyString(presenter.getWikiPage().getModifiedOn()));
//		HTML modifiedOnText = new HTML(shb.toSafeHtml());
//
//		shb = new SafeHtmlBuilder();
//		shb.appendHtmlConstant(DisplayConstants.CREATED_BY + " ");
//		HTML createdText = new HTML(shb.toSafeHtml());
//
//		shb = new SafeHtmlBuilder();
//		shb.appendHtmlConstant(" " + DisplayConstants.ON + " " + DisplayUtils.converDataToPrettyString(presenter.getWikiPage().getCreatedOn()));		
//		HTML createdOnText = new HTML(shb.toSafeHtml());
//
//		UserBadge modifiedBy = ginInjector.getUserBadgeWidget();
//		modifiedBy.configure(presenter.getWikiPage().getModifiedBy());
//
//		UserBadge createdBy = ginInjector.getUserBadgeWidget();
//		createdBy.configure(presenter.getWikiPage().getCreatedBy());
//
//		modifiedPanel.clear();
//		modifiedPanel.add(modifiedText);
//		modifiedPanel.add(modifiedBy.asWidget());
//		modifiedPanel.add(modifiedOnText);
//
//		createdPanel.clear();
//		createdPanel.add(createdText);
//		createdPanel.add(createdBy.asWidget());
//		createdPanel.add(createdOnText);

//		FlowPanel modifiedAndCreatedSection = new FlowPanel();
//		modifiedAndCreatedSection.add(modifiedPanel);
//		modifiedAndCreatedSection.add(createdPanel);
//
//		historyPanel.clear();
//		historyPanel.add(wrapWidget(createHistoryButton(), "margin-top-5"));
//		modifiedAndCreatedSection.add(historyPanel);
//		return modifiedAndCreatedSection;
//	}
	
	@Override
	public void setModifiedByBadge(IsWidget modifiedByUserBadge) {
		modifiedByBadgePanel.setWidget(modifiedByUserBadge);
	}
	
	@Override
	public void setModifiedByText(String modifiedByText) {
		modifiedOnField.setText(modifiedByText);
	}
	
	@Override
	public void setCreatedByBadge(IsWidget createdByUserBadge) {
		createdByBadgePanel.setWidget(createdByUserBadge);
	}
	
	@Override
	public void setCreatedByText(String createdByText) {
		createdOnField.setText(createdByText);
	}
	
	
	
	
//	private Alert createDifferentVersionNotice() {
//		Alert notice = new Alert();
//		HorizontalPanel noticeWithLink = new HorizontalPanel();
//		HTML startMessage = createStartMessage();
//		Anchor linkToCurrent = createLinkToCurrentVerion();
//
//		noticeWithLink.add(wrapWidget(startMessage, "margin-left-5"));
//		noticeWithLink.add(linkToCurrent);
//		notice.add(noticeWithLink);
//
//		if(canEdit) {
//			Button restoreButton = createRestoreButton();
//			notice.add(restoreButton);
//		}
//		return notice;
//	}

//	private HTML createStartMessage() {
//		SafeHtmlBuilder builder = new SafeHtmlBuilder();
//		String noticeStart = "You are viewing an old version of this page. View the ";
//		builder = new SafeHtmlBuilder();
//		builder.appendHtmlConstant(noticeStart);
//		HTML startMessage = new HTML(builder.toSafeHtml());
//		return startMessage;
//	}
//
//	private Anchor createLinkToCurrentVerion() {
//		Anchor linkToCurrent = new Anchor();
//		linkToCurrent.setHTML("current version.");
//		linkToCurrent.setStyleName("link", true);
//		linkToCurrent.setStyleName("margin-left-5", true);
//		linkToCurrent.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				presenter.reloadWikiPage();
//			}
//		});
//		return linkToCurrent;
//	}


//	private SimplePanel wrapWidget(Widget widget, String styleNames) {
//		SimplePanel widgetWrapper = new SimplePanel();
//		widgetWrapper.addStyleName(styleNames);
//		widgetWrapper.add(widget);
//		return widgetWrapper;
//	}

//	private Widget getBreadCrumbs() {
//		final SimplePanel breadcrumbsWrapper = new SimplePanel();
//		if (!isRootWiki) {
//			List<LinkData> links = new ArrayList<LinkData>();
//			if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.EVALUATION.toString())) {
//				//point to Home
//				links.add(new LinkData("Home", new Home(ClientProperties.DEFAULT_PLACE_TOKEN)));
//				breadcrumbsWrapper.add(breadcrumb.configure(links, null));
//			} else {
//				Place ownerObjectPlace = new Synapse(wikiKey.getOwnerObjectId());
//				links.add(new LinkData(ownerObjectName, ownerObjectPlace));
//				breadcrumbsWrapper.add(breadcrumb.configure(links, presenter.getWikiPage().getTitle()));
//			}
//			//TODO: support other object types.
//		}
//		return breadcrumbsWrapper;
//	}

//	private Button createHistoryButton() {
//		final Button btn = DisplayUtils.createIconButton(DisplayConstants.SHOW_WIKI_HISTORY, DisplayUtils.ButtonType.DEFAULT, null);			
//		btn.setStyleName("wikiHistoryButton", true);
//		btn.addClickHandler(new ClickHandler() {
//
//			@Override
//			public void onClick(ClickEvent event) {
//				if(!isHistoryOpen) {
//					// If history widget is already built, make it show
//					if(isHistoryWidgetBuilt) {
//						historyWidget.showHistoryWidget();
//					} else {
//						// Configure the history widget and built the history table
//						ActionHandler actionHandler = new ActionHandler() {
//							@Override
//							public void previewClicked(Long versionToPreview,
//									Long currentVersion) {
//								presenter.previewClicked(versionToPreview, currentVersion);
//							}
//							@Override
//							public void restoreClicked(Long versionToRestore) {
//								presenter.restoreClicked(versionToRestore);
//							}
//						};
//						historyWidget.configure(wikiKey, canEdit, actionHandler);
//						isHistoryWidgetBuilt = true;
//						Widget historyWidgetPanel = historyWidget.asWidget();
//						historyWidgetPanel.addStyleName("margin-top-10");
//						wikiPagePanel.add(historyWidgetPanel);
//						btn.setText(DisplayConstants.HIDE_WIKI_HISTORY);
//					}
//				} else {
//					// hide history
//					historyWidget.hideHistoryWidget();
//					btn.setText(DisplayConstants.SHOW_WIKI_HISTORY);
//				}
//				isHistoryOpen = !isHistoryOpen;
//			}
//		});
//		return btn;
//	}
	
	@Override
	public void setWikiHistoryWidget(IsWidget historyWidget) {
		wikiHistoryPanel.setWidget(historyWidget);
	}
	
	@Override
	public void showRestoreButton() {
		restoreButton.setVisible(true);
	}

//	private Button createRestoreButton() {
//		Button btn = DisplayUtils.createIconButton("Restore", DisplayUtils.ButtonType.DEFAULT, null);
//		btn.setStyleName("wikiHistoryButton", true);
//		btn.setStyleName("margin-top-10", true);
//		btn.addClickHandler(new ClickHandler() {
//
//			@Override
//			public void onClick(ClickEvent event) {
//				// If this button is used when viewing the current version for any reason, don't do anything
//				// Otherwise, raise warning to user for confirmation before restoring
//				if(!isCurrentVersion) {
//					// versionInView should be set if !isCurrentVersion
//					if(versionInView != null) {
//						showRestorationWarning(versionInView);
//					}
//				}
//			}
//		});
//		return btn;
//	}

	@Override
	public void setSynapseAlertWidget(IsWidget synapseAlert) {
		synAlertPanel.setWidget(synapseAlert);
	}
	
	@Override
	public void showLoading() {
		loadingPanel.setVisible(true);
	}
	
	@Override
	public void hideLoading() {
		loadingPanel.setVisible(false);
	}

	@Override
	public void setMarkdownWidget(IsWidget markdownWidget) {
		markdownPanel.setWidget(markdownWidget);
	}

	@Override
	public void setBreadcrumbWidget(IsWidget breadcrumb) {
		breadcrumbPanel.setWidget(breadcrumb);
	}
	
}
