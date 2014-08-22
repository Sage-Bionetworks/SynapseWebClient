package org.sagebionetworks.web.client.widget.asynch;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The view portion of the AsynchronousProgressWidget. There is zero business
 * logic in this class.
 * 
 * @author John
 * 
 */
public class AsynchronousProgressViewImpl implements AsynchronousProgressView {

	public interface Binder extends
			UiBinder<Container, AsynchronousProgressViewImpl> {
	}

	@UiField
	Text title;
	@UiField
	ProgressBar progressBar;
	@UiField
	Button cancelButton;
	Presenter presenter;

	Container container;

	@Inject
	public AsynchronousProgressViewImpl(final Binder uiBinder) {
		container = uiBinder.createAndBindUi(this);
		this.cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onCancel();
			}
		});
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
		this.title.setText(title);
	}

	@Override
	public void setProgress(double percent, String text, String toolTips) {
		progressBar.setPercent(percent);
		progressBar.setText(text);
		progressBar.setTitle(toolTips);
	}


}
