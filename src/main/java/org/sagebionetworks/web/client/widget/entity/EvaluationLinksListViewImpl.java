package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationLinksListViewImpl extends FlowPanel implements EvaluationLinksListView {

	private Presenter presenter;
	private IconsImageBundle imageBundle;
	
	@Inject
	public EvaluationLinksListViewImpl(IconsImageBundle imageBundle) {
		this.imageBundle = imageBundle;
	}
	
	@Override
	public void configure(List<Evaluation> evaluations, String title, boolean showEvaluationIds) {
		clear();
		
		if (evaluations.size() > 0) {
			FlowPanel linkContainer = new FlowPanel();
			linkContainer.addStyleName("margin-10");
		    
			String iconHtml = DisplayUtils.getIconHtml(imageBundle.synapseStep16());
			for (Evaluation evaluation : evaluations) {
				SimplePanel wrapper = new SimplePanel();
				Anchor a = new Anchor();
				SafeHtmlBuilder linkText=  new SafeHtmlBuilder();
				linkText.appendEscaped(evaluation.getName());
				if (showEvaluationIds) {
					linkText.appendHtmlConstant(" (" + evaluation.getId() + ")");
				}
				a.setHTML(linkText.toSafeHtml());
				a.addStyleName("link");
				a.addClickHandler(getEvaluationClickHandler(evaluation));
				wrapper.add(a);
				linkContainer.add(wrapper);
			}
			
			if (title != null && title.length() > 0) {
				//add a title
				HTML html = new HTML(SafeHtmlUtils.fromSafeConstant("<h4>" + title + "</h4>"));
				add(html);
			}
			
			LayoutContainer border = new LayoutContainer();
		    border.addStyleName("notopmargin");
		    border.setBorders(true);
		    border.add(linkContainer);
			
			add(border);
		}
	}
	
	public ClickHandler getEvaluationClickHandler(final Evaluation eval) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.evaluationClicked(eval);
			}
		};
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void showLoading() {
	}
	
	/*
	 * Private Methods
	 */

}
