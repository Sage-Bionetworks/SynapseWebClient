package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.view.bootstrap.ButtonUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Reusable view widget that contains the commands for row selection, ordering (move up and down),
 * and delete
 * 
 * @author jayhodgson
 *
 */
public class SelectionToolbar implements IsWidget {

	public interface SelectionToolbarUiBinder extends UiBinder<Widget, SelectionToolbar> {
	}

	@UiField
	ButtonToolBar buttonToolbar;
	@UiField
	IndeterminateCheckBox selectAllNoneCheckBox;
	@UiField
	Button moveUpButton;
	@UiField
	Button moveDownButton;
	@UiField
	Button deleteSelectedButton;

	Widget widget;
	ClickHandler selectAllClicked, selectNoneClicked;

	// empty constructor, this widget can be used directly in your ui xml
	public SelectionToolbar() {
		SelectionToolbarUiBinder binder = GWT.create(SelectionToolbarUiBinder.class);
		widget = binder.createAndBindUi(this);

		selectAllNoneCheckBox.addClickHandler(event -> {
			// what is the new state of the checkbox after the click?
			CheckBoxState state = selectAllNoneCheckBox.getState();
			boolean selectAll = state.equals(CheckBoxState.SELECTED);
			if (selectAll) {
				if (selectAllClicked != null) {
					selectAllClicked.onClick(event);
				}
			} else {
				if (selectNoneClicked != null) {
					selectNoneClicked.onClick(event);
				}
			}
		});
	}

	public void setMoveupClicked(ClickHandler moveupClicked) {
		moveUpButton.addClickHandler(moveupClicked);
	}

	public void setMovedownClicked(ClickHandler movedownClicked) {
		moveDownButton.addClickHandler(movedownClicked);
	}

	public void setDeleteClickedCallback(ClickHandler deleteClicked) {
		deleteSelectedButton.addClickHandler(deleteClicked);
	}

	public void setSelectAllClicked(ClickHandler selectAllClicked) {
		this.selectAllClicked = selectAllClicked;
	}

	public void setSelectNoneClicked(ClickHandler selectNoneClicked) {
		this.selectNoneClicked = selectNoneClicked;
	}

	public void setVisible(boolean isVisible) {
		buttonToolbar.setVisible(isVisible);
	}

	public void setCanMoveDown(boolean canMoveDown) {
		ButtonUtils.setEnabledAndType(canMoveDown, this.moveDownButton, ButtonType.INFO);
	}

	public void setCanMoveUp(boolean canMoveUp) {
		ButtonUtils.setEnabledAndType(canMoveUp, this.moveUpButton, ButtonType.INFO);
	}

	public void setCanDelete(boolean canDelete) {
		ButtonUtils.setEnabledAndType(canDelete, this.deleteSelectedButton, ButtonType.DANGER);
	}

	public void setSelectionState(CheckBoxState state) {
		selectAllNoneCheckBox.setState(state);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	public void hideReordering() {
		moveUpButton.setVisible(false);
		moveDownButton.setVisible(false);
	}

	public void setAddStyleNames(String style) {
		widget.addStyleName(style);
	}
}
