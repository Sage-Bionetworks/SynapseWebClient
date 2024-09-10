package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.ChatPlace;
import org.sagebionetworks.web.client.view.ChatView;

public class ChatPresenter
  extends AbstractActivity
  implements Presenter<ChatPlace> {

  private ChatPlace place;
  private ChatView view;

  @Inject
  public ChatPresenter(ChatView view) {
    this.view = view;
    view.scrollToTop();
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
  }

  @Override
  public void setPlace(ChatPlace place) {
    this.place = place;
    String initialMessage = place.getParam(ChatPlace.INITIAL_MESSAGE);
    String agentId = place.getParam(ChatPlace.AGENT_ID);
    String chatbotName = place.getParam(ChatPlace.CHATBOT_NAME);

    view.render(initialMessage, agentId, chatbotName);
  }
}
