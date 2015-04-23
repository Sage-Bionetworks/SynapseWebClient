package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

	private MarkdownWidget markdownWidget;
	private SageImageBundle sageImageBundle;
	private FlowPanel commandBar;
	private SimplePanel commandBarWrapper;
	private Boolean canEdit;
	private Breadcrumb breadcrumb;
	private boolean isRootWiki;
	private String ownerObjectName; //used for linking back to the owner object
	private WikiPageKey wikiKey;
	WikiPageWidgetView.Presenter presenter;
	private boolean isDescription = false;
	private WikiHistoryWidget historyWidget;
	PortalGinInjector ginInjector;
	private boolean isHistoryOpen;
	private boolean isHistoryWidgetBuilt;
	private boolean isCurrentVersion;
	private Long versionInView;
	private FlowPanel wikiPagePanel;
	private boolean isEmbeddedInOwnerPage;
	private HorizontalPanel modifiedPanel, createdPanel;
	private SimplePanel historyPanel;

	public interface OwnerObjectNameCallback{
		public void ownerObjectNameInitialized();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Inject
	public WikiPageWidgetViewImpl(MarkdownWidget markdownWidget, Breadcrumb breadcrumb,
			WikiHistoryWidget historyWidget, PortalGinInjector ginInjector, SageImageBundle sageImageBundle) {
		super();
		this.markdownWidget = markdownWidget;
		this.sageImageBundle = sageImageBundle;
		this.breadcrumb = breadcrumb;
		this.historyWidget = historyWidget;
		this.ginInjector = ginInjector;
		modifiedPanel = new HorizontalPanel();
		createdPanel = new HorizontalPanel();
		historyPanel = new SimplePanel();
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
	public void showWarningMessageInPage(String message) {
		clear();
		add(new Alert(message, AlertType.WARNING));
	}

	@Override
	public void configure(String markdown, final WikiPageKey wikiKey,
			String ownerObjectName, Boolean canEdit, boolean isRootWiki,
			boolean isCurrentVersion, final Long versionInView, boolean isEmbeddedInOwnerPage) {
		this.ownerObjectName = ownerObjectName;
		this.canEdit = canEdit;
		this.isEmbeddedInOwnerPage = isEmbeddedInOwnerPage;
		this.wikiPagePanel = new FlowPanel();
		resetWikiMarkdown(markdown, wikiKey, isRootWiki, isCurrentVersion, versionInView);
		showDefaultViewWithWiki();
	}

	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	public void showRestorationWarning(final Long wikiVersion) {
		org.sagebionetworks.web.client.utils.Callback okCallback = new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
				presenter.restoreClicked(wikiVersion);
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
	public void showLoading() {
		clear();
		add(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showCreatedBy(boolean isVisible) {
		createdPanel.setVisible(isVisible);
	}

	@Override
	public void showModifiedBy(boolean isVisible) {
		modifiedPanel.setVisible(isVisible);
	}

	@Override
	public void showWikiHistory(boolean isVisible) {
		historyPanel.setVisible(isVisible);
	}

	@Override
	public void resetWikiMarkdown(String markdown, final WikiPageKey wikiKey,
			boolean isRootWiki, boolean isCurrentVersion, final Long versionInView) {
		this.wikiKey = wikiKey;
		this.isRootWiki = isRootWiki;
		this.isHistoryOpen = false;
		this.isHistoryWidgetBuilt = false;
		this.isCurrentVersion = isCurrentVersion;
		this.versionInView = versionInView;
		if(!isCurrentVersion) {
			markdownWidget.setMarkdown(markdown, wikiKey, false, versionInView);
		} else {
			markdownWidget.setMarkdown(markdown, wikiKey, false, null);
		}
		resetWikiPagePanel();
	}

	private void resetWikiPagePanel() {
		wikiPagePanel.clear();
		if(!isCurrentVersion) {
			// Create warning that user is viewing a different version
			Alert notice = createDifferentVersionNotice();
			wikiPagePanel.add(notice);
		}

		wikiPagePanel.add(getBreadCrumbs());
		SimplePanel topBarWrapper = new SimplePanel();
		String titleString = isRootWiki ? "" : presenter.getWikiPage().getTitle();
		topBarWrapper.add(new HTMLPanel("<h2 style=\"margin-bottom:0px;\">"+titleString+"</h2>"));
		wikiPagePanel.add(topBarWrapper);

		FlowPanel mainPanel = new FlowPanel();
		if(isCurrentVersion) {
			mainPanel.add(getCommands(canEdit));
		}
		mainPanel.add(wrapWidget(markdownWidget.asWidget(), "margin-top-5"));
		wikiPagePanel.add(mainPanel);

		FlowPanel modifiedCreatedSection = createdModifiedCreatedSection();
		wikiPagePanel.add(wrapWidget(modifiedCreatedSection, "margin-top-10 clearleft"));
	}

	/*
	 *     ________
	 *     | Wiki |
	 * ___________________________________________________________
	 * | ___________ ___________________________________________ |
	 * | | 2       | | _______________________________________ | |
	 * | |         | | | Notice                              | | |
	 * | |         | | |_____________________________________| | |
	 * | |_________| |  Wiki content                           | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             | Modified/Created by                     | |
	 * |             | Show Wiki History                     3 | |
	 * | 1           |_________________________________________| |
	 * |_________________________________________________________|
	 * 
	 * 1 - this widget
	 * 2 - WikiSubpagesWidget widget
	 * 3 - wikiPagePanel
	 */
	private void showDefaultViewWithWiki() {
		clear();

		//also add the wiki subpages widget, unless explicitly instructed not to in the markdown
		FlowPanel wikiSubpagesPanel = new FlowPanel();
		WikiSubpagesWidget widget = ginInjector.getWikiSubpagesRenderer();
		//subpages widget is special in that it applies styles to the markdown html panel (if there are subpages)
		wikiSubpagesPanel.add(widget.asWidget());
		add(wikiSubpagesPanel);
		add(wikiPagePanel);
		widget.configure(wikiKey, new HashMap<String, String>(), null, wikiSubpagesPanel, wikiPagePanel, isEmbeddedInOwnerPage, presenter.getReloadWikiPageCallback());
	}

	private FlowPanel createdModifiedCreatedSection() {
		// Add created/modified information at the end
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(DisplayConstants.MODIFIED_BY + " ");
		HTML modifiedText = new HTML(shb.toSafeHtml());

		shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(" " + DisplayConstants.ON + " " + DisplayUtils.converDataToPrettyString(presenter.getWikiPage().getModifiedOn()));
		HTML modifiedOnText = new HTML(shb.toSafeHtml());

		shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(DisplayConstants.CREATED_BY + " ");
		HTML createdText = new HTML(shb.toSafeHtml());

		shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(" " + DisplayConstants.ON + " " + DisplayUtils.converDataToPrettyString(presenter.getWikiPage().getCreatedOn()));		
		HTML createdOnText = new HTML(shb.toSafeHtml());

		UserBadge modifiedBy = ginInjector.getUserBadgeWidget();
		modifiedBy.configure(presenter.getWikiPage().getModifiedBy());

		UserBadge createdBy = ginInjector.getUserBadgeWidget();
		createdBy.configure(presenter.getWikiPage().getCreatedBy());

		modifiedPanel.clear();
		modifiedPanel.add(modifiedText);
		modifiedPanel.add(modifiedBy.asWidget());
		modifiedPanel.add(modifiedOnText);

		createdPanel.clear();
		createdPanel.add(createdText);
		createdPanel.add(createdBy.asWidget());
		createdPanel.add(createdOnText);

		FlowPanel modifiedAndCreatedSection = new FlowPanel();
		modifiedAndCreatedSection.add(modifiedPanel);
		modifiedAndCreatedSection.add(createdPanel);

		historyPanel.clear();
		historyPanel.add(wrapWidget(createHistoryButton(), "margin-top-5"));
		modifiedAndCreatedSection.add(historyPanel);
		return modifiedAndCreatedSection;
	}

	private Alert createDifferentVersionNotice() {
		Alert notice = new Alert();
		HorizontalPanel noticeWithLink = new HorizontalPanel();
		HTML startMessage = createStartMessage();
		Anchor linkToCurrent = createLinkToCurrentVerion();

		noticeWithLink.add(wrapWidget(startMessage, "margin-left-5"));
		noticeWithLink.add(linkToCurrent);
		notice.add(noticeWithLink);

		if(canEdit) {
			Button restoreButton = createRestoreButton();
			notice.add(restoreButton);
		}
		return notice;
	}

	private HTML createStartMessage() {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		String noticeStart = "You are viewing an old version of this page. View the ";
		builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant(noticeStart);
		HTML startMessage = new HTML(builder.toSafeHtml());
		return startMessage;
	}

	private Anchor createLinkToCurrentVerion() {
		Anchor linkToCurrent = new Anchor();
		linkToCurrent.setHTML("current version.");
		linkToCurrent.setStyleName("link", true);
		linkToCurrent.setStyleName("margin-left-5", true);
		linkToCurrent.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.reloadWikiPage();
			}
		});
		return linkToCurrent;
	}

	private SimplePanel wrapWidget(Widget widget, String styleNames) {
		SimplePanel widgetWrapper = new SimplePanel();
		widgetWrapper.addStyleName(styleNames);
		widgetWrapper.add(widget);
		return widgetWrapper;
	}

	private Widget getBreadCrumbs() {
		final SimplePanel breadcrumbsWrapper = new SimplePanel();
		if (!isRootWiki) {
			List<LinkData> links = new ArrayList<LinkData>();
			if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.EVALUATION.toString())) {
				//point to Home
				links.add(new LinkData("Home", new Home(ClientProperties.DEFAULT_PLACE_TOKEN)));
				breadcrumbsWrapper.add(breadcrumb.asWidget(links, null));
			} else {
				Place ownerObjectPlace = new Synapse(wikiKey.getOwnerObjectId());
				links.add(new LinkData(ownerObjectName, ownerObjectPlace));
				breadcrumbsWrapper.add(breadcrumb.asWidget(links, presenter.getWikiPage().getTitle()));
			}
			//TODO: support other object types.
		}
		return breadcrumbsWrapper;
	}

	private SimplePanel getCommands(Boolean canEdit) {
		if (commandBarWrapper == null) {
			commandBarWrapper = new SimplePanel();
			commandBarWrapper.addStyleName("margin-bottom-20 margin-top-10");
			commandBar = new FlowPanel();
			commandBarWrapper.add(commandBar);
		} else {
			commandBar.clear();
		}

		if(!isDescription) {
			Button addPageButton = createAddPageButton();
			commandBar.add(addPageButton);
			addPageButton.addStyleName("margin-left-5");
		}

		commandBarWrapper.setVisible(canEdit);
		return commandBarWrapper;
	}

	private Button createHistoryButton() {
		final Button btn = DisplayUtils.createIconButton(DisplayConstants.SHOW_WIKI_HISTORY, DisplayUtils.ButtonType.DEFAULT, null);			
		btn.setStyleName("wikiHistoryButton", true);
		btn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(!isHistoryOpen) {
					// If history widget is already built, make it show
					if(isHistoryWidgetBuilt) {
						historyWidget.showHistoryWidget();
					} else {
						// Configure the history widget and built the history table
						ActionHandler actionHandler = new ActionHandler() {
							@Override
							public void previewClicked(Long versionToPreview,
									Long currentVersion) {
								presenter.previewClicked(versionToPreview, currentVersion);
							}
							@Override
							public void restoreClicked(Long versionToRestore) {
								presenter.restoreClicked(versionToRestore);
							}
						};
						historyWidget.configure(wikiKey, canEdit, actionHandler);
						isHistoryWidgetBuilt = true;
						Widget historyWidgetPanel = historyWidget.asWidget();
						historyWidgetPanel.addStyleName("margin-top-10");
						wikiPagePanel.add(historyWidgetPanel);
						btn.setText(DisplayConstants.HIDE_WIKI_HISTORY);
					}
				} else {
					// hide history
					historyWidget.hideHistoryWidget();
					btn.setText(DisplayConstants.SHOW_WIKI_HISTORY);
				}
				isHistoryOpen = !isHistoryOpen;
			}
		});
		return btn;
	}

	private Button createRestoreButton() {
		Button btn = DisplayUtils.createIconButton("Restore", DisplayUtils.ButtonType.DEFAULT, null);
		btn.setStyleName("wikiHistoryButton", true);
		btn.setStyleName("margin-top-10", true);
		btn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
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
		return btn;
	}

	private Button createAddPageButton() {
		Button btn = DisplayUtils.createIconButton(DisplayConstants.ADD_PAGE, DisplayUtils.ButtonType.DEFAULT, "glyphicon-plus");
		btn.addStyleName("display-inline");
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Bootbox.prompt(DisplayConstants.ENTER_PAGE_TITLE, new PromptCallback() {
					@Override
					public void callback(String name) {
						presenter.createPage(name);
					}
				});
			}
		});
		return btn;
	}
}
