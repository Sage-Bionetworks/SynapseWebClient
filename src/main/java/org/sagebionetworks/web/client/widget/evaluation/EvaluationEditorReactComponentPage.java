package org.sagebionetworks.web.client.widget.evaluation;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.web.client.jsinterop.EvaluationEditorPageProps;
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

	String evaluationId;
	String accessToken;
	String entityId;
	EvaluationEditorPageProps.Callback onPageBack;
	boolean utc;

	@Inject
	public EvaluationEditorReactComponentPage(Binder binder){
		initWidget(binder.createAndBindUi(this));
	}

	public void configure(String evaluationId, String entityId, String token, boolean utc, EvaluationEditorPageProps.Callback onPageBack){
		this.evaluationId = evaluationId;
		this.entityId = entityId;
		this.onPageBack = onPageBack;
		this.accessToken = token;
		this.utc = utc;
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		EvaluationEditorPageProps editorProps = EvaluationEditorPageProps.create(accessToken, evaluationId,
				entityId, utc, this.onPageBack);
		ReactDOM.render(React.createElement(SRC.SynapseComponents.EvaluationEditorPage, editorProps),
				evaluationEditorContainer.getElement());
	}

	@UiHandler(value={"backToChallenge"})
	void onBackToChallengeClick(ClickEvent event){
		unmountReactComponents();
		onPageBack.run();
	}

	private void unmountReactComponents(){
		ReactDOM.unmountComponentAtNode(evaluationEditorContainer.getElement());
	}

}
