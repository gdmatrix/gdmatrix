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
package org.santfeliu.doc.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.doc.transform.Transformation;
import org.santfeliu.doc.transform.TransformationManager;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.IconMap;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectAction;
import org.santfeliu.web.obj.ObjectBean;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
public class DocumentBean extends ObjectBean
{
  private static String EXTENSION_ICONS_PATH = "/common/doc/images/extensions/";

  private boolean createNewVersion;
  
  public DocumentBean()
  {
  }

  public String getObjectTypeId()
  {
    return DictionaryConstants.DOCUMENT_TYPE;
  }

  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        String[] objId = DocumentConfigBean.fromObjectId(getObjectId());
        DocumentConfigBean.getPort().removeDocument(objId[0], Integer.valueOf(objId[1]));
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }

  @Override
  public String select()
  {
    String outcome = null;
    try
    {
      executeParametersManagers documentSearchBean = (executeParametersManagers) getBean("documentSearchBean");
      String[] objId = DocumentConfigBean.fromObjectId(getObjectId());
      String docId = objId[0];
      int version = Integer.parseInt(objId[1]);
      documentSearchBean.restoreFilter();
      outcome = getControllerBean().select(DocumentConfigBean.toObjectId(docId, version));
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return outcome;
  }

  @Override
  public String getDescription()
  {
    DocumentMainBean documentMainBean =
        (DocumentMainBean)getBean("documentMainBean");
    Document document = documentMainBean.getDocument();
    return getDocumentDescription(document);
  }

  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      String[] oidArray = DocumentConfigBean.fromObjectId(oid);
      String docId = oidArray[0];
      int version = Integer.valueOf(oidArray[1]);
      Document document =
        DocumentConfigBean.getPort().loadDocument(docId, version, 
        ContentInfo.ID);
      description = getDocumentDescription(document);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;    
  }
  
  private String getDocumentDescription(Document document)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(document.getTitle());
    buffer.append(" (");
    buffer.append(DocumentConfigBean.toObjectId(document.getDocId(),
      document.getVersion()));
    buffer.append(")");
    return buffer.toString();
  }

  @Override
  public boolean isEditable()
  {
    DocumentMainBean documentMainBean =
      (DocumentMainBean)getBean("documentMainBean");
    try
    {
      return documentMainBean.isEditable();
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public static String getContentTypeIcon(String mimeType)
  {
    return getContentTypeIcon(EXTENSION_ICONS_PATH, mimeType);
  }

  public static String getContentTypeIcon(String basePath, String mimeType)
  {
    String imageUrl = basePath + "altre.gif";

    if (mimeType != null)
    {
      IconMap icons = new IconMap(basePath);
      String extension = MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
      imageUrl = (String)icons.get(extension);
      if (imageUrl == null)
        imageUrl = basePath + "altre.gif";
    }

    return imageUrl;
  }

  @Override
  public List<ObjectAction> getObjectActions()
  {
    if (isNew()) return Collections.EMPTY_LIST;

    DocumentMainBean documentMainBean =
      (DocumentMainBean)getBean("documentMainBean");
    Document document = documentMainBean.getDocument();

    if (document == null) return Collections.EMPTY_LIST;

    ArrayList<ObjectAction> actions = new ArrayList<ObjectAction>();
    
    // get available document transformations
    Transformation reqTr = new Transformation(document);
    List<Transformation> transformations = new ArrayList<Transformation>();
    TransformationManager.findTransformations(reqTr, transformations);
    for (Transformation tr : transformations)
    {
      ObjectAction action = new ObjectAction();
      action.setDescription(tr.getDescription());
      action.setUrl("/documents/" + document.getDocId() +
        "?" + DocumentReader.VERSION_PARAM + "=" + document.getVersion() + "&" +
        DocumentReader.TRANSFORM_WITH_PARAM + "=" +
        tr.getTransformerId() + "/" + tr.getName());
      action.setTarget("_blank");
      String targetContentType = tr.getTargetContentType();
      if (targetContentType != null)
      {
        String extension =
          MimeTypeMap.getMimeTypeMap().getExtension(targetContentType);
        if (extension != null)
        {
          action.setImage("/common/doc/images/extensions/" +
            extension + ".gif");
        }
      }
      actions.add(action);
    }
//    ObjectAction action = new ObjectAction();
//    action.setDescription("Desa els canvis");
//    action.setExpression("#{controllerBean.objectBean.store}");
//    actions.add(action);
    return actions;
  }

  public boolean isCreateNewVersion()
  {
    return createNewVersion;
  }

  public void setCreateNewVersion(boolean createNewVersion)
  {
    this.createNewVersion = createNewVersion;
  }

  //Workaround to add old document version to page history
  @Override
  public void postStore()
  {
    if (isCreateNewVersion())
    {
      MenuItemCursor menuItem = getSelectedMenuItem();
      menuItem = getControllerBean().getHeadMenuItem(menuItem);
      if (menuItem.isNull())
      {
        error(ControllerBean.INVALID_NODE_CONFIG);
        return;
      }
      menuItem = getControllerBean().getLeafMenuItem(menuItem);
      String tabMid = menuItem.getMid();
      PageBean pageBean = getControllerBean().getPageBean();
      if (pageBean != null)
      {
        getControllerBean().getPageHistory().visit(tabMid, getObjectId());
      }
    }
  }

  //Deletes temporal file if exists
  @Override
  public void preCancel()
  {
    ControllerBean controllerBean = getControllerBean();
    if (controllerBean.existsBean("documentContentBean"))
    {
      DocumentContentBean contentBean =
        (DocumentContentBean)getBean("documentContentBean");
      if (contentBean.getTempFile() != null && !contentBean.getTempFile().isEmptyFile())
      {
        contentBean.getTempFile().reset();
      }
    }
  }
}
