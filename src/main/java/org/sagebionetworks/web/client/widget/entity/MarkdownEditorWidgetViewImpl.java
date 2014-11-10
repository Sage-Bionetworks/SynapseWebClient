package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
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
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.CloseHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.ManagementHandler;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to resolve markdown
 * 
 * @author Jay
 *
 */
public class MarkdownEditorWidgetViewImpl implements MarkdownEditorWidgetView {
	
	public interface Binder extends UiBinder<Widget, MarkdownEditorWidgetViewImpl> {}
	
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJSNIUtils;
	private WidgetRegistrar widgetRegistrar;
	private IconsImageBundle iconsImageBundle;
	BaseEditWidgetDescriptorPresenter widgetDescriptorEditor;
	private CookieProvider cookies;
	private WikiPageKey wikiKey;
	private boolean isWikiEditor;
	private WidgetDescriptorUpdatedHandler callback;
	private ManagementHandler managementHandler;
	private CloseHandler saveHandler;
	private WidgetSelectionState widgetSelectionState;
	private ResourceLoader resourceLoader;
	private MarkdownWidget markdownWidget;
	private Presenter presenter;
	
	//dialog for the formatting guide
	@UiField
	public Div mdCommands;
	
	@UiField
	public TextArea markdownTextArea;
	
	@UiField
	public FlowPanel formattingGuideContainer;
	
	/**
	 * List of toolbar commands
	 */
	@UiField
	public Button editWidgetButton;
	//insert widget menu commands
	@UiField
	public AnchorListItem attachmentLink;
	@UiField
	public AnchorListItem buttonLink;
	@UiField
	public AnchorListItem entityListLink;
	@UiField
	public AnchorListItem imageLink;
	@UiField
	public AnchorListItem joinTeamLink;
	@UiField
	public AnchorListItem linkLink;
	@UiField
	public AnchorListItem provenanceGraphLink;
	@UiField
	public AnchorListItem queryLink;
	@UiField
	public AnchorListItem referenceLink;
	@UiField
	public AnchorListItem submitToEvaluationLink;
	@UiField
	public AnchorListItem tableLink;
	@UiField
	public AnchorListItem tableOfContentsLink;
	@UiField
	public AnchorListItem userTeamLink;
	@UiField
	public AnchorListItem videoLink;
	@UiField
	public AnchorListItem youTubeLink;
	
	//Alpha mode button and commands
	@UiField
	public Button alphaInsertButton;
	@UiField
	public AnchorListItem bookmarkLink;
	@UiField
	public AnchorListItem externalWebsiteLink;
	@UiField
	public AnchorListItem supertableLink;
	@UiField
	public AnchorListItem wikifilesPreviewLink;
	@UiField
	public AnchorListItem tutorialWizardLink;
	
	@UiField
	public Button boldButton;
	@UiField
	public Button italicButton;
	@UiField
	public Button strikeButton;
	
	@UiField
	public Button codeBlockButton;
	@UiField
	public Button mathButton;
	@UiField
	public Button subscriptButton;
	@UiField
	public Button superscriptButton;
	
	//Heading commands
	@UiField
	public AnchorListItem heading1Link;
	@UiField
	public AnchorListItem heading2Link;
	@UiField
	public AnchorListItem heading3Link;
	@UiField
	public AnchorListItem heading4Link;
	@UiField
	public AnchorListItem heading5Link;
	@UiField
	public AnchorListItem heading6Link;
	
	//convenience buttons
	@UiField
	public Button attachmentButton;
	@UiField
	public Button imageButton;
	@UiField
	public Button videoButton;
	@UiField
	public Button linkButton;
	
	
	//preview
	@UiField
	public FlowPanel previewHtmlContainer;
	@UiField
	public Modal previewModal;
	@UiField
	public Button previewButton;
	
	@UiField
	public Button saveButton;
	@UiField
	public Button attachmentsButton;
	@UiField
	public Button cancelButton;
	@UiField
	public Button deleteButton;
	
	//this UI widget
	Widget widget;
	
	
	
