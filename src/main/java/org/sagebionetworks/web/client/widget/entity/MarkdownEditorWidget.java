package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Modal used to edit a wiki page.  Knows how to Save, Delete, and user can cancel. 
 * 
 * @author Jay
 *
 */
public class MarkdownEditorWidget implements MarkdownEditorWidgetView.Presenter, SynapseWidgetPresenter {
	
	// units are px
	public final int MIN_EDITOR_HEIGHT = 160;
	public final int EDITOR_BOTTOM_MARGIN = 40;
	
	private SynapseClientAsync synapseClient;
	private CookieProvider cookies;
	private GWTWrapper gwt;
	public static WikiPageKey formattingGuideWikiPageKey;
	private MarkdownEditorWidgetView view;
	private BaseEditWidgetDescriptorPresenter widgetDescriptorEditor;
	private WidgetRegistrar widgetRegistrar;
	private WidgetSelectionState widgetSelectionState;
	private WikiPage currentPage;
	private WikiPageKey wikiKey;
	private CallbackP<WikiPage> wikiPageUpdatedHandler;
	private GlobalApplicationState globalApplicationState;
	private PortalGinInjector ginInjector;
	private MarkdownWidget markdownPreview;
	private MarkdownWidget formattingGuide;
	
	@Inject
	public MarkdownEditorWidget(MarkdownEditorWidgetView view, 
			SynapseClientAsync synapseClient,
			CookieProvider cookies,
			GWTWrapper gwt,
			BaseEditWidgetDescriptorPresenter widgetDescriptorEditor,
			WidgetRegistrar widgetRegistrar,
			GlobalApplicationState globalApplicationState,
			PortalGinInjector ginInjector) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		this.cookies = cookies;
		this.widgetDescriptorEditor = widgetDescriptorEditor;
		this.widgetRegistrar = widgetRegistrar;
		this.globalApplicationState = globalApplicationState;
		widgetSelectionState = new WidgetSelectionState();
		markdownPreview = ginInjector.getMarkdownWidget();
		formattingGuide = ginInjector.getMarkdownWidget();
		view.setPresenter(this);
		view.setMarkdownPreviewWidget(markdownPreview.asWidget());
		view.setFormattingGuideWidget(formattingGuide.asWidget());
	}
	
	/**
	 * 
	 * @param ownerId
	 * @param ownerType
	 * @param markdownTextArea
	 * @param formPanel
	 * @param finishedUploadingCallback
	 * @param closeHandler if no save handler is specified, then a Save button is not shown.  If it is specified, then Save is shown and saveClicked is called when that button is clicked.
	 */
	public void configure(final WikiPageKey wikiKey, CallbackP<WikiPage> wikiPageUpdatedHandler) {
		this.wikiPageUpdatedHandler = wikiPageUpdatedHandler;
		this.wikiKey = wikiKey;
		
		//clear view state
		view.clear();
		view.setAttachmentCommandsVisible(true);
		view.setAlphaCommandsVisible(DisplayUtils.isInTestWebsite(cookies));
		if (formattingGuideWikiPageKey == null) {
			//get the page name to wiki key map
			getFormattingGuideWikiKey(new CallbackP<WikiPageKey>() {
				@Override
				public void invoke(WikiPageKey key) {
					formattingGuideWikiPageKey = key;
					initWikiPage();
				}
			});
		} else {
			initWikiPage();
		}
	}
	
	public void initWikiPage() {
		//get the wiki page
		synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage result) {
				try {
					configure(result);
				} catch (Exception e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				//if it is because of a missing root (and we have edit permission), then the pages browser should have a Create Wiki button
				if (caught instanceof NotFoundException) {
					//create a wiki page and configure with that
					createNewWiki();
				} else {
					view.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
				}
			}
		});
	}
	
	public void createNewWiki() {
		WikiPage page = new WikiPage();
		synapseClient.createV2WikiPageWithV1(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), page, new AsyncCallback<WikiPage>(){
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(WikiPage result) {
				configure(result);
			}});
	}
	
	public void configure(WikiPage page) {
		currentPage = page;
		view.configure(currentPage.getMarkdown());
		view.setTitleEditorVisible(currentPage.getParentWikiId() != null);
		view.setTitle(currentPage.getTitle());
		formattingGuide.loadMarkdownFromWikiPage(formattingGuideWikiPageKey, true);
		globalApplicationState.setIsEditing(true);
		setMarkdownTextAreaHandlers();
  	  	resizeMarkdownTextArea();
		view.setDeleteClickHandler(getDeleteClickHandler());
		view.showEditorModal();
		gwt.scheduleExecution(new Callback() {
			@Override
			public void invoke() {
		    	  resizeMarkdownTextArea();
		    	  if (view.isEditorModalAttachedAndVisible()) 
			    	  gwt.scheduleExecution(this, 500);
			}
		}, 500);	
	}
	
	private void setMarkdownTextAreaHandlers() {		
		view.addTextAreaKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				resizeMarkdownTextArea();
			}
		});
		view.addTextAreaClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				markdownEditorClicked();
			}
		});
	}
	
	public ClickHandler getDeleteClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				view.confirm(DisplayConstants.PROMPT_SURE_DELETE + " Page and Subpages?", new ConfirmCallback() {
					@Override
					public void callback(boolean isConfirmed) {
						if (isConfirmed)
							handleCommand(MarkdownEditorAction.DELETE);
					}
				});
			}
		};
	}
	
	public void resizeMarkdownTextArea() {
		long height = view.getScrollHeight(view.getMarkdown());
		if (height < MIN_EDITOR_HEIGHT)
			height = MIN_EDITOR_HEIGHT;
		view.setMarkdownHeight((height + EDITOR_BOTTOM_MARGIN) + "px");
	}
	
	public void getFormattingGuideWikiKey(final CallbackP<WikiPageKey> callback) {
		synapseClient.getPageNameToWikiKeyMap(new AsyncCallback<HashMap<String,WikiPageKey>>() {
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
		markdownPreview.configure(view.getMarkdown(), wikiKey, null);
		view.showPreviewModal();
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
		case INSERT_BIODALLIANCE_GENOME_BROWSER:
			insertNewWidget(WidgetConstants.BIODALLIANCE13_CONTENT_TYPE);
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
			insertNewWidget(WidgetConstants.JOIN_TEAM_CONTENT_TYPE);
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
			insertMarkdown(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.SUBMIT_TO_EVALUATION_CONTENT_TYPE + "?"+WidgetConstants.CHALLENGE_ID_KEY+"=123&" +WidgetConstants.UNAVAILABLE_MESSAGE + "=Join the team to submit to the evaluation" + WidgetConstants.WIDGET_END_MARKDOWN);
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
		case INSERT_VIMEO:
			insertNewWidget(WidgetConstants.VIMEO_CONTENT_TYPE);
			break;
		case INSERT_YOU_TUBE:
			insertNewWidget(WidgetConstants.YOUTUBE_CONTENT_TYPE);
			break;
		case INSERT_CYTOSCAPE_JS:
			insertNewWidget(WidgetConstants.CYTOSCAPE_CONTENT_TYPE);
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
		case INSERT_PREVIEW:
			insertNewWidget(WidgetConstants.PREVIEW_CONTENT_TYPE);
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
			saveClicked();
			break;
		case PREVIEW:
			showPreview();
			break;
		case DELETE:
			deleteClicked();
			break;
		case CANCEL:
			cancelClicked();
			break;
		case SET_PROJECT_BACKGROUND:
			insertNewWidget(WidgetConstants.PROJECT_BACKGROUND_CONTENT_TYPE);
		default:
			throw new IllegalArgumentException(
					"Unrecognized markdown editor action: " + action);
		}
	}
	
	public void cancelClicked() {
		globalApplicationState.setIsEditing(false);
		view.hideEditorModal();
		//TODO: update should not be necessary, but widget loading is based on div ids that are overloaded when the formatting guide is initialized
		if (wikiPageUpdatedHandler != null)
			wikiPageUpdatedHandler.invoke(currentPage);
	}
	
	public void saveClicked() {
		view.setSaving(true);
		currentPage.setTitle(view.getTitle());
		currentPage.setMarkdown(view.getMarkdown());
		synapseClient.updateV2WikiPageWithV1(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), currentPage, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage result) {
				//we have successfully saved, so we are no longer editing
				globalApplicationState.setIsEditing(false);
				view.hideEditorModal();
				if (wikiPageUpdatedHandler != null)
					wikiPageUpdatedHandler.invoke(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_SAVING_WIKI + caught.getMessage());
			}
		});
	}
	
	public void deleteClicked() {
		synapseClient.deleteV2WikiPage(wikiKey, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				globalApplicationState.setIsEditing(false);
				view.hideEditorModal();
				globalApplicationState.getPlaceChanger().goTo(new Synapse(wikiKey.getOwnerObjectId()));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});	
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
				handleDescriptorUpdatedEvent(event);
			}
		};
		widgetDescriptorEditor.addWidgetDescriptorUpdatedHandler(handler);
		widgetDescriptorEditor.editNew(wikiKey, contentTypeKey);
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
						handleDescriptorUpdatedEvent(event);
					}
				}
			};
			
			widgetDescriptorEditor.addWidgetDescriptorUpdatedHandler(handler);
			widgetDescriptorEditor.editExisting(wikiKey, contentTypeKey, widgetDescriptor);
		}
	}

	public void handleDescriptorUpdatedEvent(WidgetDescriptorUpdatedEvent event) {
		addFileHandles(event.getNewFileHandleIds());
		removeFileHandles(event.getDeletedFileHandleIds());
	}

	public void addFileHandles(List<String> fileHandleIds) {
		//update file handle ids if set
        if (fileHandleIds != null && fileHandleIds.size() > 0 ) {
	        HashSet<String> fileHandleIdsSet = new HashSet<String>();
	        fileHandleIdsSet.addAll(currentPage.getAttachmentFileHandleIds());
	        fileHandleIdsSet.addAll(fileHandleIds);
	        currentPage.getAttachmentFileHandleIds().clear();
	        currentPage.getAttachmentFileHandleIds().addAll(fileHandleIdsSet);
        }
	}
	
	public void removeFileHandles(List<String> fileHandleIds) {
	    if (fileHandleIds != null && fileHandleIds.size() > 0 ) {
	    	currentPage.getAttachmentFileHandleIds().removeAll(fileHandleIds);
	    }	
	}
	/**
	 * For testing purposes only
	 * @return
	 */
	public WidgetSelectionState getWidgetSelectionState() {
		return widgetSelectionState;
	}
	
	public void setTitleEditorVisible(boolean visible){
		view.setTitleEditorVisible(visible);
	}
	public String getTitle() {
		return view.getTitle();
	}
	public void setTitle(String title) {
		view.setTitle(title);
	}
	public WikiPage getWikiPage() {
		return currentPage;
	}
}
