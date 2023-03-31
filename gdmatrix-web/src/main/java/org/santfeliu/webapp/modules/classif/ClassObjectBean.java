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
package org.santfeliu.webapp.modules.classif;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Document;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import static org.santfeliu.webapp.modules.classif.ClassifModuleBean.getPort;
import org.matrix.classif.Class;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class ClassObjectBean extends ObjectBean
{
  private Class classObject = new Class();
  private transient List<Document> versions;
  private String formSelector;

  @Inject
  ClassTypeBean classTypeBean;

  @Inject
  ClassFinderBean classFinderBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CLASS_TYPE;
  }

  @Override
  public Class getObject()
  {
    return isNew() ? null : classObject;
  }

  @Override
  public ClassTypeBean getTypeBean()
  {
    return classTypeBean;
  }

  @Override
  public ClassFinderBean getFinderBean()
  {
    return classFinderBean;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

//  public String getVersionLabel()
//  {
//    int version = document.getVersion();
//    ResourceBundle bundle = ResourceBundle.getBundle(
//      "org.santfeliu.doc.web.resources.DocumentBundle", getLocale());
//
//    return version > 0 ?
//      bundle.getString("document_version") + " " + version :
//      bundle.getString("document_newVersion");
//  }


  @Override
  public String getDescription()
  {
    return isNew() ? "" : classObject.getTitle();
  }

  public Class getClassObject()
  {
    return classObject;
  }

  public void setClassObject(Class classObject)
  {
    this.classObject = classObject;
  }

  @Override
  public void loadObject() throws Exception
  {
    versions = null;
    formSelector = null;

    if (!NEW_OBJECT_ID.equals(objectId))
    {
      String now = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
      classObject = getPort(false).loadClass(objectId, now);
    }
    else
    {
      classObject = new Class();
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    classObject = getPort(false).storeClass(classObject);
    setObjectId(classObject.getClassId());

    classFinderBean.outdate();

    versions = null;
  }

  @Override
  public void removeObject() throws Exception
  {
    getPort(false).removeClass(classObject.getClassId());

    classFinderBean.outdate();
  }

//  public void loadVersions()
//  {
//    try
//    {
//      if (isNew())
//      {
//        versions = Collections.EMPTY_LIST;
//      }
//      else
//      {
//        DocumentFilter filter = new DocumentFilter();
//        filter.getDocId().add(document.getDocId());
//        filter.setVersion(-1);
//        filter.setIncludeContentMetadata(false);
//        filter.getStates().add(State.DRAFT);
//        filter.getStates().add(State.COMPLETE);
//        filter.getStates().add(State.RECORD);
//        filter.getStates().add(State.DELETED);
//        OrderByProperty order = new OrderByProperty();
//        order.setName(DocumentConstants.VERSION);
//        order.setDescending(true);
//        filter.getOrderByProperty().add(order);
//        versions = getPort(false).findDocuments(filter);
//      }
//    }
//    catch (Exception ex)
//    {
//      error(ex);
//    }
//  }

  public List<Document> getVersions()
  {
    return versions;
  }

//  public void loadVersion(int version)
//  {
//    if (!NEW_OBJECT_ID.equals(objectId) && document.getVersion() != version)
//    {
//      try
//      {
//        document = getPort(false).loadDocument(
//          objectId, -version, ContentInfo.METADATA);
//      }
//      catch (Exception ex)
//      {
//        error(ex);
//      }
//    }
//  }

//  public void removeVersion(int version)
//  {
//    try
//    {
//      getPort(false).removeDocument(objectId, version);
//      versions = null;
//      loadVersion(0);
//      info("REMOVE_OBJECT");
//    }
//    catch (Exception ex)
//    {
//      error(ex);
//    }
//  }

  @Override
  public Serializable saveState()
  {
    return new Object[] { classObject, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.classObject = (Class) array[0];
    this.formSelector = (String)array[1];
  }

}
