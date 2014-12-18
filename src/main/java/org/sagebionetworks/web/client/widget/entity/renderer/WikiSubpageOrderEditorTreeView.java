package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.SubpageNode;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpageOrderEditorTreeView extends IsWidget, SynapseView {

	void configure(SubpageNode overallRoot);
	
	public interface Presenter {
	}
}
