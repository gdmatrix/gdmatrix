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
package org.santfeliu.dic;

import org.santfeliu.dic.util.ValidationError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import static org.matrix.dic.PropertyType.BOOLEAN;
import static org.matrix.dic.PropertyType.NUMERIC;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.util.ObjectPropertiesConverter;
import org.santfeliu.dic.util.PropertyTypeChecker;
import org.santfeliu.dic.util.PropertyTypeCheckerFactory;
import org.santfeliu.dic.util.TypeValidator;

/**
 *
 * @author realor
 */
public class Type extends org.matrix.dic.Type
{
  private final TypeCache typeCache;

  protected Type(TypeCache typeCache, org.matrix.dic.Type type)
  {
    this.typeCache = typeCache;
    this.typeId = type.getTypeId();
    this.superTypeId = type.getSuperTypeId();
    this.typePath = type.getTypePath();
    this.description = type.getDescription();
    this.detail = type.getDetail();
    this.creationDateTime = type.getCreationDateTime();
    this.creationUserId = type.getCreationUserId();
    this.changeDateTime = type.getChangeDateTime();
    this.changeUserId = type.getChangeUserId();
    this.instantiable = type.isInstantiable();
    this.restricted = type.isRestricted();
    this.getPropertyDefinition().addAll(type.getPropertyDefinition());
    this.getAccessControl().addAll(type.getAccessControl());
  }

  public TypeCache getTypeCache()
  {
    return typeCache;
  }

  public String getRootTypeId()
  {
    String s = typePath.substring(1);
    int index = s.indexOf(DictionaryConstants.TYPE_PATH_SEPARATOR);
    return s.substring(0, index);
  }

  public Type getRootType()
  {
    return typeCache.getType(getRootTypeId());
  }

  public synchronized Type getSuperType()
  {
    return (getSuperTypeId() == null) ?
      null : typeCache.getType(getSuperTypeId());
  }

  public synchronized List<Type> getSuperTypes()
  {
    ArrayList<Type> path = new ArrayList<>();
    String curTypeId = getSuperTypeId();
    while (curTypeId != null)
    {
      Type type = typeCache.getType(curTypeId);
      if (type == null) curTypeId = null;
      else
      {
        path.add(type);
        curTypeId = type.superTypeId;
      }
    }
    Collections.reverse(path);
    return path;
  }

  public synchronized List<Type> getDerivedTypes()
  {
    return getDerivedTypes(false);
  }

  public synchronized List<Type> getDerivedTypes(boolean recursive)
  {
    List<String> derivedTypeIds = typeCache.getDerivedTypeIds(typeId);

    ArrayList<Type> derivedTypes = new ArrayList<Type>();
    Iterator<String> iter = derivedTypeIds.iterator();
    while (iter.hasNext())
    {
      String derivedTypeId = iter.next();
      Type derivedType = typeCache.getType(derivedTypeId);
      if (derivedType != null)
      {
        derivedTypes.add(derivedType);
        if (recursive)
        {
          derivedTypes.addAll(derivedType.getDerivedTypes(true));
        }
      }
    }
    return derivedTypes;
  }

  public synchronized List<String> getDerivedTypeIds()
  {
    return typeCache.getDerivedTypeIds(typeId);
  }

  public synchronized int getDerivedTypeCount()
  {
    return getDerivedTypeIds().size();
  }

  public boolean isRootType()
  {
    return superTypeId == null;
  }

  public synchronized boolean isLeaf()
  {
    return getDerivedTypeCount() == 0;
  }

  @Override
  public String getTypePath()
  {
    // Recalc typePath from parent typePath, because TypeCache do not warrants
    // that all typePaths are updated when a dictionary branch is moved.

    if (superTypeId != null)
    {
      Type superType = typeCache.getType(superTypeId);
      typePath = superType.getTypePath() + typeId +
        DictionaryConstants.TYPE_PATH_SEPARATOR;
    }
    else
    {
      typePath = DictionaryConstants.TYPE_PATH_SEPARATOR +
        typeId + DictionaryConstants.TYPE_PATH_SEPARATOR;
    }
    return typePath;
  }

  public List<String> getTypePathList()
  {
    String path = getTypePath();
    int separatorLength = DictionaryConstants.TYPE_PATH_SEPARATOR.length();
    path = path.substring(separatorLength, path.length() - separatorLength);
    String[] typeIds = path.split(DictionaryConstants.TYPE_PATH_SEPARATOR);
    ArrayList<String> typeIdList = new ArrayList();
    for (String curTypeId : typeIds)
    {
      typeIdList.add(curTypeId);
    }
    return typeIdList;
  }

  public boolean isDerivedFrom(String typeId)
  {
    return getTypePath().contains(DictionaryConstants.TYPE_PATH_SEPARATOR +
      typeId + DictionaryConstants.TYPE_PATH_SEPARATOR);
  }

  public synchronized List<String> getActions()
  {
    return typeCache.getActions(getRootTypeId());
  }

