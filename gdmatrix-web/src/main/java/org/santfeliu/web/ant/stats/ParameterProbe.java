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
package org.santfeliu.web.ant.stats;

/**
 *
 * @author unknown
 */
public class ParameterProbe extends CounterProbe
{
  private String parameters;
  private String[] parameterArray;

  public String getParameters()
  {
    return parameters;
  }

  public void setParameters(String parameters)
  {
    this.parameters = parameters;
  }

  @Override
  public void init()
  {
    parameterArray = parameters.split(",");
  }

  @Override
  public void processLine(Line line)
  {
    String lineParameters = line.getParameters();
    if (lineParameters != null)
    {
      for (String p : parameterArray)
      {
        String value = getParameterValue(lineParameters, p);
        if (value != null)
        {
          increment(value);
        }
      }
    }
  }

  private String getParameterValue(String parameters, String parameter)
  {
    int index = parameters.indexOf(parameter + "=");
    if (index != -1)
    {
      index += parameter.length() + 1;
      String word = parameters.substring(index);
      int index2 = word.indexOf("&");
      return index2 == -1 ? word : word.substring(0, index2);
    }
    return null;
  }
}
