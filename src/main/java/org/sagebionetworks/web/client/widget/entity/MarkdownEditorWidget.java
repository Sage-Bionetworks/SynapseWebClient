package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;

/**
 * Lightweight widget used to resolve markdown
 * 
 * @author Jay
 *
 */
public class MarkdownEditorWidget extends LayoutContainer {
	
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJSNIUtils;
	private WidgetRegistrar widgetRegistrar;
	private IconsImageBundle iconsImageBundle;
	BaseEditWidgetDescriptorPresenter widgetDescriptorEditor;
	CookieProvider cookies;
	private TextArea markdownTextArea;
	private HTML descriptionFormatInfo;
	private WikiPageKey wikiKey;
	private boolean isWikiEditor;
	private Image editWidgetButton;
	private WidgetDescriptorUpdatedHandler callback;
	private WidgetSelectionState widgetSelectionState;
	
	public interface CloseHandler{
		public void saveClicked();
		public void cancelClicked();
	}
	
	public interface ManagementHandler{
		public void attachmentsClicked();
		public void deleteClicked();
	}
	
	
	@Inject
	public MarkdownEditorWidget(SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar,
			IconsImageBundle iconsImageBundle,
			BaseEditWidgetDescriptorPresenter widgetDescriptorEditor,
			CookieProvider cookies) {
		super();
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.widgetRegistrar = widgetRegistrar;
		this.iconsImageBundle = iconsImageBundle;
		this.widgetDescriptorEditor = widgetDescriptorEditor;
		this.cookies = cookies;
		widgetSelectionState = new WidgetSelectionState();
	}
	
