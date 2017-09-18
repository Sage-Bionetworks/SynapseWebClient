package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static org.sagebionetworks.web.shared.WidgetConstants.*;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapper;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;


public class WidgetRegistrarImpl implements WidgetRegistrar {
	
	private HashMap<String, String> contentType2FriendlyName = new HashMap<String, String>();
	
	PortalGinInjector ginInjector;
	JSONObjectAdapter adapter;
	
	@Inject
	public WidgetRegistrarImpl(PortalGinInjector ginInjector, JSONObjectAdapter adapter) {
		this.ginInjector = ginInjector;
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
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, DialogCallback dialogCallback) { 
		//use gin to create a new instance of the proper class.
		WidgetEditorPresenter presenter = null;
		if(contentTypeKey.equals(BOOKMARK_CONTENT_TYPE)) {
			presenter = ginInjector.getBookmarkConfigEditor();
		} else if(contentTypeKey.equals(REFERENCE_CONTENT_TYPE)) {
			presenter = ginInjector.getReferenceConfigEditor();
		} else if (contentTypeKey.equals(JOIN_TEAM_CONTENT_TYPE)) {
			presenter = ginInjector.getJoinTeamConfigEditor();
		} else if (contentTypeKey.equals(PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceConfigEditor();
		} else if (contentTypeKey.equals(IMAGE_CONTENT_TYPE)) {
			presenter = ginInjector.getImageConfigEditor();
		} else if (contentTypeKey.equals(IMAGE_LINK_EDITOR_CONTENT_TYPE)) {
			presenter = ginInjector.getImageLinkConfigEditor();
		} else if (contentTypeKey.equals(LINK_CONTENT_TYPE)) {
			presenter = ginInjector.getLinkConfigEditor();
		} else if (contentTypeKey.equals(TABBED_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getTabbedTableConfigEditor();
		} else if (contentTypeKey.equals(API_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseAPICallConfigEditor();
		} else if (contentTypeKey.equals(QUERY_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseQueryConfigEditor();
		} else if (contentTypeKey.equals(LEADERBOARD_CONTENT_TYPE)) {
			presenter = ginInjector.getLeaderboardConfigEditor();
		} else if (contentTypeKey.equals(ATTACHMENT_PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getAttachmentConfigEditor();
		} else if (contentTypeKey.equals(ENTITYLIST_CONTENT_TYPE)) {
			presenter = ginInjector.getEntityListConfigEditor();
		} else if (contentTypeKey.equals(SHINYSITE_CONTENT_TYPE)) {
			presenter = ginInjector.getShinySiteConfigEditor();
		} else if (contentTypeKey.equals(BUTTON_LINK_CONTENT_TYPE)) {
			presenter = ginInjector.getButtonLinkConfigEditor();
		} else if (contentTypeKey.equals(USER_TEAM_BADGE_CONTENT_TYPE)) {
			presenter = ginInjector.getUserTeamConfigEditor();
		} else if (contentTypeKey.equals(VIDEO_CONTENT_TYPE) ||
				contentTypeKey.equals(YOUTUBE_CONTENT_TYPE) ||
				contentTypeKey.equals(VIMEO_CONTENT_TYPE)) {
			presenter = ginInjector.getVideoConfigEditor();
		} else if (contentTypeKey.equals(SYNAPSE_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseTableQueryResultEditor();
		} else if (contentTypeKey.equals(PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getPreviewConfigEditor();
		} else if(contentTypeKey.equals(BIODALLIANCE13_CONTENT_TYPE)) {
			presenter = ginInjector.getBiodallianceEditor();
		} else if (contentTypeKey.equals(CYTOSCAPE_CONTENT_TYPE)) {
			presenter = ginInjector.getCytoscapeConfigEditor();
		} else if(contentTypeKey.equals(PLOT_CONTENT_TYPE)) {
			presenter = ginInjector.getPlotlyConfigEditor();
		} //TODO: add other widget descriptors to this mapping as they become available
		if (presenter != null)
			presenter.configure(wikiKey, model, dialogCallback);
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
	public IsWidget getWidgetRendererForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, Callback widgetRefreshRequired, Long wikiVersionInView) { 
		//use gin to create a new instance of the proper class.
		WidgetRendererPresenter presenter = null;
		if(contentTypeKey.equals(BOOKMARK_CONTENT_TYPE)) {
			presenter = ginInjector.getBookmarkRenderer();
		} else if(contentTypeKey.equals(REFERENCE_CONTENT_TYPE)) {
			presenter = ginInjector.getReferenceRenderer();
		} else if (contentTypeKey.equals(PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceRenderer();
		} else if (contentTypeKey.equals(IMAGE_CONTENT_TYPE) ||
				contentTypeKey.equals(IMAGE_LINK_EDITOR_CONTENT_TYPE)) {
			presenter = ginInjector.getImageRenderer();
		} else if (contentTypeKey.equals(API_TABLE_CONTENT_TYPE) || 
				contentTypeKey.equals(QUERY_TABLE_CONTENT_TYPE) ||
				contentTypeKey.equals(LEADERBOARD_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseAPICallRenderer();
		} else if (contentTypeKey.equals(TOC_CONTENT_TYPE)) {
			presenter = ginInjector.getTableOfContentsRenderer();
		} else if (contentTypeKey.equals(WIKI_FILES_PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getWikiFilesPreviewRenderer();
		} else if (contentTypeKey.equals(ATTACHMENT_PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getAttachmentPreviewRenderer();
		} else if (contentTypeKey.equals(ENTITYLIST_CONTENT_TYPE)) {
			presenter = ginInjector.getEntityListRenderer();
		} else if (contentTypeKey.equals(SHINYSITE_CONTENT_TYPE)) {
			presenter = ginInjector.getShinySiteRenderer();
		} else if (contentTypeKey.equals(USERBADGE_CONTENT_TYPE)) {
			presenter = ginInjector.getUserBadgeWidget();
		} else if (contentTypeKey.equals(USER_TEAM_BADGE_CONTENT_TYPE)) {
			presenter = ginInjector.getUserTeamBadgeWidget();
		} else if (contentTypeKey.equals(JOIN_TEAM_CONTENT_TYPE)) {
			presenter = ginInjector.getJoinTeamWidget();
		} else if (contentTypeKey.equals(SUBMIT_TO_EVALUATION_CONTENT_TYPE) || contentTypeKey.equals(OLD_JOIN_EVALUATION_CONTENT_TYPE)) {
			presenter = ginInjector.getEvaluationSubmissionWidget();
		} else if (contentTypeKey.equals(BUTTON_LINK_CONTENT_TYPE)) {
			presenter = ginInjector.getButtonLinkWidget();
		} else if (contentTypeKey.equals(TUTORIAL_WIZARD_CONTENT_TYPE)) {
			presenter = ginInjector.getTutorialWidgetRenderer();
		} else if (contentTypeKey.equals(WIKI_SUBPAGES_CONTENT_TYPE) || contentTypeKey.equals(NO_AUTO_WIKI_SUBPAGES)) {
			presenter = ginInjector.getEmptyWidget();
		} else if (contentTypeKey.equals(VIDEO_CONTENT_TYPE) ||
				contentTypeKey.equals(YOUTUBE_CONTENT_TYPE) ||
				contentTypeKey.equals(VIMEO_CONTENT_TYPE)) {
			presenter = ginInjector.getVideoWidget();
		} else if (contentTypeKey.equals(SYNAPSE_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseTableQueryResultWikiWidget();
		} else if (contentTypeKey.equals(REGISTER_CHALLENGE_TEAM_CONTENT_TYPE)) {
			presenter = ginInjector.getRegisterChallengeTeamWidget();
		} else if (contentTypeKey.equals(CHALLENGE_TEAMS_CONTENT_TYPE)) {
			presenter = ginInjector.getChallengeTeamsWidget();
		} else if (contentTypeKey.equals(CHALLENGE_PARTICIPANTS_CONTENT_TYPE)) {
			presenter = ginInjector.getChallengeParticipantsWidget();
		} else if (contentTypeKey.equals(PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getPreviewWidget();
		} else if (contentTypeKey.equals(BIODALLIANCE13_CONTENT_TYPE)) {
			presenter = ginInjector.getBiodallianceRenderer();
		} else if (contentTypeKey.equals(CYTOSCAPE_CONTENT_TYPE)) {
			presenter = ginInjector.getCytoscapeRenderer();
		} else if (contentTypeKey.equals(SYNAPSE_FORM_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseTableFormWidget();
		} else if (contentTypeKey.equals(TEAM_MEMBERS_CONTENT_TYPE)) {
			presenter = ginInjector.getTeamMembersWidget();
		} else if (contentTypeKey.equals(TEAM_MEMBER_COUNT_CONTENT_TYPE)) {
			presenter = ginInjector.getTeamMemberCountWidget();
		} else if (contentTypeKey.equals(PLOT_CONTENT_TYPE)) {
			presenter = ginInjector.getPlotlyWidget();
		}
		
		//TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null) {
			LazyLoadWikiWidgetWrapper wrapper = ginInjector.getLazyLoadWikiWidgetWrapper();
			wrapper.configure(presenter, wikiKey, model, widgetRefreshRequired, wikiVersionInView);
			return wrapper;
		}	
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
		registerWidget(BOOKMARK_CONTENT_TYPE, BOOKMARK_FRIENDLY_NAME);
		registerWidget(REFERENCE_CONTENT_TYPE, REFERENCE_FRIENDLY_NAME);
		registerWidget(YOUTUBE_CONTENT_TYPE, YOUTUBE_FRIENDLY_NAME);
		registerWidget(VIMEO_CONTENT_TYPE, VIMEO_FRIENDLY_NAME);
		registerWidget(PROVENANCE_CONTENT_TYPE, PROVENANCE_FRIENDLY_NAME);
		registerWidget(IMAGE_CONTENT_TYPE, IMAGE_FRIENDLY_NAME);
		registerWidget(IMAGE_LINK_EDITOR_CONTENT_TYPE, IMAGE_LINK_FRIENDLY_NAME);
		registerWidget(ATTACHMENT_PREVIEW_CONTENT_TYPE, ATTACHMENT_PREVIEW_FRIENDLY_NAME);
		registerWidget(LINK_CONTENT_TYPE, LINK_FRIENDLY_NAME);
		registerWidget(TABBED_TABLE_CONTENT_TYPE, TABBED_TABLE_FRIENDLY_NAME);
		registerWidget(API_TABLE_CONTENT_TYPE, API_TABLE_FRIENDLY_NAME);
		registerWidget(QUERY_TABLE_CONTENT_TYPE, QUERY_TABLE_FRIENDLY_NAME);
		registerWidget(LEADERBOARD_CONTENT_TYPE, LEADERBOARD_FRIENDLY_NAME);
		registerWidget(ENTITYLIST_CONTENT_TYPE, ENTITYLIST_FRIENDLY_NAME);
		registerWidget(SHINYSITE_CONTENT_TYPE, SHINYSITE_FRIENDLY_NAME);
		registerWidget(SYNAPSE_TABLE_CONTENT_TYPE, SYNAPSE_TABLE_FRIENDLY_NAME);
		registerWidget(TUTORIAL_WIZARD_CONTENT_TYPE, TUTORIAL_WIZARD_FRIENDLY_NAME);
		registerWidget(USER_TEAM_BADGE_CONTENT_TYPE, USER_TEAM_BADGE_FRIENDLY_NAME);
		registerWidget(VIDEO_CONTENT_TYPE, VIDEO_FRIENDLY_NAME);
		registerWidget(PREVIEW_CONTENT_TYPE, PREVIEW_FRIENDLY_NAME);
		registerWidget(JOIN_TEAM_CONTENT_TYPE, JOIN_TEAM_FRIENDLY_NAME);
		registerWidget(BIODALLIANCE13_CONTENT_TYPE, BIODALLIANCE_FRIENDLY_NAME);
		registerWidget(CYTOSCAPE_CONTENT_TYPE, CYTOSCAPE_FRIENDLY_NAME);
		registerWidget(SYNAPSE_FORM_CONTENT_TYPE, SYNAPSE_FORM_FRIENDLY_NAME);
		registerWidget(PLOT_CONTENT_TYPE, PLOT_FRIENDLY_NAME);
	}
	
	public static String getWidgetMarkdown(String contentType, Map<String, String> widgetDescriptor, WidgetRegistrar widgetRegistrar) throws JSONObjectAdapterException {
		StringBuilder sb = new StringBuilder();
		sb.append(WIDGET_START_MARKDOWN);
		sb.append(widgetRegistrar.getMDRepresentation(contentType, widgetDescriptor));
		sb.append(WIDGET_END_MARKDOWN);
		return sb.toString();
	}

}
