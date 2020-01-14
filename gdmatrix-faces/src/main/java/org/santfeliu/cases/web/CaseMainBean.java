package org.santfeliu.cases.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;

import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;

import org.matrix.security.AccessControl;
import org.matrix.dic.Type;
import org.matrix.classif.Class;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.cases.CaseCaseCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;
import org.santfeliu.classif.web.ClassBean;
import org.santfeliu.classif.web.ClassificationConfigBean;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.web.obj.util.FillObjectParametersProcessor;
import org.santfeliu.web.obj.util.ParametersManager;
import org.santfeliu.ws.WSExceptionFactory;

public class CaseMainBean extends DynamicTypifiedPageBean
{
  private static final String CLASSID_VALUES_SEPARATOR = ";";

  private static final String CASE_TREE_MID_PROPERTY = "_caseTreeMid";
  
  public static final String SHOW_AUDIT_PROPERTIES = "_showAuditProperties";

  private Case cas;
  private boolean modified;
  private String startDateTime;
  private String endDateTime;
  private String classIdString; // ex: T023;T021;G004
  private String classTitle; // title of first classId in classIdString

  public CaseMainBean()
  {
    super(DictionaryConstants.CASE_TYPE, "CASE_ADMIN");       
    String caseTypeId = getProperty(CaseSearchBean.SEARCH_CASE_TYPE_PROPERTY);
    if (caseTypeId != null)
      setRootTypeId(caseTypeId);
  }

  //Accessors
  public void setCase(Case cas)
  {
    this.cas = cas;
  }

  public Case getCase()
  {
    return cas;
  }

  public void setStartDateTime(String startDateTime)
  {
    this.startDateTime = startDateTime;
  }

  public String getStartDateTime()
  {
    return startDateTime;
  }

  public void setEndDateTime(String endDateTime)
  {
    this.endDateTime = endDateTime;
  }

  public String getEndDateTime()
  {
    return endDateTime;
  }
    
  @Override
  public boolean isModified()
  {
    return true;
  }
  
  public void setModified(boolean modified)
  {
    this.modified = modified;
  }

