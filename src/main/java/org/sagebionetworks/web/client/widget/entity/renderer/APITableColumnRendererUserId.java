package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.widget.APITableColumnConfig;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererUserId implements APITableColumnRenderer {

	private SynapseClientAsync synapseClient;
	private Map<String, String> userId2html;
	private NodeModelCreator nodeModelCreator;
	SageImageBundle sageImageBundle;
	SynapseJSNIUtils jsniUtils;
	List<String> inputUserIds;
	String outputColumnName;
	private Map<String, List<String>> outputColumnData;
	
	@Inject
	public APITableColumnRendererUserId(SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, SageImageBundle sageImageBundle, SynapseJSNIUtils jsniUtils) {
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.sageImageBundle = sageImageBundle;
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
		inputUserIds = columnData.get(inputColumnName);
		for (Iterator iterator = inputUserIds.iterator(); iterator.hasNext();) {
			String userId = (String) iterator.next();
			userId2html.put(userId, emptyString);
		}
		List<String> uniqueUserIds = new ArrayList<String>();
		uniqueUserIds.addAll(userId2html.keySet());
		synapseClient.getUserGroupHeadersById(uniqueUserIds, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					UserGroupHeaderResponsePage response = nodeModelCreator.createJSONEntity(result.getEntityJson(), UserGroupHeaderResponsePage.class);
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
							html.append(DisplayUtils.getIconHtml(sageImageBundle.defaultProfilePicture20()));
							
						html.append("&nbsp;"+ugh.getDisplayName()+"</a>");
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
								for (Iterator iterator = inputUserIds.iterator(); iterator.hasNext();) {
									String userId = (String) iterator.next();
									String html = userId2html.get(userId);
									if (html != null && html.length() > 0)
										out.add(html);
									else
										out.add(userId);
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
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}

			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	

}
