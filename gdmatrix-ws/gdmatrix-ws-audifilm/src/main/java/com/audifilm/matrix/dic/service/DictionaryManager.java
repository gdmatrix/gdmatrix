package com.audifilm.matrix.dic.service;

import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import com.audifilm.matrix.dic.service.types.DicTypeInterface;
import com.audifilm.matrix.common.service.PKUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.EnumType;
import org.matrix.dic.EnumTypeFilter;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.dic.Property;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;

import javax.annotation.Resource;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.matrix.security.AccessControl;

import org.matrix.dic.DictionaryConstants;
import org.matrix.util.Entity;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jpa.JPA;
import org.santfeliu.security.UserCache;
import org.santfeliu.ws.WSUtils;
import com.audifilm.matrix.security.service.SecurityManager;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.util.MatrixConfig;

/** 
 *
 * @author comasfc 
 */
@WebService(endpointInterface = "org.matrix.dic.DictionaryManagerPort")
@HandlerChain(file = "handlers.xml")
@JPA
public class DictionaryManager implements DictionaryManagerPort
{

  @Resource
  WebServiceContext wsContext;
  @PersistenceContext(unitName = "AudiDictionary")
  public EntityManager entityManager;

  static final String DIC_ADMIN_ROLE = "DIC_ADMIN";
  static final Logger log = Logger.getLogger(DictionaryManager.class.getName());
  WSEndpoint endpoint;

  SecurityManager securityManager;

  private static FixedProperties _fixedProperties;