	/**
	 * 
	 * @param ownerId
	 * @param ownerType
	 * @param markdownTextArea
	 * @param formPanel
	 * @param callback
	 * @param saveHandler if no save handler is specified, then a Save button is not shown.  If it is specified, then Save is shown and saveClicked is called when that button is clicked.
	 */
	public void configure(final WikiPageKey wikiKey,
			final TextArea markdownTextArea, LayoutContainer formPanel,
			boolean showFieldLabel, final boolean isWikiEditor,
			final WidgetDescriptorUpdatedHandler callback,
			final CloseHandler saveHandler,
			final ManagementHandler managementHandler, int colWidth) {
		this.markdownTextArea = markdownTextArea;
		this.wikiKey = wikiKey;
		this.isWikiEditor = isWikiEditor;
		this.callback = callback;
		
		String formattingTipsHtml = WebConstants.SYNAPSE_MARKDOWN_FORMATTING_TIPS_HTML;
		descriptionFormatInfo = new HTML(formattingTipsHtml);
		//Toolbar
		HorizontalPanel mdCommands = new HorizontalPanel();
		mdCommands.addStyleName("view header-inner-commands-container");

		editWidgetButton = getNewCommand("Edit Widget", iconsImageBundle.editGrey16(),new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleEditWidgetCommand();
			}
		}); 
		mdCommands.add(editWidgetButton);
		
		Button insertButton = new Button("Insert", AbstractImagePrototype.create(iconsImageBundle.addSquareGrey16()));
		insertButton.setWidth(55);
		insertButton.setMenu(createWidgetMenu(callback));
		FormData descriptionLabelFormData = new FormData();
		descriptionLabelFormData.setMargins(new Margins(0,15,0,17));
		if (showFieldLabel)
			formPanel.add(new Label("Description:"),descriptionLabelFormData);
		FormData mdCommandFormData = new FormData();
		formPanel.add(mdCommands,mdCommandFormData);
		
		markdownTextArea.addStyleName("col-xs-12 col-sm-12 col-md-12 col-lg-12");
		markdownTextArea.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateEditWidget();
			}
		});
		
		markdownTextArea.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				updateEditWidget();
			}
		});
		
		// followed by description.
		SimplePanel descriptionWrapper= new SimplePanel();
		descriptionWrapper.add(markdownTextArea);
		
		FormData descriptionData = new FormData("-5");
		//descriptionData.setHeight(310);		
        formPanel.add(descriptionWrapper, descriptionData);
		
		//Preview
		final com.google.gwt.user.client.ui.Button previewButton =  new com.google.gwt.user.client.ui.Button();
		previewButton.removeStyleName("gwt-Button");
		previewButton.setText(DisplayConstants.ENTITY_DESCRIPTION_PREVIEW_BUTTON_TEXT);
		previewButton.addStyleName("btn btn-default");
		previewButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPreview(markdownTextArea.getValue(), isWikiEditor);
			}
		});
		
		FormData previewFormData = new FormData("-5");
		previewFormData.setMargins(new Margins(10,0,0,0));
		
		LayoutContainer overallRow = DisplayUtils.createRowContainer();
		LayoutContainer row = DisplayUtils.createRowContainer();		
		HorizontalPanel mdCommandsLowerLeft = new HorizontalPanel();
		mdCommandsLowerLeft.addStyleName("col-md-6");
		mdCommandsLowerLeft.setVerticalAlign(VerticalAlignment.MIDDLE);
		LayoutContainer mdCommandsLowerRight = new LayoutContainer();
		mdCommandsLowerRight.addStyleName("col-md-6");		
		row.add(mdCommandsLowerLeft);
		row.add(mdCommandsLowerRight);
		overallRow.add(row);
		formPanel.add(overallRow, previewFormData);		
		if (managementHandler != null) {
			final com.google.gwt.user.client.ui.Button deleteButton =  new com.google.gwt.user.client.ui.Button();
			deleteButton.removeStyleName("gwt-Button");
			deleteButton.setHTML(DisplayConstants.BUTTON_DELETE_WIKI);
			deleteButton.addStyleName("btn btn-danger");
			deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					managementHandler.deleteClicked();
				}
			});
			mdCommandsLowerLeft.add(deleteButton);
			mdCommandsLowerLeft.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
			final com.google.gwt.user.client.ui.Button attachmentsButton =  new com.google.gwt.user.client.ui.Button();
			attachmentsButton.removeStyleName("gwt-Button");
			attachmentsButton.setText(DisplayConstants.BUTTON_WIKI_ATTACHMENTS);
			attachmentsButton.addStyleName("btn btn-default");
			attachmentsButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					managementHandler.attachmentsClicked();
				}
			});
			mdCommandsLowerLeft.add(attachmentsButton);
			mdCommandsLowerLeft.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
		}
		mdCommandsLowerLeft.add(previewButton);

		if (saveHandler != null) {
			SimplePanel space = new SimplePanel();
			space.addStyleName("margin-left-35");
			mdCommandsLowerLeft.add(space);
			
			//also add a save button to the lower command bar
			final com.google.gwt.user.client.ui.Button saveButton =  new com.google.gwt.user.client.ui.Button();
			saveButton.removeStyleName("gwt-Button");
			saveButton.setText(DisplayConstants.SAVE_BUTTON_LABEL);
			saveButton.addStyleName("btn btn-primary right margin-right-5");
			saveButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					saveHandler.saveClicked();
				}
			});
						
			final com.google.gwt.user.client.ui.Button cancelButton =  new com.google.gwt.user.client.ui.Button();
			cancelButton.removeStyleName("gwt-Button");
			cancelButton.setText(DisplayConstants.BUTTON_CANCEL);
			cancelButton.addStyleName("btn btn-default right");
			cancelButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					saveHandler.cancelClicked();
				}
			});

			mdCommandsLowerRight.add(cancelButton);
			mdCommandsLowerRight.add(saveButton);
		}
		
		
		//Formatting Guide
		final Button formatLink = new Button(DisplayConstants.ENTITY_DESCRIPTION_TIPS_TEXT);
		formatLink.setIcon(AbstractImagePrototype.create(iconsImageBundle.slideInfo16()));
		formatLink.setWidth(120);
		mdCommands.add(formatLink);
		mdCommands.add(insertButton);
		formatLink.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				//pop up format guide
				showFormattingGuideDialog();
			}
		});
		
		Image image = getNewCommand("Insert Image", iconsImageBundle.imagePlus16(),new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleInsertWidgetCommand(WidgetConstants.IMAGE_CONTENT_TYPE, callback);
			}
		}); 
		mdCommands.add(image);
		
		if (isWikiEditor) {
			Image attachment = getNewCommand("Insert Attachment", iconsImageBundle.attachment16(),new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleInsertWidgetCommand(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE, callback);
				}
			}); 
			mdCommands.add(attachment);
		}

		
		Image link = getNewCommand("Insert Link", iconsImageBundle.link16(),new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleInsertWidgetCommand( WidgetConstants.LINK_CONTENT_TYPE, callback);
			}
		}); 
		mdCommands.add(link);
		
	}
	
	public void handleEditWidgetCommand() {
		if (widgetSelectionState != null && widgetSelectionState.isWidgetSelected()) {
			final int widgetStartIndex = widgetSelectionState.getWidgetStartIndex();
			final int widgetEndIndex = widgetSelectionState.getWidgetEndIndex();
			String innerText = widgetSelectionState.getInnerWidgetText();
			markdownTextArea.setSelectionRange(widgetStartIndex, innerText.length());
			String contentTypeKey = widgetRegistrar.getWidgetContentType(innerText);
			Map<String, String> widgetDescriptor = widgetRegistrar.getWidgetDescriptor(innerText);
			BaseEditWidgetDescriptorPresenter.editExistingWidget(widgetDescriptorEditor, wikiKey, contentTypeKey, widgetDescriptor, new WidgetDescriptorUpdatedHandler() {
				@Override
			public void onUpdate(WidgetDescriptorUpdatedEvent event) {
					//replace old widget text
					String text = markdownTextArea.getText();
					if (widgetStartIndex > -1 && widgetEndIndex > -1) {
						markdownTextArea.setText(text.substring(0, widgetStartIndex) + text.substring(widgetEndIndex));
						markdownTextArea.setCursorPos(widgetStartIndex);
						if (event.getInsertValue()!=null) {
							insertMarkdown(event.getInsertValue());
						}
						if (callback != null)
							callback.onUpdate(event);
					}
				}
			}, isWikiEditor);	
		}
	}
	
	public void updateEditWidget(){
		editWidgetButton.setResource(iconsImageBundle.editGrey16());
		DisplayUtils.updateWidgetSelectionState(widgetSelectionState, markdownTextArea.getText(), markdownTextArea.getCursorPos());
		 
		if (widgetSelectionState.isWidgetSelected()) {
			editWidgetButton.setResource(iconsImageBundle.edit16());
		}
	}
	
	public void showPreview(String descriptionMarkdown, final boolean isWiki) {
	    //get the html for the markdown
	    synapseClient.markdown2Html(descriptionMarkdown, true, DisplayUtils.isInTestWebsite(cookies), new AsyncCallback<String>() {
	    	@Override
			public void onSuccess(String result) {
	    		try {
					showPreviewHTML(result, isWiki);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				//preview failed
				showErrorMessage(DisplayConstants.PREVIEW_FAILED_TEXT + caught.getMessage());
			}
		});	
	}

	public void showPreviewHTML(String result, boolean isWiki) throws JSONObjectAdapterException {
		final Dialog window = new Dialog();
		window.setMaximizable(false);
	    window.setSize(650, 500);
	    window.setPlain(true);  
	    window.setModal(true);  
	    window.setHeading("Preview");
	    window.setLayout(new FitLayout());
	    window.setButtons(Dialog.OK);
	    window.setHideOnButtonClick(true);

		HTMLPanel panel;
		if(result == null || "".equals(result)) {
	    	panel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_DESCRIPTION + "</div>"));
		}
		else{
			panel = new HTMLPanel(result);
		}
		DisplayUtils.loadTableSorters(panel, synapseJSNIUtils);
		MarkdownWidget.loadMath(panel, synapseJSNIUtils, true);
		MarkdownWidget.loadWidgets(panel, wikiKey, isWiki, widgetRegistrar, synapseClient, iconsImageBundle, true);
		FlowPanel f = new FlowPanel();
		f.setStyleName("entity-description-preview-wrapper");
		f.add(panel);
		window.add(new ScrollPanel(f));
		window.show();
	}
	
	public void showFormattingGuideDialog() {
        final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setSize(550, 600);
        window.setPlain(true); 
        window.setModal(true); 

        window.setHeading(DisplayConstants.ENTITY_DESCRIPTION_TIPS_TEXT); 
        window.setButtons(Dialog.OK);
        window.setHideOnButtonClick(true);

        window.setLayout(new FitLayout());
        ScrollPanel wrapper = new ScrollPanel();
        wrapper.add(descriptionFormatInfo);
	    window.add(wrapper);
        // show the window
	    window.show();		
	}

	private Menu createWidgetMenu(final WidgetDescriptorUpdatedHandler callback) {
	    Menu menu = new Menu();
	    menu.setEnableScrolling(false);
	    if (isWikiEditor) {
		    menu.add(getNewCommand("Attachment", new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		handleInsertWidgetCommand(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE, callback);
		    	};
			}));
	    }
	    
	    menu.add(getNewCommand(WidgetConstants.BUTTON_LINK_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.BUTTON_LINK_CONTENT_TYPE, callback);
	    	};
		}));

	    menu.add(getNewCommand(WidgetConstants.ENTITYLIST_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    	@Override
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.ENTITYLIST_CONTENT_TYPE, callback);
	    	}
	    }));	    
	    menu.add(getNewCommand(WidgetConstants.IMAGE_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.IMAGE_CONTENT_TYPE, callback);
	    	};
		}));
	    menu.add(getNewCommand("Link", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.LINK_CONTENT_TYPE, callback);
	    	};
		}));

	    menu.add(getNewCommand(WidgetConstants.PROVENANCE_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.PROVENANCE_CONTENT_TYPE, callback);
	    	};
		}));

	    menu.add(getNewCommand(WidgetConstants.QUERY_TABLE_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.QUERY_TABLE_CONTENT_TYPE, callback);
	    	};
		}));

	    menu.add(getNewCommand(WidgetConstants.REFERENCE_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.REFERENCE_CONTENT_TYPE, callback);
	    	};
	    }));

	    menu.add(getNewCommand(WidgetConstants.TABBED_TABLE_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.TABBED_TABLE_CONTENT_TYPE, callback);
	    	};
		}));
    	menu.add(getNewCommand("Table Of Contents", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.TOC_CONTENT_TYPE + WidgetConstants.WIDGET_END_MARKDOWN);
	    	};
		}));
    	menu.add(getNewCommand("YouTube Video", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.YOUTUBE_CONTENT_TYPE, callback);	
	    	};
		}));
    	menu.add(getNewCommand("Wiki Pages", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		insertMarkdown(SharedMarkdownUtils.getWikiSubpagesMarkdown());
	    	};
		}));
	    
	    
	    /**
	     * load alpha test site widgets
	     */
	    if (DisplayUtils.isInTestWebsite(cookies)) {
	    	menu.add(new SeparatorMenuItem());

	    	menu.add(getNewCommand(WidgetConstants.BOOKMARK_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    		public void componentSelected(ComponentEvent ce) {
	    			handleInsertWidgetCommand(WidgetConstants.BOOKMARK_CONTENT_TYPE, callback);
	    		}
	    	}));

	    	menu.add(getNewCommand(WidgetConstants.SHINYSITE_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		handleInsertWidgetCommand(WidgetConstants.SHINYSITE_CONTENT_TYPE, callback);
		    	};
			}));
	    	menu.add(getNewCommand("Synapse API SuperTable", new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		handleInsertWidgetCommand(WidgetConstants.API_TABLE_CONTENT_TYPE, callback);
		    	};
			}));
	    	menu.add(getNewCommand("Wiki Files Preview", new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.WIKI_FILES_PREVIEW_CONTENT_TYPE + WidgetConstants.WIDGET_END_MARKDOWN);
		    	};
			}));
	    	menu.add(getNewCommand("Join Evaluation Button", new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.JOIN_EVALUATION_CONTENT_TYPE + "?"+WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY+"=evalId1,evalId2&" +WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY + "=42" + WidgetConstants.WIDGET_END_MARKDOWN);
		    	};
			}));
	    	menu.add(getNewCommand(WidgetConstants.TUTORIAL_WIZARD_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.TUTORIAL_WIZARD_CONTENT_TYPE + "?"+WidgetConstants.WIDGET_ENTITY_ID_KEY+"=syn123&" +WidgetConstants.TEXT_KEY + "=Tutorial"+ WidgetConstants.WIDGET_END_MARKDOWN);
		    	};
			}));

	    	
	    }

	    return menu;
	  }
	
	public void handleInsertWidgetCommand(String contentTypeKey, final WidgetDescriptorUpdatedHandler callback){
		BaseEditWidgetDescriptorPresenter.editNewWidget(widgetDescriptorEditor, wikiKey, contentTypeKey, new WidgetDescriptorUpdatedHandler() {
			@Override
		public void onUpdate(WidgetDescriptorUpdatedEvent event) {
			if (event.getInsertValue()!=null) {
				insertMarkdown(event.getInsertValue());
			}
			callback.onUpdate(event);
		}
		}, isWikiEditor);
	}
	
	public void insertMarkdown(String md) {
		String currentValue = markdownTextArea.getValue();
		if (currentValue == null)
			currentValue = "";
		
		int cursorPos = markdownTextArea.getCursorPos();
		if (cursorPos < 0 || cursorPos > currentValue.length())
			cursorPos = currentValue.length();
		DisplayUtils.updateTextArea(markdownTextArea, currentValue.substring(0, cursorPos) + md + currentValue.substring(cursorPos));
		//SWC-406: set cursor to after the current markdown
		markdownTextArea.setCursorPos(cursorPos + md.length());
	}
	
	/**
	 * Deletes all instances of the given markdown from the editor
	 * @param md
	 */
	public void deleteMarkdown(String md) {
		//replace all instances of the md with the empty string
		StringBuilder newValue = new StringBuilder(markdownTextArea.getValue());
        
		int idx = 0;
        while((idx = newValue.indexOf(md, idx)) != -1) {
            newValue.replace(idx, idx + md.length(), "");
        }
		
		DisplayUtils.updateTextArea(markdownTextArea, newValue.toString());
	}

	
	public Image getNewCommand(String tooltipText, ImageResource image, ClickHandler clickHandler){
		Image command = new Image(image);
		command.addStyleName("imageButton");
		command.addClickHandler(clickHandler);
		DisplayUtils.addTooltip(this.synapseJSNIUtils, command, tooltipText, TOOLTIP_POSITION.BOTTOM);
		return command;
	}
	
	public MenuItem getNewCommand(String text, SelectionListener selectionListener){
		MenuItem item = new MenuItem(text);
		item.addSelectionListener(selectionListener);
		return item;
	}
	
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
