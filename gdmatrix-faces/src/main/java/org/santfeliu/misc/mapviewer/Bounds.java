package org.santfeliu.misc.mapviewer;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 *
 * @author realor
 */
public class Bounds implements Serializable
{
  private double minx;
  private double miny;
  private double maxx;
  private double maxy;

  public Bounds()
  {
  }

  public Bounds(double minx, double miny, double maxx, double maxy)
  {
    this.minx = minx;
    this.miny = miny;
    this.maxx = maxx;
    this.maxy = maxy;
  }

  public Bounds(Bounds bounds)
  {
    this.minx = bounds.minx;
    this.miny = bounds.miny;
    this.maxx = bounds.maxx;
    this.maxy = bounds.maxy;
  }

  public Bounds(String text)
  {
    StringTokenizer tokenizer = new StringTokenizer(text, ",");
    if (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      minx = Double.parseDouble(token);
      if (tokenizer.hasMoreTokens())
      {
        token = tokenizer.nextToken();
        miny = Double.parseDouble(token);
        if (tokenizer.hasMoreTokens())
        {
          token = tokenizer.nextToken();
          maxx = Double.parseDouble(token);
          if (tokenizer.hasMoreTokens())
          {
            token = tokenizer.nextToken();
            maxy = Double.parseDouble(token);
          }
        }
      }
    }
  }

  public double getMinX()
  {
    return minx;
  }

  public void setMinX(double minx)
  {
    this.minx = minx;
  }

  public double getMinY()
  {
    return miny;
  }

  public void setMinY(double miny)
  {
    this.miny = miny;
  }

  public double getMaxX()
  {
    return maxx;
  }

  public void setMaxX(double maxx)
  {
    this.maxx = maxx;
  }

  public double getMaxY()
  {
    return maxy;
  }

  public void setMaxY(double maxy)
  {
    this.maxy = maxy;
  }

  public double getWidth()
  {
    return maxx - minx;
  }

  public double getHeight()
  {
    return maxy - miny;
  }

  public Bounds getScaled(double factor)
  {
    double xoffset = getWidth() * factor * 0.5;
    double yoffset = getHeight() * factor * 0.5;
    double xcenter = 0.5 * (minx + maxx);
    double ycenter = 0.5 * (miny + maxy);
    
    return new Bounds(
      xcenter - xoffset, ycenter - yoffset,
      xcenter + xoffset, ycenter + yoffset);
  }

  public Bounds getAdjusted(double width, double height)
  {
    Bounds bounds = new Bounds(this);
    double newRatio = width / height;
    double ratio = getWidth() / getHeight();
    if (newRatio > ratio)
    {
      // increase height
      double newHeight = getWidth() * height / width;
      double offset = (newHeight - getHeight()) / 2;
      bounds.miny -= offset;
      bounds.maxy += offset;
    }
    else
    {
      // increase width
      double newWidth = getHeight() * width / height;
      double offset = (newWidth - getWidth()) / 2;
      bounds.minx -= offset;
      bounds.maxx += offset;
    }
    return bounds;
  }

  @Override
  public String toString()
  {
    return minx + "," + miny + "," + maxx + "," + maxy;
  }
}
