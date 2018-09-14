package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * An implementation of the QueryResultEditorView with zero business logic.
 * 
 * @author John
 *
 */
public class QueryResultEditorViewImpl implements QueryResultEditorView {
	
	public interface Binder extends UiBinder<Widget, QueryResultEditorViewImpl> {}

	@UiField
	HTMLPanel editorPanel;
	@UiField
	ButtonToolBar buttonToolbar;
	@UiField
	SimplePanel tablePanel;
	@UiField
	SimplePanel progressPanel;
	@UiField
	Button addRowButton;
	@UiField
	Button selectTogglebutton;
	@UiField
	Button selectDropDown;
	@UiField
	AnchorListItem selectAllItem;
	@UiField
	AnchorListItem selectNoneItem;
	@UiField
	Button addRowToolButton;
	@UiField
	Button deleteSelectedButton;
	@UiField
	Alert errorAlert;
	@UiField
	Button saveRowsButton;
	@UiField
	Modal editRowsModal;
	@UiField
	Button cancelButton;
	@UiField
	Modal inProgressModal;
	@UiField
	Button cancelProgressButton;
	Presenter presenter;
	Widget widget;
	
	@Inject
	public QueryResultEditorViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenterin) {
		this.presenter = presenterin;
		this.addRowButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddRow();
			}
		});
		this.selectTogglebutton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onToggleSelect();
			}
		});
		this.selectAllItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSelectAll();
			}
		});
		this.selectNoneItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSelectNone();
			}
		});
		this.addRowToolButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddRow();
			}
		});
		this.deleteSelectedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onDeleteSelected();
			}
		});
		saveRowsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		//SWC-1748: prevent TAB out of modal
		saveRowsButton.addDomHandler(DisplayUtils.getPreventTabHandler(saveRowsButton), KeyDownEvent.getType());
		// Track clicks to the close button at the top of the dialog
		editRowsModal.addCloseHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onCancel();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onCancel();
			}
		});
		cancelProgressButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onCancel();	
			}
		});
	}

	@Override
	public void setTablePageWidget(TablePageWidget pageWidget) {
		this.tablePanel.add(pageWidget);
	}

	@Override
	public void setDeleteButtonEnabled(boolean enabled) {
		if(enabled){
			this.deleteSelectedButton.setEnabled(true);
			this.deleteSelectedButton.setType(ButtonType.DANGER);
		}else{
			this.deleteSelectedButton.setEnabled(false);
			this.deleteSelectedButton.setType(ButtonType.DEFAULT);
		}
	}

	@Override
	public void showErrorMessage(String message) {
		errorAlert.setText(message);
	}

	@Override
	public void setErrorMessageVisible(boolean visible) {
		errorAlert.setVisible(visible);
	}
	
	@Override
	public void setAddRowButtonVisible(boolean visible) {
		addRowButton.setVisible(visible);
	}
	
	@Override
	public void setButtonToolbarVisible(boolean visible) {
		buttonToolbar.setVisible(visible);
	}

	@Override
	public void setProgressWidget(IsWidget progress) {
		progressPanel.add(progress);
	}

	@Override
	public void showConfirmDialog(String message, final Callback callback) {
		DisplayUtils.confirm(message, callback);
	}

	@Override
	public void showEditor() {
		editRowsModal.show();
	}

	@Override
	public void setSaveButtonLoading(boolean isLoading) {
		if (isLoading) {
			this.saveRowsButton.state().loading();
		} else {
			this.saveRowsButton.state().reset();
		}
	}

	@Override
	public void hideEditor() {
		editRowsModal.hide();
	}
	
	@Override
	public void hideProgress() {
		inProgressModal.hide();
	}
	@Override
	public void showProgress() {
		inProgressModal.show();	
	}
	
	@Override
	public void showErrorDialog(String message) {
		Bootbox.alert(message);
	}
	@Override
	public void showMessage(String title, String message) {
		DisplayUtils.showInfo(message);
	}
}
