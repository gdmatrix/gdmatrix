package org.santfeliu.cms;

import org.apache.commons.collections.LRUMap;

/**
 *
 * @author lopezrj
 */
public class CNodeMap extends LRUMap
{

  public CNodeMap(int size)
  {
    super(size);
  }
  
  protected void processRemovedLRU(Object key, Object value)
  {
    /*
    CNode cNode = (CNode)value;
    CNode parentCNode = cNode.getParent();
    if (parentCNode != null)
    {
      parentCNode.clearChildrenList();
    }
    */
  }

}
