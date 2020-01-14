package org.santfeliu.forum.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.matrix.forum.ForumManagerPort;
import org.matrix.forum.ForumManagerService;
import org.matrix.security.SecurityConstants;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class ForumConfigBean
{
  private static final String TEMPORARY_USERID_ATTRIBUTE = "temporaryUserId";
  private static final String HIT_TIME_PROPERTY = "expireHitTime";
  private static final long DEFAULT_HIT_TIME = 60000; // 1 minute

  private static HashMap<String, UserHit> hits;

  public static ForumManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(ForumManagerService.class);
    return endpoint.getPort(ForumManagerPort.class,
      getUserId(), UserSessionBean.getCurrentInstance().getPassword());
  }
  
  public static synchronized void registerUserHit(String forumId)
  {
    if (hits == null) hits = new HashMap();
    String userId = getUserId();
    UserHit hit = hits.get(userId);
    if (hit == null)
      hit = new UserHit(userId, forumId, System.currentTimeMillis());
    else
    {
      hit.setForumId(forumId);
      hit.setTime(System.currentTimeMillis());
    }
    hits.put(userId, hit);
  }

  public static synchronized int getForumHits(String forumId)
  {
    ArrayList<String> toDelete = new ArrayList();
    int counter = 0;
    for (UserHit hit : hits.values())
    {
      if (hit.getForumId().equals(forumId))
      {
        if(!hit.isExpired(getHitTime()))
          counter++;
        else
          toDelete.add(hit.getUserId());
      }
    }
    //Remove expired hits
    for (String userId : toDelete)
    {
      hits.remove(userId);
    }
    return counter;
  }

  public static synchronized List<UserHit> getForumHitList(String forumId)
  {
    ArrayList<String> toDelete = new ArrayList();
    ArrayList<UserHit> result = new ArrayList();
    for (UserHit hit : hits.values())
    {
      if (hit.getForumId().equals(forumId))
      {
        if(!hit.isExpired(getHitTime()))
          result.add(new UserHit(hit));
        else
          toDelete.add(hit.getUserId());
      }
    }
    //Remove expired hits from Map
    for (String userId : toDelete)
    {
      hits.remove(userId);
    }

    //Sort result List
    Collections.sort(result);

    return result;
  }

  public static boolean isUserConnected(String userId, String forumId)
  {
    UserHit hit = hits.get(userId);
    if (hit == null)
      return false;
    else
      return !hit.isExpired(getHitTime()) &&
        (forumId == null || forumId.equals(hit.getForumId()));
  }

  private static String getUserId()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    if (userSessionBean.isAnonymousUser() || userSessionBean.isAutoLoginUser())
    {
      userId =
        (String)userSessionBean.getAttribute(TEMPORARY_USERID_ATTRIBUTE);
      if (userId == null)
      {
        userId = generateRandomUserId();
        userSessionBean.setAttribute(TEMPORARY_USERID_ATTRIBUTE, userId);
      }
    }
    else
      userSessionBean.setAttribute(TEMPORARY_USERID_ATTRIBUTE, null);

    return userId;
  }

  private static String generateRandomUserId()
  {
    return SecurityConstants.TEMP_USER_PREFIX + 
      RandomStringUtils.randomAlphanumeric(19);
  }

  private static long getHitTime()
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(HIT_TIME_PROPERTY);
    if (value == null)
      return DEFAULT_HIT_TIME;
    else
      return Long.valueOf(value).longValue();
  }

}
