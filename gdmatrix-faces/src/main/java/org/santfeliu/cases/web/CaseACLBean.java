package org.santfeliu.cases.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.security.AccessControl;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.Type;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.security.web.ACLPageBean;

/**
 *
 * @author blanquepa
 */
public class CaseACLBean extends ACLPageBean
{
  private String currentTypeId;

  @Override
  public String show()
  {
    return "case_acl";
  }

  public String searchRole()
  {
    return getControllerBean().searchObject("Role",
      "#{caseACLBean.editingAccessControlItem.accessControl.roleId}");
  }

  public List<SelectItem> getActions()
  {
    if (!isNew())
     return DictionaryConfigBean.getActionSelectItems(
        DictionaryConstants.CASE_TYPE);
    else return Collections.EMPTY_LIST;
  }

  public List<AccessControl> getTypeRows()
  {
    if (getMainTypeId() == null)
    {
      typeRows = null;
    }
    else
    {
      if (typeRows == null || checkTypeChange())
      {
        typeRows = new ArrayList<AccessControl>();
        try
        {
          DictionaryManagerPort port = DictionaryConfigBean.getPort();
          Type type = port.loadType(currentTypeId);
          typeRows = type.getAccessControl();
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
    }
    return typeRows;
  }

  protected List<AccessControl> getMainAccessControlList()
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    return caseMainBean.getCase().getAccessControl();
  }

  private boolean checkTypeChange()
  {
    boolean result = !getMainTypeId().equals(currentTypeId);
    if (result)
    {
      currentTypeId = getMainTypeId();
      typeRows = null;
    }
    return result;
  }

  private String getMainTypeId()
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    return caseMainBean.getCurrentTypeId();
  }

}