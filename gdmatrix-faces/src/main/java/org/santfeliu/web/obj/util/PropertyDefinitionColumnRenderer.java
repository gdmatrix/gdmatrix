package org.santfeliu.web.obj.util;

import java.io.Serializable;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author blanquepa
 */
public class PropertyDefinitionColumnRenderer extends ColumnRenderer implements Serializable
{
  private String typeIdPropName;
  
  public PropertyDefinitionColumnRenderer(String typeIdPropName)
  {
    this.typeIdPropName = typeIdPropName;
  }

  public Object getValue(String columnName, Object row)
  {
    Object result = null;
    String typeId = (String)PojoUtils.getDeepStaticProperty(row, this.typeIdPropName);
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      PropertyDefinition pd = type.getPropertyDefinition(columnName);
      if (pd != null)
        result = pd.getValue().get(0);
    }
    return result;
  }

  @Override
  public boolean isValueEscaped()
  {
    return false;
  }

}
