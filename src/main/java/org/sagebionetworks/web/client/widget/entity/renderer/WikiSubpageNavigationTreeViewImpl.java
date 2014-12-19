package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree.SubpageNavTreeNode;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageNavigationTreeViewImpl extends FlowPanel implements WikiSubpageNavigationTreeView {
	
	private GlobalApplicationState globalAppState;
	
	@Inject
	public WikiSubpageNavigationTreeViewImpl(GlobalApplicationState globalAppState) {
		this.globalAppState = globalAppState;
		
		addStyleName("notopmargin nav bs-sidenav");
		add(new HTML("<h4 class=\"margin-left-15\">Pages</h4>"));
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void configure(SubpageNavTreeNode overallRoot) {
		UnorderedListPanel ul = new UnorderedListPanel();
		ul.addStyleName("notopmargin nav bs-sidenav margin-bottom-10");
		addTreeItemsRecursive(ul, overallRoot);
		this.add(ul);
	}
	
	private void addTreeItemsRecursive(UnorderedListPanel ul, SubpageNavTreeNode root) {
		String styleName = root.isCurrentPage() ? "active" : "";
		ul.add(makeListItem(root), styleName);
		if (!root.getChildren().isEmpty()) {
			UnorderedListPanel subList = new UnorderedListPanel();
			
			subList.addStyleName("nav");
			ul.add(subList);
			for (SubpageNavTreeNode child : root.getChildren()) {
				addTreeItemsRecursive(subList, child);
			}
		}
	}
	
	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	private Widget makeListItem(final SubpageNavTreeNode node) {
		final Anchor l = new Anchor(node.getText());
		l.addStyleName("link");
		l.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalAppState.getPlaceChanger().goTo(node.getTargetPlace());
			}
		});
		return l;
	}
}
