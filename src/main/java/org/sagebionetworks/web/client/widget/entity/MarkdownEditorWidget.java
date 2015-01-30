package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget used to edit markdown
 * 
 * @author Jay
 *
 */
public class MarkdownEditorWidget implements MarkdownEditorWidgetView.Presenter, SynapseWidgetPresenter {
	
	private SynapseClientAsync synapseClient;
	private CookieProvider cookies;
	private GWTWrapper gwt;
	public static WikiPageKey formattingGuideWikiPageKey;
	private MarkdownEditorWidgetView view;
	private BaseEditWidgetDescriptorPresenter widgetDescriptorEditor;
	private WidgetRegistrar widgetRegistrar;
	private WikiPageKey wikiKey;
	private WidgetSelectionState widgetSelectionState;
	WidgetDescriptorUpdatedHandler widgetDescriptorUpdatedHandler;
	private boolean isWikiEditor;
	private Callback attachmentsHandler, saveHandler, cancelHandler, deleteHandler; 
	
	@Inject
	public MarkdownEditorWidget(MarkdownEditorWidgetView view, 
			SynapseClientAsync synapseClient,
			CookieProvider cookies,
			GWTWrapper gwt,
			BaseEditWidgetDescriptorPresenter widgetDescriptorEditor,
			WidgetRegistrar widgetRegistrar
			) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		this.cookies = cookies;
		this.widgetDescriptorEditor = widgetDescriptorEditor;
		this.widgetRegistrar = widgetRegistrar;
		widgetSelectionState = new WidgetSelectionState();
		view.setPresenter(this);
	}
	
	public void setActionHandler(MarkdownEditorAction action, Callback callback) {
		if (callback != null) {
			switch (action) {
			case SAVE:
				saveHandler = callback;
				view.setSaveVisible(true);
				break;
			case CANCEL:
				cancelHandler = callback;
				view.setCancelVisible(true);
				break;
			case DELETE:
				deleteHandler = callback;
				view.setDeleteVisible(true);
				break;
			case ATTACHMENTS:
				attachmentsHandler = callback;
				view.setAttachmentsButtonVisible(true);
				break;
			default:
				throw new IllegalArgumentException(
						"Markdown editor does not support callback for the action: " + action);
			}
		}
	}
	
	/**
	 * 
	 * @param ownerId
	 * @param ownerType
	 * @param markdownTextArea
	 * @param formPanel
	 * @param callback
	 * @param closeHandler if no save handler is specified, then a Save button is not shown.  If it is specified, then Save is shown and saveClicked is called when that button is clicked.
	 */
	public void configure(final WikiPageKey wikiKey,
			final String markdown, 
			final boolean isWikiEditor,
			final WidgetDescriptorUpdatedHandler callback) {
		this.isWikiEditor = isWikiEditor;
		this.wikiKey = wikiKey;
		this.widgetDescriptorUpdatedHandler = callback;
		attachmentsHandler = null;
		cancelHandler = null;
		saveHandler = null;
		deleteHandler = null;
		view.setSaveVisible(false);
		view.setCancelVisible(false);
		view.setAttachmentsButtonVisible(false);
		view.setDeleteVisible(false);
		
		//clear view state
		view.clear();
		view.setAttachmentCommandsVisible(isWikiEditor);
		view.setAlphaCommandsVisible(DisplayUtils.isInTestWebsite(cookies));
	
		if (formattingGuideWikiPageKey == null) {
			//get the page name to wiki key map
			getFormattingGuideWikiKey(new CallbackP<WikiPageKey>() {
				@Override
				public void invoke(WikiPageKey key) {
					formattingGuideWikiPageKey = key;
					view.configure(formattingGuideWikiPageKey, markdown);
				}
			});
		} else {
			view.configure(formattingGuideWikiPageKey, markdown);
		}
	}
	
	public void getFormattingGuideWikiKey(final CallbackP<WikiPageKey> callback) {
		synapseClient.getHelpPages(new AsyncCallback<HashMap<String,WikiPageKey>>() {
			@Override
			public void onSuccess(HashMap<String,WikiPageKey> result) {
				callback.invoke(result.get(WebConstants.FORMATTING_GUIDE));
			};
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
				callback.invoke(null);
			}
		});
		
	}
	
	public void showPreview() {
	    //get the html for the markdown
	    synapseClient.markdown2Html(view.getMarkdown(), true, DisplayUtils.isInTestWebsite(cookies), gwt.getHostPrefix(), new AsyncCallback<String>() {
	    	@Override
			public void onSuccess(String result) {
	    		try {
					view.showPreviewHTML(result, wikiKey, isWikiEditor, widgetRegistrar);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				//preview failed
				view.showErrorMessage(DisplayConstants.PREVIEW_FAILED_TEXT + caught.getMessage());
			}
		});
	}
	
	public void insertMarkdown(String md) {
		String currentValue = view.getMarkdown();
		if (currentValue == null)
			currentValue = "";
		int cursorPos = view.getCursorPos();
		if (cursorPos < 0 || cursorPos > currentValue.length())
			cursorPos = currentValue.length();
		view.setMarkdown(currentValue.substring(0, cursorPos) + md + currentValue.substring(cursorPos));
		//SWC-406: set cursor to after the current markdown
		view.setCursorPos(cursorPos + md.length());
	}
	
	/**
	 * Deletes all instances of the given markdown from the editor
	 * @param md
	 */
	public void deleteMarkdown(String md) {
		//replace all instances of the md with the empty string
		StringBuilder newValue = new StringBuilder(view.getMarkdown());
        
		int idx = 0;
        while((idx = newValue.indexOf(md, idx)) != -1) {
            newValue.replace(idx, idx + md.length(), "");
        }
        view.setMarkdown(newValue.toString());
	}

	@Override
	public void markdownEditorClicked() {
		//update edit widget button state
		view.setEditButtonEnabled(false);
		DisplayUtils.updateWidgetSelectionState(widgetSelectionState, view.getMarkdown(), view.getCursorPos());
		 
		if (widgetSelectionState.isWidgetSelected()) {
			view.setEditButtonEnabled(true);
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public String getMarkdown() {
		return view.getMarkdown();
	}
	
	@Override
	public void handleCommand(MarkdownEditorAction action) {
		//editor knows how to handle all commands (not a subscribe model since we don't need it yet).
		switch (action) {
		case EDIT_WIDGET:
			editExistingWidget();
			break;
		case INSERT_ATTACHMENT:
			insertNewWidget(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE);
			break;
		case INSERT_BUTTON_LINK:
			insertNewWidget(WidgetConstants.BUTTON_LINK_CONTENT_TYPE);
			break;
		case INSERT_ENTITY_LIST:
			insertNewWidget(WidgetConstants.ENTITYLIST_CONTENT_TYPE);
			break;
		case INSERT_IMAGE:
			insertNewWidget(WidgetConstants.IMAGE_CONTENT_TYPE);
			break;
		case INSERT_JOIN_TEAM:
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.JOIN_TEAM_CONTENT_TYPE + "?"+WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY + "=42&" + WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY + "=false&" +WidgetConstants.IS_MEMBER_MESSAGE + "=You have successfully joined the team&" + WidgetConstants.JOIN_TEAM_BUTTON_TEXT + "="+WidgetConstants.JOIN_TEAM_DEFAULT_BUTTON_TEXT+"&" +WidgetConstants.JOIN_TEAM_SUCCESS_MESSAGE + "="+WidgetConstants.JOIN_TEAM_DEFAULT_SUCCESS_MESSAGE + WidgetConstants.WIDGET_END_MARKDOWN);
			break;
		case INSERT_LINK:
			insertNewWidget(WidgetConstants.LINK_CONTENT_TYPE);
			break;
		case INSERT_PROV_GRAPH:
			insertNewWidget(WidgetConstants.PROVENANCE_CONTENT_TYPE);
			break;
		case INSERT_QUERY_TABLE:
			insertNewWidget(WidgetConstants.QUERY_TABLE_CONTENT_TYPE);
			break;
		case INSERT_REFERENCE:
			insertNewWidget(WidgetConstants.REFERENCE_CONTENT_TYPE);
			break;
		case INSERT_SUBMIT_TO_EVALUATION:
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.SUBMIT_TO_EVALUATION_CONTENT_TYPE + "?"+WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY+"=evalId1,evalId2&" +WidgetConstants.UNAVAILABLE_MESSAGE + "=Join the team to submit to the evaluation" + WidgetConstants.WIDGET_END_MARKDOWN);
			break;
		case INSERT_TABLE:
			insertNewWidget(WidgetConstants.TABBED_TABLE_CONTENT_TYPE);
			break;
		case INSERT_TOC:
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.TOC_CONTENT_TYPE + WidgetConstants.WIDGET_END_MARKDOWN);
			break;
		case INSERT_USER_TEAM_BADGE:
			insertNewWidget(WidgetConstants.USER_TEAM_BADGE_CONTENT_TYPE);
			break;
		case INSERT_VIDEO:
			insertNewWidget(WidgetConstants.VIDEO_CONTENT_TYPE);
			break;
		case INSERT_YOU_TUBE:
			insertNewWidget(WidgetConstants.YOUTUBE_CONTENT_TYPE);
			break;
		case INSERT_BOOKMARK:
			insertNewWidget(WidgetConstants.BOOKMARK_CONTENT_TYPE);
			break;
		case INSERT_SYNAPSE_TABLE:
			insertNewWidget(WidgetConstants.SYNAPSE_TABLE_CONTENT_TYPE);
			break;
		case INSERT_EXTERNAL_WEBSITE:
			insertNewWidget(WidgetConstants.SHINYSITE_CONTENT_TYPE);
			break;
		case INSERT_API_SUPERTABLE:
			insertNewWidget(WidgetConstants.API_TABLE_CONTENT_TYPE);
			break;
		case INSERT_WIKI_FILES_PREVIEW:
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.WIKI_FILES_PREVIEW_CONTENT_TYPE + WidgetConstants.WIDGET_END_MARKDOWN);
			break;
		case INSERT_TUTORIAL_WIZARD:
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.TUTORIAL_WIZARD_CONTENT_TYPE + "?"+WidgetConstants.WIDGET_ENTITY_ID_KEY+"=syn123&" +WidgetConstants.TEXT_KEY + "=Tutorial"+ WidgetConstants.WIDGET_END_MARKDOWN);
			break;
		case INSERT_REGISTER_CHALLENGE_TEAM:
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.REGISTER_CHALLENGE_TEAM_CONTENT_TYPE + "?"+WidgetConstants.CHALLENGE_ID_KEY + "=123&" +WidgetConstants.BUTTON_TEXT_KEY + "=Register team" + WidgetConstants.WIDGET_END_MARKDOWN);
			break;
		case INSERT_CHALLENGE_TEAMS:
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.CHALLENGE_TEAMS_CONTENT_TYPE + "?"+WidgetConstants.CHALLENGE_ID_KEY + "=123"+ WidgetConstants.WIDGET_END_MARKDOWN);
			break;
		case INSERT_CHALLENGE_PARTICIPANTS:
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.CHALLENGE_PARTICIPANTS_CONTENT_TYPE + "?"+WidgetConstants.CHALLENGE_ID_KEY + "=123&"+ WidgetConstants.IS_IN_CHALLENGE_TEAM_KEY +"=false" + WidgetConstants.WIDGET_END_MARKDOWN);
			break;
			
		case BOLD:
			surroundWithTag("**");
			break;
		case ITALIC:
			surroundWithTag("_");
			break;
		case STRIKETHROUGH:
			surroundWithTag("--");
			break;
		case CODE_BLOCK:
			surroundWithTag("\n```\n", "\n```\n", true);
			break;
		case MATH:
			surroundWithTag("$$\\(", "\\)$$", false);
			break;
		case SUBSCRIPT:
			surroundWithTag("~");
			break;
		case SUPERSCRIPT:
			surroundWithTag("^");
			break;
		case H1:
			surroundWithTag("#", "", false);			
			break;
		case H2:
			surroundWithTag("##", "", false);
			break;
		case H3:
			surroundWithTag("###", "", false);
			break;
		case H4:
			surroundWithTag("####", "", false);
			break;
		case H5:
			surroundWithTag("#####", "", false);
			break;
		case H6:
			surroundWithTag("######", "", false);
			break;
		case SAVE:
			view.setSaving(true);
			saveHandler.invoke();
			break;
		case ATTACHMENTS:
			attachmentsHandler.invoke();
			break;
		case PREVIEW:
			showPreview();
			break;
		case CANCEL:
			cancelHandler.invoke();
			break;
		case DELETE:
			deleteHandler.invoke();
			break;
		case SET_PROJECT_BACKGROUND:
			insertNewWidget(WidgetConstants.PROJECT_BACKGROUND_CONTENT_TYPE);
		default:
			throw new IllegalArgumentException(
					"Unrecognized markdown editor action: " + action);
		}
	}
	
	public void surroundWithTag(String tag) {
		surroundWithTag(tag, tag, false);
	}
	
	public void surroundWithTag(String startTag, String endTag, boolean isMultiline) {
		int selectionLength = view.getSelectionLength();
		String text = view.getMarkdown();
		int currentPos = view.getCursorPos();
		String newText = DisplayUtils.surroundText(text, startTag, endTag, isMultiline, currentPos, selectionLength);
		view.setMarkdown(newText);
		view.setCursorPos(currentPos+startTag.length());
		view.setMarkdownFocus();
	}
	
	/**
	 * Pop up an editor to create a new widget of the given class type (class that implements WidgetDescriptor).  Add the given handler, which will be notified when the widget descriptor has been updated.
	 * @param entityId
	 * @param attachmentName
	 * @param handler
	 */
	public void insertNewWidget(String contentTypeKey) {
		WidgetDescriptorUpdatedHandler handler = new WidgetDescriptorUpdatedHandler() {
			@Override
			public void onUpdate(WidgetDescriptorUpdatedEvent event) {
				if (event.getInsertValue()!=null) {
					insertMarkdown(event.getInsertValue());
				}
				if (widgetDescriptorUpdatedHandler != null)
					widgetDescriptorUpdatedHandler.onUpdate(event);
			}
		};
		widgetDescriptorEditor.addWidgetDescriptorUpdatedHandler(handler);
		widgetDescriptorEditor.editNew(wikiKey, contentTypeKey, isWikiEditor);
	}
	
	/**
	 * Pop up an editor to create a new widget of the given class type (class that implements WidgetDescriptor).  Add the given handler, which will be notified when the widget descriptor has been updated.
	 * @param entityId
	 * @param attachmentName
	 * @param handler
	 */
	public void editExistingWidget() {
		if (widgetSelectionState.isWidgetSelected()) {
			final int widgetStartIndex = widgetSelectionState.getWidgetStartIndex();
			final int widgetEndIndex = widgetSelectionState.getWidgetEndIndex();
			String innerText = widgetSelectionState.getInnerWidgetText();
			view.setSelectionRange(widgetStartIndex, innerText.length());
			String contentTypeKey = widgetRegistrar.getWidgetContentType(innerText);
			Map<String, String> widgetDescriptor = widgetRegistrar.getWidgetDescriptor(innerText);
			
			WidgetDescriptorUpdatedHandler handler = new WidgetDescriptorUpdatedHandler() {
				@Override
				public void onUpdate(WidgetDescriptorUpdatedEvent event) {
					//replace old widget text
					String text = view.getMarkdown();
					if (widgetStartIndex > -1 && widgetEndIndex > -1) {
						view.setMarkdown(text.substring(0, widgetStartIndex) + text.substring(widgetEndIndex));
						view.setCursorPos(widgetStartIndex);
						if (event.getInsertValue()!=null) {
							insertMarkdown(event.getInsertValue());
						}
						if (widgetDescriptorUpdatedHandler != null)
							widgetDescriptorUpdatedHandler.onUpdate(event);
					}
				}
			};
			
			widgetDescriptorEditor.addWidgetDescriptorUpdatedHandler(handler);
			widgetDescriptorEditor.editExisting(wikiKey, contentTypeKey, widgetDescriptor, isWikiEditor);
		}
	}

	/**
	 * For testing purposes only
	 * @return
	 */
	public WidgetSelectionState getWidgetSelectionState() {
		return widgetSelectionState;
	}
}
