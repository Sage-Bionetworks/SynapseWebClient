package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.CertificationQuizPlace;
import org.sagebionetworks.web.client.view.CertificationQuizView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class CertificationQuizPresenter extends AbstractActivity implements Presenter<CertificationQuizPlace> {
    private CertificationQuizView view;
    private CertificationQuizPlace place;

    @Inject
    public CertificationQuizPresenter(CertificationQuizView view) {
        this.view = view;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);
        view.createReactComponentWidget();
    }

    @Override
    public void setPlace(CertificationQuizPlace place) {
        this.place = place;
    }

    public CertificationQuizPlace getPlace() { return place; }

    @Override
    public String mayStop() { return null; }
}
