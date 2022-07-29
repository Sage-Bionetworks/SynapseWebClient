package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.presenter.OAuthClientEditorPresenter;

import com.google.gwt.user.client.ui.IsWidget;

public interface OAuthClientEditorView extends IsWidget {

    public void setPresenter(OAuthClientEditorPresenter presenter);
    public void render();

}