  private SecurityManager getSecurityManager()
  {
    if (securityManager==null)
    {
      try
      {
        securityManager = new SecurityManager();
      }
      catch(Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
    return securityManager;
  }


  public boolean removeType(String typeId)
  {
    //throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
    throw new WebServiceException("ACTION_DENIED");
  }

  public Type loadType(String globalTypeId)
  {
    Entity typeEntity = getEndpoint().getEntity(DictionaryConstants.TYPE_TYPE);

    log.log(Level.INFO, "loadType {0}", new Object[]
            {
              globalTypeId
            });
    if (globalTypeId == null)
    {
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
    }
    Type type = null;
    DicTypeInterface obj = null;
    try
    {
      obj = DicTypeAdmin.getTypeInstance(getEndpoint(), globalTypeId);
      if (obj == null)
      {
        return null;
      }

      String typeId = PKUtil.extractFromMatrixPK(typeEntity, globalTypeId);
      type = obj.loadType(this, typeId);

      List<PropertyDefinition> list =
              loadPropertyDefinitionsFromPropertiesFile(typeId);
      if (list!=null && !list.isEmpty())
      {
        type.getPropertyDefinition().addAll(list);
      }
    }
    catch(Exception ex)
    {
      Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
    }
    return type;
  }

  public Type storeType(Type type)
  {
    log.log(Level.INFO, "storeType {0}", new Object[] {
              type
            });
    if (type == null)
    {
      throw new WebServiceException("NOT_SUPPORTED_YET");
    } 
    else
    {
      throw new WebServiceException("NOT_IMPLEMENTED");
    }
    /*
    Type newType = null;
    DicTypeInterface obj = null;
    try
    {
      obj = DicTypeAdmin.getTypeInstanceByType(getEndpoint(), type);
      if (obj != null)
      {
        newType = obj.storeType(this, type);
      }
    }
    catch(Exception ex)
    {
      Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
    }
    return newType; */
  }

  public int countTypes(TypeFilter filter)
  {
    int count = 0;
    try
    {
      List<DicTypeInterface> typesList = DicTypeAdmin.getTypeInstanceByTypeFilter(getEndpoint(), filter);
      for (DicTypeInterface obj : typesList)
      {
        count += obj.countTypes(this, filter);
      }
    }
    catch(Exception ex)
    {
      Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
      //throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
      return 0;
    }
    return count;
  }

  public List<Type> findTypes(TypeFilter filter)
  {


    List<Type> result = new ArrayList<Type>();
    try
    {
      List<DicTypeInterface> typesList = DicTypeAdmin.getTypeInstanceByTypeFilter(getEndpoint(), filter);
      int firstResult = filter.getFirstResult();
      int maxResults = filter.getMaxResults();
      int elementsleftcount = maxResults;
      for (DicTypeInterface dicType : typesList)
      {

        int dicTypeCount = dicType.countTypes(this, filter);
        if (dicTypeCount<=firstResult)
        {
          //NO CARREGO RES
          firstResult-=dicTypeCount;
        }
        else 
        {
          //CARREGO TOT EL QUE PUC FINS MAXRESULTS
          result.addAll(dicType.findTypes(this, filter));

          firstResult=0;
          elementsleftcount = maxResults - result.size();
        }
        filter.setFirstResult(firstResult);
        if (maxResults>0) {
          if (elementsleftcount<=0) break;
          filter.setMaxResults(elementsleftcount);
        }//Ja els tinc tots ja puc marxar
      }
    }
    catch(Exception ex)
    {
      Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
      //throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
    }
    return result;

  }

  public List<Property> initProperties(String typeId, List<Property> property)
  {
    return property;
  }
  
  public List<Property> completeProperties(String typeId, List<Property> property)
  {
    return property;
  }

  public List<String> getTypeActions(String globalTypeId)
  {
    Entity typeEntity = getEndpoint().getEntity(DictionaryConstants.TYPE_TYPE);


    log.log(Level.INFO, "getTypeActions {0}", new Object[] {globalTypeId});
    if (globalTypeId == null)
    {
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
    }
    try
    {
      DicTypeInterface obj = DicTypeAdmin.getTypeInstance(getEndpoint(), globalTypeId);
      if (obj == null)
      {
        return null;
      }

      String typeId = PKUtil.extractFromMatrixPK(typeEntity, globalTypeId);
      return obj.getTypeActions(this, typeId);
    }
    catch(Exception ex)
    {
      Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
    }
  }

  public List<String> listModifiedTypes(String dateTime1, String dateTime2)
  {
    List<String> result = new ArrayList<String>();
    try
    {
      Collection<DicTypeInterface> typesCol = DicTypeAdmin.getAllTypes();
      for (DicTypeInterface obj : typesCol)
      {
        result.addAll(obj.listModifiedTypes(this, dateTime1, dateTime2));
      }
    }
    catch(Exception ex)
    {
      Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");
    }
    return result;
  }

  public WSEndpoint getEndpoint()
  {
    if (endpoint == null)
    {
      String endpointName = WSUtils.getServletAdapter(wsContext).getName();
      endpoint = WSDirectory.getInstance().getEndpoint(endpointName);
    }
    return endpoint;
  }

  public List<String> loadTypeActions(String  module)
  {
    /*
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    User user = UserCache.getUser(credentials);
    if (user == null) return Collections.emptyList();

    List<String> actions = getSecurityManager().findActionsList(module, user.getUserId());
    return actions;
    */


    List<String> actions = new ArrayList<String>();
    actions.add(DictionaryConstants.READ_ACTION);
    actions.add(DictionaryConstants.WRITE_ACTION);
    actions.add(DictionaryConstants.CREATE_ACTION);
    actions.add(DictionaryConstants.DELETE_ACTION);
    return actions;
  }

  public List<AccessControl> loadAccessControlList(String  module)
  {
    /*
     * PER PRIMERA VERSIÓ
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    User user = UserCache.getUser(credentials);
    if (user == null) return Collections.emptyList();

    List<AccessControl> actions = getSecurityManager().findAccessControlList(module, user.getUserId());
     *
     */
    List<AccessControl> actions = Collections.emptyList();

    return actions;
  }

  public boolean isUserAllowed(DicTypeInterface dicType, String action)
  {
    return UserCache.getUser(wsContext).isInRole(DIC_ADMIN_ROLE);
  }

  public FixedProperties getFixedProperties()
  {
    if (_fixedProperties==null)
    {
      String filePropertiesPath = MatrixConfig.getPathProperty(
        getClass().getName() + ".propertyDefinitionsFile");
      _fixedProperties = new FixedProperties(filePropertiesPath);
    }
    return _fixedProperties;
  }

  public List<PropertyDefinition> loadPropertyDefinitionsFromPropertiesFile(String typeId)
  {
    FixedProperties props = getFixedProperties();
    if (props==null) return Collections.emptyList();
    return props.loadPropertyDefinitions(typeId);
  }

  public EnumType loadEnumType(String enumTypeId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public EnumType storeEnumType(EnumType enumType)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean removeEnumType(String enumTypeId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int countEnumTypes(EnumTypeFilter filter)
  {
    return 0;
  }

  public List<EnumType> findEnumTypes(EnumTypeFilter filter)
  {
    return Collections.emptyList();
  }

  public EnumTypeItem loadEnumTypeItem(String enumTypeItemId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public EnumTypeItem storeEnumTypeItem(EnumTypeItem enumTypeItem)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean removeEnumTypeItem(String enumTypeItemId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int countEnumTypeItems(EnumTypeItemFilter filter)
  {
    return 0;
  }

  public List<EnumTypeItem> findEnumTypeItems(EnumTypeItemFilter filter)
  {
    return Collections.emptyList();
  }


}
