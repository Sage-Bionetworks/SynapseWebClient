package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.client.widget.modal.Dialog.Callback;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Modal dialog that contains the quiz info widget. Used to prompt for certification.
 * 
 * @author jayhodgson
 *
 */
public class QuizInfoDialog implements SynapseWidgetPresenter {

	private Dialog modal;
	private QuizInfoWidget widget;
	private GlobalApplicationState globalApplicationState;

	@Inject
	public QuizInfoDialog(Dialog modal, QuizInfoWidget widget, GlobalApplicationState globalApplicationState) {
		this.modal = modal;
		this.widget = widget;
		this.globalApplicationState = globalApplicationState;
	}

	/**
	 * dialog must be added to the parent container
	 */
	public Widget asWidget() {
		return modal.asWidget();
	}

	public void show() {
		Callback callback = new Callback() {
			@Override
			public void onDefault() {}

			@Override
			public void onPrimary() {
				// go to certification quiz
				globalApplicationState.getPlaceChanger().goTo(new Quiz(WebConstants.CERTIFICATION));
			}
		};

		widget.configure();
		// if certification is required, then show the remind me later button.
		modal.configure("Join the Synapse Certified User Community", widget.asWidget(), "Become Certified today!", null, callback, true);
		modal.show();
	}
}
