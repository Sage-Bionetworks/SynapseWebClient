package org.sagebionetworks.web.client.widget.evaluation;

import java.util.Date;
import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationEditorModalView extends IsWidget {

	void setPresenter(Presenter presenter);

	void setEvaluationName(String name);

	String getEvaluationName();

	void setSubmissionInstructionsMessage(String message);

	String getSubmissionInstructionsMessage();

	void setSubmissionReceiptMessage(String message);

	String getSubmissionReceiptMessage();

	void setSynAlert(IsWidget w);

	void setCreatedByWidget(IsWidget w);

	void setDescription(String description);

	String getDescription();

	void setCreatedOn(String createdOnString);

	void setRoundStart(Date roundStart);

	Date getRoundStart();

	void setRoundDuration(Long roundDurationMs);

	Double getRoundDuration();

	void setNumberOfRounds(Long numberOfRounds);

	Double getNumberOfRounds();

	void setSubmissionLimit(Long submissionLimit);

	Double getSubmissionLimit();

	void show();

	void hide();

	void clear();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSave();
	}

}