  public void setClassId(String classId) // call from search & select
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    if (classIdString == null || classIdString.trim().length() == 0)
    {
      setClassIdString(classBean.getClassId(classId));
    }
    else
    {
      setClassIdString(classIdString +
        CLASSID_VALUES_SEPARATOR + classBean.getClassId(classId));
    }
  }

  public String getClassIdString()
  {
    return classIdString;
  }

  public void setClassIdString(String classIdString)
  {
    this.classIdString = classIdString;
    // get class title
    if (classIdString != null && classIdString.trim().length() > 0)
    {
      int index = classIdString.indexOf(CLASSID_VALUES_SEPARATOR);
      String classId = (index != -1) ?
        classIdString.substring(0, index) : classIdString;
      try
      {
        // get current title
        ClassificationManagerPort port = ClassificationConfigBean.getPort();
        Class clazz = port.loadClass(classId, null);
        classTitle = clazz.getTitle();
      }
      catch (Exception ex)
      {
        this.classTitle = null;
      }
    }
    else
    {
      this.classTitle = null;
    }
  }

  public boolean isValidClassId()
  {
    return classTitle != null ||
     (classIdString == null || classIdString.trim().length() == 0);
  }

  public String getClassTitle()
  {
    return classTitle;
  }

  public boolean isEditable() throws Exception
  {
    if (cas == null || cas.getCaseId() == null)
      return true;

    if (UserSessionBean.getCurrentInstance().isUserInRole(
      CaseConstants.CASE_ADMIN_ROLE))
      return true;

    Type currentType = getCurrentType();
    if (currentType == null)
      return true;

    Set<AccessControl> acls = new HashSet();
    acls.addAll(currentType.getAccessControl());
    acls.addAll(cas.getAccessControl());

    if (acls != null)
    {
      for (AccessControl acl : acls)
      {
        String action = acl.getAction();
        if (DictionaryConstants.WRITE_ACTION.equals(action))
        {
          String roleId = acl.getRoleId();
          if (UserSessionBean.getCurrentInstance().isUserInRole(roleId))
            return true;
        }
      }
    }

    return false;
  }

  public Date getCreationDateTime()
  {
    if (cas != null && cas.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(cas.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (cas != null && cas.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(cas.getChangeDateTime());
    else
      return null;
  }

  public String getTypeCaseId()
  {
    String subCaseId = null;

    if (cas != null && cas.getProperty() != null)
    {
      try
      {
        List<String> value = (List<String>) PojoUtils.getDynamicProperty(
          cas.getProperty(), CaseConstants.TYPECASEID);
        subCaseId = value.get(0);
      }
      catch (Exception ex)
      {
        //return null
      }
    }

    return subCaseId;
  }

  //Actions
  public String show()
  {
    return "case_main";
  }

  public String showClass()
  {
    return getControllerBean().showObject("Class", cas.getClassId().get(0));
  }

  public String showType()
  {
    return getControllerBean().showObject("Type", getCurrentTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getCurrentTypeId() != null && getCurrentTypeId().trim().length() > 0;
  }

  public String searchClass()
  {
    return getControllerBean().searchObject("Class",
      "#{caseMainBean.classId}");
  }

  public String searchType()
  {
    return searchType("#{caseMainBean.currentTypeId}");
  }

  public boolean isShowInTreeEnabled()
  {
    return getCaseTreeMid() != null;
  }
  
  public String getCaseTreeMid()
  {
    String caseTreeMid = null;
    if (cas != null && cas.getProperty() != null)
    {
      try
      {
        org.santfeliu.dic.Type type =
          TypeCache.getInstance().getType(cas.getCaseTypeId());
        if (type != null)
        {
          PropertyDefinition pd =
            type.getPropertyDefinition(CASE_TREE_MID_PROPERTY);
          if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
          {
            caseTreeMid = pd.getValue().get(0);
          }
        }
      }
      catch (Exception ex)
      {
        //return null
      }
    }
    return caseTreeMid;
  }
  
  public boolean isShowAuditProperties()
  {
    try
    {
      if (UserSessionBean.getCurrentInstance().isUserInRole(
        CaseConstants.CASE_ADMIN_ROLE))
        return true;

      if (cas != null && cas.getProperty() != null)
      {
        org.santfeliu.dic.Type type =
          TypeCache.getInstance().getType(cas.getCaseTypeId());
        if (type != null)
        {
          PropertyDefinition pd =
            type.getPropertyDefinition(SHOW_AUDIT_PROPERTIES);
          if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
          {
            String showAuditProperty = pd.getValue().get(0);
            return (!"false".equalsIgnoreCase(showAuditProperty));
          }
          else
            return true;
        }
      }
      return true;
    }
    catch (Exception ex)
    {
      return true;
    }
  }

  @Override
  public String store()
  {
    try
    {
      cas.setStartDate(getStartDate(startDateTime));
      cas.setStartTime(getStartTime(startDateTime));
      cas.setEndDate(getEndDate(endDateTime));
      cas.setEndTime(getEndTime(endDateTime));
      cas.setCaseTypeId(getCurrentTypeId());
      cas.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        cas.getProperty().addAll(properties);
      
      //ClassId
      cas.getClassId().clear();
      List<String> classIdList =
        TextUtils.stringToList(this.classIdString, CLASSID_VALUES_SEPARATOR);
      if (classIdList != null)
        cas.getClassId().addAll(classIdList);

      cas = CaseConfigBean.getPort().storeCase(cas);

      startDateTime = concatDateTime(cas.getStartDate(), cas.getStartTime());
      endDateTime = concatDateTime(cas.getEndDate(), cas.getEndTime());

      setFormDataFromProperties(cas.getProperty());
      setClassIdString(TextUtils.collectionToString(
        cas.getClassId(), CLASSID_VALUES_SEPARATOR));
            

      setObjectId(cas.getCaseId());
      CaseCaseCache.getInstance().clear(cas.getCaseId());
      CaseTreeBean.getCasePropertiesMap().remove(cas.getCaseId());
    }
    catch (Exception ex)
    {
      error(ex);
      List<String> details = WSExceptionFactory.getDetails(ex);
      if (details.size() > 0) error(details);
      ex.printStackTrace();
    }
    return show();
  }

  //Events
  public void valueChanged(ValueChangeEvent event)
  {
    modified = true;
  }

  protected void load()
  {
    if (isNew())
    {
      cas = new Case();
      parametersManager = new ParametersManager();     
      parametersManager.addProcessor(new FillObjectParametersProcessor(cas));
      parametersManager.processParameters();
      setCurrentTypeId(cas.getCaseTypeId());
      setFormDataFromProperties(cas.getProperty());
    }
    else
    {
      try
      {
        cas = CaseConfigBean.getPort().loadCase(getObjectId());
        setCurrentTypeId(cas.getCaseTypeId());
        setClassIdString(TextUtils.collectionToString(cas.getClassId(), CLASSID_VALUES_SEPARATOR));
        startDateTime = concatDateTime(cas.getStartDate(), cas.getStartTime());
        endDateTime = concatDateTime(cas.getEndDate(), cas.getEndTime());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        cas = new Case();
        setCurrentTypeId(null);
        setClassIdString(null);
        startDateTime = null;
        endDateTime = null;
        error(ex);
      }
      setFormDataFromProperties(cas.getProperty());
    }
  }

  private String getStartDate(String dateTime)
  {
    String date = null;
    if (dateTime == null)
      date = new SimpleDateFormat("yyyyMMdd").format(new Date());
    else
      date = getDate(dateTime);

    return date;
  }

  private String getEndDate(String dateTime)
  {
    return getDate(dateTime);
  }

  private String getStartTime(String dateTime)
  {
    String time = null;
    if (dateTime == null)
      time = new SimpleDateFormat("HHmmss").format(new Date());
    else
      time = getTime(dateTime);

    return time;
  }

  private String getEndTime(String dateTime)
  {
    return getTime(dateTime);
  }

  private String getDate(String dateTime)
  {
    String date = null;

    if (dateTime != null && dateTime.length() == 14)
      date = dateTime.substring(0, 8);

    return date;
  }

  private String getTime(String dateTime)
  {
    String time = null;

    if (dateTime != null && dateTime.length() == 14)
      time = dateTime.substring(8);

    return time;
  }

  private String concatDateTime(String date, String time)
  {
    String dateTime = null;
    
    if (date != null && time != null)
      dateTime = date + time;
    else if (date != null && time == null)
      dateTime = date + "000000";

    return dateTime;
  }

  private boolean isKeywordsDefined()
  {
    Type type = getCurrentType();
    if (type != null)
    {
      for (PropertyDefinition pd : type.getPropertyDefinition())
      {
        if (pd.getName().equals("keywords"))
          return true;
      }
    }
    
    return false;
  }

  @Override
  protected List<Property> getFormDataAsProperties()
  {
    List<Property> result = new ArrayList<Property>();
    List<Property> properties = super.getFormDataAsProperties();
    if (properties != null)
    {
      for (Property property : properties)
      {
        if (!"classId".equals(property.getName())
            && !CaseConstants.TYPECASEID.equals(property.getName()))
        {
          result.add(property);
        }
      }

      return result;
    }
    else
      return null;
  }
}