  public synchronized String formatTypePath(boolean includeTypeId,
    boolean includeDescription, boolean includeSelfType)
  {
    return formatTypePath(
      includeTypeId, includeDescription, includeSelfType, null);
  }

  public synchronized String formatTypePath(boolean includeTypeId,
    boolean includeDescription, boolean includeSelfType, String rootTypeId)
  {
    boolean isRootTypeDescendant = (rootTypeId == null);
    StringBuilder buffer = new StringBuilder();
    List<Type> path = getSuperTypes();
    path.add(this);
    String separator = "";
    for (Type typeInPath : path)
    {
      if (isRootTypeDescendant)
      {
        boolean isSelfType = typeInPath.equals(path.get(0));
        if (!isSelfType || isSelfType && includeSelfType)
        {
          buffer.append(separator);
          separator = " / ";
          if (includeTypeId)
          {
            buffer.append(typeInPath.getTypeId());
          }
          if (includeDescription)
          {
            if (includeTypeId) buffer.append("-");
            buffer.append(typeInPath.getDescription().trim());
          }
        }
      }
      if (!isRootTypeDescendant)
        isRootTypeDescendant = typeInPath.getTypeId().equals(rootTypeId);
    }
    return buffer.toString();
  }

  public boolean canPerformAction(String action, Set<String> roles)
  {
    return DictionaryUtils.canPerformAction(action, roles, getAccessControl());
  }

  public Set<String> getPropertyNames()
  {
    HashSet<String> names = new HashSet<>();
    for (PropertyDefinition pd : getPropertyDefinition())
    {
      names.add(pd.getName());
    }
    return names;
  }

  public List<PropertyDefinition> getInheritPropertyDefinition()
  {
    return getPropertyDefinition(true, true, true);
  }

  public List<PropertyDefinition> getPropertyDefinition(
    boolean includeRootType, boolean includeSelfType, boolean includeHidden)
  {
    List<PropertyDefinition> pdList = new ArrayList<>();
    HashMap<String, PropertyDefinition> propertyMap = new HashMap<>();
    Set<String> rootTypePropertyNames = null;

    List<Type> types = this.getSuperTypes();
    if (includeSelfType) types.add(this);
    if (!includeRootType)
    {
      Type rootType = types.get(0);
      rootTypePropertyNames = rootType.getPropertyNames();
    }

    int first = includeRootType ? 0 : 1;
    for (int i = first; i < types.size(); i++)
    {
      Type type = types.get(i);
      for (PropertyDefinition pd : type.getPropertyDefinition())
      {
        String propertyName = pd.getName();
        if (includeHidden || !pd.isHidden())
        {
          if (includeRootType || !rootTypePropertyNames.contains(propertyName))
          {
            PropertyDefinition superPd = propertyMap.put(propertyName, pd);
            if (superPd != null)
            {
              // override, replace pd
              int index = pdList.indexOf(superPd);
              pdList.set(index, pd);
            }
            else
            {
              pdList.add(pd);
            }
          }
        }
        else if (!includeHidden && pd.isHidden() && propertyMap.containsKey(pd.getName()))
        {
          //override, remove pd from list
          pdList.remove(propertyMap.get(pd.getName()));
        }
      }
    }
    // order of properties in preserved
    return pdList;
  }

  public PropertyDefinition getPropertyDefinition(String name)
  {
    PropertyDefinition result = null;
    if (name != null)
    {
      for (PropertyDefinition pd : this.propertyDefinition)
      {
        if (name.equals(pd.getName()))
        {
          result = pd;
          break;
        }
      }
    }
    return result != null ? result :
      (isRootType() ? null : getSuperType().getPropertyDefinition(name));
  }

  public Object getPropertyValue(Property property, boolean multiValued)
  {
    List<String> stringList = property.getValue();
    PropertyDefinition pd = getPropertyDefinition(property.getName());
    if (pd != null)
    {
      PropertyType propType = pd.getType();
      switch (propType)
      {
        case NUMERIC:
          if (multiValued)
          {
            ArrayList<Double> numberList = new ArrayList<>();
            for (String value : stringList)
            {
              numberList.add(Double.valueOf(value));
            }
            return numberList;
          }
          else
          {
            return Double.valueOf(stringList.get(0));
          }

        case BOOLEAN:
          if (multiValued)
          {
            ArrayList<Boolean> booleanList = new ArrayList<>();
            for (String value : stringList)
            {
              booleanList.add(Boolean.valueOf(value));
            }
            return booleanList;
          }
          else
          {
            return Boolean.valueOf(stringList.get(0));
          }
      }
    }
    return multiValued ? stringList : stringList.get(0);
  }

