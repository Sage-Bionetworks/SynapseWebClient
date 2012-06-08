package org.sagebionetworks.web.client.services;

import org.sagebionetworks.web.shared.NodeType;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("node")
public interface NodeService extends RemoteService {	

	public String getNodeAnnotationsJSON(NodeType type, String id);
	
	public String getNodePreview(NodeType type, String id);
	
	public String updateNodeAnnotations(NodeType type, String id, String annotationsJson, String etag);

	public String getNodeType(String resourceId);
}
