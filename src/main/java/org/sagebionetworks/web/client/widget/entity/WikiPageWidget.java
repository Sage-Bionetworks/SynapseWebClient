package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.CloseHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.ManagementHandler;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowser;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
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
public class WikiPageWidget extends LayoutContainer {
	
	private MarkdownWidget markdownWidget;
	private PagesBrowser pagesBrowser;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private Callback callback;
	private MarkdownEditorWidget markdownEditorWidget;
	private IconsImageBundle iconsImageBundle;
	private JSONObjectAdapter jsonObjectAdapter;
	private Button editButton, addPageButton;
	private HorizontalPanel commandBar;
	private SimplePanel commandBarWrapper;
	private WikiPageKey wikiKey;
	private Boolean canEdit;
	private WikiPage currentPage;
	private Breadcrumb breadcrumb;
	private boolean isEmbeddedInOwnerPage;
	private AdapterFactory adapterFactory;
	private String ownerObjectName, ownerHistoryToken; //used for linking back to the owner object
	private WikiAttachments wikiAttachments;
	private WidgetRegistrar widgetRegistrar;
	
	public interface Callback{
		public void pageUpdated();
	}
	
	public interface OwnerObjectNameCallback{
		public void ownerObjectNameInitialized();
	}
	
	
	@Inject
	public WikiPageWidget(SynapseClientAsync synapseClient, MarkdownWidget markdownWidget, PagesBrowser pagesBrowser, NodeModelCreator nodeModelCreator, MarkdownEditorWidget markdownEditorWidget, IconsImageBundle iconsImageBundle, JSONObjectAdapter jsonObjectAdapter, Breadcrumb breadcrumb, AdapterFactory adapterFactory, WikiAttachments wikiAttachments, WidgetRegistrar widgetRegistrar) {
		super();
		this.synapseClient = synapseClient;
		this.markdownWidget = markdownWidget;
		this.pagesBrowser = pagesBrowser;
		this.nodeModelCreator = nodeModelCreator;
		this.markdownEditorWidget = markdownEditorWidget;
		this.iconsImageBundle = iconsImageBundle;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.breadcrumb = breadcrumb;
		this.adapterFactory = adapterFactory;
		this.wikiAttachments = wikiAttachments;
		this.widgetRegistrar = widgetRegistrar;
	}
	
