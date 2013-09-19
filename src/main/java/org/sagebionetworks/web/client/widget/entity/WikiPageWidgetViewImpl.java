package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.CloseHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.ManagementHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Lightweight widget used to show a wiki page (has a markdown widget and pagebrowser)
 * 
 * @author Jay
 *
 */
public class WikiPageWidgetViewImpl extends LayoutContainer implements WikiPageWidgetView {
	
	private MarkdownWidget markdownWidget;
	private MarkdownEditorWidget markdownEditorWidget;
	private IconsImageBundle iconsImageBundle;
	private Button editButton, addPageButton;
	HandlerRegistration editButtonHandlerRegistration, addButtonHandlerRegistration; 
	private LayoutContainer commandBar;
	private SimplePanel commandBarWrapper;
	private Boolean canEdit;
	private WikiPage currentPage;
	private Breadcrumb breadcrumb;
	private boolean isEmbeddedInOwnerPage;
	private String ownerObjectName; //used for linking back to the owner object
	private WikiAttachments wikiAttachments;
	private int colWidth;
	private WikiPageKey wikiKey;
	private WidgetRegistrar widgetRegistrar;
	WikiPageWidgetView.Presenter presenter;
	
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
	public WikiPageWidgetViewImpl(MarkdownWidget markdownWidget, MarkdownEditorWidget markdownEditorWidget, IconsImageBundle iconsImageBundle, Breadcrumb breadcrumb, WikiAttachments wikiAttachments, WidgetRegistrar widgetRegistrar) {
		super();
		this.markdownWidget = markdownWidget;
		this.markdownEditorWidget = markdownEditorWidget;
		this.iconsImageBundle = iconsImageBundle;
		this.breadcrumb = breadcrumb;
		this.wikiAttachments = wikiAttachments;
		this.widgetRegistrar = widgetRegistrar;
	}
	
	@Override
	public void show404() {
		removeAll(true);
		add(new HTML(DisplayUtils.get404Html()));
		layout(true);
	}
	
	@Override
	public void show403() {
		removeAll(true);
		add(new HTML(DisplayUtils.get403Html()));
		layout(true);
	}
	
	@Override
	public void showNoWikiAvailableUI() {
		removeAll(true);
		SimplePanel createWikiButtonWrapper = new SimplePanel();
		createWikiButtonWrapper.addStyleName("margin-bottom-20");
		createWikiButtonWrapper.add(getInsertPageButton(true));
		add(createWikiButtonWrapper);
		layout(true);
	}
	
	@Override
	public void configure(WikiPage newPage, WikiPageKey wikiKey,
			String ownerObjectName, Boolean canEdit, boolean isEmbeddedInOwnerPage, int colWidth) {
		this.wikiKey = wikiKey;
		this.canEdit = canEdit;
		this.ownerObjectName = ownerObjectName;
		this.currentPage = newPage;
		this.isEmbeddedInOwnerPage = isEmbeddedInOwnerPage;
		this.colWidth = Math.round(colWidth/2);
		String ownerHistoryToken = DisplayUtils.getSynapseHistoryToken(wikiKey.getOwnerObjectId());
		markdownWidget.setMarkdown(newPage.getMarkdown(), wikiKey, true, false);
		showDefaultViewWithWiki();
	}
	
	@Override
	public void updateWikiPage(WikiPage newPage){
		currentPage = newPage;
	}
	
	private void showDefaultViewWithWiki() {
		removeAll(true);
		SimplePanel topBarWrapper = new SimplePanel();
		topBarWrapper.addStyleName("margin-top-5");
		String titleString = isEmbeddedInOwnerPage ? "" : currentPage.getTitle();
		topBarWrapper.add(new HTMLPanel("<h2 style=\"margin-bottom:0px;\">"+titleString+"</h2>"));
		add(topBarWrapper);
		
		FlowPanel mainPanel = new FlowPanel();
		mainPanel.add(getBreadCrumbs(colWidth));
		mainPanel.add(getCommands(canEdit));
		mainPanel.add(wrapWidget(markdownWidget.asWidget(), "margin-top-5"));
		add(mainPanel);
		
		layout(true);
	}
	
