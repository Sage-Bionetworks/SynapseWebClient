package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditorViewImpl extends Composite implements WikiSubpagesOrderEditorView {
	public interface Binder extends UiBinder<Widget, WikiSubpagesOrderEditorViewImpl> {}
	
	
	@UiField
	SimplePanel treePanel;
	@UiField
	SimplePanel instructionPanel;
	@UiField
	Button upButton;
	@UiField
	Button downButton;
	
	private Presenter presenter;
	private Tree tree;
	private HasChangesHandler hasChangesHandler;

	@Inject
	public WikiSubpagesOrderEditorViewImpl(Binder binder) {
		initWidget(binder.createAndBindUi(this));
		addUpDownButtonHandlers();
	}
	
	private void addTreeSelectionHandler(final Tree tree) {
		tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				TreeItem selectedItem = event.getSelectedItem();
				TreeItem parent = selectedItem.getParentItem();
				int index = 0;
				int maxIndex;
				if (parent != null) {
					maxIndex = parent.getChildCount() - 1;
					index = parent.getChildIndex(selectedItem);
				} else {
					maxIndex = tree.getItemCount() - 1;
					for (int i = 0; i < tree.getItemCount(); i++) {
						if (tree.getItem(i) == selectedItem) {
							index = i;
							break;
						}
					}
				}
				upButton.setEnabled(true);
				downButton.setEnabled(true);
				if (index == 0) {
					upButton.setEnabled(false);
				}
				if (index == maxIndex) {
					downButton.setEnabled(false);
				}
			}
		});
	}
	
	@Override
	public void configure(Tree subpageTree, HasChangesHandler hasChangesHandler) {
		this.tree = subpageTree;
		this.hasChangesHandler = hasChangesHandler;
		treePanel.setWidget(tree);
		addTreeSelectionHandler(tree);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	private void addUpDownButtonHandlers() {
		upButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				moveItemIfAble(true);
			}
		});
		
		downButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				moveItemIfAble(false);
			}
		});
	}
	
	private void moveItemIfAble(boolean up) {
		if (tree != null && tree.getSelectedItem() != null) {
			TreeItem selectedItem = tree.getSelectedItem();
			TreeItem parent = selectedItem.getParentItem();
			int index = -1;
			int maxIndex = -1;
			if (parent != null) {
				maxIndex = parent.getChildCount() - 1;
				index = parent.getChildIndex(selectedItem);
				if (up) {
					if (index > 0) {
						parent.removeItem(selectedItem);
						parent.insertItem(index - 1, selectedItem);
						hasChangesHandler.hasChanges(true);
					}
				} else {
					if (index < maxIndex) {
						parent.removeItem(selectedItem);
						parent.insertItem(index + 1, selectedItem);
						hasChangesHandler.hasChanges(true);
					}
				}
			} else {
				maxIndex = tree.getItemCount() - 1;
				for (int i = 0; i < tree.getItemCount(); i++) {
					if (tree.getItem(i) == selectedItem) {
						index = i;
						break;
					}
				}
				if (up) {
					if (index > 0) {
						tree.removeItem(selectedItem);
						tree.insertItem(index - 1, selectedItem);
						hasChangesHandler.hasChanges(true);
					}
				} else {
					if (index < maxIndex) {
						tree.removeItem(selectedItem);
						tree.insertItem(index + 1, selectedItem);
						hasChangesHandler.hasChanges(true);
					}
				}
			}
			tree.setSelectedItem(selectedItem, true);
		}
	}

	/********************* TODO *********************/
	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}
}
