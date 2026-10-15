package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.jsinterop.ForumTableProps.OnClickLinkHandler;
import org.sagebionetworks.web.client.jsinterop.ForumTableProps.OnIsEmptyChangeHandler;

public interface ForumWidgetView extends IsWidget {
  public interface Presenter {
    void onClickShowAllThreads();

    Widget asWidget();

    void onSortReplies(boolean ascending);

    void onClickNewThread();
  }

  void setPresenter(Presenter presenter);

  void configureThreadList(
    String forumId,
    OnClickLinkHandler onClickLink,
    OnIsEmptyChangeHandler onIsEmptyChange
  );

  void setThreadCountAlert(Widget w);

  void setNewThreadModal(Widget w);

  void setAlert(Widget w);

  void setSubscribeButton(Widget w);

  void showErrorMessage(String errorMessage);

  void setThreadListUIVisible(boolean visible);

  void setNewThreadButtonVisible(boolean visible);

  void setShowAllThreadsButtonVisible(boolean visible);

  void setDefaultThreadWidget(Widget w);

  void setDefaultThreadWidgetVisible(boolean visible);

  boolean isDeletedThreadListVisible();

  void setDeletedThreadListVisible(boolean visible);

  void configureDeletedThreadList(
    String forumId,
    OnClickLinkHandler onClickLink
  );

  void setMainContainerVisible(boolean visible);

  void setSubscribersWidget(Widget w);

  void setSubscribersWidgetVisible(boolean visible);

  void setForumSearchVisible(boolean visible);

  void configureForumSearch(String forumId, String projectId);

  void configureDiscussionThread(String threadId, int limit);

  void setDiscussionThreadVisible(boolean visible);

  void setActionMenu(IsWidget w);
}
