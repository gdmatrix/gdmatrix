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
package org.santfeliu.classif.web;

import java.util.Date;
import java.util.List;
import org.santfeliu.web.obj.PageBean;
import org.matrix.classif.Class;
import org.matrix.classif.ClassFilter;
import org.matrix.classif.ClassificationManagerPort;
import org.santfeliu.util.BigList;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class ClassHistoryBean extends PageBean
{
  private int firstRowIndex;
  private BigList<Class> rows;

  public ClassHistoryBean()
  {
  }

  public BigList getRows()
  {
    if (isNew()) return null;
    if (rows == null)
    {
      rows = new BigList(15, 10)
      {
        @Override
        public int getElementCount()
        {
          try
          {
            ClassificationManagerPort port = ClassificationConfigBean.getPort();
            return port.countClasses(getFilter());
          }
          catch (Exception ex)
          {
            error(ex);
          }
          return 0;
        }

        @Override
        public List getElements(int firstResult, int maxResults)
        {
          try
          {
            ClassificationManagerPort port = ClassificationConfigBean.getPort();
            ClassFilter filter = getFilter();
            filter.setFirstResult(firstResult);
            filter.setMaxResults(maxResults);
            return port.findClasses(filter);
          }
          catch (Exception ex)
          {
            error(ex);
          }
          return null;
        }

        private ClassFilter getFilter()
        {
          ClassBean classBean = (ClassBean)getBean("classBean");
          String classId = classBean.getClassId();
          ClassFilter filter = new ClassFilter();
          filter.setClassId(classId);
          return filter;
        }
      };
    }
    return rows;
  }

  public int getRowCount()
  {
    return getRows() == null ? 0 : getRows().size();
  }

  public int getFirstRowIndex()
  {
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  public Date getRowStartDate()
  {
    Class row = (Class)getValue("#{row}");
    return TextUtils.parseInternalDate(row.getStartDateTime());
  }

  public Date getRowEndDate()
  {
    Class row = (Class)getValue("#{row}");
    return TextUtils.parseInternalDate(row.getEndDateTime());
  }

  public String getRowStyle()
  {
    Class row = (Class)getValue("#{row}");
    return ClassificationConfigBean.getClassStyle(row);
  }

  @Override
  public String show()
  {
    return "class_history";
  }

  public String showClass()
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    Class row = (Class)getValue("#{row}");
    String objectId = classBean.getObjectId(row);
    return getControllerBean().showObject("Class", objectId);
  }
}
