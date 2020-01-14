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
package org.santfeliu.dic.util;

/**
 *
 * @author blanquepa
 */
  public class ValidationError
  {
    private String propName;
    private String propValue;
    private String errMessage;
    private String errParam;

    public ValidationError(String propName, Object propValue, String message)
    {
      this(propName, propValue, message, null);
    }

    public ValidationError(String propName, Object propValue, String message,
      String param)
    {
      this.propName = propName;
      this.propValue = propValue != null ? String.valueOf(propValue) : null;
      this.errMessage = message;
      this.errParam = param;
    }

    public String getErrMessage()
    {
      return errMessage;
    }

    public void setErrMessage(String errMessage)
    {
      this.errMessage = errMessage;
    }

    public String getErrParam()
    {
      return errParam;
    }

    public void setErrParam(String errParam)
    {
      this.errParam = errParam;
    }

    public String getPropName()
    {
      return propName;
    }

    public void setPropName(String propName)
    {
      this.propName = propName;
    }

    public String getPropValue()
    {
      return propValue;
    }

    public void setPropValue(String propValue)
    {
      this.propValue = propValue;
    }

    @Override
    public String toString()
    {
      if (errParam != null)
        errParam = "(" + errParam + ")";
      else
        errParam = "";
      return errMessage +" " + errParam +": " + propName + 
        (propValue != null ? "->" + propValue : "");
    }
  }
