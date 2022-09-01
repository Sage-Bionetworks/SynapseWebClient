package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.place.OAuthClientEditorPlace;
import org.sagebionetworks.web.client.view.OAuthClientEditorView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class OAuthClientEditorPresenter extends AbstractActivity implements Presenter<OAuthClientEditorPlace> {

    private OAuthClientEditorView view;
    private OAuthClientEditorPlace place;
    private SynapseContextPropsProvider propsProvider;

    @Inject
    public OAuthClientEditorPresenter(OAuthClientEditorView view, SynapseContextPropsProvider propsProvider) {
        this.view = view;
        this.propsProvider = propsProvider;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);
        view.createReactComponentWidget(propsProvider);
    }

    @Override
    public void setPlace(OAuthClientEditorPlace place){
        this.place = place;
    }

    public OAuthClientEditorPlace getPlace() {return place;}

    @Override
    public String mayStop(){
        return null;
    }
}
