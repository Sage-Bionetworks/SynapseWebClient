package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesViewImpl extends LayoutContainer implements WikiSubpagesView {

	private Presenter presenter;
		
	@Inject
	public WikiSubpagesViewImpl() {
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		
	};

	@Override
	public void configure(TreeItem root) {
		clear();
		//this widget shows nothing if it doesn't have any pages!
		if (root == null)
			return;
		SimplePanel p = new SimplePanel();
		p.addStyleName("pagesTree");
		//only show the tree if the root has children
		if (root != null && root.getChildCount() > 0) {
			Tree t = new Tree();
			t.addItem(root);
			root.setState(true);
			p.setWidget(t);
		}
			
		this.add(p);
		this.layout(true);
	}	
	
	@Override
	public String getHTML(String href, String title, boolean isCurrentPage) {
		StringBuilder html = new StringBuilder();
		html.append("<a class=\"link");
		if (isCurrentPage)
			html.append(" boldText");
		html.append("\" href=\"");
		html.append(href);
		html.append("\">");
		html.append(title);
		html.append("</a>");
		return html.toString();
	}
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		this.removeAll(true);
	}
}
