package org.sagebionetworks.web.client.widget.entity;

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
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
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
	private IconsImageBundle iconsImageBundle;
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
			SynapseJSNIUtils synapseJSNIUtils,
			IconsImageBundle iconsImageBundle,
			ResourceLoader resourceLoader, 
			MarkdownWidget markdownWidget) {
		super();
		this.widget = binder.createAndBindUi(this);
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.iconsImageBundle = iconsImageBundle;
		this.resourceLoader = resourceLoader;
		this.markdownWidget = markdownWidget;
		markdownWidget.addStyleName("margin-10");
		
		editWidgetButton.addClickHandler(getClickHandler(MarkdownEditorAction.EDIT_WIDGET));
		attachmentLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_ATTACHMENT));
		buttonLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_BUTTON_LINK));
		entityListLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_ENTITY_LIST));
		imageLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_IMAGE));
		joinTeamLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_JOIN_TEAM));
		linkLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_LINK));
		provenanceGraphLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_PROV_GRAPH));
		queryLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_QUERY_TABLE));
		referenceLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_REFERENCE));
		submitToEvaluationLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_SUBMIT_TO_EVALUATION));
		tableLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_TABLE));
		tableOfContentsLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_TOC));
		userTeamLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_USER_TEAM_BADGE));
		videoLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_VIDEO));
		youTubeLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_YOU_TUBE));
		bookmarkLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_BOOKMARK));
		externalWebsiteLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_EXTERNAL_WEBSITE));
		supertableLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_API_SUPERTABLE));
		wikifilesPreviewLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_WIKI_FILES_PREVIEW));
		tutorialWizardLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_TUTORIAL_WIZARD));
		boldButton.addClickHandler(getClickHandler(MarkdownEditorAction.BOLD));
		italicButton.addClickHandler(getClickHandler(MarkdownEditorAction.ITALIC));
		strikeButton.addClickHandler(getClickHandler(MarkdownEditorAction.STRIKETHROUGH));
		codeBlockButton.addClickHandler(getClickHandler(MarkdownEditorAction.CODE_BLOCK));
		mathButton.addClickHandler(getClickHandler(MarkdownEditorAction.MATH));
		subscriptButton.addClickHandler(getClickHandler(MarkdownEditorAction.SUBSCRIPT));
		superscriptButton.addClickHandler(getClickHandler(MarkdownEditorAction.SUPERSCRIPT));
		heading1Link.addClickHandler(getClickHandler(MarkdownEditorAction.H1));
		heading2Link.addClickHandler(getClickHandler(MarkdownEditorAction.H2));
		heading3Link.addClickHandler(getClickHandler(MarkdownEditorAction.H3));
		heading4Link.addClickHandler(getClickHandler(MarkdownEditorAction.H4));
		heading5Link.addClickHandler(getClickHandler(MarkdownEditorAction.H5));
		heading6Link.addClickHandler(getClickHandler(MarkdownEditorAction.H6));
		attachmentButton.addClickHandler(getClickHandler(MarkdownEditorAction.ATTACHMENTS));
		imageButton.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_IMAGE));
		videoButton.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_VIDEO));
		previewButton.addClickHandler(getClickHandler(MarkdownEditorAction.PREVIEW));
		deleteButton.addClickHandler(getClickHandler(MarkdownEditorAction.DELETE));
		attachmentsButton.addClickHandler(getClickHandler(MarkdownEditorAction.ATTACHMENTS));
		saveButton.addClickHandler(getClickHandler(MarkdownEditorAction.SAVE));
		cancelButton.addClickHandler(getClickHandler(MarkdownEditorAction.CANCEL));
		
		heading1Link.addStyleName("font-size-36");
		heading2Link.addStyleName("font-size-30");
		heading3Link.addStyleName("font-size-24");
		heading4Link.addStyleName("font-size-18");
		heading5Link.addStyleName("font-size-14");
		heading6Link.addStyleName("font-size-12");
		
		markdownTextArea.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.markdownEditorClicked();
			}
		});
		
		markdownTextArea.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				presenter.markdownEditorClicked();
				resizeMarkdownTextArea();
			}
		});
	}
	
	private ClickHandler getClickHandler(final MarkdownEditorAction action) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.handleCommand(action);		
			}
		};
	}
	
	@Override
	public void setSaving(boolean isSaving) {
		if (isSaving) 
			saveButton.state().loading();
		else
			saveButton.state().reset();
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
	public void configure( 
			WikiPageKey formattingGuideWikiPageKey,
			String markdown) {
		markdownTextArea.setText(markdown);
		if (formattingGuideWikiPageKey != null)
			initFormattingGuide(formattingGuideWikiPageKey);
		resizeMarkdownTextArea();
		
		if (mdCommands != null && mdCommands.getElement() != null)
			Window.scrollTo(0, mdCommands.getElement().getAbsoluteTop());
	}
	
	@Override
	public void setAttachmentsButtonVisible(boolean visible) {
		attachmentsButton.setVisible(visible);
	}
	
	@Override
	public void setAttachmentCommandsVisible(boolean visible) {
		attachmentLink.setVisible(visible);
		attachmentButton.setVisible(visible);
	}
	
	@Override
	public void setCancelVisible(boolean visible) {
		cancelButton.setVisible(visible);
	}
	@Override
	public void setSaveVisible(boolean visible) {
		saveButton.setVisible(visible);
	}
	@Override
	public void setDeleteVisible(boolean visible) {
		deleteButton.setVisible(visible);
	}

	public void initFormattingGuide(WikiPageKey formattingGuideWikiPageKey) {
		markdownWidget.loadMarkdownFromWikiPage(formattingGuideWikiPageKey, false);
		formattingGuideContainer.clear();
		formattingGuideContainer.add(markdownWidget);
	}
	
	private void resizeMarkdownTextArea() {
		markdownTextArea.setHeight(Integer.toString((int)(Window.getClientHeight() * .7)) + "px");
	}
	
	@Override
	public void setAlphaCommandsVisible(boolean visible) {
		alphaInsertButton.setVisible(visible);
	}
	
	
	@Override
	public void setEditButtonEnabled(boolean enabled) {
		editWidgetButton.setEnabled(enabled);
	}

	@Override
	public void showPreviewHTML(String result, WikiPageKey wikiKey, boolean isWiki, WidgetRegistrar widgetRegistrar) throws JSONObjectAdapterException {
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
	
	@Override
	public void setMarkdown(String markdown) {
		DisplayUtils.updateTextArea(markdownTextArea, markdown);
	}
	
	@Override
	public int getCursorPos() {
		return markdownTextArea.getCursorPos();
	}
	
	@Override
	public void setCursorPos(int pos) {
		markdownTextArea.setCursorPos(pos);
	}
	
	@Override
	public void setMarkdownFocus() {
		markdownTextArea.setFocus(true);
	}
	@Override
	public int getSelectionLength() {
		return markdownTextArea.getSelectionLength();
	}
	
	@Override
	public void setSelectionRange(int pos, int length) {
		markdownTextArea.setSelectionRange(pos, length);
	}
}
