package com.audifilm.matrix.common.service;

/**
 *
 * @author comasfc
 */
public enum VersionType
{

  FOTO_PERSON_CASE_PERSON("FPXP"),
  FOTO_PERSON_CASE_REPRESENTANT("FPXR"),
  FOTO_ADDRESS_CASE_PERSONDOM("FAXP"),
  FOTO_ADDRESS_CASE_REPRESENTANTDOM("FAXR"),

  CASE_PERSON_CASE("CPX"),
  CASE_PERSON_CASEINTERESSAT("CPXI");

  private String versionTypeId;

  private VersionType(String versionTypeId)
  {
    this.versionTypeId = versionTypeId;
  }

  public String getVersionTypeId()
  {
    return versionTypeId;
  }

  @Override
  public String toString()
  {
    return versionTypeId;
  }

  public boolean equals(String id)
  {
    if (id == null)
    {
      return false;
    }
    return versionTypeId.equals(id);
  }

  public boolean equals(VersionType other)
  {
    if (other == null)
    {
      return false;
    }
    return versionTypeId.equals(other.versionTypeId);
  }

  public static VersionType getVersionType(String versionTypeId)
  {
    if (versionTypeId == null)
    {
      return null;
    }
    for (VersionType element : VersionType.values())
    {
      if (element.equals(versionTypeId))
      {
        return element;
      }
    }
    return null;
  }
}
