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
      String title = String.valueOf(args[1]);
      String docTypeId = String.valueOf(args[2]);
      
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
        String role = String.valueOf(args[3]);
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
