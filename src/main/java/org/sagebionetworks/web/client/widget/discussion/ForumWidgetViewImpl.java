package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.jsinterop.ForumSearchProps.OnSearchResultsVisibleHandler;
import org.sagebionetworks.web.client.jsinterop.ForumTableProps;
import org.sagebionetworks.web.client.jsinterop.ForumTableProps.OnClickLinkHandler;
import org.sagebionetworks.web.client.jsinterop.ForumTableProps.OnIsEmptyChangeHandler;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ForumWidgetViewImpl implements ForumWidgetView {

  private static final int THREADS_LIMIT = 20;
  private static final String FULL_HEIGHT = "none";

  @UiField
  Button newThreadButton;

  @UiField
  Div threadListArea;

  @UiField
  ReactComponent threadListContainer;

  @UiField
  SimplePanel threadCountAlertContainer;

  @UiField
  SimplePanel newThreadModalContainer;

  @UiField
  Div synAlertContainer;

  @UiField
  Button showAllThreadsButton;

  @UiField
  SimplePanel defaultThreadContainer;

  @UiField
  Span subscribeButtonContainer;

  @UiField
  ReactComponent deletedThreadListContainer;

  @UiField
  Div deleteThreadsArea;

  @UiField
  Div mainContainer;

  @UiField
  Span subscribersContainer;

  @UiField
  Div actionMenuContainer;

  @UiField
  Div forumSearchContainer;

  @UiField
  Div discussionThreadContainer;

  // flex containers
  @UiField
  Div headingFlexContainer;

  @UiField
  Div subscribersFlexContainer;

  @UiField
  Div forumSearchFlexContainer;

  @UiField
  Div subscribeButtonFlexContainer;

  @UiField
  Div newThreadButtonFlexContainer;

  private Presenter presenter;
  Widget widget;

  public interface Binder extends UiBinder<Widget, ForumWidgetViewImpl> {}

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public ForumWidgetViewImpl() {
    widget = binder.createAndBindUi(this);
    newThreadButton.addClickHandler(event -> {
      presenter.onClickNewThread();
    });
    showAllThreadsButton.addClickHandler(event -> {
      presenter.onClickShowAllThreads();
    });
  }

  private void setSearchResultsVisible(boolean searchResultsVisible) {
    headingFlexContainer.setVisible(!searchResultsVisible);
    subscribersFlexContainer.setVisible(!searchResultsVisible);
    subscribeButtonFlexContainer.setVisible(!searchResultsVisible);
    newThreadButtonFlexContainer.setVisible(!searchResultsVisible);
    actionMenuContainer.setVisible(!searchResultsVisible);
    mainContainer.setVisible(!searchResultsVisible);

    if (searchResultsVisible) {
      forumSearchFlexContainer.addStyleName("flexcontainer-column-fill-width");
      forumSearchFlexContainer.removeStyleName("flexcontainer-xs-align-end");
    } else {
      forumSearchFlexContainer.removeStyleName(
        "flexcontainer-column-fill-width"
      );
      forumSearchFlexContainer.addStyleName("flexcontainer-xs-align-end");
    }
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void configureThreadList(
    String forumId,
    OnClickLinkHandler onClickLink,
    OnIsEmptyChangeHandler onIsEmptyChange
  ) {
    ForumTableProps props = ForumTableProps.create(
      forumId,
      null,
      DiscussionFilter.EXCLUDE_DELETED.name(),
      THREADS_LIMIT,
      onClickLink,
      null,
      true,
      onIsEmptyChange,
      FULL_HEIGHT
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ForumTable,
      props
    );
    threadListContainer.render(component);
  }

  @Override
  public void setThreadCountAlert(Widget w) {
    threadCountAlertContainer.setWidget(w);
  }

  @Override
  public void setNewThreadModal(Widget w) {
    newThreadModalContainer.setWidget(w);
  }

  @Override
  public void setAlert(Widget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }

  @Override
  public void showErrorMessage(String errorMessage) {
    DisplayUtils.showErrorMessage(errorMessage);
  }

  @Override
  public void setThreadListUIVisible(boolean visible) {
    threadListArea.setVisible(visible);
  }

  @Override
  public void setNewThreadButtonVisible(boolean visible) {
    newThreadButton.setVisible(visible);
  }

  @Override
  public void setShowAllThreadsButtonVisible(boolean visible) {
    showAllThreadsButton.setVisible(visible);
  }

  @Override
  public void setSubscribeButton(Widget w) {
    subscribeButtonContainer.clear();
    subscribeButtonContainer.add(w);
  }

  @Override
  public void setDefaultThreadWidget(Widget w) {
    defaultThreadContainer.setWidget(w);
  }

  @Override
  public void setDefaultThreadWidgetVisible(boolean visible) {
    defaultThreadContainer.setVisible(visible);
  }

  @Override
  public void setSubscribersWidget(Widget w) {
    subscribersContainer.clear();
    subscribersContainer.add(w);
  }

  @Override
  public void setSubscribersWidgetVisible(boolean visible) {
    subscribersContainer.setVisible(visible);
  }

  @Override
  public boolean isDeletedThreadListVisible() {
    return deletedThreadListContainer.isVisible();
  }

  @Override
  public void setDeletedThreadListVisible(boolean visible) {
    deleteThreadsArea.setVisible(visible);
    deletedThreadListContainer.setVisible(visible);
  }

  @Override
  public void configureDeletedThreadList(
    String forumId,
    OnClickLinkHandler onClickLink
  ) {
    ForumTableProps props = ForumTableProps.create(
      forumId,
      null,
      DiscussionFilter.DELETED_ONLY.name(),
      THREADS_LIMIT,
      onClickLink,
      null,
      true,
      null,
      FULL_HEIGHT
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ForumTable,
      props
    );
    deletedThreadListContainer.render(component);
  }

  @Override
  public void setMainContainerVisible(boolean visible) {
    mainContainer.setVisible(visible);
  }

  @Override
  public void setActionMenu(IsWidget w) {
    w.asWidget().removeFromParent();
    actionMenuContainer.clear();
    actionMenuContainer.add(w);
  }

  @Override
  public void setForumSearchVisible(boolean visible) {
    forumSearchContainer.setVisible(visible);
    if (!visible) {
      setSearchResultsVisible(false);
    }
  }

  @Override
  public void configureForumSearch(String forumId, String projectId) {
    OnSearchResultsVisibleHandler onSearchUIVisible = visible -> {
      setSearchResultsVisible(visible);
    };
    ForumSearchWrapper widget = new ForumSearchWrapper(
      forumId,
      projectId,
      onSearchUIVisible
    );
    forumSearchContainer.clear();
    forumSearchContainer.add(widget);
  }

  @Override
  public void setDiscussionThreadVisible(boolean visible) {
    discussionThreadContainer.setVisible(visible);
  }

  @Override
  public void configureDiscussionThread(String threadId, int limit) {
    DiscussionThreadViewImpl widget = new DiscussionThreadViewImpl(
      threadId,
      limit
    );
    discussionThreadContainer.clear();
    discussionThreadContainer.add(widget);
  }
}
