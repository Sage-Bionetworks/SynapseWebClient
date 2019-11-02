package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This is the base of a list of ColumnModels. It consists of a view of columns and if editable it
 * also includes a button for launching a editor in a modal.
 * 
 * @author John
 *
 */
public class ColumnModelsViewBaseImpl extends Composite implements ColumnModelsViewBase {

	public interface Binder extends UiBinder<Widget, ColumnModelsViewBaseImpl> {
	}

	@UiField
	SimplePanel viewerPanel;
	@UiField
	Modal editModal;
	@UiField
	SimplePanel columnEditorModalPanel;
	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Div synapseAlertContainer;
	Presenter presenter;
	@UiField
	SimplePanel jobTrackerWidgetContainer;

	@Inject
	public ColumnModelsViewBaseImpl(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
	}

	@Override
	public void setViewer(ColumnModelsView viewer) {
		this.viewerPanel.add(viewer);
	}

	@Override
	public void setEditor(IsWidget editor) {
		columnEditorModalPanel.clear();
		this.columnEditorModalPanel.add(editor);
	}

	@Override
	public void showEditor() {
		saveButton.state().reset();
		editModal.show();
	}

	@Override
	public void hideEditor() {
		editModal.hide();
	}

	@Override
	public void setLoading() {
		saveButton.state().loading();
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synapseAlertContainer.clear();
		synapseAlertContainer.add(w);
	}

	@Override
	public void resetSaveButton() {
		saveButton.state().reset();
	}

	@Override
	public void setJobTrackingWidget(IsWidget jobTrackingWidget) {
		jobTrackerWidgetContainer.clear();
		jobTrackerWidgetContainer.add(jobTrackingWidget);
	}

	@Override
	public void setJobTrackingWidgetVisible(boolean visible) {
		jobTrackerWidgetContainer.setVisible(visible);
	}
}
