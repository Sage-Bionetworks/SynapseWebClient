package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReferenceConfigViewImpl implements ReferenceConfigView {
	public interface ReferenceConfigViewImplUiBinder extends UiBinder<Widget, ReferenceConfigViewImpl> {
	}

	private Presenter presenter;
	private Widget widget;
	@UiField
	TextBox referenceField;

	@Inject
	public ReferenceConfigViewImpl(ReferenceConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void initView() {}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void checkParams() throws IllegalArgumentException {}

	@Override
	public void showLoading() {

	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
		referenceField.setValue("");
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setReference(String reference) {
		referenceField.setValue(reference);
	}

	@Override
	public String getReference() {
		return referenceField.getValue();
	}

}
