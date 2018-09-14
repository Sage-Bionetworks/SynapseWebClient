package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
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
	
	private Presenter presenter;
	//dialog for the formatting guide
	@UiField
	public Div mdCommands;
	
	@UiField
	public org.gwtbootstrap3.client.ui.TextArea markdownTextArea;
	
	@UiField
	public Div selectTeamModalContainer;
	/**
	 * List of toolbar commands
	 */
	@UiField
	public Button editWidgetButton;
	@UiField
	public Button writeMarkdownButton;
	//insert widget menu commands
	@UiField
	public AnchorListItem attachmentLink;
	@UiField
	public AnchorListItem genomeBrowserLink;
	@UiField
	public AnchorListItem cytoscapeJsLink;
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
	public AnchorListItem detailsSummaryLink;
	@UiField
	public AnchorListItem provenanceGraphLink;
	@UiField
	public AnchorListItem queryLink;
	@UiField
	public AnchorListItem leaderboardLink;
	@UiField
	public AnchorListItem referenceLink;
	@UiField
	public AnchorListItem previewLink;
	@UiField
	public AnchorListItem submitToEvaluationLink;
	@UiField
	public AnchorListItem tableLink;
	@UiField
	public AnchorListItem tableOfContentsLink;
	@UiField
	public AnchorListItem userLink;
	@UiField
	public AnchorListItem graphLink;
	@UiField
	public Button userButton;
	
	@UiField
	public AnchorListItem teamLink;
	@UiField
	public AnchorListItem synapseFormLink;
	@UiField
	public AnchorListItem videoLink;
	
	@UiField
	public Button formattingGuideButton;
	
	//Alpha mode button and commands
	@UiField
	public Button alphaInsertButton;
	@UiField
	public AnchorListItem externalWebsiteLink;
	@UiField
	public AnchorListItem supertableLink;
	@UiField
	public AnchorListItem registerChallengeTeamLink;
	@UiField
	public AnchorListItem challengeParticipantsLink;
	@UiField
	public AnchorListItem challengeTeamsLink;
	@UiField
	public AnchorListItem synapseTableLink;
	@UiField
	public AnchorListItem wikifilesPreviewLink;
	@UiField
	public AnchorListItem tutorialWizardLink;
	@UiField
	public AnchorListItem teamMembersLink;
	@UiField
	public AnchorListItem teamMemberCountLink;
	
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
	public Button imageLinkButton;
	@UiField
	public Button videoButton;
	@UiField
	public Button linkButton;
	@UiField
	public Button markdownPreviewButton;
	
	//preview
	@UiField
	public SimplePanel previewHtmlContainer;
	@UiField
	public Div previewUI;
	@UiField
	public Div writingUI;
	Span widget = new Span();
	//this UI widget
	Widget viewWidget;
	Widget formattingGuideWidget;
	Binder binder;
	@Inject
	public MarkdownEditorWidgetViewImpl(Binder binder) {
		super();
		this.binder = binder;
	}
	
	@Override
	public void addTextAreaKeyUpHandler(KeyUpHandler handler) {
		markdownTextArea.addKeyUpHandler(handler);
	}
	
	@Override
	public void addTextAreaClickHandler(ClickHandler handler) {
		markdownTextArea.addClickHandler(handler);
	}
	
	private void lazyConstruct() {
		if (viewWidget == null) {
			viewWidget = binder.createAndBindUi(this);
			editWidgetButton.addClickHandler(getClickHandler(MarkdownEditorAction.EDIT_WIDGET));
			attachmentLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_ATTACHMENT));
			genomeBrowserLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_BIODALLIANCE_GENOME_BROWSER));
			buttonLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_BUTTON_LINK));
			entityListLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_ENTITY_LIST));
			imageLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_IMAGE));
			joinTeamLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_JOIN_TEAM));
			linkLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_LINK));
			detailsSummaryLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_DETAILS_SUMMARY));
			provenanceGraphLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_PROV_GRAPH));
			queryLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_QUERY_TABLE));
			leaderboardLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_LEADERBOARD));
			referenceLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_REFERENCE));
			submitToEvaluationLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_SUBMIT_TO_EVALUATION));
			tableLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_TABLE));
			tableOfContentsLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_TOC));
			userLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_USER_LINK));
			graphLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_GRAPH));
			userButton.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_USER_LINK));
			teamLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_USER_TEAM_BADGE));
			synapseFormLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_SYNAPSE_FORM));
			videoLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_VIDEO));
			previewLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_PREVIEW));
			synapseTableLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_SYNAPSE_TABLE));
			externalWebsiteLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_EXTERNAL_WEBSITE));
			supertableLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_API_SUPERTABLE));
			wikifilesPreviewLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_WIKI_FILES_PREVIEW));
			tutorialWizardLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_TUTORIAL_WIZARD));
			registerChallengeTeamLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_REGISTER_CHALLENGE_TEAM));
			challengeTeamsLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_CHALLENGE_TEAMS));
			challengeParticipantsLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_CHALLENGE_PARTICIPANTS));
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
			attachmentButton.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_ATTACHMENT));
			imageButton.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_IMAGE));
			imageLinkButton.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_IMAGE_LINK));
			videoButton.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_VIDEO));
			linkButton.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_LINK));
			cytoscapeJsLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_CYTOSCAPE_JS));
			teamMembersLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_TEAM_MEMBERS));
			teamMemberCountLink.addClickHandler(getClickHandler(MarkdownEditorAction.INSERT_TEAM_MEMBER_COUNT));
			markdownPreviewButton.addClickHandler(getClickHandler(MarkdownEditorAction.MARKDOWN_PREVIEW));
			heading1Link.addStyleName("font-size-36");
			heading2Link.addStyleName("font-size-30");
			heading3Link.addStyleName("font-size-24");
			heading4Link.addStyleName("font-size-18");
			heading5Link.addStyleName("font-size-14");
			heading6Link.addStyleName("font-size-12");
			formattingGuideButton.addClickHandler(event -> {
				if (formattingGuideWidget.getParent() != null) {
					formattingGuideWidget.removeFromParent();
				}
				Dialog formattingGuideModal = new Dialog();
				formattingGuideModal.configure("Formatting Guide", null, "Close", null, true);
				formattingGuideModal.add(formattingGuideWidget);
				formattingGuideModal.show();
			});
			
			markdownTextArea.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					presenter.onKeyPress(event);
				}
			});
			writeMarkdownButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					showEditMode();
				}
			});
			widget.add(viewWidget);
		}
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void configure(String markdown) {
		lazyConstruct();
		markdownTextArea.setText(markdown);		
	}
	
	@Override
	public void setAttachmentCommandsVisible(boolean visible) {
		lazyConstruct();
		attachmentLink.setVisible(visible);
		attachmentButton.setVisible(visible);
	}
	
	@Override
	public void setAlphaCommandsVisible(boolean visible) {
		lazyConstruct();
		alphaInsertButton.setVisible(visible);
	}
	
	@Override
	public void setEditButtonEnabled(boolean enabled) {
		editWidgetButton.setEnabled(enabled);
	}
	
	
	
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showLoading() {
	}
	
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void clear() {
		if (markdownTextArea != null) {
			markdownTextArea.setText("");	
		}
	}

	@Override
	public String getMarkdown() {
		return markdownTextArea.getText();
	}
	
	@Override
	public void setMarkdown(String markdown) {
		lazyConstruct();
		markdownTextArea.setValue(markdown);
	}
	
	@Override
	public int getCursorPos() {
		lazyConstruct();
		return markdownTextArea.getCursorPos();
	}
	
	@Override
	public void setCursorPos(int pos) {
		lazyConstruct();
		markdownTextArea.setCursorPos(pos);
	}
	
	@Override
	public void setMarkdownFocus() {
		lazyConstruct();
		markdownTextArea.setFocus(true);
	}
	
	@Override
	public int getSelectionLength() {
		lazyConstruct();
		return markdownTextArea.getSelectionLength();
	}
	
	@Override
	public void setSelectionRange(int pos, int length) {
		lazyConstruct();
		markdownTextArea.setSelectionRange(pos, length);
	}
	
	@Override
	public int getClientHeight() {
		return Window.getClientHeight();
	};
	
	@Override
	public void setMarkdownTextAreaHeight(int heightPx) {
		lazyConstruct();
		markdownTextArea.setHeight(heightPx + "px");
	}
	
	@Override
	public void setFormattingGuideWidget(Widget formattingGuideWidget) {
		this.formattingGuideWidget = formattingGuideWidget;
	}
	@Override
	public boolean isEditorAttachedAndVisible() {
		return widget.isAttached() && widget.isVisible();
	}

	@Override
	public void setImageCommandsVisible(boolean visible) {
		lazyConstruct();
		imageLink.setVisible(visible);
		imageButton.setVisible(visible);
	}

	@Override
	public void setVideoCommandsVisible(boolean visible) {
		lazyConstruct();
		videoLink.setVisible(visible);
		videoButton.setVisible(visible);
	}

	@Override
	public void setExternalImageButtonVisible(boolean visible) {
		lazyConstruct();
		imageLinkButton.setVisible(visible);
	}
	
	@Override
	public void setFocus(boolean focused) {
		lazyConstruct();
		markdownTextArea.setFocus(focused);
	}
	@Override
	public void setEditorEnabled(boolean enabled) {
		lazyConstruct();
		markdownTextArea.setEnabled(enabled);
	}
	
	@Override
	public void setMarkdownPreviewWidget(Widget markdownPreviewWidget) {
		lazyConstruct();
		previewHtmlContainer.setWidget(markdownPreviewWidget);
	}

	@Override
	public void showPreview() {
		writingUI.setVisible(false);
		previewUI.setVisible(true);
	}
	
	@Override
	public void showEditMode() {
		previewUI.setVisible(false);
		writingUI.setVisible(true);	
	}
	@Override
	public void setSelectTeamModal(Widget widget) {
		lazyConstruct();
		selectTeamModalContainer.clear();
		selectTeamModalContainer.add(widget);
	}
}