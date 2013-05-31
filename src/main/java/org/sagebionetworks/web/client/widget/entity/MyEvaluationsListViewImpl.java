package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEvaluationsListViewImpl extends LayoutContainer implements MyEvaluationsListView {

	private Presenter presenter;
	private IconsImageBundle imageBundle;
	
	@Inject
	public MyEvaluationsListViewImpl(IconsImageBundle imageBundle) {
		this.imageBundle = imageBundle;
	}
	
	@Override
	public void configure(List<Evaluation> evaluations) {
		this.removeAll();
		StringBuilder htmlBuilder = new StringBuilder();
		if (evaluations.size() > 0) {
			htmlBuilder.append("<h3>"+ DisplayConstants.MY_EVALUATIONS +"</h3>");
			htmlBuilder.append("<div class=\"myEvaluationListContainer\"\">");
			String iconHtml = DisplayUtils.getIconHtml(imageBundle.synapseStep16());
			for (Evaluation evaluation : evaluations) {
				htmlBuilder.append("<div>");
				htmlBuilder.append(iconHtml);
				htmlBuilder.append("<a class=\"myEvaluationLink\" href=\"");
				htmlBuilder.append(DisplayUtils.getSynapseHistoryToken(evaluation.getContentSource()));
				htmlBuilder.append("\">");
				htmlBuilder.append(evaluation.getName());
				htmlBuilder.append("</a></div>");
			}
			htmlBuilder.append("<div>");
			
			add(new HTMLPanel(htmlBuilder.toString()));
		}
		layout(true);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	
	/*
	 * Private Methods
	 */

}
