/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.webapp.modules.geo.pdfgen;

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

  public Bounds(double lon, double lat, double zoom)
  {
    // TODO: set bounds
    minx = 420197.02739444;
    miny = 4582053.0456967;
    maxx = 420867.52246818;
    maxy = 4582523.4237793;
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
