package com.audifilm.matrix.dic.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author realor
 */
public class DBCaseType extends DBGenesysEntity
{

  String typeId;
  String description;

  public DBCaseType()
  {
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId != null ? typeId.trim() : null;
  }

  public String[] getIds()
  {
    return new String[]
            {
              typeId
            };
  }
}
