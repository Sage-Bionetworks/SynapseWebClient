package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.dom.client.Element;
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
	public void configure(TocItem root) {
		clear();
		//this widget shows nothing if it doesn't have any pages!
		TocItem mainPage = (TocItem) root.getChild(0);
		if (mainPage.getChildCount() == 0)
			return;
		
		//only show the tree if the root has children
		if (mainPage.getChildCount() > 0) {
			TreeStore<ModelData> store = new TreeStore<ModelData>();
			store.add(root.getChildren(), true);
			final TreePanel<ModelData> tree = new TreePanel<ModelData>(store);
			
			tree.setAutoExpand(true);
			tree.addStyleName("pagesTree");
			//Remove folder icons
			tree.getStyle().setNodeCloseIcon(null);
			tree.getStyle().setNodeOpenIcon(null);
			
			//Set the text for the tree
			tree.setLabelProvider(new ModelStringProvider<ModelData>() {
				@Override
				public String getStringValue(ModelData model, String property) {
					return model.toString();
				}	
			});
			
			//Adjust the height of the tree as items are expanded/collapsed
			Listener<TreePanelEvent<ModelData>> expandCollapseListener = new Listener<TreePanelEvent<ModelData>>() {
				public void handleEvent(TreePanelEvent<ModelData> be) {
					Element child = tree.getElement().getFirstChildElement();
					tree.setHeight(child.getClientHeight() + "px");
				}			
			};
			
			tree.addListener(Events.Expand, expandCollapseListener);
			tree.addListener(Events.Collapse, expandCollapseListener);
			this.add(tree);
		}
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
