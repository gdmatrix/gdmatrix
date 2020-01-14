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
package org.santfeliu.workflow.form;

import java.io.Serializable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.matrix.workflow.WorkflowConstants;

import org.santfeliu.util.Properties;


/**
 *
 * @author unknown
 */
public class Form implements Serializable
{
  protected String variable;
  protected String state;
  protected String description;
  protected String type;  
  protected String group = "0";
  protected boolean forwardEnabled;
  protected boolean backwardEnabled;
  protected String error = "";  
  protected Properties parameters = new Properties();
  protected static final Set emptySet = new HashSet();

  public Form()
  {
  }

  public void setVariable(String variable)
  {
    this.variable = variable;
  }

  public String getVariable()
  {
    return variable;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setState(String state)
  {
    this.state = state;
  }

  public String getState()
  {
    return state;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getType()
  {
    return type;
  }

  public void setGroup(String group)
  {
    this.group = group;
  }

  public String getGroup()
  {
    return group;
  }

  public void setForwardEnabled(boolean forwardEnabled)
  {
    this.forwardEnabled = forwardEnabled;
  }

  public boolean isForwardEnabled()
  {
    return forwardEnabled;
  }

  public void setBackwardEnabled(boolean backwardEnabled)
  {
    this.backwardEnabled = backwardEnabled;
  }

  public boolean isBackwardEnabled()
  {
    return backwardEnabled;
  }

  public void setParameters(Properties parameters)
  {
    if (parameters != null)
    {
      this.parameters = parameters;
    }
  }

  public Properties getParameters()
  {
    return parameters;
  }

  public boolean equals(Object o)
  {
    if (o instanceof Form)
    {
      return variable.equals(((Form)o).variable);
    }
    return false;
  }

  public String toString()
  {
    return description;
  }

  public static final Comparator getComparator()
  {
    return comparator;
  }

  public Set getReadVariables()
  {
    return emptySet;
  }
  
  public Set getWriteVariables()
  {
    return emptySet;
  }

  public void setError(String error)
  {
    this.error = error;
  }

  public String getError()
  {
    return error;
  }

  private static final Comparator comparator = new Comparator()
  {
    public int compare(Object o1, Object o2)
    {      
      if (o1 instanceof Form && o2 instanceof Form)
      {
        Form form1 = (Form)o1;
        Form form2 = (Form)o2;
        return form1.getGroup().compareTo(form2.getGroup());
      }
      return 0;
    }
    
    public boolean equals(Object o)
    {
      return o == comparator;
    }
  };

  public static void main(String args[])
  {
    Form form = new Form();
    form.setBackwardEnabled(false);
    form.setForwardEnabled(true);
    form.setType("inputText");
    form.setDescription("Test");
    form.setGroup("0");
    form.setState(WorkflowConstants.FORWARD_STATE);
    Properties p = new Properties();
    p.put("var", "test");
    p.put("message", "hola");
    form.setParameters(p);
    String v = FormFactory.format(form);
    System.out.println(v);
    
    form = FormFactory.parse(v);
    System.out.println(form.getClass());
    v = FormFactory.format(form);
    System.out.println(v);
  }
}
