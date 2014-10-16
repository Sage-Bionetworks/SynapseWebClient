package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.HomeViewImpl;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuizInfoViewImpl extends Composite implements QuizInfoWidgetView {
	private Presenter presenter;
	
	@UiField
	SpanElement beforeLockdownMessage;
	@UiField
	SpanElement lockdownDate1;
	@UiField
	SpanElement lockdownDate2;
	
	public interface Binder extends UiBinder<Widget, QuizInfoViewImpl> {}
	
	@Inject
	public QuizInfoViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public void configure(boolean isCertificationRequired) {
		clear();
		if (!isCertificationRequired) {
			DisplayUtils.show(beforeLockdownMessage);
			lockdownDate1.setInnerHTML(HomeViewImpl.LOCKDOWN_DATE_STRING);
			lockdownDate2.setInnerHTML(HomeViewImpl.LOCKDOWN_DATE_STRING);
		} else {
			DisplayUtils.hide(beforeLockdownMessage);
		}
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

}
