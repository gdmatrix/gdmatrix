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

import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.kernel.PersonDocument;
import org.matrix.kernel.PersonDocumentView;
import org.matrix.kernel.PersonDocumentFilter;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.TypifiedPageBean;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author unknown
 */
public class DocumentPersonsBean extends TypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_personRootTypeId";

  private List<PersonDocumentView> rows;
  private PersonDocument editingPersonDocument;

  public DocumentPersonsBean()
  {
    super(DictionaryConstants.PERSON_DOCUMENT_TYPE, "DOC_ADMIN");

    DocumentMainBean documentMainBean =
      (DocumentMainBean)getBean("documentMainBean");
    Document document = documentMainBean.getDocument();
    Type docType = TypeCache.getInstance().getType(document.getDocTypeId());
    if (docType != null)
    {
      PropertyDefinition pd =
        docType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        setRootTypeId(pd.getValue().get((0)));
    }
    load();
  }

  public PersonDocument getEditingPersonDocument()
  {
    return editingPersonDocument;
  }

  public void setEditingPersonDocument(PersonDocument editingDocPerson)
  {
    this.editingPersonDocument = editingDocPerson;
  }

  public List<PersonDocumentView> getRows()
  {
    return rows;
  }

  public void setRows(List<PersonDocumentView> rows)
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      getEditingPersonDocument().getPersonDocTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getEditingPersonDocument().getPersonDocTypeId() != null &&
      getEditingPersonDocument().getPersonDocTypeId().trim().length() > 0;
  }

  public String show()
  {
    return "document_persons";
  }

  public String store()
  {
    if (editingPersonDocument == null)
    {
      load();
    }
    return show();
  }

  public String showPerson()
  {
    return getControllerBean().showObject("Person",
      (String)getValue("#{row.personView.personId}"));
  }

  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{documentPersonsBean.editingPersonDocument.personId}");
  }

  public String createDocumentPerson()
  {
    editingPersonDocument = new PersonDocument();
    return null;
  }

  public String editDocumentPerson()
  {
    try
    {
      PersonDocumentView row = (PersonDocumentView)getExternalContext().
        getRequestMap().get("row");
      String personDocId = row.getPersonDocId();
      if (personDocId != null)
        editingPersonDocument =
          KernelConfigBean.getPort().loadPersonDocument(personDocId);
    }
    catch (Exception e)
    {
      error(e);
    }
    return null;
  }

  public String cancelDocumentPerson()
  {
    editingPersonDocument = null;
    return null;
  }

  public String storeDocumentPerson()
  {
    try
    {
      if (editingPersonDocument != null)
      {
        if (((DocumentBean)getObjectBean()).isCreateNewVersion())
        {
          DocumentMainBean documentMainBean =
            (DocumentMainBean)getBean("documentMainBean");
          Document document = documentMainBean.getDocument();
          document.setVersion(DocumentConstants.NEW_VERSION);
          Document storedDocument = 
            DocumentConfigBean.getClient().storeDocument(document);
          editingPersonDocument.setDocId(storedDocument.getDocId());
          editingPersonDocument.setVersion(storedDocument.getVersion());
          String objectId =
            DocumentConfigBean.toObjectId(storedDocument.getDocId(), storedDocument.getVersion());
          getControllerBean().show(getSelectedMenuItem().getMid(), objectId);
        }
        else
        {
          String objectId = getObjectId();
          String[] objId = DocumentConfigBean.fromObjectId(objectId);
          editingPersonDocument.setDocId(objId[0]);
          editingPersonDocument.setVersion(Integer.valueOf(objId[1]));
        }

        KernelConfigBean.getPort().storePersonDocument(editingPersonDocument);
        editingPersonDocument = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      editingPersonDocument = null;
      load();
    }
    return null;
  }

  public String removeDocumentPerson()
  {
    try
    {
      if (((DocumentBean)getObjectBean()).isCreateNewVersion())      
      {
        DocumentMainBean documentMainBean =
          (DocumentMainBean)getBean("documentMainBean");
        Document document = documentMainBean.getDocument();
        document.setVersion(DocumentConstants.NEW_VERSION);
        Document storedDocument =
          DocumentConfigBean.getClient().storeDocument(document);
        PersonDocumentView row = (PersonDocumentView)getRequestMap().get("row");
        KernelConfigBean.getPort().removePersonDocument(row.getPersonDocId());
        String objectId =
          DocumentConfigBean.toObjectId(storedDocument.getDocId(), storedDocument.getVersion());
        getControllerBean().show(getSelectedMenuItem().getMid(), objectId);
      }
      else
      {
        PersonDocumentView row = (PersonDocumentView)getRequestMap().get("row");
        KernelConfigBean.getPort().removePersonDocument(row.getPersonDocId());
      }

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public List<SelectItem> getDocumentPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    if (editingPersonDocument != null)
      return personBean.getSelectItems(editingPersonDocument.getPersonId());
    else
      return personBean.getSelectItems(ControllerBean.NEW_OBJECT_ID);
  }

  public Date getCreationDateTime()
  {
    if (editingPersonDocument != null && editingPersonDocument.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(editingPersonDocument.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (editingPersonDocument != null && editingPersonDocument.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(editingPersonDocument.getChangeDateTime());
    else
      return null;
  }

  @Override
  protected org.santfeliu.dic.Type getSelectedType()
  {
    return TypeCache.getInstance().getType(DictionaryConstants.PERSON_DOCUMENT_TYPE);
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        PersonDocumentFilter filter = new PersonDocumentFilter();
        String[] objId = DocumentConfigBean.fromObjectId(getObjectId());
        filter.setDocId(objId[0]);
        filter.setVersion(Integer.valueOf(objId[1]));
        rows = KernelConfigBean.getPort().findPersonDocumentViews(filter);

        setRowsTypeLabels();

      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void setRowsTypeLabels()
  {
    for (PersonDocumentView personDocView : rows)
    {
      if (personDocView.getPersonDocId() != null)
      {
        String typeId = personDocView.getPersonDocTypeId();
        TypeCache typeCache = TypeCache.getInstance();
        try
        {
          Type type = typeCache.getType(typeId);
          if (type != null)
            personDocView.setPersonDocTypeId(type.getDescription());
        }
        catch (Exception ex)
        {
          warn(ex.getMessage());
        }
      }
    }
  }

}
