package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree.SubpageNavTreeNode;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
		HorizontalPanel w = new HorizontalPanel();
		w.setWidth("100%");
		w.setHeight("25px");
		FocusPanel anchorContainer = new FocusPanel();
		anchorContainer.addStyleName("imageButton");
		Anchor l = new Anchor(root.getPageTitle());
		l.addStyleName("subpage-link " + styleName);
		anchorContainer.add(l);
		w.add(anchorContainer);
		
		ul.add(w, styleName);
		if (!root.getChildren().isEmpty()) {
			UnorderedListPanel subList = new UnorderedListPanel();
			subList.addStyleName("nav");
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
			FlowPanel iconContainer = new FlowPanel();
			iconContainer.add(collapseAnchor);
			iconContainer.add(expandAnchor);
			w.add(iconContainer);
			if (root.isCollapsed()) {
				collapseClickHandler.onClick(null);
			} else {
				expandClickHandler.onClick(null);
			}
			anchorContainer.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					expandClickHandler.onClick(null);
					presenter.reloadWiki(root);
				}
			});
			for (SubpageNavTreeNode child : root.getChildren()) {
				addTreeItemsRecursive(subList, child);
			}
		} else {
			anchorContainer.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
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
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

}
