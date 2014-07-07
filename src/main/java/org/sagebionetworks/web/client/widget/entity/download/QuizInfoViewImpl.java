package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.view.HomeViewImpl;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
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
	
	@UiField
	Button remindMeLaterButton;
	@UiField
	Button becomeCertifiedButton;

	public interface Binder extends UiBinder<Widget, QuizInfoViewImpl> {}
	
	@Inject
	public QuizInfoViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
		initializeUI();
	}
	
	@Override
	public void clear() {
	}
	
	private void initializeUI() {
		clear();
		int daysRemaining = HomeViewImpl.getDaysRemaining();
		if (daysRemaining > 0) {
			DisplayUtils.show(remindMeLaterButton);
			remindMeLaterButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.cancelClicked();
				}
			});
			DisplayUtils.show(beforeLockdownMessage);
			lockdownDate1.setInnerHTML(HomeViewImpl.LOCKDOWN_DATE_STRING);
			lockdownDate2.setInnerHTML(HomeViewImpl.LOCKDOWN_DATE_STRING);
		} else {
			DisplayUtils.hide(beforeLockdownMessage);
			DisplayUtils.hide(remindMeLaterButton);
		}
		
		becomeCertifiedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.continueClicked();
				presenter.goTo(new Quiz(WebConstants.CERTIFICATION));
			}
		});
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
