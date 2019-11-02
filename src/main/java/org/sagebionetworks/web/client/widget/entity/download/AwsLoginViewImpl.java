package org.sagebionetworks.web.client.widget.entity.download;

import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.html.Strong;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AwsLoginViewImpl implements AwsLoginView {

	public interface Binder extends UiBinder<Widget, AwsLoginViewImpl> {
	}

	Widget w;

	@UiField
	Strong endpointField;
	@UiField
	Input accessKeyField;
	@UiField
	Input secretKeyField;

	@Inject
	public AwsLoginViewImpl(Binder uiBinder) {
		w = uiBinder.createAndBindUi(this);
	}

	@Override
	public void setEndpoint(String value) {
		endpointField.setText(value);
	}

	@Override
	public String getAccessKey() {
		return accessKeyField.getValue();
	}

	@Override
	public String getSecretKey() {
		return secretKeyField.getValue();
	}

	@Override
	public void clear() {
		endpointField.setText("");
		accessKeyField.setValue("");
		secretKeyField.setValue("");
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}
}
