package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministerEvaluationsListViewImpl implements AdministerEvaluationsListView {

	private Presenter presenter;
	private EvaluationLinksList evalList;
	private EvaluationAccessControlListModalWidget aclEditor;
	
	@Inject
	public AdministerEvaluationsListViewImpl(EvaluationLinksList evalList, EvaluationAccessControlListModalWidget aclEditor) {
		this.evalList = evalList;
		this.aclEditor = aclEditor;
	}
	
	@Override
	public void configure(List<Evaluation> evaluations) {
		evalList.configure(evaluations, getEvaluationClicked(), "Evaluations", true);
	}
	
	public CallbackP<Evaluation> getEvaluationClicked(){
		return new CallbackP<Evaluation>(){
			@Override
			public void invoke(Evaluation evaluation) {
				//evaluation clicked.
				aclEditor.configure(evaluation);
				
				aclEditor.showSharing(new Callback() {
					@Override
					public void invoke() {
					}
				});
			}
		};
	}
	
	@Override
	public Widget asWidget() {
		return evalList.asWidget();
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
	public void clear() {
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
