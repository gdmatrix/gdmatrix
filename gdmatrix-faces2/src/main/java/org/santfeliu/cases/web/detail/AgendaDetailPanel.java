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

import java.util.List;
import org.santfeliu.agenda.web.EventSearchBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.DetailPanel;

/**
 *
 * @author blanquepa
 */
public class AgendaDetailPanel extends DetailPanel
{
  private static final String AGENDA_SEARCH_MID = "agendaSearchMid";
  private static final String DATE_FORMAT = "dateFormat";
  private static final String SHOW_MORE_TEXT = "showMoreText";
  private static final String PAGE_SIZE = "pageSize";
  private static final int DEFAULT_PAGE_SIZE = 5;
  
  private String agendaSearchMid;
  private String caseId;
  private List themes;
  

  @Override
  public void loadData(DetailBean detailBean) 
  {
    agendaSearchMid = getProperty(AGENDA_SEARCH_MID); 
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(agendaSearchMid);

    themes = mic.getMultiValuedProperty(EventSearchBean.SEARCH_EVENT_THEME);
    if (themes == null)
      themes = mic.getMultiValuedProperty("theme");
    
    caseId = ((CaseDetailBean) detailBean).getCaseId();
    
    
//    CaseEventFilter caseEventFilter = new CaseEventFilter();
//    caseEventFilter.setCaseId(getCaseId());
//    List<CaseEventView> views = CaseConfigBean.getPort().findCaseEventViews(caseEventFilter); 
  }
  
  @Override
  public boolean isRenderContent() 
  {
    return caseId != null;
  }
  
  @Override
  public String getType() 
  {
    return "agenda";
  }

  public List getThemes() 
  {
    return themes;
  }

  public String getCaseId() 
  {
    return caseId;
  }

  public void setCaseId(String caseId) 
  {
    this.caseId = caseId;
  }
  
  public String getAgendaSearchMid()
  {
    return agendaSearchMid != null ?
      agendaSearchMid : getProperty(AGENDA_SEARCH_MID);
  }  

  public String getEventUrl()
  {
    String agendaSearchMid = getProperty(AGENDA_SEARCH_MID);
    return "go.faces?xmid=" + agendaSearchMid + "&eventid=#{e.eventId}";
  }

  public String getDateFormat()
  {
    return getProperty(DATE_FORMAT);
  }

  public String getShowMoreText()
  {
    return getProperty(SHOW_MORE_TEXT);
  }

  public int getPageSize()
  {
    String pageSize = getProperty(PAGE_SIZE);
    return pageSize != null ? Integer.parseInt(pageSize) : DEFAULT_PAGE_SIZE;
  }
}
