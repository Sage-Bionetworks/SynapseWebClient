package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.SubpageNode;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget.SubPageTreeItem;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageOrderEditorTreeViewImpl extends FlowPanel implements WikiSubpageOrderEditorTreeView {
	
	Map<Widget, V2WikiHeader> item2header;
	Map<V2WikiHeader, Widget> header2item;
	
	
	@Inject
	public WikiSubpageOrderEditorTreeViewImpl() {
		
		//addStyleName("notopmargin nav bs-sidenav");
		addStyleName("margin-top-10 nav bs-sidenav");	// TODO: Switch back
		add(new HTML("<h4 class=\"margin-left-15\">Pages</h4>"));
		
		item2header = new HashMap<Widget, V2WikiHeader>();
		header2item = new HashMap<V2WikiHeader, Widget>();
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configure(SubpageNode overallRoot) {
		UnorderedListPanel ul = new UnorderedListPanel();
		ul.addStyleName("notopmargin nav bs-sidenav margin-bottom-10");
		addTreeItemsRecursive(ul, overallRoot.getChildren());
		this.add(ul);
	}
	
	private void addTreeItemsRecursive(UnorderedListPanel ul, List<SubpageNode> children) {
		for (SubpageNode child : children) {
			ul.add(makeListItem(child.getHeader()));
			if (!child.getChildren().isEmpty()) {
				UnorderedListPanel subList = new UnorderedListPanel();
				// If moved, want to move entire list panel.
				header2item.put(child.getHeader(), subList);
				
				subList.addStyleName("nav");
				subList.setVisible(true);
				ul.add(subList);
				addTreeItemsRecursive(subList, child.getChildren());
			}
		}
	}
	
	private Widget makeListItem(V2WikiHeader header) {
		Anchor anchor = new Anchor(header.getTitle());
		item2header.put(anchor, header);
		header2item.put(header, anchor);
		return anchor;
	}

}
