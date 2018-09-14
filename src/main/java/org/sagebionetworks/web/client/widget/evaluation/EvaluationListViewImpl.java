package org.sagebionetworks.web.client.widget.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.HelpWidget;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationListViewImpl extends Panel implements EvaluationListView {
	List<InlineRadio> evaluationCheckboxes;
	
	private Presenter presenter;
	
	@Inject
	public EvaluationListViewImpl() {
		evaluationCheckboxes = new ArrayList<InlineRadio>();
	}
	
	@Override
	public void configure(List<Evaluation> list) {
		clear();
		
		if(list == null || list.size() == 0){
			addNoAttachmentRow();
		} else {
			populateTable(list);			
		}
	}

	private void addNoAttachmentRow() {
		add(new InlineHTML("No evaluations found"));
	}
	
	private void populateTable(List<Evaluation> list) {		
		ButtonGroup group = new ButtonGroup();
		for(final Evaluation data: list){
			Div row = new Div();
			final InlineRadio selectBox = new InlineRadio("evaluationButtons", data.getName());
			selectBox.addStyleName("margin-left-10");
			row.add(selectBox);
			evaluationCheckboxes.add(selectBox);
			if (DisplayUtils.isDefined(data.getSubmissionInstructionsMessage())) {
				HelpWidget helpWidget = new HelpWidget();
				helpWidget.setHelpMarkdown(SafeHtmlUtils.htmlEscape(data.getSubmissionInstructionsMessage()));
				helpWidget.setAddStyleNames("margin-left-10 greyText-imp");
				row.add(helpWidget.asWidget());
			}
			group.add(row);
		}
		add(group);
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		super.clear();
		evaluationCheckboxes.clear();
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public Integer getSelectedEvaluationIndex() {
		for (int i = 0; i < evaluationCheckboxes.size(); i++) {
			if (evaluationCheckboxes.get(i).getValue()) {
				return i;
			}
		}
		return null;
	}
	@Override
	public void setSelectedEvaluationIndex(int i) {
		evaluationCheckboxes.get(i).setValue(true, true);
	}
}
