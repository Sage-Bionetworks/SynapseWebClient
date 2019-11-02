package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class TimedRetryWidget extends FlowPanel {
	int timerRemainingSec;

	/**
	 * @param timerSec Seconds to wait until automatically invoking the callback
	 * @param callback Called when either the timer hits 0, or if the user clicks the Try Now button
	 */
	public void configure(int timerSec, final Callback callback) {
		clear();
		timerRemainingSec = timerSec;
		final HTML timerLabel = new HTML(getWaitingString(timerRemainingSec));
		final Timer timer = new Timer() {
			@Override
			public void run() {
				timerRemainingSec--;
				if (timerRemainingSec <= 0) {
					cancel();
					callback.invoke();
					return;
				}
				timerLabel.setHTML(getWaitingString(timerRemainingSec));
			}
		};
		timer.scheduleRepeating(1000);

		Button btn = DisplayUtils.createButton(DisplayConstants.TRY_NOW, ButtonType.DEFAULT);
		btn.addStyleName("btn-lg left");
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				timer.cancel();
				callback.invoke();
			}
		});
		add(btn);
		add(timerLabel);
	}

	private String getWaitingString(int remainingSec) {
		return "&nbsp;" + DisplayConstants.WAITING + " " + remainingSec + "s...";
	}
}
