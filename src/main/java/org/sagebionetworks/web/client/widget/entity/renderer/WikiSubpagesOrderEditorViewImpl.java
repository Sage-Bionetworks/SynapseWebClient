package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditorViewImpl extends Composite implements WikiSubpagesOrderEditorView {
	public interface Binder extends UiBinder<Widget, WikiSubpagesOrderEditorViewImpl> {}
	
	
	@UiField
	SimplePanel treePanel;
	@UiField
	SimplePanel instructionPanel;
	@UiField
	SimplePanel synAlertContainer;
	@UiField
	Button upButton;
	@UiField
	Button downButton;
	@UiField
	Button leftButton;
	@UiField
	Button rightButton;

	private Presenter presenter;
	private WikiSubpageOrderEditorTree tree;
	
	@Inject
	public WikiSubpagesOrderEditorViewImpl(Binder binder) {
		initWidget(binder.createAndBindUi(this));
		addButtonHandlers();
	}
	
	public void disableUpDownButtons() {
		upButton.setEnabled(false);
		downButton.setEnabled(false);
	}
	
	@Override
	public void configure(WikiSubpageOrderEditorTree subpageTree) {
		this.tree = subpageTree;
		treePanel.clear();
		treePanel.setWidget(tree.asWidget());
		subpageTree.setMovabilityCallback(getTreeItemMovabilityCallback());
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	private void addButtonHandlers() {
		upButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tree.moveUp();
			}
		});
		
		downButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tree.moveDown();
			}
		});
		
		rightButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tree.moveRight();
			}
		});
		
		leftButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tree.moveLeft();
			}
		});
	}
	
	@Override
	public void initializeState() {
		disableUpDownButtons();
	}
	
	public TreeItemMovabilityCallback getTreeItemMovabilityCallback() {
		return new TreeItemMovabilityCallback() {
			@Override
			public void invoke(boolean canMoveUpOrRight, boolean canMoveDown, boolean canMoveLeft) {
				upButton.setEnabled(canMoveUpOrRight);
				downButton.setEnabled(canMoveDown);
				leftButton.setEnabled(canMoveLeft);
				rightButton.setEnabled(canMoveUpOrRight);
			}
		};
	}
	
	public interface TreeItemMovabilityCallback {
		public void invoke(boolean canMoveUpOrRight, boolean canMoveDown, boolean canMoveLeft);
	}
	
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
