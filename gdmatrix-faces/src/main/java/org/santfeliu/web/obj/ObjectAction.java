package org.santfeliu.web.obj;

import java.io.Serializable;

/**
 *
 * @author realor
 */
public class ObjectAction implements Serializable
{
  private String image;
  private String description;
  private String expression;
  private String url;
  private String target;

  public ObjectAction()
  {
  }

  public String getExpression()
  {
    return expression;
  }

  public void setExpression(String expression)
  {
    this.expression = expression;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getTarget()
  {
    return target;
  }

  public void setTarget(String target)
  {
    this.target = target;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getImage()
  {
    return image;
  }

  public void setImage(String image)
  {
    this.image = image;
  }
}