	public void configure(final WikiPageKey inWikiKey, final Boolean canEdit, Callback callback, boolean isEmbeddedInOwnerPage) {
		this.canEdit = canEdit;
		this.wikiKey = inWikiKey;
		this.isEmbeddedInOwnerPage = isEmbeddedInOwnerPage;
		this.removeAll(true);
		
		//set up callback
		if (callback != null)
			this.callback = callback;
		else 
			this.callback = new Callback() {
				@Override
				public void pageUpdated() {
				}
			};
		
		setOwnerObjectName(new OwnerObjectNameCallback() {
			@Override
			public void ownerObjectNameInitialized() {
				//get the wiki page
				synapseClient.getWikiPage(wikiKey, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						try {
							currentPage = nodeModelCreator.createJSONEntity(result, WikiPage.class);
							wikiKey.setWikiPageId(currentPage.getId());
							markdownWidget.setMarkdown(currentPage.getMarkdown(), wikiKey, false);
							pagesBrowser.configure(wikiKey, ownerObjectName, ownerHistoryToken, DisplayConstants.PAGES, canEdit);
							showDefaultViewWithWiki();
						} catch (JSONObjectAdapterException e) {
							onFailure(e);
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						//if it is because of a missing root (and we have edit permission), then the pages browser should have a Create Wiki button
						if (caught instanceof NotFoundException) {
							SimplePanel createWikiButtonWrapper = new SimplePanel();
							createWikiButtonWrapper.addStyleName("margin-bottom-20");
							createWikiButtonWrapper.add(getInsertPageButton(true));
							add(createWikiButtonWrapper);
							layout(true);
						}
						else {
							showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
						}
					}
				});				
			}
		});
	}
	
	private void showDefaultViewWithWiki() {
		removeAll(true);
		SimplePanel topBarWrapper = new SimplePanel();
		topBarWrapper.addStyleName("span-24 margin-top-5");
		HorizontalPanel topBar = new HorizontalPanel();
		String titleString = isEmbeddedInOwnerPage ? "" : currentPage.getTitle();
		topBar.add(new HTMLPanel("<h2 style=\"width:750px; margin-bottom:0px;\">"+titleString+"</h2>"));
		topBar.add(getCommands(canEdit));
		topBarWrapper.add(topBar);
		add(topBarWrapper);
		
		FlowPanel mainPanel = new FlowPanel();
		mainPanel.addStyleName("span-24 notopmargin");
		mainPanel.add(getBreadCrumbs());
		mainPanel.add(markdownWidget.asWidget());
		mainPanel.add(pagesBrowser.asWidget());
		
		add(mainPanel);
		layout(true);
	}
	
	private Widget getBreadCrumbs() {
		final SimplePanel breadcrumbsWrapper = new SimplePanel();
		breadcrumbsWrapper.addStyleName("span-24 notopmargin");
		if (!isEmbeddedInOwnerPage) {
			List<LinkData> links = new ArrayList<LinkData>();
			Place ownerObjectPlace = new Synapse(wikiKey.getOwnerObjectId());
			links.add(new LinkData(ownerObjectName, ownerObjectPlace));
			breadcrumbsWrapper.add(breadcrumb.asWidget(links, currentPage.getTitle()));
			layout(true);
			//TODO: support other object types.  
		}
		return breadcrumbsWrapper;
	}
	
	public void setOwnerObjectName(final OwnerObjectNameCallback callback) {
		if (wikiKey.getOwnerObjectType().equalsIgnoreCase(WidgetConstants.WIKI_OWNER_ID_ENTITY)) {
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
								ownerHistoryToken = DisplayUtils.getSynapseHistoryToken(wikiKey.getOwnerObjectId());
								ownerObjectName = theHeader.getName();
								callback.ownerObjectNameInitialized();
							}
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {					
						showErrorMessage(caught.getMessage());
					}
				});
			} catch (JSONObjectAdapterException e) {
				showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			}
		}
	}
	
	private SimplePanel getCommands(Boolean canEdit) {
		if (commandBarWrapper == null) {
			commandBarWrapper = new SimplePanel();
			commandBarWrapper.addStyleName("span-3 notopmargin");
			commandBar = new HorizontalPanel();
			commandBar.setVerticalAlign(VerticalAlignment.MIDDLE);
			commandBar.setHorizontalAlign(HorizontalAlignment.RIGHT);
			commandBarWrapper.add(commandBar);
		}
			
		if(editButton == null) {			
			editButton = new Button(DisplayConstants.BUTTON_EDIT_WIKI, AbstractImagePrototype.create(iconsImageBundle.editGrey16()));
			editButton.setId(DisplayConstants.ID_BTN_EDIT);
			editButton.setHeight(25);
			commandBar.add(editButton);
			commandBar.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));			
		}
		
		if(addPageButton == null) {
			addPageButton = getInsertPageButton(false);
			commandBar.add(addPageButton);
			commandBar.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));			
		}
		
		editButton.setEnabled(canEdit);
		addPageButton.setEnabled(canEdit);
		configureEditButton();
		
		return commandBarWrapper;
	}
	
	private Button getInsertPageButton(final boolean isFirstPage) {
		String buttonText = isFirstPage ? DisplayConstants.CREATE_WIKI : DisplayConstants.ADD_PAGE;
		Button insertButton = new Button(buttonText, AbstractImagePrototype.create(iconsImageBundle.addSquareGrey16()));
		insertButton.setWidth(115);
		insertButton.setHeight(25);
		insertButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (isFirstPage) {
					createPage(DisplayConstants.DEFAULT_ROOT_WIKI_NAME);
				}
				else {
					NameAndDescriptionEditorDialog.showNameDialog(DisplayConstants.LABEL_NAME, new NameAndDescriptionEditorDialog.Callback() {					
						@Override
						public void onSave(String name, String description) {
							createPage(name);
						}
					});
				}
			}
		});
		return insertButton;
	}
	
	private void configureEditButton() {
		editButton.removeAllListeners();
		editButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				//change to edit mode
				removeAll(true);
				//create the editor textarea, and configure the editor widget
				TextArea mdField = new TextArea();
				mdField.setValue(currentPage.getMarkdown());
				mdField.setWidth("910px");
				mdField.setHeight("400px");

				LayoutContainer form = new LayoutContainer();
				form.addStyleName("span-24");
				TextBox titleField = null;
				if (!isEmbeddedInOwnerPage) {
					titleField = new TextBox();
					titleField.setValue(currentPage.getTitle());
					titleField.addStyleName("font-size-32 margin-left-10 margin-bottom-10");
					titleField.setWidth("910px");
					titleField.setHeight("35px");
					
					form.add(titleField);
				}
				//also add commands at the bottom
				
				markdownEditorWidget.configure(wikiKey, mdField, form, false, new WidgetDescriptorUpdatedHandler() {
					@Override
					public void onUpdate(WidgetDescriptorUpdatedEvent event) {
					}
				}, getCloseHandler(titleField, mdField), getManagementHandler());
				form.addStyleName("margin-bottom-40");
				add(form);
				layout(true);
			}
		});		
	}

	private ManagementHandler getManagementHandler() {
		return new ManagementHandler() {
			@Override
			public void attachmentsClicked() {
				wikiAttachments.configure(wikiKey, currentPage, new WikiAttachments.Callback() {
					@Override
					public void attachmentDeleted(String fileName) {
						//when an attachment is deleted from the wiki attachments dialog, let's delete references from the markdown editor
						//delete previews, and image references
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
				showDialog(wikiAttachments);
			}
			@Override
			public void deleteClicked() {
				//delete wiki
				MessageBox.confirm(DisplayConstants.LABEL_DELETE + " Page",
						DisplayConstants.PROMPT_SURE_DELETE + " Page and Subpages?",
						new Listener<MessageBoxEvent>() {
					@Override
					public void handleEvent(MessageBoxEvent be) {
						Button btn = be.getButtonClicked();
						if(Dialog.YES.equals(btn.getItemId())) {
							synapseClient.deleteWikiPage(wikiKey, new AsyncCallback<Void>() {
								
								@Override
								public void onSuccess(Void result) {
									//go to the owner object
									breadcrumb.goTo(new Synapse(wikiKey.getOwnerObjectId()));
								}
								
								@Override
								public void onFailure(Throwable caught) {
									showErrorMessage(caught.getMessage());
								}
							});
						}
					}
				});
			}
		};
	}
	
	private CloseHandler getCloseHandler(final TextBox titleField, final TextArea mdField) {
		return new CloseHandler() {
			
			@Override
			public void saveClicked() {
				//before saving, we need to update the page first (widgets may have added/removed file handles from the list, like ImageConfigEditor)
				synapseClient.getWikiPage(wikiKey, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						try {
							currentPage = nodeModelCreator.createJSONEntity(result, WikiPage.class);
							wikiKey.setWikiPageId(currentPage.getId());
							//page updated, now apply our updates
							if (titleField != null)
								currentPage.setTitle(titleField.getValue());
							currentPage.setMarkdown(mdField.getValue());
							
							JSONObjectAdapter json = jsonObjectAdapter.createNew();
							try {
								currentPage.writeToJSONObject(json);
								synapseClient.updateWikiPage(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), json.toJSONString(), new AsyncCallback<String>() {
									@Override
									public void onSuccess(String result) {
										//showDefaultViewWithWiki();
										refresh();
									}
									@Override
									public void onFailure(Throwable caught) {
										showErrorMessage(caught.getMessage());
									}
								});
							} catch (JSONObjectAdapterException e) {
								showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
							}
							
						} catch (JSONObjectAdapterException e) {
							onFailure(e);
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
					}
				});
			}
			
			@Override
			public void cancelClicked() {
				showDefaultViewWithWiki();
			}
		};
	}

	public void createPage(final String name) {
		WikiPage page = new WikiPage();
		//if this is creating the root wiki, then refresh the full page
		final boolean isCreatingWiki = wikiKey.getWikiPageId() ==null;
		page.setParentWikiId(wikiKey.getWikiPageId());
		page.setTitle(name);
		String wikiPageJson;
		try {
			wikiPageJson = page.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createWikiPage(wikiKey.getOwnerObjectId(),  wikiKey.getOwnerObjectType(), wikiPageJson, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					if (isCreatingWiki)
						DisplayUtils.showInfo("Wiki Created", "");
					else
						DisplayUtils.showInfo("Page '" + name + "' Added", "");
					
					refresh();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					showErrorMessage(DisplayConstants.ERROR_PAGE_CREATION_FAILED);
				}
			});
		} catch (JSONObjectAdapterException e) {			
			showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
		}
	}
	
	
	private void refresh() {
		configure(wikiKey, canEdit, callback, isEmbeddedInOwnerPage);
	}
	
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
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
}
