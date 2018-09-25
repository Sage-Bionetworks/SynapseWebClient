package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.widget.SelectionToolbar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BiodallianceEditorViewImpl implements BiodallianceEditorView {
	public interface BiodallianceEditorViewImplUiBinder extends UiBinder<Widget, BiodallianceEditorViewImpl> {}
	private Presenter presenter;
	@UiField
	TextBox chrField;
	@UiField
	TextBox viewStartField;
	@UiField
	TextBox viewEndField;
	@UiField
	FlowPanel tracksContainer;
	@UiField
	Button addTrackButton;
	@UiField
	RadioButton humanButton;
	@UiField
	RadioButton mouseButton;
	@UiField
	SelectionToolbar selectionToolbar;
	@UiField
	Table trackColumnHeaders;
	Widget widget;
	org.sagebionetworks.web.client.widget.SelectableListView.Presenter selectionToolbarHandler;
	@Inject
	public BiodallianceEditorViewImpl(BiodallianceEditorViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		addTrackButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addTrackClicked();
			}
		});
		selectionToolbar.setDeleteClickedCallback(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionToolbarHandler.deleteSelected();
			}
		});
		selectionToolbar.setMovedownClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionToolbarHandler.onMoveDown();
			}
		});
		
		selectionToolbar.setMoveupClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionToolbarHandler.onMoveUp();
			}
		});
		selectionToolbar.setSelectAllClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionToolbarHandler.selectAll();
			}
		});
		selectionToolbar.setSelectNoneClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionToolbarHandler.selectNone();
			}
		});
	}
	@Override
	public void setSelectionToolbarHandler(
			org.sagebionetworks.web.client.widget.SelectableListView.Presenter selectionToolbarHandler) {
		this.selectionToolbarHandler = selectionToolbarHandler;
	}
	@Override
	public void initView() {
	}

	
	
	@Override
	public void checkParams() throws IllegalArgumentException {
	}

	@Override
	public Widget asWidget() {
		return widget;
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void addTrack(Widget w) {
		tracksContainer.add(w);
	}
	
	@Override
	public void clearTracks() {
		tracksContainer.clear();
	}
	
	@Override
	public void clear() {
		tracksContainer.clear();
		chrField.setValue("");
		viewStartField.setValue("");
		viewEndField.setValue("");
	}

	public String getChr() {
		return chrField.getValue();
	}

	public void setChr(String chr) {
		this.chrField.setValue(chr);
	}

	public String getViewStart() {
		return viewStartField.getValue();
	}

	public void setViewStart(String viewStart) {
		this.viewStartField.setValue(viewStart);
	}

	public String getViewEnd() {
		return viewEndField.getValue();
	}

	public void setViewEnd(String viewEnd) {
		this.viewEndField.setValue(viewEnd);
	}
	@Override
	public boolean isHuman() {
		return humanButton.getValue();
	}
	@Override
	public void setHuman() {
		humanButton.setActive(true);
		humanButton.setValue(true, true);
		mouseButton.setActive(false);
	}
	
	@Override
	public boolean isMouse() {
		return mouseButton.getValue();
	}
	@Override
	public void setMouse() {
		mouseButton.setActive(true);
		mouseButton.setValue(true, true);
		humanButton.setActive(false);
	}
	
	@Override
	public void setCanDelete(boolean canDelete) {
		selectionToolbar.setCanDelete(canDelete);
	}
	
	@Override
	public void setCanMoveDown(boolean canMoveDown) {
		selectionToolbar.setCanMoveDown(canMoveDown);
	}
	@Override
	public void setCanMoveUp(boolean canMoveUp) {
		selectionToolbar.setCanMoveUp(canMoveUp);
	}
	@Override
	public void setButtonToolbarVisible(boolean visible) {
		selectionToolbar.setVisible(visible);
	}
	@Override
	public void setTrackHeaderColumnsVisible(boolean visible) {
		trackColumnHeaders.setVisible(visible);
	}
}
