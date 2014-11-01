package org.sagebionetworks.web.client.widget.entity.download;

import java.util.Date;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.client.widget.modal.Dialog.Callback;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

/**
 * Modal dialog that contains the quiz info widget.  Used to prompt for certification.
 * @author jayhodgson
 *
 */
public class QuizInfoDialog implements SynapseWidgetPresenter {

	private Dialog modal;
	private QuizInfoWidget widget;
	private GlobalApplicationState globalApplicationState;
	private CookieProvider cookies;
	
	@Inject
	public QuizInfoDialog(Dialog modal, QuizInfoWidget widget, GlobalApplicationState globalApplicationState, CookieProvider cookies) {
		this.modal = modal;
		this.widget = widget;
		this.globalApplicationState = globalApplicationState;
		this.cookies = cookies;
	}
	
	/**
	 * dialog must be added to the parent container
	 */
	public Widget asWidget() {
		return modal.asWidget();
	}

	public void show(boolean isCertificationRequired, final org.sagebionetworks.web.client.utils.Callback remindMeLaterCallback) {
		//are we ignoring (for a day)?
		if (!isCertificationRequired && cookies.getCookie(CookieKeys.IGNORE_CERTIFICATION_REMINDER) != null) {
			//bail
			if (remindMeLaterCallback != null)
				remindMeLaterCallback.invoke();
			return;
		}
		
		Callback callback = new Callback() {
			@Override
			public void onDefault() {
				//remind me later clicked
				//ignore for a day
				Date date = new Date();
				CalendarUtil.addDaysToDate(date, 1);
				cookies.setCookie(CookieKeys.IGNORE_CERTIFICATION_REMINDER, Boolean.TRUE.toString(), date);
				if (remindMeLaterCallback != null)
					remindMeLaterCallback.invoke();
			}
			@Override
			public void onPrimary() {
				//go to certification quiz
				globalApplicationState.getPlaceChanger().goTo(new Quiz(WebConstants.CERTIFICATION));
			}
		};
		
		widget.configure(isCertificationRequired);
		//if certification is required, then show the remind me later button.
		String remindMeLaterButtonText = isCertificationRequired ? null : "Remind me later";
		modal.configure("Join the Synapse Certified User Community", widget.asWidget(), "Become Certified today!", remindMeLaterButtonText, callback, true);
		modal.show();
	}
}
