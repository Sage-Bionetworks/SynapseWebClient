package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget.SubPageTreeItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesViewImpl extends FlowPanel implements WikiSubpagesView {

	private Presenter presenter;
	WikiSubpagesOrderEditorModalWidget orderEditorModal;
	private GlobalApplicationState globalAppState;
	private IconsImageBundle iconsImageBundle;
	private static final String SHOW_SUBPAGES_STYLE="col-xs-12 col-md-3 well";
	private static final String SHOW_SUBPAGES_MD_STYLE="col-xs-12 col-md-9";
	private static final String HIDE_SUBPAGES_STYLE="col-xs-12";
	private static final String HIDE_SUBPAGES_MD_STYLE="col-xs-12";
	
	private Button showHideButton;
	private Button editOrderButton;
	private FlowPanel navTreeContainer;
	private FlowPanel wikiSubpagesContainer;
	private FlowPanel wikiPageContainer;
	boolean isShowingSubpages;
	
	@Inject
	public WikiSubpagesViewImpl(GlobalApplicationState globalAppState, WikiSubpagesOrderEditorModalWidget orderEditorModal, IconsImageBundle iconsImageBundle) {
		this.globalAppState = globalAppState;
		this.orderEditorModal = orderEditorModal;
		this.iconsImageBundle = iconsImageBundle;
		orderEditorModal.setSize(ModalSize.SMALL);
	}
	
	@Override
	public void clear() {
		super.clear();
	}
	
	@Override
	public void configure(WikiSubpageNavigationTree navTree, final WikiSubpageOrderEditorTree editorTree, FlowPanel wikiSubpagesContainer, FlowPanel wikiPageContainer) {
		clear();
		
		this.wikiSubpagesContainer = wikiSubpagesContainer;
		this.wikiPageContainer = wikiPageContainer;
		navTreeContainer = new FlowPanel();
		navTreeContainer.addStyleName("margin-bottom-10");
		//this widget shows nothing if it doesn't have any pages!
		if (navTree.getRootChildrenCount() == 0)
			return;
		
		//only show the tree if the root has children
		if (navTree.getRootChildrenCount() > 0) {

			showHideButton = DisplayUtils.createButton("");
			editOrderButton = DisplayUtils.createButton("Edit Order");
			editOrderButton.addStyleName("btn btn-default btn-xs pull-left");

			editOrderButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					orderEditorModal.configure(editorTree, presenter.getUpdateOrderHintCallback(new GetOrderHintCallback() {
						@Override
						public List<String> getCurrentOrderHint() {	// TODO: redundant?
							return editorTree.getIdListOrderHint();
						}
					}));
					orderEditorModal.show(
							presenter.getUpdateOrderHintCallback(new GetOrderHintCallback() {
								@Override
								public List<String> getCurrentOrderHint() {
									return editorTree.getIdListOrderHint();
								}
							}));
				}
			});
			
			showHideButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (isShowingSubpages)
						hideSubpages();
					else
						showSubpages();
				}
			});
			
			navTreeContainer.add(navTree.asWidget());
			
			add(navTreeContainer);
			add(editOrderButton);
			add(showHideButton);
			
			showSubpages();
		} else {
			hideSubpages();
		}
		clearWidths();
	}
	
	/**
	 * Work around the Chrome bug.  See DisplayUtils.clearElementWidth() for more info.
	 */
	private void clearWidths() {
		DisplayUtils.clearElementWidth(getElement());
		if (wikiSubpagesContainer != null) 
			DisplayUtils.clearElementWidth(wikiSubpagesContainer.getElement());
		if (navTreeContainer != null)
			DisplayUtils.clearElementWidth(navTreeContainer.getElement());
		if (wikiPageContainer != null)
			DisplayUtils.clearElementWidth(wikiPageContainer.getElement());
	}
	
	@Override
	public void hideSubpages() {
		isShowingSubpages = false;
		// This call to layout is necessary to force the scroll bar to appear on page-load
		if (wikiSubpagesContainer != null){
			wikiSubpagesContainer.removeStyleName(SHOW_SUBPAGES_STYLE);
			wikiSubpagesContainer.addStyleName(HIDE_SUBPAGES_STYLE);
		}
		
		if (editOrderButton != null) {
			editOrderButton.setVisible(false);
		}
				
		if (showHideButton != null) {
			showHideButton.setText("Show Pages " + DisplayConstants.RIGHT_ARROWS);
			showHideButton.addStyleName("btn btn-default btn-xs left margin-right-40");
		}
		
		if (navTreeContainer != null)
			DisplayUtils.hide(navTreeContainer);
		
		if (wikiPageContainer != null) {
			wikiPageContainer.removeStyleName(SHOW_SUBPAGES_MD_STYLE);
			wikiPageContainer.addStyleName(HIDE_SUBPAGES_MD_STYLE);
			wikiPageContainer.setVisible(true);
		}
	}
	
	@Override
	public void showSubpages() {
		isShowingSubpages = true;
		if (wikiSubpagesContainer != null) {
			wikiSubpagesContainer.removeStyleName(HIDE_SUBPAGES_STYLE);
			wikiSubpagesContainer.addStyleName(SHOW_SUBPAGES_STYLE);
		}
		
		if (editOrderButton != null) {
			editOrderButton.setVisible(true);
		}
			
		if (wikiPageContainer != null) {
			wikiPageContainer.removeStyleName(HIDE_SUBPAGES_MD_STYLE);
			wikiPageContainer.addStyleName(SHOW_SUBPAGES_MD_STYLE);
		}
				
		if (showHideButton != null) {
			showHideButton.setText(DisplayConstants.LEFT_ARROWS);
			showHideButton.addStyleName("btn btn-default btn-xs right");		
		}
		
		if (navTreeContainer != null)
			DisplayUtils.show(navTreeContainer);
	}
	
	private Widget getListItem(final SubPageTreeItem treeItem) {
		final Anchor l = new Anchor(treeItem.getText());
		l.addStyleName("link");
		l.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalAppState.getPlaceChanger().goTo(treeItem.getTargetPlace());
			}
		});
		return l;
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

	public interface GetOrderHintCallback {
		public List<String> getCurrentOrderHint();
	}
	
	/*
	 * Image Resources for Tree Expand/Collapse Icons
	 */
	
	public class  SubpagesTreeResources implements Tree.Resources {
		@Override
	    public ImageResource treeClosed() {
	        return iconsImageBundle.transparent1();
	    }

	    @Override
	    public ImageResource treeOpen() {
	        return iconsImageBundle.transparent1();
	    }

		@Override
		public ImageResource treeLeaf() {
			return iconsImageBundle.transparent1();
		}
	}
}
