package org.santfeliu.doc.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.Type;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.security.web.ACLPageBean;

/**
 *
 * @author blanquepa
 */
public class DocumentACLBean extends ACLPageBean
{
  private String currentTypeId;

  @Override
  public String show()
  {
    return "document_acl";
  }

  public String searchRole()
  {
    return getControllerBean().searchObject("Role",
      "#{documentACLBean.editingAccessControlItem.accessControl.roleId}");
  }

  public List<SelectItem> getActions()
  {
    if (!isNew())
     return DictionaryConfigBean.getActionSelectItems(
        DictionaryConstants.DOCUMENT_TYPE);
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
    DocumentMainBean documentMainBean = 
      (DocumentMainBean)getBean("documentMainBean");
    return documentMainBean.getDocument().getAccessControl();
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
    DocumentMainBean documentMainBean =
      (DocumentMainBean)getBean("documentMainBean");
    return documentMainBean.getCurrentTypeId();
  }

}
