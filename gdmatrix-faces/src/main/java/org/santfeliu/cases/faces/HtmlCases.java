package org.santfeliu.cases.faces;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.Property;
import org.santfeliu.cases.web.CaseConfigBean;
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
public class HtmlCases extends UIComponentBase
{
  private Integer _rows;
  private Translator _translator;
  private String _translationGroup;
  private String _style;
  private String _styleClass;
  private String _var;
  //new filter attributes
  private String _caseTypeId;
  private String _propertyName;
  private List _propertyValues;
  private String _caseSearchMid;
  private ResultsManager _resultsManager;

  @Override
  public String getFamily()
  {
    return "Cases";
  }

  public String getCaseTypeId()
  {
    if (_caseTypeId != null) return _caseTypeId;
    ValueBinding vb = getValueBinding("caseTypeId");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setCaseTypeId(String _caseTypeId)
  {
    this._caseTypeId = _caseTypeId;
  }

  public String getPropertyName()
  {
    if (_propertyName != null) return _propertyName;
    ValueBinding vb = getValueBinding("propertyName");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setPropertyName(String _propertyName)
  {
    this._propertyName = _propertyName;
  }

  public List getPropertyValues()
  {
    if (_propertyValues != null) return _propertyValues;
    ValueBinding vb = getValueBinding("propertyValues");
    return vb != null ? (List)vb.getValue(getFacesContext()) : null;
  }

  public void setPropertyValues(List _propertyValues)
  {
    this._propertyValues = _propertyValues;
  }

  public Integer getRows()
  {
    if (_rows != null) return _rows.intValue();
    ValueBinding vb = getValueBinding("rows");
    Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
    return v != null ? v.intValue() : 0;
  }

  public void setRows(Integer _rows)
  {
    this._rows = _rows;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;

  }

  public void setStyle(String _style)
  {
    this._style = _style;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyleClass(String _styleClass)
  {
    this._styleClass = _styleClass;
  }


  public Translator getTranslator()
  {
    if (_translator != null)
      return _translator;
    ValueBinding vb = getValueBinding("translator");
    return vb != null? (Translator) vb.getValue(getFacesContext()): null;
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
    ValueBinding vb = getValueBinding("translationGroup");
    return vb != null? (String) vb.getValue(getFacesContext()): null;
  }

  public String getVar()
  {
    return _var;
  }

  public void setVar(String _var)
  {
    this._var = _var;
  }

  public String getCaseSearchMid()
  {
    if (_caseSearchMid != null)
      return _caseSearchMid;
    ValueBinding vb = getValueBinding("caseSearchMid");
    return vb != null? (String) vb.getValue(getFacesContext()): null;
  }

  public void setCaseSearchMid(String _caseSearchMid)
  {
    this._caseSearchMid = _caseSearchMid;
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
      CaseFilter filter = new CaseFilter();
      if (_rows != null)
        filter.setMaxResults(_rows.intValue());

      //types
      if (getCaseTypeId() != null && !getCaseTypeId().isEmpty())
        filter.setCaseTypeId(getCaseTypeId());

      //properties
      String propertyName = getPropertyName();
      if (propertyName != null)
      {
        Property property = new Property();
        property.setName(propertyName);
        property.getValue().addAll(getPropertyValues());
        filter.getProperty().add(property);
      }

      Translator translator = getTranslator();

      List<Case> cases;
      String nodeId = (String)getAttributes().get("nodeId");
      if (nodeId != null)
      {
        String workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();
        String username = UserSessionBean.getCurrentInstance().getUsername();
        cases = WidgetCache.getInstance().getWidgetObjects(workspaceId, nodeId, username);
        if (cases == null)
        {
          cases = CaseConfigBean.getPort().findCases(filter);
          WidgetCache.getInstance().setWidgetObjects(workspaceId, nodeId, username, cases);
        }
      }
      else
      {
        cases = CaseConfigBean.getPort().findCases(filter);
      }

      if (cases != null && cases.size() > 0)
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
        encodeDocuments(cases, writer, translator, clientId);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      FacesUtils.addMessage(this, "CAN_NOT_SHOW_CASES", null,
        FacesMessage.SEVERITY_ERROR);
    }
  }

  private void encodeDocuments(List<Case> cases, ResponseWriter writer,
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
    for (Case cas : cases)
    {
      writer.startElement("div", this);
      String rowClass = (count % 2 == 0) ? "row1" : "row2";
      writer.writeAttribute("class", rowClass, null);
      encodeCase(cas, writer, translator, clientId);
      count++;
      writer.endElement("div");
    }
    writer.endElement("div");
  }

  private void encodeCase(Case cas, ResponseWriter writer,
    Translator translator, String clientId) throws IOException
  {
    if (_var != null)
    {
      Map requestMap =
        getFacesContext().getExternalContext().getRequestMap();
      requestMap.put(getVar(), cas);
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
        Object columnValue = resultsManager.getColumnValue(cas, columnName);
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
    if (_caseTypeId != null)
    {
      ValueBinding vb = getValueBinding("caseTypeId");
      if (vb != null)
      {
        if (!vb.isReadOnly(context))
        {
          vb.setValue(context, _caseTypeId);
          _caseTypeId = null;
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
    Object values[] = new Object[11];
    values[0] = super.saveState(context);
    values[1] = _rows;
    values[2] = _translator;
    values[3] = _translationGroup;
    values[4] = _style;
    values[5] = _styleClass;
    values[6] = _var;
    values[7] = _caseTypeId;
    values[8] = _propertyName;
    values[9] = _propertyValues;
    values[10] = _resultsManager;
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
    _caseTypeId = (String)values[7];
    _propertyName = (String)values[8];
    _propertyValues = (List)values[9];
    _resultsManager = (ResultsManager)values[10];
  }
}
