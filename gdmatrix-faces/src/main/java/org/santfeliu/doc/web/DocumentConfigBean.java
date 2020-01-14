package org.santfeliu.doc.web;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.web.UserSessionBean;


public class DocumentConfigBean implements Serializable
{
  public DocumentConfigBean()
  {
  }
  
  public static CachedDocumentManagerClient getClient() throws Exception
  {
    CachedDocumentManagerClient client = new CachedDocumentManagerClient(
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());
    return client;
  }
  
  public static DocumentManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(DocumentManagerService.class);
    return endpoint.getPort(DocumentManagerPort.class,
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static String toObjectId(String docId, int version)
  {
    return docId + "-" + String.valueOf(version);
  }

  public static String[] fromObjectId(String objectId) throws Exception
  {
    String[] split = objectId.split("-");
    if (split.length == 2)
      return split;
    else if (split.length == 1)
      return new String[]{objectId, "0"};
    else throw new Exception("INVALID_OBJECTID");
  }

  public Map getDocTypes()
  {
    Map typesMap = new HashMap();
    try
    {
      for (Type userDocType : getUserDocTypes(DictionaryConstants.CREATE_ACTION))
      {
        String typeId = userDocType.getTypeId();
        org.santfeliu.dic.Type type =
          TypeCache.getInstance().getType(typeId);

        if (type.isInstantiable())
        {
          String typePath = null;
          if (DictionaryConstants.DOCUMENT_TYPE.equals(typeId))
            typePath = type.formatTypePath(false, true, true);
          else
            typePath = type.formatTypePath(false, true, false);

          typesMap.put(typeId, typePath);
        }
      }
    }
    catch(Exception ex)
    {
      //return empty map
    }    
    return typesMap;
  }

  private List<Type> getUserDocTypes(String action)
    throws Exception
  {
    TypeFilter filter = new TypeFilter();
    filter.setAction(action);
    filter.setTypePath("/" + DictionaryConstants.DOCUMENT_TYPE + "/%");
    return DictionaryConfigBean.getPort().findTypes(filter);
  }
}
