package org.sagebionetworks.web.client.services;

import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("layout")
public interface LayoutService extends RemoteService {

	ProvTreeNode layoutProvTree(ProvTreeNode root);
	
	ProvGraph dagLayout(ProvGraph provGraph);
	
}
