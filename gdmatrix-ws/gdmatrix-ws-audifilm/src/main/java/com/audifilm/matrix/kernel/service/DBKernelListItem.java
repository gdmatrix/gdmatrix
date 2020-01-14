package com.audifilm.matrix.kernel.service;

import org.matrix.kernel.KernelListItem;

public class DBKernelListItem extends DBEntityBase
{
  private String listId;
  private String itemId;
  private String auxItemId;
  private String label;
  private String description;
  private String tcqual;
  private int tcvnum;

  public DBKernelListItem()
  {
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

  public void setAuxItemId(String auxItemId)
  {
    this.auxItemId = auxItemId;
  }

  public String getAuxItemId()
  {
    return auxItemId;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getLabel()
  {
    return label;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setTcqual(String tcqual)
  {
    this.tcqual = tcqual;
  }

  public String getTcqual()
  {
    return tcqual;
  }

  public void setTcvnum(int tcvnum)
  {
    this.tcvnum = tcvnum;
  }

  public int getTcvnum()
  {
    return tcvnum;
  }
  
  public void copyFrom(KernelListItem listItem)
  {
    itemId = listItem.getItemId();
    label = listItem.getLabel();
    description = listItem.getDescription();
  }
  
  public void copyTo(KernelListItem listItem)
  {
    listItem.setItemId(itemId);
    listItem.setLabel(label);
    listItem.setDescription(description);
  }
}
