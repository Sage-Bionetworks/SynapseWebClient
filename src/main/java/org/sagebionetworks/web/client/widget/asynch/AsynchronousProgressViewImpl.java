package org.sagebionetworks.web.client.widget.asynch;

import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The view portion of the AsynchronousProgressWidget. There is zero business logic in this class.
 * 
 * @author John
 * 
 */
public class AsynchronousProgressViewImpl implements AsynchronousProgressView {

	public interface Binder extends UiBinder<Div, AsynchronousProgressViewImpl> {
	}

	@UiField
	Div progressColumn;
	@UiField
	Div spinnerColumn;
	@UiField
	LoadingSpinner spinner;
	@UiField
	Div title;
	@UiField
	ProgressBar progressBar;
	@UiField
	Div message;
	Presenter presenter;
	Div container;

	@Inject
	public AsynchronousProgressViewImpl(final Binder uiBinder) {
		container = uiBinder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return container;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setTitle(String title) {
		this.title.clear();
		this.title.add(new Text(title));
	}

	@Override
	public void setIsDetermiante(boolean isDeterminate) {
		spinnerColumn.setVisible(!isDeterminate);
		progressColumn.setVisible(isDeterminate);
	}

	@Override
	public void setDeterminateProgress(double percent, String text, String message) {
		progressBar.setPercent(percent);
		progressBar.setText(text);
		this.message.clear();
		if (message != null) {
			this.message.add(new Text(message));
		}
	}

	@Override
	public void setIndetermianteProgress(String message) {
		this.message.clear();
		if (message != null) {
			this.message.add(new Text(message));
		}
	}

	@Override
	public boolean isAttached() {
		return progressBar.isAttached();
	}

	@Override
	public void showWhiteSpinner() {
		spinner.setIsWhite(true);
	}

	@Override
	public void setProgressMessageVisible(boolean visible) {
		message.setVisible(visible);
	}
}
