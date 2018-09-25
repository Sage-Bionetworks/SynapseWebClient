package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree.SubpageNavTreeNode;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageNavigationTreeViewImpl extends FlowPanel implements WikiSubpageNavigationTreeView {

	private WikiSubpageNavigationTreeView.Presenter presenter;
	private UnorderedListPanel ulNavTree;

	@Inject
	public WikiSubpageNavigationTreeViewImpl() {
		addStyleName("notopmargin nav bs-sidenav");
		add(new HTML("<h4 class=\"margin-left-15\">Pages</h4>"));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void configure(SubpageNavTreeNode overallRoot) {
		ulNavTree = new UnorderedListPanel();
		ulNavTree.addStyleName("notopmargin nav bs-sidenav margin-bottom-10");
		addTreeItemsRecursive(ulNavTree, overallRoot);
		this.add(ulNavTree);
	}

	@Override
	public void resetNavTree(SubpageNavTreeNode overallRoot) {
		ulNavTree.clear();
		addTreeItemsRecursive(ulNavTree, overallRoot);
	}

	private void addTreeItemsRecursive(UnorderedListPanel ul, final SubpageNavTreeNode root) {
		String styleName = presenter.isCurrentPage(root) ? "active" : "";
		Div w = new Div();
		w.setWidth("100%");
		Anchor l = new Anchor(root.getPageTitle());
		l.addStyleName("subpage-link " + styleName);
		l.setHref("#!Synapse:" + ((Synapse)root.getTargetPlace()).toToken());
		w.add(l);
		
		ul.add(w, styleName);
		if (!root.getChildren().isEmpty()) {
			UnorderedListPanel subList = new UnorderedListPanel();
			subList.addStyleName("nav wiki-tree-nav");
			final Div subListContainer = new Div();
			subListContainer.add(subList);
			ul.add(subListContainer);
			final org.gwtbootstrap3.client.ui.Anchor collapseAnchor = new org.gwtbootstrap3.client.ui.Anchor();
			collapseAnchor.setIcon(IconType.ANGLE_DOWN);
			collapseAnchor.setPull(Pull.RIGHT);
			final org.gwtbootstrap3.client.ui.Anchor expandAnchor = new org.gwtbootstrap3.client.ui.Anchor();
			expandAnchor.setIcon(IconType.ANGLE_RIGHT);
			expandAnchor.setPull(Pull.RIGHT);
			
			ClickHandler collapseClickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					subListContainer.setVisible(false);
					collapseAnchor.setVisible(false);
					expandAnchor.setVisible(true);
					root.setCollapsed(true);
				}
			};
			collapseAnchor.addClickHandler(collapseClickHandler);
			final ClickHandler expandClickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					subListContainer.setVisible(true);
					collapseAnchor.setVisible(true);
					expandAnchor.setVisible(false);
					root.setCollapsed(false);
				}
			}; 
			expandAnchor.addClickHandler(expandClickHandler);
			w.add(collapseAnchor);
			w.add(expandAnchor);
			if (root.isCollapsed()) {
				collapseClickHandler.onClick(null);
			} else {
				expandClickHandler.onClick(null);
			}
			l.addClickHandler(event -> {
				if (!DisplayUtils.isAnyModifierKeyDown(event)) {
					event.preventDefault();
					expandClickHandler.onClick(null);
					presenter.reloadWiki(root);
				}
			});
			for (SubpageNavTreeNode child : root.getChildren()) {
				addTreeItemsRecursive(subList, child);
			}
		} else {
			l.addClickHandler(event -> {
				if (!DisplayUtils.isAnyModifierKeyDown(event)) {
					event.preventDefault();
					presenter.reloadWiki(root);
				}
			});
		}
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

}
