package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
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
	public interface Binder extends UiBinder<Widget, WikiSubpagesOrderEditorViewImpl> {
	}


	@UiField
	SimplePanel treePanel;
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

	@UiField
	Button upButton2;
	@UiField
	Button downButton2;
	@UiField
	Button leftButton2;
	@UiField
	Button rightButton2;

	@UiField
	LoadingSpinner loadingUI;

	private WikiSubpageOrderEditorTree tree;

	@Inject
	public WikiSubpagesOrderEditorViewImpl(Binder binder) {
		initWidget(binder.createAndBindUi(this));
		addButtonHandlers();
	}

	public void disableDirectionalButtons() {
		upButton.setEnabled(false);
		downButton.setEnabled(false);
		leftButton.setEnabled(false);
		rightButton.setEnabled(false);
		upButton2.setEnabled(false);
		downButton2.setEnabled(false);
		leftButton2.setEnabled(false);
		rightButton2.setEnabled(false);

	}

	@Override
	public void configure(WikiSubpageOrderEditorTree subpageTree) {
		this.tree = subpageTree;
		treePanel.clear();
		treePanel.setWidget(tree.asWidget());
		subpageTree.setMovabilityCallback(getTreeItemMovabilityCallback());
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	private void addButtonHandlers() {
		ClickHandler moveUpClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tree.moveUp();
			}
		};
		upButton.addClickHandler(moveUpClickHandler);
		upButton2.addClickHandler(moveUpClickHandler);

		ClickHandler moveDownClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tree.moveDown();
			}
		};
		downButton.addClickHandler(moveDownClickHandler);
		downButton2.addClickHandler(moveDownClickHandler);

		ClickHandler moveRightClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setLoadingVisible(true);
				disableDirectionalButtons();
				tree.moveRight();
			}
		};
		rightButton.addClickHandler(moveRightClickHandler);
		rightButton2.addClickHandler(moveRightClickHandler);

		ClickHandler moveLeftClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setLoadingVisible(true);
				disableDirectionalButtons();
				tree.moveLeft();
			}
		};
		leftButton.addClickHandler(moveLeftClickHandler);
		leftButton2.addClickHandler(moveLeftClickHandler);
	}

	@Override
	public void initializeState() {
		disableDirectionalButtons();
	}

	public TreeItemMovabilityCallback getTreeItemMovabilityCallback() {
		return new TreeItemMovabilityCallback() {
			@Override
			public void invoke(boolean canMoveUpOrRight, boolean canMoveDown, boolean canMoveLeft) {
				upButton.setEnabled(canMoveUpOrRight);
				upButton2.setEnabled(canMoveUpOrRight);
				downButton.setEnabled(canMoveDown);
				downButton2.setEnabled(canMoveDown);
				leftButton.setEnabled(canMoveLeft);
				leftButton2.setEnabled(canMoveLeft);
				rightButton.setEnabled(canMoveUpOrRight);
				rightButton2.setEnabled(canMoveUpOrRight);
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

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
	}
}
