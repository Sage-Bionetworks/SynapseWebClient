package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.Window;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorReactComponentPage;

public class ChallengeTabViewImpl implements ChallengeTabView {

	@UiField
	Container evaluationListContainer;
	@UiField
	Container challengeWidgetContainer;
	@UiField
	Span actionMenuContainer;

	@UiField
	Row adminTabContainer;

	@UiField
	Container evaluationEditorContainer;

	public interface TabsViewImplUiBinder extends UiBinder<Widget, ChallengeTabViewImpl> {
	}

	Widget widget;

	public ChallengeTabViewImpl() {
		// empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void setEvaluationList(Widget w) {
		evaluationListContainer.add(w);
	}

	@Override
	public void setChallengeWidget(Widget w) {
		challengeWidgetContainer.add(w);
	}
	
	@Override
	public void setActionMenu(IsWidget w) {
		actionMenuContainer.clear();
		actionMenuContainer.add(w);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void hideAdminTabContents(){
		DisplayUtils.hide(adminTabContainer);
	}

	@Override
	public void showAdminTabContents(){
		DisplayUtils.show(adminTabContainer);
	}

	@Override
	public void addEvaluationEditor(EvaluationEditorReactComponentPage evaluationEditor){
		evaluationEditorContainer.add(evaluationEditor.asWidget());
	}
}

