package org.sagebionetworks.web.client.widget.evaluation;

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
	void show();
	void hide();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSave();
	}

}
