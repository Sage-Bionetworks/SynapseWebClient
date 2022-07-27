package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.presenter.OAuthClientEditorPresenter;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OAuthClientEditorViewImpl implements OAuthClientEditorView {
    public interface OAuthClientEditorViewImplUiBinder extends UiBinder<Widget, OAuthClientEditorViewImpl>{}

    private OAuthClientEditorPresenter presenter;
    private Header headerWidget;

    Widget widget;

    @Inject
    public OAuthClientEditorViewImpl(OAuthClientEditorViewImplUiBinder binder, Header headerWidget, SynapseContextPropsProvider propsProvider) {
        widget = binder.createAndBindUi(this);
        this.headerWidget = headerWidget;
        headerWidget.configure();
    }

    @Override
    public void setPresenter(OAuthClientEditorPresenter presenter) {
        this.presenter =presenter;
        headerWidget.configure();
        headerWidget.refresh();
        Window.scrollTo(0,0);
        render();
    }

    @Override
    public Widget asWidget() { return widget; }

    @Override
    public void render() {
    }
}
