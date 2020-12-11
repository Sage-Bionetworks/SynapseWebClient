package org.sagebionetworks.web.client.widget.evaluation;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.web.client.jsinterop.EvaluationEditorProps;
import org.sagebionetworks.web.client.jsinterop.EvaluationRoundEditorListProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class EvaluationEditorReactComponentPage extends Composite {
	public interface Binder extends UiBinder<Widget, EvaluationEditorReactComponentPage> {}

	@UiField
	Anchor backToChallenge;

	@UiField
	ReactComponentDiv evaluationEditorContainer;
	@UiField
	ReactComponentDiv evaluationRoundEditorContainer;

	String evaluationId;
	String sessionToken;
	EvaluationEditorProps.Callback onPageBack;
	boolean utc;

	@Inject
	public EvaluationEditorReactComponentPage(Binder binder){
		initWidget(binder.createAndBindUi(this));
	}

	public void configure(String evaluationId, String sessionToken, boolean utc, EvaluationEditorProps.Callback onPageBack){
		this.evaluationId = evaluationId;
		this.onPageBack = onPageBack;
		this.sessionToken = sessionToken;
		this.utc = utc;
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		EvaluationEditorProps editorProps = EvaluationEditorProps.create(sessionToken, evaluationId,
				null, utc, this.onPageBack);
		ReactDOM.render(React.createElement(SRC.SynapseComponents.EvaluationEditor, editorProps),
				evaluationEditorContainer.getElement());

		EvaluationRoundEditorListProps roundListProps = EvaluationRoundEditorListProps.create(sessionToken, evaluationId, utc);
		ReactDOM.render(React.createElement(SRC.SynapseComponents.EvaluationRoundEditorList, roundListProps),
				evaluationRoundEditorContainer.getElement());
	}

	@UiHandler(value={"backToChallenge"})
	void onBackToChallengeClick(ClickEvent event){
		unmountReactComponents();
		onPageBack.run();
	}

	private void unmountReactComponents(){
		ReactDOM.unmountComponentAtNode(evaluationRoundEditorContainer.getElement());
		ReactDOM.unmountComponentAtNode(evaluationEditorContainer.getElement());
	}

}
