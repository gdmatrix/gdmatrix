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
package org.santfeliu.webapp;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.setup.ObjectSetupCache;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
public abstract class FinderBean extends BaseBean
{
  private static final String SMART_SEARCH_TIP_DOCID_PROPERTY =
    "smartSearchTipDocId";
  private static final Map<String, String> smartSearchTipContentMap =
    new HashMap();
  private static long lastSmartSearchTipRefresh = System.currentTimeMillis();

  private int filterTabSelector;
  private int objectPosition = -1;
  private boolean finding;
  protected transient ObjectSetup objectSetup;

  public int getFilterTabSelector()
  {
    return filterTabSelector;
  }

  public void setFilterTabSelector(int selector)
  {
    this.filterTabSelector = selector;
  }

  public abstract void find();

  public abstract void smartFind();

  public int getObjectCount()
  {
    return 0;
  }

  public String getObjectId(int position)
  {
    return NEW_OBJECT_ID;
  }

  public int getObjectPosition()
  {
    return objectPosition;
  }

  public void setObjectPosition(int objectPosition)
  {
    this.objectPosition = objectPosition;
  }

  public boolean isFinding()
  {
    return finding;
  }

  public void setFinding(boolean finding)
  {
    this.finding = finding;
  }

  public String getSmartSearchTip()
  {
    if (System.currentTimeMillis() - lastSmartSearchTipRefresh >
      (60 * 60 * 1000))
    {
      smartSearchTipContentMap.clear();
      lastSmartSearchTipRefresh = System.currentTimeMillis();
    }

    String docId = getSmartSearchTipDocId();
    if (docId != null)
    {
      if (!smartSearchTipContentMap.containsKey(docId))
      {
        String content = getDocContent(docId);
        smartSearchTipContentMap.put(docId, content);
      }
      return smartSearchTipContentMap.get(docId);
    }
    else
    {
      return null;
    }
  }

  public boolean isScrollEnabled()
  {
    if (objectPosition < 0 || objectPosition >= getObjectCount()) return false;

    ObjectBean objectBean = getObjectBean();

    return getObjectCount() > 1 && !objectBean.isNew() &&
      objectBean.getObjectId().equals(getObjectId(objectPosition));
  }

  public boolean hasNext()
  {
    return objectPosition >= 0 && objectPosition < getObjectCount() - 1;
  }

  public boolean hasPrevious()
  {
    return objectPosition >= 1;
  }

  public void view(int objectPosition)
  {
    if (objectPosition < 0 || objectPosition >= getObjectCount()) return;

    this.objectPosition = objectPosition;
    String objectId = getObjectId(objectPosition);
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      navigatorBean.view(objectId);
    }
  }

  public void viewNext()
  {
    if (hasNext())
    {
      view(objectPosition + 1);
    }
  }

  public void viewPrevious()
  {
    if (hasPrevious())
    {
      view(objectPosition - 1);
    }
  }

  public void loadObjectSetup() throws Exception
  {
    String setupName = getProperty("objectSetup");
    if (setupName == null)
    {
      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      String typeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      Type type = TypeCache.getInstance().getType(typeId);
      PropertyDefinition propdef = type.getPropertyDefinition("objectSetup");
      if (propdef != null && !propdef.getValue().isEmpty())
      {
        setupName = propdef.getValue().get(0);
      }
    }

    ObjectSetup defaultSetup = getObjectBean().getTypeBean().getObjectSetup();
    if (setupName != null)
    {
      objectSetup = ObjectSetupCache.getConfig(setupName);
    }
    else
    {
      objectSetup = defaultSetup;
    }
  }

  private String getSmartSearchTipDocId()
  {
    try
    {
      if (objectSetup == null) loadObjectSetup();
      String docId = objectSetup.getSmartSearchTipDocId();
      if (docId == null)
      {
        docId = getProperty(SMART_SEARCH_TIP_DOCID_PROPERTY);
        if (docId == null)
        {
          NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
          String typeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
          Type type = TypeCache.getInstance().getType(typeId);
          PropertyDefinition propdef =
            type.getPropertyDefinition("_" + SMART_SEARCH_TIP_DOCID_PROPERTY);
          if (propdef != null && !propdef.getValue().isEmpty())
          {
            docId = propdef.getValue().get(0);
          }
        }
      }
      return docId;
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  private String getDocContent(String docId)
  {
    try
    {
      Document document = DocModuleBean.getPort(true).loadDocument(docId, 0,
        ContentInfo.ALL);
      DataHandler dh = DocumentUtils.getContentData(document);
      long size = document.getContent().getSize();
      int iSize = (int)size;
      InputStream is = dh.getInputStream();
      byte[] byteArray = new byte[iSize];
      is.read(byteArray);
      return new String(byteArray);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

}
