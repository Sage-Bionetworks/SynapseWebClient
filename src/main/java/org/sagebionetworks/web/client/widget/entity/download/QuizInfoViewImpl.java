package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuizInfoViewImpl extends Composite implements QuizInfoWidgetView {
	private Presenter presenter;

	public interface Binder extends UiBinder<Widget, QuizInfoViewImpl> {
	}

	@Inject
	public QuizInfoViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void clear() {}

	@Override
	public void configure() {}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {}

	@Override
	public void showErrorMessage(String message) {}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

}
