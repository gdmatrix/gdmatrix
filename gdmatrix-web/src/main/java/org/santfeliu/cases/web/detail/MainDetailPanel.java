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
package org.santfeliu.cases.web.detail;

import java.util.Date;
import org.matrix.cases.Case;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.DetailPanel;

/**
 *
 * @author blanquepa
 */
public class MainDetailPanel extends DetailPanel
{
  private CaseDetailBean detailBean;

  @Override
  public void loadData(DetailBean detailBean)
  {
    this.detailBean = (CaseDetailBean) detailBean;
  }

  public Case getCase()
  {
    if (detailBean != null)
      return detailBean.getCase();
    else
      return null;
  }

  public String getStartDate()
  {
    Case cas = getCase();
    if (cas != null)
      return formatDate(cas.getStartDate());
    else
      return null;
  }

  public String getStartTime()
  {
    Case cas = getCase();
    if (cas != null)
      return formatTime(cas.getStartTime());
    else
      return null;
  }

  public String getEndDate()
  {
    Case cas = getCase();
    if (cas != null)
      return formatDate(cas.getEndDate());
    else
      return null;
  }

  public String getEndTime()
  {
    Case cas = getCase();
    if (cas != null)
      return formatTime(cas.getEndTime());
    else
      return null;
  }

  public String getCaseType()
  {
    Case cas = getCase();
    if (cas != null)
    {
      Type type = TypeCache.getInstance().getType(cas.getCaseTypeId());
      if (type != null)
        return type.getDescription();
    }

    return null;
  }

  @Override
  public boolean isRenderContent()
  {
    return true;
  }

  public String getType()
  {
    return "main";
  }

  private String formatDate(String date)
  {
    if (date != null)
    {
      Date d = TextUtils.parseInternalDate(date);
      return TextUtils.formatDate(d, "dd/MM/yyyy");
    }
    return null;    
  }

  private String formatTime(String time)
  {
    if (time != null && !time.equals("000000"))
    {
      Date t = TextUtils.parseUserDate(time, "HHmmss");
      return TextUtils.formatDate(t, "HH:mm");
    }
    return null;
  }
  
}
