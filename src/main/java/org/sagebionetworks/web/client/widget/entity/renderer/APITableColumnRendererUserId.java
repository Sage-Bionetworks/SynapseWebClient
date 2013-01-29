package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererUserId implements APITableColumnRenderer {

	private SynapseClientAsync synapseClient;
	private Map<String, String> userId2html;
	private NodeModelCreator nodeModelCreator;
	private String baseProfileAttachmentUrl = GWT.getModuleBaseURL()+"profileAttachment";
	SageImageBundle sageImageBundle;
	
	@Inject
	public APITableColumnRendererUserId(SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, SageImageBundle sageImageBundle) {
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.sageImageBundle = sageImageBundle;
	}
	
	@Override
	public void init(List<String> columnData, final AsyncCallback<Void> callback) {
		String emptyString = "";
		userId2html = new HashMap<String, String>();
		//get unique user ids, then ask for the user group headers
		for (Iterator iterator = columnData.iterator(); iterator.hasNext();) {
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
						html.append("<a class=\"link\" href=\"#Profile:"+ugh.getOwnerId()+"\">");
						if (ugh.getPic() != null && ugh.getPic().getPreviewId() != null && ugh.getPic().getPreviewId().length() > 0) {
							//also include a little profile pic in the link
							html.append("<span class=\"iconSpan\"><img src=\"");
							html.append(DisplayUtils.createUserProfileAttachmentUrl(baseProfileAttachmentUrl, ugh.getOwnerId(), ugh.getPic().getPreviewId(), null));
							html.append("\" style=\"width: 20px; height: 20px\"></img></span>");
						}
						else
							html.append(DisplayUtils.getIconHtml(sageImageBundle.defaultProfilePicture20()));
							
						html.append("&nbsp;"+ugh.getDisplayName()+"</a>");
						userId2html.put(ugh.getOwnerId(), html.toString());
					}
						
					
					callback.onSuccess(null);
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
	
	@Override
	public int getColumnCount() {
		return 1;
	}
	@Override
	public String getRenderedColumnName(int rendererColIndex) {
		return null;
	}
	
	@Override
	public String render(String value, int rendererColIndex) {
		String html = userId2html.get(value);
		if (html != null && html.length() > 0) {
			return html;
		} else return value;
	}

}
