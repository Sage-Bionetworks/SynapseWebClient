package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.BootstrapAlertType;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.CloseHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.ManagementHandler;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
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
	private MarkdownEditorWidget markdownEditorWidget;
	private IconsImageBundle iconsImageBundle;
//	private Button editButton, addPageButton; 
	private LayoutContainer commandBar;
	private SimplePanel commandBarWrapper;
	private Boolean canEdit;
	private Breadcrumb breadcrumb;
	private boolean isRootWiki;
	private String ownerObjectName; //used for linking back to the owner object
	private WikiAttachments wikiAttachments;
	private WikiPageKey wikiKey;
	private WidgetRegistrar widgetRegistrar;
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
	private boolean isAttachmentsWidgetConfigured;
	public interface Callback{
		public void pageUpdated();
	}
	
	public interface OwnerObjectNameCallback{
		public void ownerObjectNameInitialized();
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Inject
	public WikiPageWidgetViewImpl(MarkdownWidget markdownWidget, MarkdownEditorWidget markdownEditorWidget, 
			IconsImageBundle iconsImageBundle, Breadcrumb breadcrumb, WikiAttachments wikiAttachments,
			WidgetRegistrar widgetRegistrar, WikiHistoryWidget historyWidget, PortalGinInjector ginInjector) {
		super();
		this.markdownWidget = markdownWidget;
		this.markdownEditorWidget = markdownEditorWidget;
		this.iconsImageBundle = iconsImageBundle;
		this.breadcrumb = breadcrumb;
		this.wikiAttachments = wikiAttachments;
		this.widgetRegistrar = widgetRegistrar;
		this.historyWidget = historyWidget;
		this.ginInjector = ginInjector;
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
	public void showNoWikiAvailableUI(boolean isDescription) {
		clear();
		this.isDescription = isDescription;
		if (!isDescription) {
			SimplePanel createWikiButtonWrapper = new SimplePanel();		
			Button insertBtn = createInsertOrAddPageButton(true);		
			createWikiButtonWrapper.add(insertBtn);
			add(createWikiButtonWrapper);
		}
	}
	
	@Override
	public void configure(String markdown, final WikiPageKey wikiKey,
			String ownerObjectName, Boolean canEdit, boolean isRootWiki, boolean isDescription, 
			boolean isCurrentVersion, final Long versionInView, boolean isEmbeddedInOwnerPage) {
		this.wikiKey = wikiKey;
		this.canEdit = canEdit;
		this.isDescription = isDescription;
		this.ownerObjectName = ownerObjectName;
		this.isRootWiki = isRootWiki;
		this.isHistoryOpen = false;
		this.isHistoryWidgetBuilt = false;
		this.isCurrentVersion = isCurrentVersion;
		this.versionInView = versionInView;
		this.isEmbeddedInOwnerPage = isEmbeddedInOwnerPage;
		isAttachmentsWidgetConfigured = false;
		if(!isCurrentVersion) {
			markdownWidget.setMarkdown(markdown, wikiKey, true, false, versionInView);
		} else {
			markdownWidget.setMarkdown(markdown, wikiKey, true, false, null);
		}
		showDefaultViewWithWiki();
	}
	
	private void showDefaultViewWithWiki() {
		clear();
		if(!isCurrentVersion) {
			// Create warning that user is viewing a different version
			FlowPanel noticePanel = createDifferentVersionNotice();
			add(wrapWidget(noticePanel, "alert alert-"+BootstrapAlertType.WARNING.toString().toLowerCase()+" wikiVersionNotice"));
		}
		wikiPagePanel = new FlowPanel();
		//also add the wiki subpages widget, unless explicitly instructed not to in the markdown
		FlowPanel wikiSubpagesPanel = new FlowPanel();
		WikiSubpagesWidget widget = ginInjector.getWikiSubpagesRenderer();
		//subpages widget is special in that it applies styles to the markdown html panel (if there are subpages)
		widget.configure(wikiKey, new HashMap<String, String>(), null, wikiSubpagesPanel, wikiPagePanel, isEmbeddedInOwnerPage);
		wikiSubpagesPanel.add(widget.asWidget());
		add(wikiSubpagesPanel);
		add(wikiPagePanel);
		
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
		
		HorizontalPanel modifiedPanel = new HorizontalPanel();
		modifiedPanel.add(modifiedText);
		modifiedPanel.add(wrapWidget(modifiedBy.asWidget(), "padding-left-5"));
		modifiedPanel.add(modifiedOnText);
		
		HorizontalPanel createdPanel = new HorizontalPanel();
		createdPanel.add(createdText);
		createdPanel.add(wrapWidget(createdBy.asWidget(), "padding-left-5"));
		createdPanel.add(createdOnText);
		
		FlowPanel modifiedAndCreatedSection = new FlowPanel();
		modifiedAndCreatedSection.add(modifiedPanel);
		modifiedAndCreatedSection.add(createdPanel);
		modifiedAndCreatedSection.add(wrapWidget(createHistoryButton(), "margin-top-5"));
		return modifiedAndCreatedSection;
	}
	
	private FlowPanel createDifferentVersionNotice() {
		FlowPanel noticePanel = new FlowPanel();
		HorizontalPanel noticeWithLink = new HorizontalPanel();
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		
		ImageResource icon = iconsImageBundle.warning16();	
		builder.appendHtmlConstant(AbstractImagePrototype.create(icon).getHTML());
		HTML warningIcon = new HTML(builder.toSafeHtml());
		
		String noticeStart = "You are viewing an old version of this page. View the ";
		builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant(noticeStart);
		HTML startMessage = new HTML(builder.toSafeHtml());
		
		Anchor linkToCurrent = new Anchor();
		linkToCurrent.setHTML("current version.");
		linkToCurrent.setStyleName("link", true);
		linkToCurrent.setStyleName("margin-left-5", true);
		linkToCurrent.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.configure(wikiKey, canEdit, null, true);
			}
		});
		
		noticeWithLink.add(warningIcon);
		noticeWithLink.add(wrapWidget(startMessage, "margin-left-5"));
		noticeWithLink.add(linkToCurrent);
		noticePanel.add(noticeWithLink);

		if(canEdit) {
			Button restoreButton = createRestoreButton();
			noticePanel.add(restoreButton);
		}
		return noticePanel;
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
			commandBar = new LayoutContainer();
			commandBarWrapper.add(commandBar);
		} else {
			commandBar.removeAll();
		}
			
		Button editButton = createEditButton();			
		commandBar.add(editButton, new MarginData(0, 5, 0, 0));			
		
		if(!isDescription) {
			Button addPageButton = createInsertOrAddPageButton(false);
			commandBar.add(addPageButton);
		}
		
		commandBarWrapper.setVisible(canEdit);
		commandBar.layout(true);
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
	
	private Button createEditButton() {
		String editLabel = isDescription ? DisplayConstants.EDIT_DESCRIPTION : DisplayConstants.BUTTON_EDIT_WIKI;
		Button btn = DisplayUtils.createIconButton(editLabel, DisplayUtils.ButtonType.DEFAULT, "glyphicon-pencil");			
		btn.addStyleName("display-inline");			
		btn.getElement().setId(DisplayConstants.ID_BTN_EDIT);
		
		btn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if(isHistoryOpen) {
					historyWidget.hideHistoryWidget();
				}
				//change to edit mode
				clear();
				//inform presenter that edit was clicked
				presenter.editClicked();
				//create the editor textarea, and configure the editor widget
				final TextArea mdField = new TextArea();
				mdField.setValue(presenter.getWikiPage().getMarkdown());
				mdField.addStyleName("markdownEditor");
				
				LayoutContainer form = new LayoutContainer();
				final TextBox titleField = new TextBox();
				if (!isRootWiki) {
					titleField.setValue(presenter.getWikiPage().getTitle());
					titleField.addStyleName("font-size-32 margin-left-10 margin-bottom-10");
					titleField.setHeight("35px");					
					form.add(titleField);
				}
				//also add commands at the bottom
				markdownEditorWidget.configure(wikiKey, mdField, form, false, true, new WidgetDescriptorUpdatedHandler() {
					@Override
					public void onUpdate(WidgetDescriptorUpdatedEvent event) {
						presenter.addFileHandles(event.getNewFileHandleIds());
					}
				}, getCloseHandler(titleField, mdField), getManagementHandler());
				form.addStyleName("margin-bottom-40 margin-top-10");
				add(form);
			}
		});

		return btn;
	}

	private Button createInsertOrAddPageButton(final boolean isFirstPage) {
		Button btn = DisplayUtils.createIconButton(getInsertBtnText(isFirstPage), DisplayUtils.ButtonType.DEFAULT, "glyphicon-plus");
		btn.addStyleName("display-inline");
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isFirstPage) {
					presenter.createPage(DisplayConstants.DEFAULT_ROOT_WIKI_NAME);
				}
				else {
					NameAndDescriptionEditorDialog.showNameDialog(DisplayConstants.LABEL_NAME, new NameAndDescriptionEditorDialog.Callback() {					
						@Override
						public void onSave(String name, String description) {
							presenter.createPage(name);
						}
					});
				}
				
			}
		});
		return btn;
	}

	private String getInsertBtnText(final boolean isFirstPage) {
		String buttonText;
		if(isFirstPage) {
			buttonText = DisplayConstants.CREATE_WIKI;
		} else {
			buttonText = DisplayConstants.ADD_PAGE;
		}
		return buttonText;
	}
	
	private ManagementHandler getManagementHandler() {
		return new ManagementHandler() {
			@Override
			public void attachmentsClicked() {
				if (!isAttachmentsWidgetConfigured) {
					isAttachmentsWidgetConfigured = true;
					wikiAttachments.configure(wikiKey, presenter.getWikiPage(), new WikiAttachments.Callback() {
						@Override
						public void attachmentsToDelete(String fileName, List<String> fileHandleIds) {
							//when an attachment is deleted from the wiki attachments dialog, let's delete references from the markdown editor
							//delete previews, and image references
							presenter.removeFileHandles(fileHandleIds);
							Map<String, String> descriptor = new HashMap<String, String>();
							descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, fileName);
							try {
								String imageMD = WidgetRegistrarImpl.getWidgetMarkdown(WidgetConstants.IMAGE_CONTENT_TYPE, descriptor , widgetRegistrar);
								markdownEditorWidget.deleteMarkdown(imageMD);
								//works because AttachmentPreviewWidget looks for the same parameter ImageWidget
								String previewMD = WidgetRegistrarImpl.getWidgetMarkdown(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE, descriptor , widgetRegistrar);
								markdownEditorWidget.deleteMarkdown(previewMD);
							} catch (JSONObjectAdapterException e) {
							}
						}
						
						@Override
						public void attachmentClicked(String fileName) {
							//when an attachment is clicked in the wiki attachments dialog, let's add a reference in the markdown editor
							Map<String, String> descriptor = new HashMap<String, String>();
							descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, fileName);
							try {
								String previewMD = WidgetRegistrarImpl.getWidgetMarkdown(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE, descriptor , widgetRegistrar);
								markdownEditorWidget.insertMarkdown(previewMD);
							} catch (JSONObjectAdapterException e) {
							}						
						}
					});
				}
				showDialog(wikiAttachments);
			}
			@Override
			public void deleteClicked() {
				//delete wiki
				DisplayUtils.showConfirmDialog(
						DisplayConstants.LABEL_DELETE + " Page",
						DisplayConstants.PROMPT_SURE_DELETE + " Page and Subpages?",
						new org.sagebionetworks.web.client.utils.Callback() {
							@Override
							public void invoke() {
								presenter.deleteButtonClicked();
							}
						});
			}
		};
	}
	
	private CloseHandler getCloseHandler(final TextBox titleField, final TextArea mdField) {
		return new CloseHandler() {
			@Override
			public void saveClicked() {
				presenter.saveClicked(titleField.getValue(), mdField.getValue());
			}
			
			@Override
			public void cancelClicked() {
				presenter.cancelClicked();
			}
		};
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
		DisplayUtils.showOkCancelMessage(DisplayConstants.RESTORING_WIKI_VERSION_WARNING_TITLE, DisplayConstants.RESTORING_WIKI_VERSION_WARNING_MESSAGE, 
				MessagePopup.WARNING, 500, okCallback, cancelCallback);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	public static void showDialog(WikiAttachments wikiAttachments) {
        final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setSize(400, 400);
        window.setPlain(true); 
        window.setModal(true); 
        
        window.setHeading("Attachments"); 
        window.setButtons(Dialog.OK);
        window.setHideOnButtonClick(true);

        window.setLayout(new FitLayout());
        ScrollPanel scrollPanelWrapper = new ScrollPanel();
        scrollPanelWrapper.add(wikiAttachments.asWidget());
	    window.add(scrollPanelWrapper);
	    window.show();		
	}
	@Override
	public void showLoading() {
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
}
