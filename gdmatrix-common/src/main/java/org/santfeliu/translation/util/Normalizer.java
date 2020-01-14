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
package org.santfeliu.translation.util;

import java.util.ArrayList;

/**
 * Normalized normalizer = new Normalizer();
 * normalizer.append("...");
 * normalizer.append('.');
 * normalizer.append("...");
 * String normalizedText = normalized.end(); // switch mode
 * normalizer.append("...");
 * normalizer.append('.');
 * normalizer.append("...");
 * String denormalizedText = normalized.end(); // reset
 *
 * @author realor
 */
public class Normalizer
{
  public static final int NORMALIZE = 0;
  public static final int DENORMALIZE = 1;

  public static final String OPEN_TAG = "(*";
  public static final String CLOSE_TAG = "*)";
  public static final String HELP_TAG = ":::";

  private StringBuffer outBuffer = new StringBuffer();
  private StringBuffer tagBuffer = new StringBuffer();
  private StringBuffer markBuffer = new StringBuffer();
  private ArrayList<String> variables = new ArrayList<String>();
  private int mode = NORMALIZE;
  private boolean insideMark = false;
  private boolean leftBlank;
  private boolean rightBlank;

  public Normalizer()
  {
  }

  public int getMode()
  {
    return mode;
  }

  public void append(String s)
  {
    for (char ch : s.toCharArray())
    {
      append(ch);
    }
  }

  public void append(char ch)
  {
    tagBuffer.append(ch);
    if (!insideMark)
    {
      if (OPEN_TAG.equals(tagBuffer.toString()))
      {
        insideMark = true;
        tagBuffer.setLength(0);
        markBuffer.setLength(0);
      }
      else
      {
        while (tagBuffer.length() > 0 &&
           !OPEN_TAG.startsWith(tagBuffer.toString()))
        {
          toOutBuffer(tagBuffer.charAt(0));
          tagBuffer.delete(0, 1);
        }
      }
    }
    else
    {
      if (CLOSE_TAG.equals(tagBuffer.toString()))
      {
        insideMark = false;
        transformMark(markBuffer.toString());
        tagBuffer.setLength(0);
        markBuffer.setLength(0);
      }
      else
      {
        while (tagBuffer.length() > 0 &&
           !CLOSE_TAG.startsWith(tagBuffer.toString()))
        {
          markBuffer.append(tagBuffer.charAt(0));
          tagBuffer.delete(0, 1);
        }
      }
    }
  }
  
  public boolean insideMark()
  {
    return insideMark;
  }

  public int length()
  {
    return outBuffer.length();
  }

  public String end()
  {
    closeMark();
    String text = outBuffer.toString();
    if (mode == NORMALIZE)
    {
      outBuffer.setLength(0);
      tagBuffer.setLength(0);
      markBuffer.setLength(0);
      insideMark = false;
      mode = DENORMALIZE;
      if (text.startsWith(" ")) leftBlank = true;
      if (text.endsWith(" ")) rightBlank = true;
      if (leftBlank || rightBlank) text = text.trim();
    }
    else
    {
      if (leftBlank) text = " " + text;
      if (rightBlank) text = text + " ";
      reset();
    }
    return text;
  }

  public void reset()
  {
    outBuffer.setLength(0);
    tagBuffer.setLength(0);
    markBuffer.setLength(0);
    insideMark = false;
    variables.clear();
    leftBlank = false;
    rightBlank = false;
    mode = NORMALIZE;
  }

  @Override
  public String toString()
  {
    return outBuffer.toString();
  }

  private void closeMark()
  {
    if (markBuffer.length() > 0)
    {
      transformMark(markBuffer.toString());
      markBuffer.setLength(0);
    }
  }

  private void toOutBuffer(char ch)
  {
    if (ch == ' ')
    {
      int length = outBuffer.length();
      if (length == 0)
      {
        outBuffer.append(ch);
      }
      else if (outBuffer.charAt(length - 1) != ' ')
      {
        outBuffer.append(ch);
      }
    }
    else
    {
      outBuffer.append(ch);
    }
  }

  private void transformMark(String mark)
  {
    String text;
    String help;
    int index = mark.indexOf(HELP_TAG);
    if (index == -1)
    {
      text = mark.trim();
      help = "";
    }
    else
    {
      text = mark.substring(0, index).trim();
      help = mark.substring(index + HELP_TAG.length()).trim();
    }
    if (mode == NORMALIZE)
    {
      outBuffer.append(OPEN_TAG);
      if (text.length() > 0)
      {
        outBuffer.append(String.valueOf(variables.size()));
        variables.add(text);
      }
      if (help.length() > 0)
      {
        outBuffer.append(HELP_TAG);
        outBuffer.append(help);
      }
      outBuffer.append(CLOSE_TAG);
    }
    else // DENORMALIZE
    {
      if (text.length() > 0)
      {
        try
        {
          index = Integer.parseInt(text.trim());
          outBuffer.append(variables.get(index));
        }
        catch (Exception ex) // bad number or index
        {
        }
      }
    }
  }

  public static void main(String args[])
  {
    String text = "En (*Ricard::prova*) és un (* informàtic:::test *) de (*Sant Feliu*).  ";
    System.out.println("Source:[" + text +"]");

    Normalizer n = new Normalizer();
    n.append(text);
    text = n.end();

    System.out.println("Normalized:[" + text +"]");
    String translation = "(*0*) es un (*1*) de (*2*).";

    n.append(translation);
    text = n.end();
    System.out.println("Denormalized:[" + text +"]");
  }
}
