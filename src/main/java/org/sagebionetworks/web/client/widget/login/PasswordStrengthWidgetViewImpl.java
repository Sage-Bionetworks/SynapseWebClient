package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.constants.ProgressBarType;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PasswordStrengthWidgetViewImpl implements PasswordStrengthWidgetView {

	public interface PasswordStrengthWidgetViewImplUiBinder extends UiBinder<Widget, PasswordStrengthWidgetViewImpl> {}
	
	@UiField
	Progress progress;
	@UiField
	ProgressBar progressBar;
	@UiField
	Text feedback;
	Widget w;
	private Presenter presenter;
	
	@Inject
	public PasswordStrengthWidgetViewImpl(PasswordStrengthWidgetViewImplUiBinder binder) {
		w = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}
	
	@Override
	public void showWeakPasswordUI(String reason) {
		progressBar.setType(ProgressBarType.DANGER);
		progressBar.setPercent(25);
		progressBar.setText("Weak");
		feedback.setText(reason);
	}
	
	@Override
	public void showFairPasswordUI(String reason) {
		progressBar.setType(ProgressBarType.WARNING);
		progressBar.setPercent(50);
		progressBar.setText("Fair");
		feedback.setText(reason);
	}
	
	@Override
	public void showGoodPasswordUI() {
		progressBar.setType(ProgressBarType.INFO);
		progressBar.setPercent(75);
		progressBar.setText("Good");
		feedback.setText("");
	}
	
	@Override
	public void showStrongPasswordUI() {
		progressBar.setType(ProgressBarType.SUCCESS);
		progressBar.setPercent(100);
		progressBar.setText("Strong");
		feedback.setText("");
	}
}
