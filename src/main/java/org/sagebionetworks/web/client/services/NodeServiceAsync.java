package org.sagebionetworks.web.client.services;

import org.sagebionetworks.web.shared.NodeType;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface NodeServiceAsync {
	void getNodeAnnotationsJSON(NodeType type, String id, AsyncCallback<String> callback);

	void getNodePreview(NodeType type, String id, AsyncCallback<String> callback);

	void updateNodeAnnotations(NodeType type, String id, String annotationsJson, String etag, AsyncCallback<String> callback);
	
	void getNodeType(String resourceId, AsyncCallback<String> callback);
}
