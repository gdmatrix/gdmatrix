package com.audifilm.matrix.kernel.service;

import java.io.Serializable;

public class DBKernelListItemPK implements Serializable
{
  private String listId;
  private String itemId;

  public DBKernelListItemPK()
  {
  }

  public DBKernelListItemPK(String listId, String itemId)
  {
    this.listId = listId;
    this.itemId = itemId;
  }

  public void setListId(String listId)
  {
    this.listId = listId;
  }

  public String getListId()
  {
    return listId;
  }

  public void setItemId(String itemId)
  {
    this.itemId = itemId;
  }

  public String getItemId()
  {
    return itemId;
  }
  
  public boolean equals(Object o)
  {
    DBKernelListItemPK pk = (DBKernelListItemPK)o;
    return pk.getListId().equals(listId) && pk.getItemId().equals(itemId);
  }
  
  public int hashCode()
  {
    return (listId + KernelManager.PK_SEPARATOR + itemId).hashCode();
  }
}
