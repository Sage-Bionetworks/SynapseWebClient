package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.ForumSearchProps.OnSearchResultsVisibleHandler;

public class ForumWidgetViewImpl implements ForumWidgetView {

  @UiField
  Button newThreadButton;

  @UiField
  SimplePanel threadListContainer;

  @UiField
  SimplePanel newThreadModalContainer;

  @UiField
  Div synAlertContainer;

  @UiField
  Button showAllThreadsButton;

  @UiField
  ButtonGroup repliesSortButtonGroup;

  @UiField
  SimplePanel singleThreadContainer;

  @UiField
  SimplePanel defaultThreadContainer;

  @UiField
  Span subscribeButtonContainer;

  @UiField
  SimplePanel deletedThreadListContainer;

  @UiField
  Div deleteThreadsArea;

  @UiField
  Div mainContainer;

  @UiField
  Span subscribersContainer;

  @UiField
  Button sortRepliesAscendingButton;

  @UiField
  Button sortRepliesDescendingButton;

  @UiField
  Div singleThreadAndSortContainer;

  @UiField
  Div actionMenuContainer;

  @UiField
  Div forumSearchContainer;

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
  private SynapseReactClientFullContextPropsProvider propsProvider;
  Widget widget;

  public interface Binder extends UiBinder<Widget, ForumWidgetViewImpl> {}

  @Inject
  public ForumWidgetViewImpl(
    Binder binder,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    widget = binder.createAndBindUi(this);
    this.propsProvider = propsProvider;
    newThreadButton.addClickHandler(event -> {
      presenter.onClickNewThread();
    });
    showAllThreadsButton.addClickHandler(event -> {
      presenter.onClickShowAllThreads();
    });
    sortRepliesAscendingButton.addClickHandler(event -> {
      clearSelectedSort();
      sortRepliesAscendingButton.setActive(true);
      presenter.onSortReplies(true);
    });
    sortRepliesDescendingButton.addClickHandler(event -> {
      clearSelectedSort();
      sortRepliesDescendingButton.setActive(true);
      presenter.onSortReplies(false);
    });
  }

  private void clearSelectedSort() {
    sortRepliesAscendingButton.setActive(false);
    sortRepliesDescendingButton.setActive(false);
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
      forumSearchFlexContainer.removeStyleName(
        "flexcontainer-align-items-flex-end"
      );
    } else {
      forumSearchFlexContainer.removeStyleName(
        "flexcontainer-column-fill-width"
      );
      forumSearchFlexContainer.addStyleName(
        "flexcontainer-align-items-flex-end"
      );
    }
  }

  @Override
  public void setSingleThread(Widget w) {
    singleThreadContainer.setWidget(w);
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
  public void setThreadList(Widget w) {
    threadListContainer.setWidget(w);
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
  public void setSingleThreadUIVisible(boolean visible) {
    singleThreadAndSortContainer.setVisible(visible);
  }

  @Override
  public void setThreadListUIVisible(boolean visible) {
    threadListContainer.setVisible(visible);
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
  public void setSortRepliesButtonVisible(boolean visible) {
    repliesSortButtonGroup.setVisible(visible);
    if (!visible) {
      // reset
      clearSelectedSort();
      sortRepliesAscendingButton.setActive(true);
    }
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
  public void setDeletedThreadList(Widget widget) {
    deletedThreadListContainer.setWidget(widget);
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
      propsProvider,
      forumId,
      projectId,
      onSearchUIVisible
    );
    forumSearchContainer.clear();
    forumSearchContainer.add(widget);
  }
}
