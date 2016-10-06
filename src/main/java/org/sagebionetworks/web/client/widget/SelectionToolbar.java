package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.view.bootstrap.ButtonUtils;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Reusable view widget that contains the commands for row selection, ordering (move up and down), and delete
 * @author jayhodgson
 *
 */
public class SelectionToolbar implements IsWidget {

	public interface SelectionToolbarUiBinder extends UiBinder<Widget, SelectionToolbar> {}
	@UiField
	ButtonToolBar buttonToolbar;
	@UiField
	Button selectTogglebutton;
	@UiField
	Button selectDropDown;
	@UiField
	AnchorListItem selectAllItem;
	@UiField
	AnchorListItem selectNoneItem;
	@UiField
	Button moveUpButton;
	@UiField
	Button moveDownButton;
	@UiField
	Button deleteSelectedButton;
	
	Widget widget;
	boolean selectAll;
	ClickHandler selectAllClicked, selectNoneClicked;
	
	//empty constructor, this widget can be used directly in your ui xml
	public SelectionToolbar() {
		SelectionToolbarUiBinder binder = GWT.create(SelectionToolbarUiBinder.class);
		widget = binder.createAndBindUi(this);
		selectAll = true;
		
		selectTogglebutton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectAll) {
					if (selectAllClicked != null) {
						selectAllClicked.onClick(event);	
					}
				} else {
					if (selectNoneClicked != null) {
						selectNoneClicked.onClick(event);	
					}
				}
				
				selectAll = !selectAll;
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
		selectAllItem.addClickHandler(selectAllClicked);
	}
	public void setSelectNoneClicked(ClickHandler selectNoneClicked) {
		this.selectNoneClicked = selectNoneClicked;
		selectNoneItem.addClickHandler(selectNoneClicked);
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
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	public void hideReordering() {
		moveUpButton.setVisible(false);
		moveDownButton.setVisible(false);
	}
	
	public void addStyleName(String style) {
		widget.addStyleName(style);
	}
}
