package org.santfeliu.util.data;

import java.util.List;
import java.util.Map;
import org.santfeliu.util.Table;

/**
 *
 * @author realor
 */
public interface DataProvider
{
  public void init(String reference);
  public Table getData(Map context);
  public List<String> getParameters();
}
