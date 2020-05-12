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
package org.santfeliu.doc.faces;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.misc.widget.WidgetCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.util.ColumnDefinition;
import org.santfeliu.web.obj.util.ResultsManager;

/**
 *
 * @author blanquepa
 */
public class HtmlDocuments extends UIComponentBase
{
  private Integer _rows;
  private Translator _translator;
  private String _translationGroup;
  private String _style;
  private String _styleClass;
  private String _var;
  //new filter attributes
  private String _docTypeId;
  private String _propertyName;
  private List _propertyValues;
  private List _orderBy;
  private Boolean _directDownload;
  private String _documentSearchMid;
  private ResultsManager _resultsManager;

  @Override
  public String getFamily()
  {
    return "Documents";
  }

  public String getDocTypeId()
  {
    if (_docTypeId != null) return _docTypeId;
    ValueExpression ve = getValueExpression("docTypeId");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDocTypeId(String _docTypeId)
  {
    this._docTypeId = _docTypeId;
  }

  public List getOrderBy()
  {
    if (_orderBy != null) return _orderBy;
    ValueExpression ve = getValueExpression("orderBy");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setOrderBy(List _orderBy)
  {
    this._orderBy = _orderBy;
  }

  public String getPropertyName()
  {
    if (_propertyName != null) return _propertyName;
    ValueExpression ve = getValueExpression("propertyName");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setPropertyName(String _propertyName)
  {
    this._propertyName = _propertyName;
  }

  public List getPropertyValues()
  {
    if (_propertyValues != null) return _propertyValues;
    ValueExpression ve = getValueExpression("propertyValues");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setPropertyValues(List _propertyValues)
  {
    this._propertyValues = _propertyValues;
  }

  public Integer getRows()
  {
    if (_rows != null) return _rows.intValue();
    ValueExpression ve = getValueExpression("rows");
    Integer v = ve != null ? (Integer)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.intValue() : 0;
  }

  public void setRows(Integer _rows)
  {
    this._rows = _rows;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setStyle(String _style)
  {
    this._style = _style;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String _styleClass)
  {
    this._styleClass = _styleClass;
  }


  public Translator getTranslator()
  {
    if (_translator != null)
      return _translator;
    ValueExpression ve = getValueExpression("translator");
    return ve != null? (Translator) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setTranslator(Translator _translator)
  {
    this._translator = _translator;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null)
      return _translationGroup;
    ValueExpression ve = getValueExpression("translationGroup");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): null;
  }

  public String getVar()
  {
    return _var;
  }

  public void setVar(String _var)
  {
    this._var = _var;
  }

  public Boolean getDirectDownload()
  {
    if (_directDownload != null) return _directDownload.booleanValue();
    ValueExpression ve = getValueExpression("directDownload");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : Boolean.TRUE.booleanValue();
  }

  public void setDirectDownload(Boolean _directDownload)
  {
    this._directDownload = _directDownload;
  }

  public String getDocumentSearchMid()
  {
    if (_documentSearchMid != null)
      return _documentSearchMid;
    ValueExpression ve = getValueExpression("documentSearchMid");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setDocumentSearchMid(String _documentSearchMid)
  {
    this._documentSearchMid = _documentSearchMid;
  }

  public ResultsManager getResultsManager()
  {
    return _resultsManager;
  }

  public void setResultsManager(ResultsManager _resultsManager)
  {
    this._resultsManager = _resultsManager;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    String clientId = getClientId(context);
    ResponseWriter writer = context.getResponseWriter();
    try
    {
      DocumentFilter filter = new DocumentFilter();
      if (_rows != null)
        filter.setMaxResults(_rows.intValue());

      //types
      if (getDocTypeId() != null && !getDocTypeId().isEmpty())
        filter.setDocTypeId(getDocTypeId());

      //properties
      String propertyName = getPropertyName();
      if (propertyName != null)
      {
        Property property = new Property();
        property.setName(propertyName);
        property.getValue().addAll(getPropertyValues());
        filter.getProperty().add(property);
      }

      //order by
      if (getOrderBy() != null && !getOrderBy().isEmpty())
      {
        filter.getOrderByProperty().clear();
        Iterator it = getOrderBy().iterator();
        while (it.hasNext())
        {
          String orderByElement = (String) it.next();
          String[] parts = orderByElement.split(":");
          OrderByProperty orderByProperty = new OrderByProperty();
          orderByProperty.setName(parts[0]);
          if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
          {
            orderByProperty.setDescending(true);
          }
          filter.getOrderByProperty().add(orderByProperty);
        }
      }
      filter.setIncludeContentMetadata(true);

      Translator translator = getTranslator();

      List<Document> documents;
      String nodeId = (String)getAttributes().get("nodeId");
      if (nodeId != null)
      {
        String workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();        
        String username = UserSessionBean.getCurrentInstance().getUsername();
        documents = WidgetCache.getInstance().getWidgetObjects(workspaceId, nodeId, username);
        if (documents == null)
        {
          documents = getDocumentManagerClient().findDocuments(filter);
          WidgetCache.getInstance().setWidgetObjects(workspaceId, nodeId, username, documents);
        }
      }
      else
      {
        documents = getDocumentManagerClient().findDocuments(filter);
      }

      if (documents != null && documents.size() > 0)
      {
        if (getResultsManager() == null)
          setResultsManager(new ResultsManager("",""));
        if (getResultsManager().getColumnNames() == null ||
          getResultsManager().getColumnNames().isEmpty())
        {
          ColumnDefinition iconColDef = new ColumnDefinition("icon");
          iconColDef.setType(ColumnDefinition.CUSTOM_TYPE);
          getResultsManager().addDefaultColumn(iconColDef);

          ColumnDefinition titleColDef = new ColumnDefinition("title");
          titleColDef.setType(ColumnDefinition.CUSTOM_TYPE);
          getResultsManager().addDefaultColumn(titleColDef);
        }
        encodeDocuments(documents, writer, translator, clientId);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      FacesUtils.addMessage(this, "CAN_NOT_SHOW_DOCUMENTS", null,
        FacesMessage.SEVERITY_ERROR);
    }
  }
  
  private void encodeDocuments(List<Document> documents, ResponseWriter writer,
    Translator translator, String clientId)
    throws IOException
  {
    writer.startElement("div", this);
    String style = getStyle();
    if (style != null)
      writer.writeAttribute("style", style, null);
    String styleClass = getStyleClass();
    if (styleClass != null)
      writer.writeAttribute("class", styleClass, null);

    int count = 0;
    for (Document document : documents)
    {
      writer.startElement("div", this);
      String rowClass = (count % 2 == 0) ? "row1" : "row2";
      writer.writeAttribute("class", rowClass, null);
      encodeDocument(document, writer, translator, clientId);
      count++;
      writer.endElement("div");
    }
    writer.endElement("div");
  }

  private void encodeDocument(Document document, ResponseWriter writer,
    Translator translator, String clientId) throws IOException
  {
    if (_var != null)
    {
      Map requestMap =
        getFacesContext().getExternalContext().getRequestMap();
      requestMap.put(getVar(), document);
    }
    String contextPath = MatrixConfig.getProperty("contextPath");

    //Row division
    ResultsManager resultsManager = getResultsManager();
    List<String> columnNames = resultsManager.getColumnNames().isEmpty() ?
      resultsManager.getDefaultColumnNames() :
      resultsManager.getColumnNames();
    for (String columnName : columnNames)
    {
      writer.startElement("div", this);
      ColumnDefinition colDef = resultsManager.getColumnDefinition(columnName);
      String style = resultsManager.getColumnStyle(columnName);
      if (style != null)
        writer.writeAttribute("style", style, null);
      String styleClass = resultsManager.getColumnStyleClass(columnName);
      if (styleClass != null)
        writer.writeAttribute("class", styleClass, null);

      if (!colDef.isCustomType())
      {
        Object columnValue = resultsManager.getColumnValue(document, columnName);
        if (columnValue != null)
        {
          if (colDef.isImageType())
          {
            writer.startElement("img", this);
            writer.writeAttribute("src", (String)columnValue, null);
            writer.endElement("img");
          }
          else if (colDef.isLinkType())
          {
            writer.startElement("a", this);
            writer.writeAttribute("href", (String)columnValue, null);
            renderPlainText((String)columnValue, writer, translator);
            writer.endElement("a");
          }
          else
          {
            encodeColumnValue(columnValue, writer, translator);
          }
        }
      }
      else if (colDef.isCustomType() && "icon".equals(columnName))
      {
        //Icon field
        writer.startElement("img", this);
        String contentType = document.getContent().getContentType();
        String iconUrl = DocumentUtils.typeToImage(
          contextPath + "/common/doc/images/extensions/", contentType);
        writer.writeAttribute("src", iconUrl, null);
        writer.endElement("img");
      }
      else if (colDef.isCustomType() && "title".equals(columnName))
      {
        //Title field
        writer.startElement("a", this);
        String title = document.getTitle();
        String contentType = document.getContent().getContentType();
        String linkUrl = null;
        if (!getDirectDownload())
          linkUrl = contextPath + "/go.faces?xmid=" + getDocumentSearchMid() + "&docid=" + document.getDocId();
        else
          linkUrl = contextPath + "/documents/" + document.getDocId() +
            "/" + DocumentUtils.getFilename(document.getTitle(), contentType);
        writer.writeAttribute("href", linkUrl, null);
        renderPlainText(title, writer, translator);
        writer.endElement("a");
      }
      writer.endElement("div");
    }
  }

  private void encodeColumnValue(Object columnValue, ResponseWriter writer,
    Translator translator) throws IOException
  {
    if (columnValue == null)
      renderPlainText("", writer, translator);

    if (columnValue instanceof List)
    {
      writer.startElement("ul", this);
      for (Object item : (List)columnValue)
      {
        writer.startElement("li", this);
        renderPlainText(String.valueOf(item), writer, translator);
        writer.endElement("li");
      }
      writer.endElement("ul");
    }
    else
      renderPlainText(String.valueOf(columnValue), writer, translator);
  }

  private void renderPlainText(String text,
    ResponseWriter writer, Translator translator) throws IOException
  {
    String textToRender = null;
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      StringWriter sw = new StringWriter();
      translator.translate(new StringReader(text), sw, "text/plain",
        userLanguage, translationGroup);
      textToRender = sw.toString();
    }
    else textToRender = text;

    String lines[] = textToRender.split("\n");
    writer.writeText(lines[0], JSFAttr.VALUE_ATTR);
    for (int i = 1; i < lines.length; i++)
    {
      writer.startElement("br", this);
      writer.endElement("br");
      writer.writeText(lines[i], JSFAttr.VALUE_ATTR);
    }
  }

  private void renderHtmlText(String text,
    ResponseWriter writer, Translator translator) throws IOException
  {
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      translator.translate(new StringReader(text),
        writer, "text/html", userLanguage, translationGroup);
    }
    else writer.write(text);
  }

  @Override
  public void processValidators(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processValidators(context);
  }

  @Override
  public void processUpdates(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processUpdates(context);
    try
    {
      updateModel(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }

  public void updateModel(FacesContext context)
  {
    if (_docTypeId != null)
    {
      ValueExpression ve = getValueExpression("docTypeId");
      if (ve != null)
      {
        if (!ve.isReadOnly(context.getELContext()))
        {
          ve.setValue(context.getELContext(), _docTypeId);
          _docTypeId = null;
        }
      }
    }
    if (_propertyName != null && _propertyValues != null)
    {
      ValueBinding vbn = getValueBinding("propertyName");
      ValueBinding vbv = getValueBinding("propertyValues");
      if (vbn != null && vbv != null)
      {
        if (!vbn.isReadOnly(context))
        {
          vbn.setValue(context, _propertyName);
          _propertyName = null;
        }
        if (!vbv.isReadOnly(context))
        {
          vbv.setValue(context, _propertyValues);
          _propertyValues = null;
        }
      }
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[12];
    values[0] = super.saveState(context);
    values[1] = _rows;
    values[2] = _translator;
    values[3] = _translationGroup;
    values[4] = _style;
    values[5] = _styleClass;
    values[6] = _var;
    values[7] = _docTypeId;
    values[8] = _propertyName;
    values[9] = _propertyValues;
    values[10] = _orderBy;
    values[11] = _resultsManager;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _rows = (Integer)values[1];
    _translator = (Translator)values[2];
    _translationGroup = (String)values[3];
    _style = (String)values[4];
    _styleClass = (String)values[5];
    _var = (String)values[6];
    _docTypeId = (String)values[7];
    _propertyName = (String)values[8];
    _propertyValues = (List)values[9];
    _orderBy = (List)values[10];
    _resultsManager = (ResultsManager)values[11];
  }

  private DocumentManagerClient getDocumentManagerClient()
  {
    String userId = UserSessionBean.getCurrentInstance().getUsername();
    String password = UserSessionBean.getCurrentInstance().getPassword();
    return new CachedDocumentManagerClient(userId, password);
  }
}
