package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationListViewImpl extends Panel implements EvaluationListView {
	List<Evaluation> selectedEvaluations;
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public EvaluationListViewImpl(IconsImageBundle iconsImageBundle) {
		selectedEvaluations = new ArrayList<Evaluation>();
		this.iconsImageBundle = iconsImageBundle;
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
		for(final Evaluation data: list){
			Div row = new Div();
			final InlineCheckBox selectBox = new InlineCheckBox(data.getName());
			selectBox.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (selectBox.getValue()) {
						selectedEvaluations.add(data);
					} else {
						selectedEvaluations.remove(data);
					}
				}
			});
			selectBox.addStyleName("margin-left-10");
			row.add(selectBox);
			
			if (DisplayUtils.isDefined(data.getSubmissionInstructionsMessage())) {
				AbstractImagePrototype img = AbstractImagePrototype.create(iconsImageBundle.informationBalloon16());
				Anchor moreInfoButton = DisplayUtils.createIconLink(img, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						DisplayUtils.showInfoDialog(data.getName(), data.getSubmissionInstructionsMessage(), null);
					}
				});
				moreInfoButton.addStyleName("margin-left-10");
				row.add(moreInfoButton);
			}
			add(row);
		}
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
		selectedEvaluations.clear();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	public List<Evaluation> getSelectedEvaluations() {
		return selectedEvaluations;
	}
}
