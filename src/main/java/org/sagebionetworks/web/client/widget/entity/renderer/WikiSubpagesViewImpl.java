package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;

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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesViewImpl extends LayoutContainer implements WikiSubpagesView {

	private Presenter presenter;
	private GlobalApplicationState globalAppState;
	
	@Inject
	public WikiSubpagesViewImpl(GlobalApplicationState globalAppState) {
		this.setLayout(new FitLayout());
		this.globalAppState = globalAppState;
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		
	};

	@Override
	public void configure(TocItem root) {
		clear();
		final FlowPanel treePanel = new FlowPanel();
		treePanel.addStyleName("well well-small display-table");
		treePanel.add(new HTML("<h3>Wiki Subpages</h3>"));
		
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
					return "<span class=\"link\">" + model.toString() + "</span>";
				}	
			});
			
			//Adjust the height of the tree as items are expanded/collapsed
			Listener<TreePanelEvent<ModelData>> expandCollapseListener = new Listener<TreePanelEvent<ModelData>>() {
				public void handleEvent(TreePanelEvent<ModelData> be) {
					Element child = tree.getElement().getFirstChildElement();
					String newHeight = child.getClientHeight() + "px";
					tree.setHeight(newHeight);
					treePanel.setHeight(newHeight);
				}			
			};
			
			tree.addListener(Events.Expand, expandCollapseListener);
			tree.addListener(Events.Collapse, expandCollapseListener);
			
			tree.addListener(Events.OnClick, new Listener<TreePanelEvent<TocItem>>() {
	            public void handleEvent(TreePanelEvent<TocItem> event) {
	            	String tagName = event.getTarget().getTagName();
	            	//go to the target place (unless the target is an img, which it is when clicking the expand/collapse arrows)
	            	if (!tagName.toUpperCase().equals("IMG"))
	            		globalAppState.getPlaceChanger().goTo(event.getItem().getTargetPlace());
	            };
	        });
			treePanel.add(tree);
		}
		this.add(treePanel);
		this.layout(true);
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
