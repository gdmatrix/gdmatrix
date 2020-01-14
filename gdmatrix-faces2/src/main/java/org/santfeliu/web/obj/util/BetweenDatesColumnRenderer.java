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
package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class BetweenDatesColumnRenderer extends ColumnRenderer implements Serializable
{
  private String startDatePropName;
  private String endDatePropName;
  private String dateFormat = "yyyyMMdd";
  private String resultYes;
  private String resultNo;

  public BetweenDatesColumnRenderer(String startDatePropName, String endDatePropName,
    String dateFormat, String resultYes, String resultNo)
  {
    this.startDatePropName = startDatePropName;
    this.endDatePropName = endDatePropName;
    this.dateFormat = dateFormat;
    this.resultYes = resultYes;
    this.resultNo = resultNo;
  }

  public Object getValue(String columnName, Object row)
  {
    String startDate = null;
    String endDate = null;

    Object obj = PojoUtils.getDeepStaticProperty(row, startDatePropName);
    if (obj != null)
      startDate = ((List<String>)obj).get(0);

    obj = PojoUtils.getDeepStaticProperty(row, endDatePropName);
    if (obj != null)
      endDate = ((List<String>)obj).get(0);

    Date sDate = TextUtils.parseUserDate(startDate, dateFormat);
    Date eDate = TextUtils.parseUserDate(endDate, dateFormat);
    Date today = TextUtils.parseUserDate(
      TextUtils.formatDate(new Date(), dateFormat), dateFormat);

    if (sDate != null && sDate.compareTo(today) <= 0 &&
      eDate != null && eDate.compareTo(today) >= 0 || 
      (sDate == null && eDate == null))
      return resultYes;
    else
      return resultNo;
  }

  @Override
  public boolean isValueEscaped()
  {
    return false;
  }

}
