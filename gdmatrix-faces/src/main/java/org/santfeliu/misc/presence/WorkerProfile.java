package org.santfeliu.misc.presence;

import java.io.Serializable;

/**
 *
 * @author realor
 */
public class WorkerProfile implements Serializable
{
  private String userId;
  private String personId;
  private String displayName;
  private String caseId;
  private int workingTime; // in seconds for week
  private int bonusTime; // in seconds
  private String bonusStartDate; // apply bonus only after this date

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public int getWorkingTime()
  {
    return workingTime;
  }

  public void setWorkingTime(int workingTime)
  {
    this.workingTime = workingTime;
  }

  public int getBonusTime()
  {
    return bonusTime;
  }

  public void setBonusTime(int bonusTime)
  {
    this.bonusTime = bonusTime;
  }

  public String getBonusStartDate()
  {
    return bonusStartDate;
  }

  public void setBonusStartDate(String bonusStartDate)
  {
    this.bonusStartDate = bonusStartDate;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("userId: ").append(userId).append("\n");
    buffer.append("personId: ").append(personId).append("\n");
    buffer.append("displayName: ").append(displayName).append("\n");
    buffer.append("caseId: ").append(caseId).append("\n");
    buffer.append("workingTime: ").append(workingTime).append("\n");
    buffer.append("bonusTime: ").append(bonusTime).append("\n");
    buffer.append("bonusStartDate: ").append(bonusStartDate).append("\n");

    return buffer.toString();
  }
}
