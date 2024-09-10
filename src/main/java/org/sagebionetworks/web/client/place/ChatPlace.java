package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ChatPlace extends ParameterizedPlace {

  public static final String INITIAL_MESSAGE = "initalMessage";
  public static final String AGENT_ID = "agentId";
  public static final String CHATBOT_NAME = "chatbotName";

  public ChatPlace(String token) {
    super(token);
  }

  @Prefix("Chat")
  public static class Tokenizer implements PlaceTokenizer<ChatPlace> {

    @Override
    public String getToken(ChatPlace place) {
      return place.toToken();
    }

    @Override
    public ChatPlace getPlace(String token) {
      return new ChatPlace(token);
    }
  }
}
