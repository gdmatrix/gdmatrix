package org.santfeliu.cases.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.cases.Case;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.agenda.web.EventBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.ObjectDumper;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;

public class CaseEventsBean extends DynamicTypifiedPageBean
{
  public static final String ALL_TYPES_VISIBLE_PROPERTY = "_eventsAllTypesVisible";
  public static final String ROOT_TYPE_ID_PROPERTY = "_eventRootTypeId";
  public static final String ROW_TYPE_ID_PROPERTY = "_eventsRowTypeId";  
  public static final String GROUPBY_PROPERTY = "_eventsGroupBy";
  public static final String GROUP_SELECTION_MODE_PROPERTY = "_eventsGroupSelectionMode";    
  public static final String ORDERBY_PROPERTY = "_eventsOrderBy";  
  public static final String SHOW_PROPERTIES_PROPERTY = "_eventProperties";
  
  private CaseEvent editingEvent;
  private List<CaseEventView> rows;

  private String lastCaseEventMid = null;

  public CaseEventsBean()
  {
    super(DictionaryConstants.CASE_EVENT_TYPE, "CASE_ADMIN", false);    
  }

  public CaseEvent getEditingEvent()
  {
    return editingEvent;
  }

  public void setEditingEvent(CaseEvent editingEvent)
  {
    this.editingEvent = editingEvent;
  }
  
  public List<CaseEventView> getAllRows()
  {
    return rows;
  }

  public String show()
  {
    try
    {
      preShow();
      String mid = UserSessionBean.getCurrentInstance().getSelectedMid();
      if (!mid.equals(lastCaseEventMid))
      {
        load();
      }
      lastCaseEventMid = mid;      
      postShow();    
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      return "case_events";
    }
  }

  @Override
  public String store()
  {
    if (editingEvent != null)
    {
      storeEvent();
    }
    else
    {
      load();
    }
    return show();
  }
  
  public String showEvent()
  {
    return getControllerBean().showObject("Event",
     (String)getValue("#{row.event.eventId}"));
  }
  
  public String searchEvent()
  {
    return getControllerBean().searchObject("Event",
      "#{caseEventsBean.editingEvent.eventId}");
  }
  
