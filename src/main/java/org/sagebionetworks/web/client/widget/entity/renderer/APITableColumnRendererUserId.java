package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererUserId implements APITableColumnRenderer {

	private SynapseClientAsync synapseClient;
	private Map<String, String> userId2html;
	private NodeModelCreator nodeModelCreator;
	SynapseJSNIUtils jsniUtils;
	List<String> inputUserIds;
	String outputColumnName;
	private Map<String, List<String>> outputColumnData;
	
	@Inject
	public APITableColumnRendererUserId(SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, SynapseJSNIUtils jsniUtils) {
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsniUtils = jsniUtils;
	}
	
	@Override
	public void init(Map<String, List<String>> columnData,
			APITableColumnConfig config,
			final AsyncCallback<APITableInitializedColumnRenderer> callback) {
		String inputColumnName = APITableWidget.getSingleInputColumnName(config);
		outputColumnName = APITableWidget.getSingleOutputColumnName(config);
		
		String emptyString = "";
		userId2html = new HashMap<String, String>();
		//get unique user ids, then ask for the user group headers
		inputUserIds = APITableWidget.getColumnValues(inputColumnName, columnData);
		if (inputUserIds == null) {
			//user defined an input column that doesn't exist in the service output
			callback.onFailure(new IllegalArgumentException(DisplayConstants.ERROR_API_TABLE_RENDERER_MISSING_INPUT_COLUMN + inputColumnName));
			return;
		}
		
		for (String userId : inputUserIds) {
			if (userId != null)
				userId2html.put(userId, emptyString);
		}
		
		ArrayList<String> uniqueUserIds = new ArrayList<String>();
		uniqueUserIds.addAll(userId2html.keySet());
		synapseClient.getUserGroupHeadersById(uniqueUserIds, new AsyncCallback<UserGroupHeaderResponsePage>() {
			@Override
			public void onSuccess(UserGroupHeaderResponsePage response) {
				for (UserGroupHeader ugh : response.getChildren()){
					StringBuilder html = new StringBuilder();
					html.append("<a class=\"link\" href=\"#!Profile:"+ugh.getOwnerId()+"\">");
					if (ugh.getPic() != null && ugh.getPic().getPreviewId() != null && ugh.getPic().getPreviewId().length() > 0) {
						//also include a little profile pic in the link
						html.append("<span class=\"iconSpan\"><img src=\"");
						html.append(DisplayUtils.createUserProfileAttachmentUrl(jsniUtils.getBaseProfileAttachmentUrl(), ugh.getOwnerId(), ugh.getPic().getPreviewId(), null));
						html.append("\" style=\"width: 20px; height: 20px\"></img></span>");
					}
					else
						html.append(DisplayUtils.getFontelloIcon("user font-size-13 imageButton userProfileImage lightGreyText displayInline"));
						
					html.append("&nbsp;"+DisplayUtils.getDisplayName(ugh)+"</a>");
					userId2html.put(ugh.getOwnerId(), html.toString());
				}

				callback.onSuccess(new APITableInitializedColumnRenderer() {
					@Override
					public Map<String, List<String>> getColumnData() {
						if (outputColumnData == null) {
							//create
							outputColumnData = new HashMap<String, List<String>>();
							//iterate through input user ids to create output list
							List<String> out = new ArrayList<String>();
							for (String userId : inputUserIds) {
								if (userId != null) {
									String html = userId2html.get(userId);
									if (DisplayUtils.isDefined(html))
										out.add(html);
									else
										out.add(userId);
								} else
									out.add("");
							}
							outputColumnData.put(outputColumnName, out);
						}
						return outputColumnData;
					}
					
					@Override
					public List<String> getColumnNames() {
						return APITableWidget.wrap(outputColumnName);
					}
				});
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
}
