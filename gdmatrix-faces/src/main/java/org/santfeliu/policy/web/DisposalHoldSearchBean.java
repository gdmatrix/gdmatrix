package org.santfeliu.policy.web;

import java.util.List;
import org.matrix.policy.DisposalHoldFilter;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author realor
 */
public class DisposalHoldSearchBean extends BasicSearchBean
{
  private DisposalHoldFilter filter = new DisposalHoldFilter();

  public DisposalHoldFilter getFilter()
  {
    return filter;
  }

  public void setFilter(DisposalHoldFilter filter)
  {
    this.filter = filter;
  }

  @Override
  public int countResults()
  {
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    return null;
  }

  @Override
  public String show()
  {
    return "disposal_hold_main";
  }
}
