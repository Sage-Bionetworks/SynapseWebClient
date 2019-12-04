package org.sagebionetworks.web.client.widget.entity.registration;

import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.ATTACHMENT_PREVIEW_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.BIODALLIANCE13_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.BIODALLIANCE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.BOOKMARK_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.BOOKMARK_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.BUTTON_LINK_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.CHALLENGE_PARTICIPANTS_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.CHALLENGE_TEAMS_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.COLLAPSED_SECTION;
import static org.sagebionetworks.web.shared.WidgetConstants.CYTOSCAPE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.CYTOSCAPE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.DETAILS_SUMMARY_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.ENTITYLIST_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.ENTITYLIST_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_LINK_EDITOR_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_LINK_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.JOIN_TEAM_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.JOIN_TEAM_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.LEADERBOARD_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.LEADERBOARD_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.LINK_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.LINK_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.NO_AUTO_WIKI_SUBPAGES;
import static org.sagebionetworks.web.shared.WidgetConstants.OLD_JOIN_EVALUATION_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.PLOT_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.PLOT_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.PREVIEW_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.PREVIEW_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.PROVENANCE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.PROVENANCE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.QUERY_TABLE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.QUERY_TABLE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.REFERENCE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.REFERENCE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.REGISTER_CHALLENGE_TEAM_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.SHINYSITE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.SHINYSITE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.SUBMIT_TO_EVALUATION_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.SYNAPSE_FORM_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.SYNAPSE_FORM_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.SYNAPSE_TABLE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.SYNAPSE_TABLE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.TABBED_TABLE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.TABBED_TABLE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.TEAM_MEMBERS_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.TEAM_MEMBERS_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.TEAM_MEMBER_COUNT_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.TEAM_MEMBER_COUNT_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.TOC_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.TUTORIAL_WIZARD_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.TUTORIAL_WIZARD_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.USERBADGE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.USER_TEAM_BADGE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.USER_TEAM_BADGE_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.VIDEO_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.VIDEO_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.VIMEO_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.VIMEO_FRIENDLY_NAME;
import static org.sagebionetworks.web.shared.WidgetConstants.WIDGET_END_MARKDOWN;
import static org.sagebionetworks.web.shared.WidgetConstants.WIDGET_START_MARKDOWN;
import static org.sagebionetworks.web.shared.WidgetConstants.WIKI_FILES_PREVIEW_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.WIKI_SUBPAGES_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.YOUTUBE_CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.YOUTUBE_FRIENDLY_NAME;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
	 * Given a widget content type, returns the widget that can be used to edit the WidgetDescriptor
	 * (model) for that widget type.
	 * 
	 * @param widgetClass
	 * @return
	 */
	@Override
	public void getWidgetEditorForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, DialogCallback dialogCallback, AsyncCallback<WidgetEditorPresenter> callback) {
		GWT.runAsync(WidgetEditorPresenter.class, new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				WidgetEditorPresenter presenter = getWidgetEditorForWidgetDescriptor(wikiKey, contentTypeKey, model, dialogCallback);
				callback.onSuccess(presenter);
			}

			@Override
			public void onFailure(Throwable reason) {
				callback.onFailure(reason);
			}
		});
	}

	/**
	 * Is public for testing purposes only
	 * 
	 * @param wikiKey
	 * @param contentTypeKey
	 * @param model
	 * @param dialogCallback
	 * @return
	 */
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, DialogCallback dialogCallback) {
		// use gin to create a new instance of the proper class.
		WidgetEditorPresenter presenter = null;
		if (contentTypeKey.equals(REFERENCE_CONTENT_TYPE)) {
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
		} else if (contentTypeKey.equals(DETAILS_SUMMARY_CONTENT_TYPE)) {
			presenter = ginInjector.getDetailsSummaryConfigEditor();
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
		} else if (contentTypeKey.equals(SUBMIT_TO_EVALUATION_CONTENT_TYPE) || contentTypeKey.equals(OLD_JOIN_EVALUATION_CONTENT_TYPE)) {
			presenter = ginInjector.getEvaluationSubmissionConfigEditor();
		} else if (contentTypeKey.equals(USER_TEAM_BADGE_CONTENT_TYPE)) {
			presenter = ginInjector.getUserTeamConfigEditor();
		} else if (contentTypeKey.equals(VIDEO_CONTENT_TYPE) || contentTypeKey.equals(YOUTUBE_CONTENT_TYPE) || contentTypeKey.equals(VIMEO_CONTENT_TYPE)) {
			presenter = ginInjector.getVideoConfigEditor();
		} else if (contentTypeKey.equals(SYNAPSE_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseTableQueryResultEditor();
		} else if (contentTypeKey.equals(PREVIEW_CONTENT_TYPE)) {
			presenter = ginInjector.getPreviewConfigEditor();
		} else if (contentTypeKey.equals(BIODALLIANCE13_CONTENT_TYPE)) {
			presenter = ginInjector.getBiodallianceEditor();
		} else if (contentTypeKey.equals(CYTOSCAPE_CONTENT_TYPE)) {
			presenter = ginInjector.getCytoscapeConfigEditor();
		} else if (contentTypeKey.equals(PLOT_CONTENT_TYPE)) {
			presenter = ginInjector.getPlotlyConfigEditor();
		} else if (contentTypeKey.equals(SYNAPSE_FORM_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseFormConfigEditor();
		} else if (contentTypeKey.equals(TEAM_MEMBER_COUNT_CONTENT_TYPE) || contentTypeKey.equals(TEAM_MEMBERS_CONTENT_TYPE)) {
			presenter = ginInjector.getTeamSelectEditor();
		} // TODO: add other widget descriptors to this mapping as they become available
		if (presenter != null)
			presenter.configure(wikiKey, model, dialogCallback);

		return presenter;
	}

	@Override
	public String getWidgetContentType(Map<String, String> model) {
		return model.get("contentType");
	}

	/**
	 * Given a widget content type, returns the widget that can be used to edit the WidgetDescriptor
	 * (model) for that widget type.
	 * 
	 * @param widgetClass
	 * @return
	 */
	@Override
	public IsWidget getWidgetRendererForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, Callback widgetRefreshRequired, Long wikiVersionInView) {
		LazyLoadWikiWidgetWrapper wrapper = ginInjector.getLazyLoadWikiWidgetWrapper();
		wrapper.configure(contentTypeKey, wikiKey, model, widgetRefreshRequired, wikiVersionInView);
		return wrapper;
	}

	@Override
	public void getWidgetRendererForWidgetDescriptorAfterLazyLoad(String contentTypeKey, AsyncCallback<WidgetRendererPresenter> callback) {
		// use gin to create a new instance of the proper class.
		GWT.runAsync(WidgetRendererPresenter.class, new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				getWidgetRendererForWidgetDescriptorAfterLazyLoadAndCodeSplit(contentTypeKey, callback);
			}

			@Override
			public void onFailure(Throwable reason) {
				callback.onFailure(reason);
			}
		});
	}

	public void getWidgetRendererForWidgetDescriptorAfterLazyLoadAndCodeSplit(String contentTypeKey, AsyncCallback<WidgetRendererPresenter> callback) {
		WidgetRendererPresenter presenter = getWidgetRendererForWidgetDescriptorAfterLazyLoad(contentTypeKey);
		if (presenter == null) {
			callback.onFailure(new NotFoundException("Widget renderer not found for type \"" + contentTypeKey + "\""));
		} else {
			callback.onSuccess(presenter);
		}

	}

	/**
	 * Is public for testing purposes only
	 * 
	 * @param contentTypeKey
	 * @return
	 */
	public WidgetRendererPresenter getWidgetRendererForWidgetDescriptorAfterLazyLoad(String contentTypeKey) {
		WidgetRendererPresenter presenter = null;
		if (contentTypeKey.equals(BOOKMARK_CONTENT_TYPE)) {
			presenter = ginInjector.getBookmarkRenderer();
		} else if (contentTypeKey.equals(REFERENCE_CONTENT_TYPE)) {
			presenter = ginInjector.getReferenceRenderer();
		} else if (contentTypeKey.equals(PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceRenderer();
		} else if (contentTypeKey.equals(IMAGE_CONTENT_TYPE) || contentTypeKey.equals(IMAGE_LINK_EDITOR_CONTENT_TYPE)) {
			presenter = ginInjector.getImageRenderer();
		} else if (contentTypeKey.equals(API_TABLE_CONTENT_TYPE) || contentTypeKey.equals(QUERY_TABLE_CONTENT_TYPE) || contentTypeKey.equals(LEADERBOARD_CONTENT_TYPE)) {
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
		} else if (contentTypeKey.equals(VIDEO_CONTENT_TYPE) || contentTypeKey.equals(YOUTUBE_CONTENT_TYPE) || contentTypeKey.equals(VIMEO_CONTENT_TYPE)) {
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
		return presenter;
	}

	@Override
	public String getFriendlyTypeName(String contentTypeKey) {
		String friendlyName = contentType2FriendlyName.get(contentTypeKey);
		if (friendlyName != null)
			return friendlyName;
		else
			return "Widget";
	}


	@Override
	public String getMDRepresentation(String contentType, Map<String, String> model) {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(contentType);
		char prefix = '?';
		for (Iterator iterator = model.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Object value = model.get(key);
			// only include it in the md representation if the value is not null
			if (value != null) {
				urlBuilder.append(prefix).append(key).append('=').append(WidgetEncodingUtil.encodeValue(value.toString()));
			}
			prefix = '&';
		}
		return urlBuilder.toString();
	}

	@Override
	public Map<String, String> getWidgetDescriptor(String md) {
		if (md == null || md.length() == 0)
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + md);
		int delimeter = md.indexOf("?");
		Map<String, String> model = new HashMap<String, String>();
		if (delimeter < 0) {
			// no parameters
			return model;
		}
		String contentTypeKey = md.substring(0, delimeter);
		String allParamsString = md.substring(delimeter + 1);
		String[] keyValuePairs = allParamsString.split("&");

		for (int j = 0; j < keyValuePairs.length; j++) {
			String[] keyValue = keyValuePairs[j].split("=");
			model.put(keyValue[0], WidgetEncodingUtil.decodeValue(keyValue[1]));
		}
		return model;
	}

	@Override
	public String getWidgetContentType(String mdRepresentation) {
		if (mdRepresentation == null || mdRepresentation.length() == 0)
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + mdRepresentation);
		String decodedMd = mdRepresentation;
		if (decodedMd == null || decodedMd.length() == 0)
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + decodedMd);
		int delimeter = decodedMd.indexOf("?");
		if (delimeter < 0) {
			// maybe it has no parameters
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
		registerWidget(DETAILS_SUMMARY_CONTENT_TYPE, COLLAPSED_SECTION);
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
		registerWidget(TEAM_MEMBER_COUNT_CONTENT_TYPE, TEAM_MEMBER_COUNT_FRIENDLY_NAME);
		registerWidget(TEAM_MEMBERS_CONTENT_TYPE, TEAM_MEMBERS_FRIENDLY_NAME);
		registerWidget(SUBMIT_TO_EVALUATION_CONTENT_TYPE, "Submit to Evaluation Queue");
	}

	public static String getWidgetMarkdown(String contentType, Map<String, String> widgetDescriptor, WidgetRegistrar widgetRegistrar) throws JSONObjectAdapterException {
		StringBuilder sb = new StringBuilder();
		sb.append(WIDGET_START_MARKDOWN);
		sb.append(widgetRegistrar.getMDRepresentation(contentType, widgetDescriptor));
		sb.append(WIDGET_END_MARKDOWN);
		return sb.toString();
	}

}
