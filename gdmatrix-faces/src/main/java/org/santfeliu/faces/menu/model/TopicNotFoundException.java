package org.santfeliu.faces.menu.model;

/**
 *
 * @author realor
 */
public class TopicNotFoundException extends MenuException
{
  private String topic;

  public TopicNotFoundException(String topic)
  {
    super("Topic not found: " + topic);
    this.topic = topic;
  }

  public String getTopic()
  {
    return topic;
  }
}
