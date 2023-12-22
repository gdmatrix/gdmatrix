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
package org.santfeliu.webapp.modules.translation;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.translation.Translation;
import org.matrix.translation.TranslationFilter;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 *
 * @author blanquepa
 */
@ViewScoped
@Named
public class TranslationFinderBean extends FinderBean
{
  private TranslationFilter filter = new TranslationFilter();
  private List<Translation> rows;
  
  private String smartFilter;
  private int firstRow;
  private boolean outdated;  
    
  @Inject
  NavigatorBean navigatorBean;

  @Inject
  TranslationTypeBean translationTypeBean;

  @Inject
  TranslationObjectBean translationObjectBean; 
      
  @Override
  public ObjectBean getObjectBean()
  {
    return translationObjectBean;
  }

  public TranslationFilter getFilter()
  {
    return filter;
  }

  public void setFilter(TranslationFilter filter)
  {
    this.filter = filter;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }
  
  public List<Translation> getRows()
  {
    return rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }
  
  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getTransId();
  } 
  
  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }  
  
  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);

    filter = translationTypeBean.queryToFilter(smartFilter, 
      DictionaryConstants.TRANSLATION_TYPE);
    
    doFind(true);
    resetWildcards(filter);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);  
    smartFilter = translationTypeBean.filterToQuery(filter);
    doFind(true);
    resetWildcards(filter);    
    firstRow = 0;
  }
  
  public void outdate()
  {
    this.outdated = true;
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
    filter = new TranslationFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter, firstRow,
      getObjectPosition()};
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      setFinding((Boolean)stateArray[0]);
      setFilterTabSelector((Integer)stateArray[1]);
      filter = (TranslationFilter)stateArray[2];
      smartFilter = translationTypeBean.filterToQuery(filter);

      doFind(false);

      firstRow = (Integer)stateArray[3];
      setObjectPosition((Integer)stateArray[4]);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  private void doFind(boolean autoLoad)
  {
    try
    {
      if (!isFinding())
      {
        rows = Collections.emptyList();
      }
      else
      {
        String text = filter.getText();
        filter.setText(setWildcards(text));
        String translation = filter.getTranslation();
        filter.setTranslation(setWildcards(translation));        
        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return TranslationModuleBean.getPort(false)
                .countTranslations(filter);
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
              return TranslationModuleBean.getPort(false)
                .findTranslations(filter);
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
            navigatorBean.view(rows.get(0).getTransId());
            translationObjectBean.setSearchTabSelector(
              translationObjectBean.getEditModeSelector());
          }
          else if (translationObjectBean.getSearchTabSelector() ==
              translationObjectBean.getEditModeSelector())
          {
            translationObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  } 
  
  private String setWildcards(String text)
  {
    if (text != null && !text.startsWith("\"") && !text.endsWith("\""))
      text = "%" + text.replaceAll("^%|%$", "") + "%" ;
    else if (text != null && text.startsWith("\"") && text.endsWith("\""))
      text = text.replaceAll("^\"|\"$", "");
    return text;
  } 
  
  private void resetWildcards(TranslationFilter filter)
  {
    String title = filter.getText();
    if (title != null && !title.startsWith("\"") && !title.endsWith("\""))
      title = title.replaceAll("^%+|%+$", "");
    filter.setText(title);
    
    String translation = filter.getTranslation();
    if (translation != null && !translation.startsWith("\"") 
      && !translation.endsWith("\""))
        translation = translation.replaceAll("^%+|%+$", "");
    filter.setTranslation(translation);    
  }  
  
}
