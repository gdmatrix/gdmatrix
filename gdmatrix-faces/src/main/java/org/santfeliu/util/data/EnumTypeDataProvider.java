package org.santfeliu.util.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.Table;

/**
 *
 * @author realor
 */
public class EnumTypeDataProvider implements DataProvider
{
  private String enumTypeId;

  public void init(String reference)
  {
    enumTypeId = reference;
  }

  public String getEnumTypeId()
  {
    return enumTypeId;
  }

  public void setEnumTypeId(String enumTypeId)
  {
    this.enumTypeId = enumTypeId;
  }

  public Table getData(Map context)
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(DictionaryManagerService.class);
    DictionaryManagerPort port = endpoint.getPort(DictionaryManagerPort.class);
    EnumTypeItemFilter filter = new EnumTypeItemFilter();
    filter.setEnumTypeId(enumTypeId);
    List<EnumTypeItem> items = port.findEnumTypeItems(filter);
    Table table = new Table("value", "label", "title");
    for (EnumTypeItem item : items)
    {
      table.addRow(item.getValue(), item.getLabel(), item.getDescription());
    }
    return table;
  }

  public List<String> getParameters()
  {
    return Collections.EMPTY_LIST;
  }
}