  public List<ValidationError> validateProperties(Map<String, Object> properties,
    Set<String> unvalidablePropertyNames)
  {
    List<ValidationError> errors = new ArrayList();
    List<PropertyDefinition> definitions = new ArrayList();
    definitions.addAll(getPropertyDefinition());

    //Add super type definitions
    Type superType = getSuperType();
    while (superType != null)
    {
      definitions.addAll(superType.getPropertyDefinition());
      superType = superType.getSuperType();
    }

    if (!definitions.isEmpty())
    {
      for (PropertyDefinition pd : definitions)
      {
        if (!pd.isHidden() && !isMetaProperty(pd.getName()))
        {
          String propName = pd.getName();
          if (unvalidablePropertyNames == null ||
              Collections.EMPTY_SET.equals(unvalidablePropertyNames) ||
              !unvalidablePropertyNames.contains(propName))
          {
            Object propValue = properties.get(propName);

            if (propValue == null) //property or value not found
            {
              if (pd.getValue() != null && pd.getValue().size() > 0
                && pd.getMinOccurs() > 0)
              {
                propValue = pd.getValue();
                properties.put(propName, propValue);
              }
              else if (pd.getMinOccurs() > 0)
                errors.add(new ValidationError(
                  pd.getDescription(), propValue, "MANDATORY_PROPERTY_NOT_FOUND"));
            }
            else
            {
              if (propValue instanceof List)
              {
                List valueList = (List)propValue;
                if (pd.getMaxOccurs() > 0 && valueList.size() > pd.getMaxOccurs())
                  errors.add(new ValidationError(pd.getDescription(), valueList,
                    "MAX_OCCURS_EXCEEDED", String.valueOf(pd.getMaxOccurs())));
                else if (valueList.size() < pd.getMinOccurs())
                  errors.add(new ValidationError(pd.getDescription(), valueList,
                    "LESS_THAN_MIN_OCCURS", String.valueOf(pd.getMinOccurs())));

                for (Object value : valueList)
                {
                  String sValue = String.valueOf(value);
                  errors.addAll(checkValue(sValue, pd));
                }
              }
              else
              {
                String sValue = String.valueOf(propValue);

                if (pd.getMinOccurs() > 1)
                  errors.add(new ValidationError(pd.getDescription(), sValue,
                    "LESS_THAN_MIN_OCCURS", String.valueOf(pd.getMinOccurs())));

                errors.addAll(checkValue(sValue, pd));
              }
            }
          }
        }
      }

      if (isRestricted())
      {
        Set<String> propNames = properties.keySet();
        for (String propName : propNames)
        {
          if (!contains(definitions, propName))
            errors.add(new ValidationError(propName, null,
              "INVALID_PROPERTY", null));
        }
      }
    }
    return errors;
  }

  public List<ValidationError> validateObject(Object object)
    throws Exception
  {
    return validateObject(object, Collections.EMPTY_SET);
  }

  public List<ValidationError> validateObject(Object object,
    String unvalidableId) throws Exception
  {
    List<ValidationError> errors = null;

    Set<String> unvalidable = new HashSet();
    unvalidable.add(unvalidableId);
    errors = validateObject(object, unvalidable);

    return errors;
  }

  public List<ValidationError> validateObject(Object object,
    Set<String> unvalidable) throws Exception
  {
    List<ValidationError> errors = null;
    TypeValidator validator = new TypeValidator(this);

    ObjectPropertiesConverter converter = new ObjectPropertiesConverter(object);
    errors = validator.validate(converter, unvalidable);

    return errors;
  }

  public void loadDefaultValues(Map map)
  {
    if (map == null) map = new HashMap();

    List<PropertyDefinition> pdList = getInheritPropertyDefinition();
    for (PropertyDefinition pd : pdList)
    {
      if (pd.getValue() != null && pd.getValue().size() > 0 && !pd.isHidden())
      {
        if (map.get(pd.getName()) == null)
          map.put(pd.getName(), pd.getValue());
      }
    }
  }

  private boolean contains(List<PropertyDefinition> definitions, String propName)
  {
    for (PropertyDefinition pd : definitions)
    {
      if (pd.getName().equals(propName))
        return true;
    }
    return false;
  }

  private List<ValidationError> checkValue(String value,
    PropertyDefinition propDef)
  {
    List<ValidationError> messages = new ArrayList();

    //Type Checking
    PropertyType type = propDef.getType();
    PropertyTypeChecker typeChecker =
      PropertyTypeCheckerFactory.getPropertyTypeChecker(type);
    if (!typeChecker.checkValue(value))
      messages.add(new ValidationError(propDef.getDescription(), value,
        "INVALID_PROPERTY_TYPE", type.value()));

    //Size Checking
    int maxSize = propDef.getSize();
    if (maxSize > 0 && value.length() > maxSize)
      messages.add(new ValidationError(propDef.getDescription(), value,
        "PROPERTY_TOO_LONG", String.valueOf(maxSize)));

    return messages;
  }

  private boolean isMetaProperty(String propName)
  {
    return propName != null && propName.startsWith("_");
  }

  @Override
  public String toString()
  {
    return getTypeId() + ": " + getDescription();
  }
}
