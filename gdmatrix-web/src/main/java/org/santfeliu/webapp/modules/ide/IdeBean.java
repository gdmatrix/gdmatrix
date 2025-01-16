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
package org.santfeliu.webapp.modules.ide;

import org.santfeliu.webapp.modules.ide.doc.IdeDocumentType;
import org.santfeliu.webapp.modules.ide.doc.IdeDocument;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.ide.doc.IdeDocumentType.Tab;

/**
 *
 * @author realor
 */
@Named("ideBean")
@RequestScoped
public class IdeBean extends WebBean implements Serializable
{
  private String typeName = "javascript";
  private String name;
  private IdeDocument document = new IdeDocument();

  @Inject
  IdeDocumentCacheBean ideDocumentCacheBean;

  static List<SelectItem> typeSelectItems;

  public String getTypeName()
  {
    return typeName;
  }

  public void setTypeName(String typeName)
  {
    this.typeName = typeName;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public IdeDocument getDocument()
  {
    return document;
  }

  public List<Tab> getTabs()
  {
    IdeDocumentType type = IdeDocumentType.getInstance(typeName);
    return type.getTabs();
  }

  public synchronized List<SelectItem> getTypeSelectItems()
  {
    if (typeSelectItems == null)
    {
      typeSelectItems = new ArrayList<>();
      List<IdeDocumentType> types = IdeDocumentType.getTypes();
      Collections.sort(types, (t1, t2) -> t1.getLabel().compareTo(t2.getLabel()));
      for (IdeDocumentType type : types)
      {
        SelectItem selectItem = new SelectItem();
        selectItem.setLabel(type.getLabel());
        selectItem.setValue(type.getTypeName());
        typeSelectItems.add(selectItem);
      }
    }
    return typeSelectItems;
  }

  public List<String> completeDocumentName(String docName)
  {
    List<String> results = new ArrayList<>();
    try
    {
      IdeDocumentType type = IdeDocumentType.getInstance(typeName);
      DocumentManagerPort port = getPort();
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(type.getDocTypeId());
      Property property = new Property();
      property.setName(type.getDocProperty());
      property.getValue().add("%" + docName + "%");
      filter.getProperty().add(property);
      filter.getOutputProperty().add(type.getDocProperty());
      List<Document> documents = port.findDocuments(filter);
      for (Document doc : documents)
      {
        results.add(DictionaryUtils.getPropertyValue(
          doc.getProperty(), type.getDocProperty()));
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return results;
  }

  public void onTypeChange()
  {
    create();
  }

  public void onNameSelect(SelectEvent<String> event)
  {
    loadCache();
  }

  public String getDocumentIcon(IdeDocument document)
  {
    return IdeDocumentType.getInstance(document.getTypeName()).getIcon();
  }

  public void loadCache()
  {
    loadCache(IdeDocument.getReference(typeName, name));
  }

  public void loadCache(String reference)
  {
    saveCache();
    if (reference != null)
    {
      typeName = IdeDocument.getTypeName(reference);
      name = IdeDocument.getName(reference);
      document = ideDocumentCacheBean.getDocument(reference);
      if (document == null)
      {
        load();
      }
    }
  }

  public void saveCache()
  {
    ideDocumentCacheBean.putDocument(document);
  }

  public void create()
  {
    saveCache();
    name = null;
    document = new IdeDocument();
    document.setTypeName(typeName);
  }

  public void load()
  {
    try
    {
      IdeDocumentType type = IdeDocumentType.getInstance(typeName);
      DocumentManagerPort port = getPort();
      Document doc = this.findDocumentByName(type, name);
      if (doc != null)
      {
        String docId = doc.getDocId();
        int version = doc.getVersion();
        String title = doc.getTitle();
        int index = title.indexOf(":");
        if (index != -1)
        {
          title = title.substring(index + 1).trim();
        }

        doc = port.loadDocument(docId, version, ContentInfo.ALL);
        String source = IOUtils.toString(doc.getContent().getData().getInputStream(), "UTF-8");
        document = new IdeDocument();
        document.setTitle(title);
        document.setTypeName(typeName);
        document.setName(name);
        document.setDocId(docId);
        document.setVersion(version);
        document.setSource(source);
        document.setMetadata(metadataToJson(doc, type));
        saveCache();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void save()
  {
    try
    {
      if (StringUtils.isBlank(name)) return; // do nothing

      Document doc;
      IdeDocumentType type = IdeDocumentType.getInstance(typeName);
      if (name.equals(document.getName()))
      {
        doc = new Document();
        if (document.getDocId() != null)
        {
          doc.setDocId(document.getDocId());
          doc.setVersion(document.getVersion());
        }
      }
      else // save under another name
      {
        ideDocumentCacheBean.removeDocument(document.getReference());
        doc = this.findDocumentByName(type, name);
        if (doc == null)
        {
          doc = new Document();
        }
      }
      doc.setTitle(name + ": " + document.getTitle());
      doc.setDocTypeId(type.getDocTypeId());
      this.metadataFromJson(doc, document.getMetadata());
      Content content = new Content();
      byte[] bytes = document.getSource().getBytes("UTF-8");
      String contentType = type.getDocContentType();
      content.setContentType(contentType);
      DataSource ds = new MemoryDataSource(bytes, "source", contentType);
      content.setData(new DataHandler(ds));
      doc.setContent(content);
      DictionaryUtils.setProperty(doc, type.getDocProperty(), name);
      DocumentManagerPort port = getPort();
      doc = port.storeDocument(doc);
      document.setDocId(doc.getDocId());
      document.setVersion(doc.getVersion());
      document.setTypeName(typeName);
      document.setName(name); // set new name

      ideDocumentCacheBean.putDocument(document);
      growl("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
  }

  public void remove(String reference)
  {
    IdeDocument removed = ideDocumentCacheBean.removeDocument(reference);
    if (removed != null)
    {
      if (removed.getName().equals(name))
      {
        name = null;
        document = new IdeDocument();
        PrimeFaces.current().ajax().update("mainform:cnt");
      }
    }
  }

  @CMSAction
  public String show()
  {
    try
    {
      String template = UserSessionBean.getCurrentInstance().getTemplate();
      return "/templates/" + template + "/template.xhtml";
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public String getContent()
  {
    return "/pages/ide/ide.xhtml";
  }

  public void refreshSession()
  {
    // called periodically from remoteCommand to keep session alive
  }

  private DocumentManagerPort getPort()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return DocModuleBean.getPort(
      userSessionBean.getUserId(), userSessionBean.getPassword());
  }

  private Document findDocumentByName(IdeDocumentType type, String name)
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(type.getDocTypeId());
    Property property = new Property();
    property.setName(type.getDocProperty());
    property.getValue().add(name);
    filter.getProperty().add(property);
    List<Document> documents = getPort().findDocuments(filter);
    if (!documents.isEmpty())
    {
      return documents.get(0);
    }
    return null;
  }

  private void metadataFromJson(Document doc, String json)
  {
    Gson gson = new Gson();
    Map<String, Object> map = gson.fromJson(json, Map.class);
    if (map != null)
    {
      List<Property> props = DictionaryUtils.getPropertiesFromMap(map);
      doc.getProperty().clear();
      doc.getProperty().addAll(props);
    }
  }

  private String metadataToJson(Document doc, IdeDocumentType type)
  {
    Type docType = TypeCache.getInstance().getType(type.getDocTypeId());
    Map<String, Object> map =
      DictionaryUtils.getMapFromProperties(doc.getProperty(), docType);
    map.remove(type.getDocProperty());

    Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .disableHtmlEscaping().create();
    return gson.toJson(map);
  }
}
