package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesViewImpl extends FlowPanel implements WikiSubpagesView {

	private Presenter presenter;
	private GlobalApplicationState globalAppState;
	
	@Inject
	public WikiSubpagesViewImpl(GlobalApplicationState globalAppState) {
		this.globalAppState = globalAppState;
	}
	
	@Override
	public void configure(TocItem root, final ResizeLayoutPanel parentContainer, HTMLPanel markdownContainer) {
		clear();
		//this widget shows nothing if it doesn't have any pages!
		TocItem mainPage = (TocItem) root.getChild(0);
		if (mainPage.getChildCount() == 0)
			return;
		addStyleName("margin-10");
		if (parentContainer != null)
			parentContainer.addStyleName("col-xs-12 col-md-3 col-md-push-9 well well-small display-table");	
		if (markdownContainer != null)
			markdownContainer.addStyleName("col-md-9 col-md-pull-3");	
		
		//only show the tree if the root has children
		if (mainPage.getChildCount() > 0) {
			TreeStore<ModelData> store = new TreeStore<ModelData>();
			store.add(root.getChildren(), true);
			final TreePanel<ModelData> tree = new TreePanel<ModelData>(store);
			tree.setAutoExpand(true);
			tree.setAutoHeight(true);
			tree.setTrackMouseOver(false);
			tree.addStyleName("pagesTree");
			//Remove folder icons
			tree.getStyle().setNodeCloseIcon(null);
			tree.getStyle().setNodeOpenIcon(null);
			//Set the text for the tree
			tree.setLabelProvider(new ModelStringProvider<ModelData>() {
				@Override
				public String getStringValue(ModelData model, String property) {
					String style = ((TocItem)model).isCurrentPage() ? "" : "link";
					return "<span class=\""+style+"\">" + model.toString() + "</span>";
				}	
			});
			
			//Adjust the height of the tree as items are expanded/collapsed
			Listener<TreePanelEvent<ModelData>> expandCollapseListener = new Listener<TreePanelEvent<ModelData>>() {
				public void handleEvent(TreePanelEvent<ModelData> be) {
					Element child = tree.getElement().getFirstChildElement();
					String newHeight = child.getClientHeight() + "px";
					tree.setHeight(newHeight);
					setHeight(newHeight);
					String expandedHeight = child.getClientHeight()+50 + "px";
					if (parentContainer != null)
						parentContainer.setHeight(expandedHeight);
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
			
			add(new HTML("<h3>Pages</h3>"));
			add(tree);

			parentContainer.addResizeHandler(new ResizeHandler() {
				@Override
				public void onResize(ResizeEvent event) {
					//when container is resized, also resize the tree
					tree.setWidth(WikiSubpagesViewImpl.this.getOffsetWidth());
				}
			});
		}
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
}
