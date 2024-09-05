package org.sagebionetworks.web.server.servlet.filter;

public class BotHtml {

  private String head = "";
  private String body = "";

  public BotHtml() {}

  public BotHtml(String head, String body) {
    this.head = head;
    this.body = body;
  }

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}
