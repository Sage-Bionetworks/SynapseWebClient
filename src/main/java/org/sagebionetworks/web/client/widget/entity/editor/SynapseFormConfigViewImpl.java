package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseFormConfigViewImpl implements SynapseFormConfigView {
	public interface SynapseFormViewImplUiBinder extends UiBinder<Widget, SynapseFormConfigViewImpl> {
	}

	private Presenter presenter;

	@UiField
	TextBox entityIdField;
	@UiField
	Button entityFinderButton;

	Widget widget;

	@Inject
	public SynapseFormConfigViewImpl(SynapseFormViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		initClickHandlers();
	}

	private void initClickHandlers() {
		entityFinderButton.addClickHandler(event -> {
			presenter.onEntityFinderButtonClicked();
		});
	}

	@Override
	public void initView() {
		entityIdField.setValue("");
	}

	@Override
	public void checkParams() throws IllegalArgumentException {}

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
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {}

	@Override
	public String getEntityId() {
		return entityIdField.getValue();
	}

	@Override
	public void setEntityId(String entityId) {
		entityIdField.setValue(entityId);
	}
}
