package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PagesBrowserViewImpl extends LayoutContainer implements PagesBrowserView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
		
	@Inject
	public PagesBrowserViewImpl(
			IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		
	};

	@Override
	public void configure(boolean canEdit, TreeItem root) {
		this.removeAll(true);
		//this widget shows nothing if the user can't edit the entity and it doesn't have any pages!
		if (!canEdit && root == null)
			return;
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("span-24 notopmargin");
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		LayoutContainer titleBar = new LayoutContainer();		
		titleBar.setStyleName("left span-17 notopmargin");
		
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h5>" + DisplayConstants.PAGES + "</h5>");
		titleBar.add(new HTML(shb.toSafeHtml()));

		//only show the tree if the root has children
		if (root != null && root.getChildCount() > 0) {
			LayoutContainer files = new LayoutContainer();
			files.setStyleName("span-24 notopmargin");
			Tree t = new Tree();
			t.addItem(root);
			root.setState(true);
			files.add(t);
			lc.add(titleBar);
			lc.add(files);
		}
			
		lc.layout(true);
		this.add(lc);
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
	}
}