	private SimplePanel wrapWidget(Widget widget, String styleNames) {
		SimplePanel widgetWrapper = new SimplePanel();
		widgetWrapper.addStyleName(styleNames);
		widgetWrapper.add(widget);
		return widgetWrapper;
	}
	
	private Widget getBreadCrumbs(int colWidth) {
		final SimplePanel breadcrumbsWrapper = new SimplePanel();		
		if (!isEmbeddedInOwnerPage) {
			List<LinkData> links = new ArrayList<LinkData>();
			if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.EVALUATION.toString())) {
				//point to Home
				links.add(new LinkData("Home", new Home(ClientProperties.DEFAULT_PLACE_TOKEN)));
				breadcrumbsWrapper.add(breadcrumb.asWidget(links, null));
			} else {
				Place ownerObjectPlace = new Synapse(wikiKey.getOwnerObjectId());
				links.add(new LinkData(ownerObjectName, ownerObjectPlace));
				breadcrumbsWrapper.add(breadcrumb.asWidget(links, currentPage.getTitle()));
			}
			
			layout(true);
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
		}
			
		if(editButton == null) {			
			editButton = new Button(DisplayConstants.BUTTON_EDIT_WIKI);
			//, AbstractImagePrototype.create(iconsImageBundle.editGrey16())
			editButton.removeStyleName("gwt-Button");
			editButton.addStyleName("btn btn-default left display-inline");			
			editButton.getElement().setId(DisplayConstants.ID_BTN_EDIT);			
			commandBar.add(editButton, new MarginData(0, 5, 0, 0));			
		}
		
		if(addPageButton == null) {
			addPageButton = getInsertPageButton(false);
			addPageButton.addStyleName("display-inline");
			commandBar.add(addPageButton);
		}
		
		commandBarWrapper.setVisible(canEdit);
		configureEditButton();
		
		return commandBarWrapper;
	}
	
	private Button getInsertPageButton(final boolean isFirstPage) {
		String buttonText = isFirstPage ? DisplayConstants.CREATE_WIKI : DisplayConstants.ADD_PAGE;
		Button insertButton = new Button(buttonText);
		//AbstractImagePrototype.create(iconsImageBundle.addSquareGrey16())
		insertButton.removeStyleName("gwt-Button");
		insertButton.addStyleName("btn btn-default");			
		if(addButtonHandlerRegistration != null) addButtonHandlerRegistration.removeHandler();
		addButtonHandlerRegistration = insertButton.addClickHandler(new ClickHandler() {
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
		return insertButton;
	}
	
	private void configureEditButton() {
		if(editButtonHandlerRegistration != null) editButtonHandlerRegistration.removeHandler();
		editButtonHandlerRegistration = editButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				//change to edit mode
				removeAll(true);
				//create the editor textarea, and configure the editor widget
				final TextArea mdField = new TextArea();
				mdField.setValue(currentPage.getMarkdown());
				mdField.addStyleName("markdownEditor");
				mdField.setHeight("400px");
				
				LayoutContainer form = new LayoutContainer();
				final TextBox titleField = new TextBox();
				if (!isEmbeddedInOwnerPage) {
					titleField.setValue(currentPage.getTitle());
					titleField.addStyleName("font-size-32 margin-left-10 margin-bottom-10");
					titleField.setHeight("35px");
					
					form.add(titleField);
				}
				//also add commands at the bottom
				
				markdownEditorWidget.configure(wikiKey, mdField, form, false, true, new WidgetDescriptorUpdatedHandler() {
					@Override
					public void onUpdate(WidgetDescriptorUpdatedEvent event) {
						//update wiki attachments
						presenter.refreshWikiAttachments(titleField.getValue(), mdField.getValue(), null);
					}
				}, getCloseHandler(titleField, mdField), getManagementHandler(), colWidth);
				form.addStyleName("margin-bottom-40 margin-top-10");
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
						com.extjs.gxt.ui.client.widget.button.Button btn = be.getButtonClicked();
						if(Dialog.YES.equals(btn.getItemId())) {
							presenter.deleteButtonClicked();
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
	public void clear() {
		removeAll(true);
	}
}
