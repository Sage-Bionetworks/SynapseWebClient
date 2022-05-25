package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.presenter.DataAccessManagementPresenter;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DataAccessManagementViewImpl implements DataAccessManagementView {
    public interface DataAccessManagementViewImplUiBinder extends UiBinder<Widget, DataAccessManagementViewImpl> {
    }

    private DataAccessManagementPresenter presenter;
    private Header headerWidget;

    Widget widget;

    @Inject
    public DataAccessManagementViewImpl(DataAccessManagementViewImplUiBinder binder, Header headerWidget) {
        widget = binder.createAndBindUi(this);
        this.headerWidget = headerWidget;
        headerWidget.configure();
    }

    @Override
    public void setPresenter(DataAccessManagementPresenter presenter) {
        this.presenter = presenter;
        headerWidget.configure();
        headerWidget.refresh();
        Window.scrollTo(0,0);
    }

    @Override
    public Widget asWidget() { return widget; }

    @Override
    public void render() {

    }
}