  public String removeEvent()
  {
    try
    {
      CaseEventView row = (CaseEventView)getRequestMap().get("row");
      preRemove();
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCaseEvent(row.getCaseEventId());
      getViewPropertiesMap().remove(row.getCaseEventId());
      postRemove();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeEvent()
  {
    try
    {
      if (editingEvent.getEventId() == null || 
        editingEvent.getEventId().isEmpty())
      {
        throw new Exception("EVENT_MUST_BE_SELECTED");
      }
      
      preStore();
      String caseId = getObjectId();
      editingEvent.setCaseId(caseId);
      org.matrix.dic.Type type = getCurrentType();
      if (type != null)
        editingEvent.setCaseEventTypeId(type.getTypeId());
      
      editingEvent.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        editingEvent.getProperty().addAll(properties);
      
      CaseManagerPort port = CaseConfigBean.getPort();
      editingEvent = port.storeCaseEvent(editingEvent);      
      
      getViewPropertiesMap().remove(editingEvent.getCaseEventId());      

      postStore();
      editingEvent = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String createEvent()
  {
    editingEvent = new CaseEvent();
    getData().clear();    
    setCurrentTypeId(null);
    return null;
  }
  
  public String editEvent()
  {
    try
    {
      CaseEventView row = (CaseEventView)getExternalContext().
        getRequestMap().get("row");   
      String caseEventId = row.getCaseEventId();
      if (caseEventId != null)
      {
        editingEvent = CaseConfigBean.getPort().loadCaseEvent(caseEventId);
        setCurrentTypeId(editingEvent.getCaseEventTypeId());
        setFormDataFromProperties(editingEvent.getProperty());
      }
      else
      {
        editingEvent = new CaseEvent();
        getData().clear();        
        setCurrentTypeId(null);
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelEvent()
  {
    editingEvent = null;
    return null;
  }
  
  @Override
  public boolean isModified()
  {
    return editingEvent != null;
  }  
  
  public List<SelectItem> getEventSelectItems()
  {
    EventBean eventBean = (EventBean)getBean("eventBean");
    return eventBean.getSelectItems(editingEvent.getEventId());
  }
  
  public String getEventDate()
  {
    String result = "";
    CaseEventView row = 
      (CaseEventView)getExternalContext().getRequestMap().get("row");   
    if (row.getEvent().getStartDateTime() != null)
    {
      Date date = TextUtils.parseInternalDate(row.getEvent().getStartDateTime());
      if (date != null)
      {
        result = TextUtils.formatDate(date, "dd/MM/yyyy");
      }
    }
    return result;
  }
  
  protected void load()
  {
    try
    {
      editingEvent = null;      
      
      CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
      Case cas = caseMainBean.getCase();
      Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
      if (caseType != null)
        loadPropertyDefinitions(caseType);
      
      if (!isNew())
      {
        preLoad();
        CaseEventFilter filter = new CaseEventFilter();
        filter.setCaseId(getObjectId());
        filter.setCaseEventTypeId(rowTypeId);
        rows = CaseConfigBean.getPort().findCaseEventViews(filter);
        loadViewPropertiesMap(rows);
        postLoad();
        setGroups(rows, getGroupExtractor());
        //TODO Order: parametrize using GroupablePageBean
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  private void loadPropertyDefinitions(Type caseType)
  {
    if (caseType != null)
    {      
      rootTypeId = getIndexedDicProperty(caseType, ROOT_TYPE_ID_PROPERTY, rootTypeId);

      groupSelectionMode = getIndexedDicProperty(caseType, GROUP_SELECTION_MODE_PROPERTY, null);

      groupBy = getIndexedDicProperty(caseType, GROUPBY_PROPERTY, null);
                
      String showPropertiesString = getIndexedDicProperty(caseType, SHOW_PROPERTIES_PROPERTY, null);
      if (showPropertiesString != null)
        defaultViewDumper = new ObjectDumper(showPropertiesString);
      else
        defaultViewDumper = new ObjectDumper(new ArrayList());      

      rowTypeId = getIndexedDicProperty(caseType, ROW_TYPE_ID_PROPERTY, null);

      String allTypesVisibleString = getIndexedDicProperty(caseType, ALL_TYPES_VISIBLE_PROPERTY, null);
      if (allTypesVisibleString != null)
      {
        allTypesVisible = Boolean.parseBoolean(allTypesVisibleString);
      }
            
      String orderByString = getIndexedDicProperty(caseType, ORDERBY_PROPERTY, null);
      if (orderByString != null)
      {
        String[] array = orderByString.split(",");
        if (array != null)
          orderBy = Arrays.asList(array);
      }      
    }        
  }    
  
  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }
  
  public String getRowTypeLabel()
  {
    CaseEventView caseEventView = 
      (CaseEventView)getExternalContext().getRequestMap().get("row");
    if (caseEventView.getCaseEventId() != null)
    {
      String typeId = caseEventView.getCaseEventTypeId();
      TypeCache typeCache = TypeCache.getInstance();
      try
      {
        Type type = typeCache.getType(typeId);
        if (type != null) return type.getDescription();
      }
      catch (Exception ex)
      {
        warn(ex.getMessage());
      }
    }
    return null;
  }  
  
  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");
  }
  
  @Override
  protected String getRowId(Object row)
  {
    CaseEventView caseEventRow = (CaseEventView)row;
    return caseEventRow.getCaseEventId();
  }

  @Override
  protected String getRowTypeId(Object row)
  {
    CaseEventView caseEventRow = (CaseEventView)row;
    return caseEventRow.getCaseEventTypeId();
  }

  @Override
  protected String getShowPropertiesPropertyName(Object row)
  {
    return SHOW_PROPERTIES_PROPERTY;
  }
}
