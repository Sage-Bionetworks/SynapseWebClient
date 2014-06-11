package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;

import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.CloseHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.ManagementHandler;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
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
public class MarkdownEditorWidgetViewImpl extends FlowPanel implements MarkdownEditorWidgetView {
	
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJSNIUtils;
	private WidgetRegistrar widgetRegistrar;
	private IconsImageBundle iconsImageBundle;
	BaseEditWidgetDescriptorPresenter widgetDescriptorEditor;
	private CookieProvider cookies;
	private TextArea markdownTextArea;
	private WikiPageKey wikiKey;
	private boolean isWikiEditor;
	private com.google.gwt.user.client.ui.Button editWidgetButton;
	private WidgetDescriptorUpdatedHandler callback;
	private WidgetSelectionState widgetSelectionState;
	private ResourceLoader resourceLoader;
	private MarkdownWidget markdownWidget;
	private Presenter presenter;
	
	@Inject
	public MarkdownEditorWidgetViewImpl(SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar,
			IconsImageBundle iconsImageBundle,
			BaseEditWidgetDescriptorPresenter widgetDescriptorEditor,
			CookieProvider cookies,
			ResourceLoader resourceLoader, 
			MarkdownWidget markdownWidget) {
		super();
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.widgetRegistrar = widgetRegistrar;
		this.iconsImageBundle = iconsImageBundle;
		this.widgetDescriptorEditor = widgetDescriptorEditor;
		this.cookies = cookies;
		this.resourceLoader = resourceLoader;
		this.markdownWidget = markdownWidget;
		markdownWidget.addStyleName("margin-10");
		widgetSelectionState = new WidgetSelectionState();
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
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
			WikiPageKey formattingGuideWikiPageKey,
			final TextArea markdownTextArea, 
			LayoutContainer formPanel,
			boolean showFieldLabel, 
			final boolean isWikiEditor,
			final WidgetDescriptorUpdatedHandler callback,
			final CloseHandler saveHandler,
			final ManagementHandler managementHandler) {
		if (formattingGuideWikiPageKey != null)
			initFormattingGuide(formattingGuideWikiPageKey);
		this.markdownTextArea = markdownTextArea;
		resizeMarkdownTextArea();
		this.wikiKey = wikiKey;
		this.isWikiEditor = isWikiEditor;
		this.callback = callback;
		
		//Toolbar
		final HorizontalPanel mdCommands = new HorizontalPanel();
		mdCommands.setSpacing(2);
		mdCommands.addStyleName("view markdown-editor-commands-container");
		editWidgetButton = getNewCommand("Edit Widget", "glyphicon-pencil",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleEditWidgetCommand();
			}
		}); 
		mdCommands.add(editWidgetButton);
		
		Button insertButton = new Button("Insert", AbstractImagePrototype.create(iconsImageBundle.glyphCirclePlus16()));
		
		insertButton.addStyleName("whiteBackgroundGxt");
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
				resizeMarkdownTextArea();
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
				presenter.showPreview(markdownTextArea.getValue(), isWikiEditor);
			}
		});
		
		FormData previewFormData = new FormData("-5");
		previewFormData.setMargins(new Margins(10,0,0,0));
		
		LayoutContainer overallRow = DisplayUtils.createRowContainer();
		LayoutContainer row = DisplayUtils.createRowContainer();
		FlowPanel commands = new FlowPanel();
		commands.addStyleName("col-md-12");
		row.add(commands);
		overallRow.add(row);
		formPanel.add(overallRow, previewFormData);
		final com.google.gwt.user.client.ui.Button deleteButton =  DisplayUtils.createButton(DisplayConstants.BUTTON_DELETE_WIKI, ButtonType.DANGER);
		final com.google.gwt.user.client.ui.Button attachmentsButton =  DisplayUtils.createButton(DisplayConstants.BUTTON_WIKI_ATTACHMENTS, ButtonType.DEFAULT);
		boolean canManage = managementHandler != null;
		if (canManage) {
			deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					managementHandler.deleteClicked();
				}
			});
			
			attachmentsButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					managementHandler.attachmentsClicked();
				}
			});
		}
		final com.google.gwt.user.client.ui.Button saveButton =  DisplayUtils.createButton(DisplayConstants.SAVE_BUTTON_LABEL, ButtonType.PRIMARY);
		final com.google.gwt.user.client.ui.Button cancelButton = DisplayUtils.createButton(DisplayConstants.BUTTON_CANCEL, ButtonType.DEFAULT);
		boolean canSave = saveHandler != null;
		if (canSave) {
			saveButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					DisplayUtils.changeButtonToSaving(saveButton);
					saveHandler.saveClicked();
				}
			});
						
			cancelButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					saveHandler.cancelClicked();
				}
			});
		}
		
		//add to container
		//save, attachments, preview
		if (canSave) {
			saveButton.addStyleName("margin-right-5");
			commands.add(saveButton);
		}
			
		if (canManage) {
			attachmentsButton.addStyleName("margin-right-5");
			commands.add(attachmentsButton);
		}
		previewButton.addStyleName("margin-right-5");
		commands.add(previewButton);
		
		//then cancel, delete
		if (canManage) {
			deleteButton.addStyleName("pull-right");
			commands.add(deleteButton);
		}
		if (canSave) {
			cancelButton.addStyleName("pull-right margin-right-5");
			commands.add(cancelButton);
		}
		
		
		//Formatting Guide
		com.google.gwt.user.client.ui.Button formatLink = getNewCommand(DisplayConstants.ENTITY_DESCRIPTION_TIPS_TEXT, null, "glyphicon-question-sign", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//pop up format guide
				showFormattingGuideDialog();
			}
		});
		mdCommands.add(formatLink);
		mdCommands.add(insertButton);
		
		//basic commands
		String startTag = "**";
		String endTag = startTag;
		com.google.gwt.user.client.ui.Button boldCommand = getNewCommand("Bold", "glyphicon-bold", getBasicCommandClickHandler(startTag, endTag, false));
		boldCommand.addStyleName("margin-left-10");
		mdCommands.add(boldCommand);

		startTag = "_";
		endTag = startTag;
		com.google.gwt.user.client.ui.Button italicCommand = getNewCommand("Italicize", "glyphicon-italic",  getBasicCommandClickHandler(startTag, endTag, false));
		mdCommands.add(italicCommand);

		startTag = "--";
		endTag = startTag;
		com.google.gwt.user.client.ui.Button strikeCommand = getNewCommand("Strikeout", "", getBasicCommandClickHandler(startTag, endTag, false));
		strikeCommand.setHTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getFontelloIcon("strike")));
		strikeCommand.addStyleName("font-size-15");
		mdCommands.add(strikeCommand);

		startTag = "\n```\n";
		endTag = startTag;
		com.google.gwt.user.client.ui.Button codeCommand = getNewCommand("Code Block", "Optionally specify the language for syntax highlighting.", "", getBasicCommandClickHandler(startTag, endTag, true));
		mdCommands.add(codeCommand);
		
		startTag = "$$\\(";
		endTag = "\\)$$";
		com.google.gwt.user.client.ui.Button mathCommand = getNewCommand("TeX", "LaTeX math equation.", "", getBasicCommandClickHandler(startTag, endTag, false));
		mdCommands.add(mathCommand);
		
		
		startTag = "~";
		endTag = startTag;
		com.google.gwt.user.client.ui.Button subscript = getNewCommand("", "Subscript", "", getBasicCommandClickHandler(startTag, endTag, false));
		subscript.setHTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getFontelloIcon("subscript")));
		subscript.addStyleName("font-size-15");
		mdCommands.add(subscript);
		
		startTag = "^";
		endTag = startTag;
		com.google.gwt.user.client.ui.Button superscript = getNewCommand("", "Superscript", "", getBasicCommandClickHandler(startTag, endTag, false));
		superscript.setHTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getFontelloIcon("superscript")));
		superscript.addStyleName("font-size-15");
		mdCommands.add(superscript);
		
		Button headingButton = new Button("Heading");
		DisplayUtils.addToolTip(headingButton, "Heading");
		headingButton.addStyleName("whiteBackgroundGxt boldText");
		headingButton.setWidth(60);
		headingButton.setMenu(createHeadingMenu());
		headingButton.addStyleName("margin-right-10");
		mdCommands.add(headingButton);
		
		if (isWikiEditor) {
			com.google.gwt.user.client.ui.Button attachment = getNewCommand("Insert Attachment", "glyphicon-paperclip",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleInsertWidgetCommand(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE, callback);
				}
			}); 

			mdCommands.add(attachment);
		}

		com.google.gwt.user.client.ui.Button image = getNewCommand("Insert Image", "glyphicon-camera",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleInsertWidgetCommand(WidgetConstants.IMAGE_CONTENT_TYPE, callback);
			}
		});
		mdCommands.add(image);
		
		com.google.gwt.user.client.ui.Button video = getNewCommand("Insert Video", "glyphicon-facetime-video",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleInsertWidgetCommand(WidgetConstants.VIDEO_CONTENT_TYPE, callback);
			}
		}); 
		mdCommands.add(video);

		com.google.gwt.user.client.ui.Button link = getNewCommand("Insert Link", "glyphicon-link",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleInsertWidgetCommand( WidgetConstants.LINK_CONTENT_TYPE, callback);
			}
		}); 

		mdCommands.add(link);
		formPanel.layout(true);
		//defers code until after the browser redraws the page (because of the gxt LayoutContainer life cycle). 
		//scrolls the browser window such that the markdown command bar is at the top of the screen
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				if (mdCommands != null && mdCommands.getElement() != null)
					Window.scrollTo(0, mdCommands.getElement().getAbsoluteTop());
			}
		});
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
	public void initFormattingGuide(WikiPageKey formattingGuideWikiPageKey) {
		markdownWidget.loadMarkdownFromWikiPage(formattingGuideWikiPageKey, false);
	}
	
	private void resizeMarkdownTextArea() {
		markdownTextArea.setHeight(Integer.toString((int)(Window.getClientHeight() * .7)) + "px");
	}
	
	private ClickHandler getBasicCommandClickHandler(final String startTag, final String endTag, final boolean isMultiline) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleBasicCommand(startTag, endTag, isMultiline);
			}
		};
	}
	
	private void handleBasicCommand(String startTag, String endTag, boolean isMultiline) {
		int selectionLength = markdownTextArea.getSelectionLength();
		String text = markdownTextArea.getText();
		int currentPos = markdownTextArea.getCursorPos();
		try {
			String newText = DisplayUtils.surroundText(text, startTag, endTag, isMultiline, currentPos, selectionLength);
			markdownTextArea.setText(newText);
			markdownTextArea.setCursorPos(currentPos+startTag.length());
			markdownTextArea.setFocus(true);
		} catch (IllegalArgumentException e) {
			showErrorMessage(e.getMessage());
		}
	}
	
	public void updateEditWidget(){
		editWidgetButton.setEnabled(false);
		DisplayUtils.updateWidgetSelectionState(widgetSelectionState, markdownTextArea.getText(), markdownTextArea.getCursorPos());
		 
		if (widgetSelectionState.isWidgetSelected()) {
			editWidgetButton.setEnabled(true);
		}
	}
	

	@Override
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
		MarkdownWidget.loadMath(panel, synapseJSNIUtils, true, resourceLoader);
		MarkdownWidget.loadWidgets(panel, wikiKey, isWiki, widgetRegistrar, synapseClient, iconsImageBundle, true, null, null);
		FlowPanel f = new FlowPanel();
		f.setStyleName("entity-description-preview-wrapper");
		f.add(panel);
		window.add(new ScrollPanel(f));
		window.show();
	}
	
	public void showFormattingGuideDialog() {
        final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setSize(590, 600);
        window.setPlain(true); 
        window.setModal(true); 

        window.setHeading(DisplayConstants.ENTITY_DESCRIPTION_TIPS_TEXT); 
        window.setButtons(Dialog.OK);
        window.setHideOnButtonClick(true);

        window.setLayout(new FitLayout());
        ScrollPanel wrapper = new ScrollPanel();
        wrapper.add(markdownWidget);
	    window.add(wrapper);
        // show the window
	    window.show();		
	}

	private Menu createHeadingMenu() {
	    Menu menu = new Menu();
	    menu.setEnableScrolling(false);
	    for (int i = 1; i < 7; i++) {
	    	StringBuilder hashes = new StringBuilder();
	    	for (int j = 0; j < i; j++) {
				hashes.append("#");
			}
	    	addHeadingMenuItem(menu, "<H"+i+">Heading "+i+"</H"+i+">", "\n" + hashes.toString());	
		}

	    return menu;
	}
	
	private void addHeadingMenuItem(Menu menu, String text, final String startTag) {
		menu.add(getNewCommand(text, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				handleBasicCommand(startTag, "", false);
			};
		}));
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
    	menu.add(getNewCommand("User/Team", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.USER_TEAM_BADGE_CONTENT_TYPE, callback);
	    	};
		}));
    	menu.add(getNewCommand(WidgetConstants.VIDEO_FRIENDLY_NAME, new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.VIDEO_CONTENT_TYPE, callback);
	    	};
		}));
    	menu.add(getNewCommand("YouTube Video", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.YOUTUBE_CONTENT_TYPE, callback);	
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
	    	menu.add(getNewCommand("Join Team Button", new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.JOIN_TEAM_CONTENT_TYPE + "?"+WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY + "=42&" + WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY + "=true&" +WidgetConstants.IS_MEMBER_MESSAGE + "=You have successfully joined the challenge&" + WidgetConstants.JOIN_TEAM_BUTTON_TEXT + "="+WidgetConstants.JOIN_TEAM_DEFAULT_BUTTON_TEXT+"&" +WidgetConstants.JOIN_TEAM_SUCCESS_MESSAGE + "="+WidgetConstants.JOIN_TEAM_DEFAULT_SUCCESS_MESSAGE + WidgetConstants.WIDGET_END_MARKDOWN);
		    	};
			}));
	    	menu.add(getNewCommand("Submit To Evaluation Button", new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.SUBMIT_TO_EVALUATION_CONTENT_TYPE + "?"+WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY+"=evalId1,evalId2&" +WidgetConstants.UNAVAILABLE_MESSAGE + "=Join the team to submit to the challenge" + WidgetConstants.WIDGET_END_MARKDOWN);
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

	/**
	 * Create an extra small default icon button
	 * @param tooltipText
	 * @param glyphIconClass
	 * @param clickHandler
	 * @return
	 */
	public com.google.gwt.user.client.ui.Button getNewCommand(String tooltipText, String glyphIconClass, ClickHandler clickHandler){
		return getNewCommand("", tooltipText, glyphIconClass, clickHandler);
	}
	
	public com.google.gwt.user.client.ui.Button getNewCommand(String title, String tooltipText, String glyphIconClass, ClickHandler clickHandler){
		com.google.gwt.user.client.ui.Button command = DisplayUtils.createIconButton(title, ButtonType.DEFAULT, glyphIconClass + " margin-bottom-5");
		command.addStyleName("btn-xs");
		command.addClickHandler(clickHandler);
		if (tooltipText != null)
			DisplayUtils.addTooltip(this.synapseJSNIUtils, command, tooltipText, TOOLTIP_POSITION.BOTTOM);
		command.setHeight("22px");
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
	
	@Override
	public void showLoading() {
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
}
