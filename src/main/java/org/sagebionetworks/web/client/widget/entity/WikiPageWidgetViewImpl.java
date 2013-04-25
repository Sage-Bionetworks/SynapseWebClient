package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
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
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowser;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.shared.WikiPageKey;

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
public class WikiPageWidgetViewImpl extends LayoutContainer implements WikiPageWidgetView {
	
	private MarkdownWidget markdownWidget;
	private PagesBrowser pagesBrowser;
	private MarkdownEditorWidget markdownEditorWidget;
	private IconsImageBundle iconsImageBundle;
	private Button editButton, addPageButton;
	private HorizontalPanel commandBar;
	private SimplePanel commandBarWrapper;
	private Boolean canEdit;
	private WikiPage currentPage;
	private Breadcrumb breadcrumb;
	private boolean isEmbeddedInOwnerPage;
	private String ownerObjectName; //used for linking back to the owner object
	private WikiAttachments wikiAttachments;
	private int spanWidth;
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
	public WikiPageWidgetViewImpl(MarkdownWidget markdownWidget, PagesBrowser pagesBrowser, MarkdownEditorWidget markdownEditorWidget, IconsImageBundle iconsImageBundle, Breadcrumb breadcrumb, WikiAttachments wikiAttachments, WidgetRegistrar widgetRegistrar) {
		super();
		this.markdownWidget = markdownWidget;
		this.pagesBrowser = pagesBrowser;
		this.markdownEditorWidget = markdownEditorWidget;
		this.iconsImageBundle = iconsImageBundle;
		this.breadcrumb = breadcrumb;
		this.wikiAttachments = wikiAttachments;
		this.widgetRegistrar = widgetRegistrar;
	}
	
	@Override
	public void showNoWikiAvailableUI(){
		removeAll(true);
		SimplePanel createWikiButtonWrapper = new SimplePanel();
		createWikiButtonWrapper.addStyleName("span-24 notopmargin margin-bottom-20");
		createWikiButtonWrapper.add(getInsertPageButton(true));
		add(createWikiButtonWrapper);
		layout(true);
	}
	
	@Override
	public void configure(WikiPage newPage, WikiPageKey wikiKey,
			String ownerObjectName, Boolean canEdit, boolean isEmbeddedInOwnerPage, int spanWidth) {
		this.wikiKey = wikiKey;
		this.canEdit = canEdit;
		this.ownerObjectName = ownerObjectName;
		this.currentPage = newPage;
		this.isEmbeddedInOwnerPage = isEmbeddedInOwnerPage;
		this.spanWidth = spanWidth;
		
		String ownerHistoryToken = DisplayUtils.getSynapseHistoryToken(wikiKey.getOwnerObjectId());
		markdownWidget.setMarkdown(newPage.getMarkdown(), wikiKey, true, false);
		pagesBrowser.configure(wikiKey, ownerObjectName, ownerHistoryToken, DisplayConstants.PAGES, canEdit);
		showDefaultViewWithWiki();
	}
	
	@Override
	public void updateWikiPage(WikiPage newPage){
		currentPage = newPage;
	}
	
	private void showDefaultViewWithWiki() {
		removeAll(true);
		SimplePanel topBarWrapper = new SimplePanel();
		String hrString = isEmbeddedInOwnerPage ? "separator" : "";
		topBarWrapper.addStyleName("span-"+spanWidth + " margin-top-5 " + hrString);
		String titleString = isEmbeddedInOwnerPage ? "" : currentPage.getTitle();
		topBarWrapper.add(new HTMLPanel("<h2 class=\"span-"+(spanWidth-5)+"\" style=\"margin-bottom:0px;\">"+titleString+"</h2>"));
		add(topBarWrapper);
		
		FlowPanel mainPanel = new FlowPanel();
		mainPanel.addStyleName("span-"+spanWidth + " notopmargin");
		mainPanel.add(getBreadCrumbs(spanWidth));
		mainPanel.add(getCommands(canEdit));
		mainPanel.add(wrapWidget(markdownWidget.asWidget(), "span-"+spanWidth + " margin-top-5"));
		mainPanel.add(wrapWidget(pagesBrowser.asWidget(), "span-"+spanWidth+" notopmargin margin-bottom-10"));
		add(mainPanel);
		
		layout(true);
	}
	
	private SimplePanel wrapWidget(Widget widget, String styleNames) {
		SimplePanel widgetWrapper = new SimplePanel();
		widgetWrapper.addStyleName(styleNames);
		widgetWrapper.add(widget);
		return widgetWrapper;
	}
	
	private Widget getBreadCrumbs(int spanWidth) {
		final SimplePanel breadcrumbsWrapper = new SimplePanel();
		breadcrumbsWrapper.addStyleName("span-"+spanWidth+" notopmargin");
		if (!isEmbeddedInOwnerPage) {
			List<LinkData> links = new ArrayList<LinkData>();
			if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.EVALUATION.toString())) {
				//point to Home
				links.add(new LinkData("Home", new Home(DisplayUtils.DEFAULT_PLACE_TOKEN)));
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
			commandBarWrapper.addStyleName("margin-bottom-20 span-"+spanWidth + " margin-top-5");
			commandBar = new HorizontalPanel();
			commandBar.addStyleName("right");
			commandBar.setVerticalAlign(VerticalAlignment.MIDDLE);
			commandBar.setHorizontalAlign(HorizontalAlignment.LEFT);
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
		}
		
		commandBarWrapper.setVisible(canEdit);
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
		editButton.removeAllListeners();
		editButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				//change to edit mode
				removeAll(true);
				//create the editor textarea, and configure the editor widget
				final TextArea mdField = new TextArea();
				mdField.setValue(currentPage.getMarkdown());
				mdField.addStyleName("span-"+spanWidth);
				mdField.setHeight("400px");

				LayoutContainer form = new LayoutContainer();
				form.addStyleName("span-" + spanWidth);
				final TextBox titleField = new TextBox();
				if (!isEmbeddedInOwnerPage) {
					titleField.setValue(currentPage.getTitle());
					titleField.addStyleName("font-size-32 margin-left-10 margin-bottom-10 span-"+spanWidth);
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
				}, getCloseHandler(titleField, mdField), getManagementHandler(), spanWidth);
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
