package org.santfeliu.classif.web;

import java.util.Date;
import org.matrix.classif.Class;
import org.santfeliu.cases.web.CaseSearchBean;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.doc.web.DocumentSearchBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author realor
 */
public class ClassBean extends ObjectBean
{
  @Override
  public String getObjectTypeId()
  {
    return "Class";
  }

  @Override
  public String getDescription(String objectId)
  {
    String description;
    String classId = getClassId(objectId);
    String startDateTime = getStartDateTime(objectId);

    ClassCache classCache = ClassCache.getInstance(startDateTime);
    Class classObject = classCache.getClass(classId);
    if (classObject == null)
    {
      description = classId;
    }
    else if (startDateTime == null)
    {
      description = classId + ": " + classObject.getTitle();
    }
    else
    {
      Date date = TextUtils.parseInternalDate(startDateTime);
      description = classId + 
        " (" + TextUtils.formatDate(date, "dd/MM/yyyy HH:mm:ss") + "): " +
        classObject.getTitle();
    }
    return description;
  }

  public String getClassId()
  {
    String classId = null;
    if (!isNew())
    {
      classId = getClassId(objectId);
    }
    return classId;
  }

  public String getStartDateTime()
  {
    String startDateTime = null;
    if (!isNew())
    {
      startDateTime = getStartDateTime(objectId);
    }
    return startDateTime;
  }

  public String getClassId(String objectId)
  {
    if (objectId == null) return ControllerBean.NEW_OBJECT_ID;
    String classId;
    int index = objectId.indexOf(":");
    if (index != -1)
    {
      classId = objectId.substring(0, index);
    }
    else classId = objectId;
    return classId;
  }

  public String getStartDateTime(String objectId)
  {
    String startDateTime;
    int index = objectId.indexOf(":");
    if (index != -1)
    {
      startDateTime = objectId.substring(index + 1);
    }
    else startDateTime = null;
    return startDateTime;
  }

  public String getObjectId(Class classObject)
  {
    if (classObject.getStartDateTime() == null) return classObject.getClassId();
    return classObject.getClassId() + ":" + classObject.getStartDateTime();
  }

  public String searchCases()
  {
    CaseSearchBean caseSearchBean = (CaseSearchBean)getBean("caseSearchBean");
    caseSearchBean.reset();
    caseSearchBean.setClassId(getClassId());
    return ControllerBean.getCurrentInstance().searchObject("Case");
  }

  public String searchDocuments()
  {
    DocumentSearchBean documentSearchBean =
      (DocumentSearchBean)getBean("documentSearchBean");
    documentSearchBean.reset();
    documentSearchBean.setClassId(getClassId());
    return ControllerBean.getCurrentInstance().searchObject("Document");
  }

  @Override
  public void postStore()
  {
    // sync classes
    ClassCache.syncInstances();
  }
  
  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        ClassificationConfigBean.getPort().removeClass(getClassId());
        removed();
        // sync classes
        ClassCache.syncInstances();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }
  
  @Override
  public boolean isEditable()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole("CLASSIF_ADMIN");
  }
}
