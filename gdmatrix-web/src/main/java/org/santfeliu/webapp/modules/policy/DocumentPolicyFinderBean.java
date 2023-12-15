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
package org.santfeliu.webapp.modules.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.policy.DocumentPolicy;
import org.matrix.policy.DocumentPolicyFilter;
import org.matrix.policy.DocumentPolicyView;
import org.matrix.policy.PolicyState;
import org.santfeliu.util.BigList;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.BaseBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class DocumentPolicyFinderBean extends BaseBean
{
  private DocumentPolicyFilter filter = new DocumentPolicyFilter();
  private List<DocumentPolicyView> rows;
  private int firstRow;
  private boolean outdated;
  
  private Map<String, StateMapValue> statesMap;  
  
  private static final String OUTCOME = "/pages/policy/document_policy.xhtml";  

  @Inject
  NavigatorBean navigatorBean;

  @Override
  public PolicyObjectBean getObjectBean()
  {
    return null;
  }

  @PostConstruct
  public void init()
  {
    statesMap = new HashMap();    
  }
  
  public String show()
  {
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";    
  }
  
  public String getContent()
  {
    return OUTCOME;
  }   

  public DocumentPolicyFilter getFilter()
  {
    return filter;
  }

  public void setFilter(DocumentPolicyFilter filter)
  {
    this.filter = filter;
  }

  public List<DocumentPolicyView> getRows()
  {
    return rows;
  }

  public void setRows(List<DocumentPolicyView> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public Map<String, StateMapValue> getStatesMap()
  {
    return statesMap;
  }

  public void setStatesMap(Map<String, StateMapValue> statesMap)
  {
    this.statesMap = statesMap;
  }
  
  public String getDocumentUrl(Document document)
  {
    String url = null;

    if (document == null)
      return null;

    Content content = document.getContent();
    if (content != null)
    {
      String mimeType = content.getContentType();
      String contentId = content.getContentId();
      String extension = MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
      String name = document.getDocId();
      
      return "/documents/" + contentId + "/" + name + "." + extension;
    }
    return url;    
  }
  
  public void setRowState(PolicyState newValue)
  {
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}"); 
    DocumentPolicy docPolicy = docPolicyView.getDocPolicy();
    

    String docPolicyId = docPolicy.getDocPolicyId();
    StateMapValue stateValue = statesMap.get(docPolicyId);    
    if (stateValue == null)
      statesMap.put(docPolicyId, new StateMapValue(docPolicy, newValue));
    else if (stateValue.docPolicy.getState() != newValue)
      stateValue.newState = newValue;
    else
      statesMap.remove(docPolicyId);
  }
  
  public PolicyState getRowState()
  {
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}"); 

    StateMapValue stateValue = 
      statesMap.get(docPolicyView.getDocPolicy().getDocPolicyId());
    return stateValue != null ? stateValue.newState : 
      docPolicyView.getDocPolicy().getState();
  }
    
  public boolean isRowStateChanged(DocumentPolicyView row)
  {  
    String docPolicyId = row.getDocPolicy().getDocPolicyId();
    StateMapValue stateValue = statesMap.get(docPolicyId);
    
    return stateValue != null 
      && stateValue.newState != row.getDocPolicy().getState();
  } 
  
  public void changeState() throws Exception
  {
    if (statesMap != null)
    {
      for (Map.Entry<String, StateMapValue> entry : statesMap.entrySet())
      {
        StateMapValue stateValue = entry.getValue();
        if (stateValue != null)
        {          
          stateValue.docPolicy.setState(stateValue.newState);
          PolicyModuleBean.getPort(false)
            .storeDocumentPolicy(stateValue.docPolicy);
        }
      }
      find();
      statesMap.clear();      
    }
  }  
  
  public void cancelChanges()
  {
    statesMap.clear();
  }  
    

  public void find()
  {
    doFind(true);
    firstRow = 0;
  }

  public void outdate()
  {
    outdated = true;
  }

  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }

  public void clear()
  {
    filter = new DocumentPolicyFilter();
    rows = null;
  }
  
  @Override
  public Serializable saveState()
  {    
    return new Object[]{ filter, firstRow, rows, outdated };    
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] stateArray = (Object[])state;
    filter = (DocumentPolicyFilter)stateArray[0];
    firstRow = (Integer)stateArray[1];
    rows = (List<DocumentPolicyView>)stateArray[2];
    outdated = (Boolean)stateArray[3];
  }  

  private void doFind(boolean autoLoad)
  {
    try
    {
      rows = new BigList(20, 10)
      {
        @Override
        public int getElementCount()
        {
          try
          {
            return PolicyModuleBean.getPort(false)
              .countDocumentPolicies(filter);
          }
          catch (Exception ex)
          {
            error(ex);
            return 0;
          }
        }

        @Override
        public List getElements(int firstResult, int maxResults)
        {
          try
          {
            filter.setFirstResult(firstResult);
            filter.setMaxResults(maxResults);
            List<DocumentPolicyView> docPolicyViewList =
              PolicyModuleBean.getPort(false).findDocumentPolicyViews(filter);

            return docPolicyViewList;
          }
          catch (Exception ex)
          {
            error(ex);
            return null;
          }
        }
      };

      outdated = false;

      if (autoLoad)
      {
        if (rows.size() == 1)
        {
          navigatorBean.view(rows.get(0).getDocPolicy().getPolicyId());
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public class StateMapValue implements Serializable
  {
    public DocumentPolicy docPolicy;
    public PolicyState newState;

    public StateMapValue(DocumentPolicy docPolicy, PolicyState newState)
    {
      this.docPolicy = docPolicy;
      this.newState = newState;
    }
  }  
   
}
