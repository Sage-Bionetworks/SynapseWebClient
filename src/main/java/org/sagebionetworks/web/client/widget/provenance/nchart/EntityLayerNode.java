package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

public class EntityLayerNode  extends LayerNode {

	public EntityLayerNode(ProvGraphNode entityNode) {
		this.setEvent(entityNode.getId());
		
		List<String> members = new ArrayList<String>();
		members.add(entityNode.getId());
		this.setSubnodes(members);
	}
}