	@Inject
	public MarkdownEditorWidgetViewImpl(
			Binder binder,
			SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar,
			IconsImageBundle iconsImageBundle,
			BaseEditWidgetDescriptorPresenter widgetDescriptorEditor,
			CookieProvider cookies,
			ResourceLoader resourceLoader, 
			MarkdownWidget markdownWidget) {
		super();
		this.widget = binder.createAndBindUi(this);
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
		
		
		editWidgetButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleEditWidgetCommand();
			}
		});
		
		
		attachmentLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE));
		buttonLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.BUTTON_LINK_CONTENT_TYPE));
		entityListLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.ENTITYLIST_CONTENT_TYPE));
		imageLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.IMAGE_CONTENT_TYPE));
		joinTeamLink.addClickHandler(getInsertMarkdownClickHandler(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.JOIN_TEAM_CONTENT_TYPE + "?"+WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY + "=42&" + WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY + "=false&" +WidgetConstants.IS_MEMBER_MESSAGE + "=You have successfully joined the team&" + WidgetConstants.JOIN_TEAM_BUTTON_TEXT + "="+WidgetConstants.JOIN_TEAM_DEFAULT_BUTTON_TEXT+"&" +WidgetConstants.JOIN_TEAM_SUCCESS_MESSAGE + "="+WidgetConstants.JOIN_TEAM_DEFAULT_SUCCESS_MESSAGE + WidgetConstants.WIDGET_END_MARKDOWN));
		linkLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.LINK_CONTENT_TYPE));
		provenanceGraphLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.PROVENANCE_CONTENT_TYPE));
		queryLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.QUERY_TABLE_CONTENT_TYPE));
		referenceLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.REFERENCE_CONTENT_TYPE));
		submitToEvaluationLink.addClickHandler(getInsertMarkdownClickHandler(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.SUBMIT_TO_EVALUATION_CONTENT_TYPE + "?"+WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY+"=evalId1,evalId2&" +WidgetConstants.UNAVAILABLE_MESSAGE + "=Join the team to submit to the evaluation" + WidgetConstants.WIDGET_END_MARKDOWN));
		tableLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.TABBED_TABLE_CONTENT_TYPE));
		tableOfContentsLink.addClickHandler(getInsertMarkdownClickHandler(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.TOC_CONTENT_TYPE + WidgetConstants.WIDGET_END_MARKDOWN));
		userTeamLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.USER_TEAM_BADGE_CONTENT_TYPE));
		videoLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.VIDEO_CONTENT_TYPE));
		youTubeLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.YOUTUBE_CONTENT_TYPE));
		bookmarkLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.BOOKMARK_CONTENT_TYPE));
		externalWebsiteLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.SHINYSITE_CONTENT_TYPE));
		supertableLink.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.API_TABLE_CONTENT_TYPE));
		wikifilesPreviewLink.addClickHandler(getInsertMarkdownClickHandler(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.WIKI_FILES_PREVIEW_CONTENT_TYPE + WidgetConstants.WIDGET_END_MARKDOWN));
		tutorialWizardLink.addClickHandler(getInsertMarkdownClickHandler(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.TUTORIAL_WIZARD_CONTENT_TYPE + "?"+WidgetConstants.WIDGET_ENTITY_ID_KEY+"=syn123&" +WidgetConstants.TEXT_KEY + "=Tutorial"+ WidgetConstants.WIDGET_END_MARKDOWN));
		//basic commands
		boldButton.addClickHandler(getBasicCommandClickHandler("**", "**", false));
		italicButton.addClickHandler(getBasicCommandClickHandler("_", "_", false));
		strikeButton.addClickHandler(getBasicCommandClickHandler("--", "--", false));
		codeBlockButton.addClickHandler(getBasicCommandClickHandler("\n```\n", "\n```\n", true));
		mathButton.addClickHandler(getBasicCommandClickHandler("$$\\(", "\\)$$", false));
		subscriptButton.addClickHandler(getBasicCommandClickHandler("~", "~", false));
		superscriptButton.addClickHandler(getBasicCommandClickHandler("^", "^", false));
		//heading commands
		heading1Link.addClickHandler(getBasicCommandClickHandler("#", "", false));
		heading1Link.addStyleName("font-size-36");
		heading2Link.addClickHandler(getBasicCommandClickHandler("##", "", false));
		heading2Link.addStyleName("font-size-30");
		heading3Link.addClickHandler(getBasicCommandClickHandler("###", "", false));
		heading3Link.addStyleName("font-size-24");
		heading4Link.addClickHandler(getBasicCommandClickHandler("####", "", false));
		heading4Link.addStyleName("font-size-18");
		heading5Link.addClickHandler(getBasicCommandClickHandler("#####", "", false));
		heading5Link.addStyleName("font-size-14");
		heading6Link.addClickHandler(getBasicCommandClickHandler("######", "", false));
		heading6Link.addStyleName("font-size-12");
		
		
		attachmentButton.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE));
		imageButton.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.IMAGE_CONTENT_TYPE));
		videoButton.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.VIDEO_CONTENT_TYPE));
		linkButton.addClickHandler(getInsertWidgetClickHandler(WidgetConstants.LINK_CONTENT_TYPE));
		
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

		
		//preview
		previewButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.showPreview(markdownTextArea.getValue(), isWikiEditor);
			}
		});
		
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
		
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveButton.state().loading();
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
	
	private ClickHandler getInsertWidgetClickHandler(final String contentType) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleInsertWidgetCommand(contentType, callback);		
			}
		};
	}
	

	private ClickHandler getInsertMarkdownClickHandler(final String markdown) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				insertMarkdown(markdown);		
			}
		};
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
			String markdown, 
			final boolean isWikiEditor,
			final WidgetDescriptorUpdatedHandler callback,
			final CloseHandler saveHandler,
			final ManagementHandler managementHandler) {
		markdownTextArea.setText(markdown);
		if (formattingGuideWikiPageKey != null)
			initFormattingGuide(formattingGuideWikiPageKey);
		resizeMarkdownTextArea();
		this.wikiKey = wikiKey;
		this.isWikiEditor = isWikiEditor;
		this.callback = callback;
		this.managementHandler = managementHandler;
		this.saveHandler = saveHandler;
		boolean canManage = managementHandler != null;
		deleteButton.setVisible(canManage);
		attachmentsButton.setVisible(canManage);
		
		boolean canSave = saveHandler != null;
		saveButton.setVisible(canSave);
		cancelButton.setVisible(canSave);
		
		attachmentLink.setVisible(isWikiEditor);
		attachmentButton.setVisible(isWikiEditor);
		
		alphaInsertButton.setVisible(DisplayUtils.isInTestWebsite(cookies));
		if (mdCommands != null && mdCommands.getElement() != null)
			Window.scrollTo(0, mdCommands.getElement().getAbsoluteTop());
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
		formattingGuideContainer.clear();
		formattingGuideContainer.add(markdownWidget);
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
		
		previewHtmlContainer.clear();
		previewHtmlContainer.add(panel);
		previewModal.show();
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
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void clear() {
		saveButton.state().reset();
		markdownTextArea.setText("");
	}

	@Override
	public String getMarkdown() {
		return markdownTextArea.getText();
	}
}
