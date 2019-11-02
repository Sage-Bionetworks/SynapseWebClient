package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree.SubpageNavTreeNode;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpageNavigationTreeView extends IsWidget, SynapseView {

	void configure(SubpageNavTreeNode overallRoot);

	public interface Presenter {

		public void reloadWiki(SubpageNavTreeNode node);

		public boolean isCurrentPage(SubpageNavTreeNode root);
	}

	public void setPresenter(Presenter presenter);

	public void resetNavTree(SubpageNavTreeNode overallRoot);
}
