package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.inject.Inject;


public class WidgetRegistrarImpl implements WidgetRegistrar {
	
	private HashMap<String, String> contentType2FriendlyName = new HashMap<String, String>();
	
	PortalGinInjector ginInjector;
	NodeModelCreator nodeModelCreator;
	JSONObjectAdapter adapter;
	
	@Inject
	public WidgetRegistrarImpl(PortalGinInjector ginInjector, NodeModelCreator nodeModelCreator, JSONObjectAdapter adapter) {
		this.ginInjector = ginInjector;
		this.nodeModelCreator = nodeModelCreator;
		this.adapter = adapter;
		initWithKnownWidgets();
	}
	

	@Override
	public void registerWidget(String contentTypeKey, String friendlyName) {
		contentType2FriendlyName.put(contentTypeKey, friendlyName);
	}
	
	/**
	 * Given a widget content type, returns the widget that can be used to edit the WidgetDescriptor (model) for that widget type.
	 * @param widgetClass
	 * @return
	 */
	@Override
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, boolean isWiki, Dialog window) { 
		//use gin to create a new instance of the proper class.
		WidgetEditorPresenter presenter = null;
		if(contentTypeKey.equals(WidgetConstants.BOOKMARK_CONTENT_TYPE)) {
			presenter = ginInjector.getBookmarkConfigEditor();
		} else if(contentTypeKey.equals(WidgetConstants.REFERENCE_CONTENT_TYPE)) {
			presenter = ginInjector.getReferenceConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.YOUTUBE_CONTENT_TYPE)) {
			presenter = ginInjector.getYouTubeConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.IMAGE_CONTENT_TYPE)) {
			if (isWiki)
				presenter = ginInjector.getImageConfigEditor();
			else
				presenter = ginInjector.getOldImageConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.LINK_CONTENT_TYPE)) {
			presenter = ginInjector.getLinkConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.TABBED_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getTabbedTableConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.API_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseAPICallConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.QUERY_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseQueryConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getAttachmentConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.ENTITYLIST_CONTENT_TYPE)) {
			presenter = ginInjector.getEntityListConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.SHINYSITE_CONTENT_TYPE)) {
			presenter = ginInjector.getShinySiteConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.BUTTON_LINK_CONTENT_TYPE)) {
			presenter = ginInjector.getButtonLinkConfigEditor();
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.configure(wikiKey, model, window);
		return presenter;
	}

	@Override
	public String getWidgetContentType(Map<String, String> model) {
		return model.get("contentType");
	}
	
	/**
	 * Given a widget content type, returns the widget that can be used to edit the WidgetDescriptor (model) for that widget type.
	 * @param widgetClass
	 * @return
	 */
	@Override
	public WidgetRendererPresenter getWidgetRendererForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, boolean isWiki, Callback widgetRefreshRequired) { 
		//use gin to create a new instance of the proper class.
		WidgetRendererPresenter presenter = null;
		if(contentTypeKey.equals(WidgetConstants.BOOKMARK_CONTENT_TYPE)) {
			presenter = ginInjector.getBookmarkRenderer();
		} else if(contentTypeKey.equals(WidgetConstants.REFERENCE_CONTENT_TYPE)) {
			presenter = ginInjector.getReferenceRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.YOUTUBE_CONTENT_TYPE)) {
			presenter = ginInjector.getYouTubeRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.IMAGE_CONTENT_TYPE)) {
			if (isWiki)
				presenter = ginInjector.getImageRenderer();
			else
				presenter = ginInjector.getOldImageRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.API_TABLE_CONTENT_TYPE) || contentTypeKey.equals(WidgetConstants.QUERY_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseAPICallRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.TOC_CONTENT_TYPE)) {
			presenter = ginInjector.getTableOfContentsRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.WIKI_SUBPAGES_CONTENT_TYPE)) {
			presenter = ginInjector.getWikiSubpagesRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.WIKI_FILES_PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getWikiFilesPreviewRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getAttachmentPreviewRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.ENTITYLIST_CONTENT_TYPE)) {
			presenter = ginInjector.getEntityListRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.SHINYSITE_CONTENT_TYPE)) {
			presenter = ginInjector.getShinySiteRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.USERBADGE_CONTENT_TYPE)) {
			presenter = ginInjector.getUserBadgeWidget();
		} else if (contentTypeKey.equals(WidgetConstants.JOIN_TEAM_CONTENT_TYPE)) {
			presenter = ginInjector.getJoinTeamWidget();
		} else if (contentTypeKey.equals(WidgetConstants.SUBMIT_TO_EVALUATION_CONTENT_TYPE) || contentTypeKey.equals(WidgetConstants.OLD_JOIN_EVALUATION_CONTENT_TYPE)) {
			presenter = ginInjector.getEvaluationSubmissionWidget();
		} else if (contentTypeKey.equals(WidgetConstants.BUTTON_LINK_CONTENT_TYPE)) {
			presenter = ginInjector.getButtonLinkWidget();
		} else if (contentTypeKey.equals(WidgetConstants.TUTORIAL_WIZARD_CONTENT_TYPE)) {
			presenter = ginInjector.getTutorialWidgetRenderer();
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.configure(wikiKey, model, widgetRefreshRequired);
		return presenter;
	}
	@Override
	public String getFriendlyTypeName(String contentTypeKey) {
		String friendlyName = contentType2FriendlyName.get(contentTypeKey);
		if (friendlyName != null)
			return friendlyName;
		else return "Widget";
	}

	
	@Override
	public String getMDRepresentation(String contentType, Map<String, String> model){
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(contentType);
		char prefix = '?';
		for (Iterator iterator = model.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Object value = model.get(key);
			//only include it in the md representation if the value is not null
			if (value != null) {
				urlBuilder.append(prefix).append(key).append('=').append(WidgetEncodingUtil.encodeValue(value.toString()));
			}
			prefix = '&';
		}
		return urlBuilder.toString();
	}
	

	@Override
	public Map<String, String> getWidgetDescriptor(String md) {
		if (md == null || md.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + md);
		int delimeter = md.indexOf("?");
		Map<String, String> model = new HashMap<String, String>();
		if (delimeter < 0) {
			//no parameters
			return model;
		}
		String contentTypeKey = md.substring(0, delimeter);
		String allParamsString = md.substring(delimeter+1);
		String[] keyValuePairs = allParamsString.split("&");
		
		for (int j = 0; j < keyValuePairs.length; j++) {
			String[] keyValue = keyValuePairs[j].split("=");
			model.put(keyValue[0], WidgetEncodingUtil.decodeValue(keyValue[1]));
		}
		return model;
	}

	@Override
	public String getWidgetContentType(String mdRepresentation) {
		if (mdRepresentation == null || mdRepresentation.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + mdRepresentation);
		String decodedMd = mdRepresentation;
		if (decodedMd == null || decodedMd.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + decodedMd);
		int delimeter = decodedMd.indexOf("?");
		if (delimeter < 0) {
			//maybe it has no parameters
			return decodedMd;
		}
		return decodedMd.substring(0, delimeter);
	}
	
	private void initWithKnownWidgets() {
		registerWidget(WidgetConstants.BOOKMARK_CONTENT_TYPE, WidgetConstants.BOOKMARK_FRIENDLY_NAME);
		registerWidget(WidgetConstants.REFERENCE_CONTENT_TYPE, WidgetConstants.REFERENCE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.YOUTUBE_CONTENT_TYPE, WidgetConstants.YOUTUBE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.PROVENANCE_CONTENT_TYPE, WidgetConstants.PROVENANCE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.IMAGE_CONTENT_TYPE, WidgetConstants.IMAGE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE, WidgetConstants.ATTACHMENT_PREVIEW_FRIENDLY_NAME);
		registerWidget(WidgetConstants.LINK_CONTENT_TYPE, WidgetConstants.LINK_FRIENDLY_NAME);
		registerWidget(WidgetConstants.TABBED_TABLE_CONTENT_TYPE, WidgetConstants.TABBED_TABLE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.API_TABLE_CONTENT_TYPE, WidgetConstants.API_TABLE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.QUERY_TABLE_CONTENT_TYPE, WidgetConstants.QUERY_TABLE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.ENTITYLIST_CONTENT_TYPE, WidgetConstants.ENTITYLIST_FRIENDLY_NAME);
		registerWidget(WidgetConstants.SHINYSITE_CONTENT_TYPE, WidgetConstants.SHINYSITE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.TUTORIAL_WIZARD_CONTENT_TYPE, WidgetConstants.TUTORIAL_WIZARD_FRIENDLY_NAME);
	}
	
	public static String getWidgetMarkdown(String contentType, Map<String, String> widgetDescriptor, WidgetRegistrar widgetRegistrar) throws JSONObjectAdapterException {
		StringBuilder sb = new StringBuilder();
		sb.append(WidgetConstants.WIDGET_START_MARKDOWN);
		sb.append(widgetRegistrar.getMDRepresentation(contentType, widgetDescriptor));
		sb.append(WidgetConstants.WIDGET_END_MARKDOWN);
		return sb.toString();
	}

}
