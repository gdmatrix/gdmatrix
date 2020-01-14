package org.santfeliu.forum.web;

/**
 *
 * @author blanquepa
 */
public class UserHit implements Comparable
{
  private String userId;
  private String forumId;
  private long time;

  public UserHit(String userId, String forumId, long time)
  {
    this.forumId = forumId;
    this.userId = userId;
    this.time = time;
  }

  public UserHit(UserHit hit)
  {
    this.forumId = hit.getForumId();
    this.userId = hit.getUserId();
    this.time = hit.getTime();
  }

  public long getTime()
  {
    return time;
  }

  public void setTime(long time)
  {
    this.time = time;
  }

  public String getForumId()
  {
    return forumId;
  }

  public void setForumId(String forumId)
  {
    this.forumId = forumId;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public int getTimeGap()
  {
    return Long.valueOf((System.currentTimeMillis() - time) / 1000).intValue();
  }

  public boolean isExpired(long gap)
  {
    return (System.currentTimeMillis() - time > gap);
  }

  public int compareTo(Object o)
  {
    UserHit hit = (UserHit)o;
    Long ltime = Long.valueOf(time);
    Long lotime = Long.valueOf(hit.getTime());

    return lotime.intValue() - ltime.intValue();
  }
}
