package org.santfeliu.util.script.function;

import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.security.AccessControl;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.web.UserSessionBean;

/**
 * Usage: cloneDocument(String docId, String title, String docTypeId, 
 *  [String roleId])
 * 
 * @author blanquepa
 */
public class CloneDocumentFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args.length >= 3 && args.length <= 4)
    {
      String docId = String.valueOf(args[0]);
      String title = (String) args[1];
      String docTypeId = (String) args[2];
      
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      DocumentManagerClient client = 
        new DocumentManagerClient(userSessionBean.getUserId(), userSessionBean.getPassword());
      Document document = 
        client.loadDocument(docId, 0, org.matrix.doc.ContentInfo.ALL);
      Content content = document.getContent();
      content.setContentId(null);
      Document newDoc = new org.matrix.doc.Document();
      
      newDoc.setTitle(title);
      newDoc.setDocTypeId(docTypeId);
      newDoc.setContent(content);      
      
      if (args.length == 4)
      {  
        String role = (String) args[3];
        AccessControl acl1 = new org.matrix.security.AccessControl();
        acl1.setAction("Read");
        acl1.setRoleId(role);
        newDoc.getAccessControl().add(acl1);
        AccessControl acl2 = new org.matrix.security.AccessControl();
        acl2.setAction("Write");
        acl2.setRoleId(role);
        newDoc.getAccessControl().add(acl2);
        AccessControl acl3 = new org.matrix.security.AccessControl();
        acl3.setAction("Delete");
        acl3.setRoleId(role);
        newDoc.getAccessControl().add(acl3);
      }
      
      return client.storeDocument(newDoc);            
    }
    
    return null;
  }  
}
