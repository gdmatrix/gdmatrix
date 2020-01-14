package org.santfeliu.cases.web.detail;

import java.util.Date;
import java.util.List;
import org.matrix.agenda.EventFilter;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.agenda.web.EventSearchBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DetailBean;

/**
 *
 * @author blanquepa
 */
public class PersonsAgendaDetailPanel extends PersonsDetailPanel
{
  private static final String AGENDA_SEARCH_MID = "agendaSearchMid";
  private static final String DATE_FORMAT = "dateFormat";
  private static final String SHOW_MORE_TEXT = "showMoreText";
  private static final String PAGE_SIZE = "pageSize";
  private static final int DEFAULT_PAGE_SIZE = 5;

  private List types;
  private List themes;
  private String agendaSearchMid;
  private int eventCount;

  @Override
  public void loadData(DetailBean detailBean)
  {
    super.loadData(detailBean);

    agendaSearchMid = getProperty(AGENDA_SEARCH_MID);
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(agendaSearchMid);

    themes = mic.getMultiValuedProperty(EventSearchBean.SEARCH_EVENT_THEME);
    if (themes == null)
      themes = mic.getMultiValuedProperty("theme");

    types = mic.getMultiValuedProperty(EventSearchBean.SEARCH_EVENT_TYPE);
    if (types == null)
      types = mic.getMultiValuedProperty("eventTypeId");

    EventFilter filter = new EventFilter();

    String startDateTime =
      TextUtils.formatDate(new Date(), "yyyyMMdd") + "000000";
    filter.setStartDateTime(startDateTime);
    //themes
    filter.getThemeId().addAll(themes);
    //types
    if (types != null && !types.isEmpty())
      filter.getEventTypeId().addAll(types);
    //persons
    if (getPersonId() != null)
      filter.setPersonId(getPersonId());

    try
    {
      eventCount = AgendaConfigBean.getPort().countEventsFromCache(filter);
    }
    catch (Exception ex)
    {
      eventCount = 0;
    }
  }

  @Override
  public String getType()
  {
    return "persons_agenda";
  }

  @Override
  public boolean isRenderContent()
  {
    List types = getAgendaTypes();
    List themes = getAgendaThemes();
    return (casePersons != null && !casePersons.isEmpty() &&
      getAgendaSearchMid() != null &&
      types != null && !types.isEmpty() &&
      themes != null && !themes.isEmpty() &&
      eventCount > 0);
  }

  public String getPersonId()
  {
    if (casePersons != null && !casePersons.isEmpty())
      return casePersons.get(0).getPersonView().getPersonId();
    else
      return null;
  }

  public List getAgendaThemes()
  {
    return themes;
  }

  public List getAgendaTypes()
  {
    return types;
  }

  public String getAgendaSearchMid()
  {
    return agendaSearchMid != null ?
      agendaSearchMid : getProperty(AGENDA_SEARCH_MID);
  }

  public String getEventUrl()
  {
    String agendaSearchMid = getProperty(AGENDA_SEARCH_MID);
    return "go.faces?xmid=" + agendaSearchMid + "&eventid=#{e.eventId}";
  }

  public String getDateFormat()
  {
    return getProperty(DATE_FORMAT);
  }

  public String getShowMoreText()
  {
    return getProperty(SHOW_MORE_TEXT);
  }

  public int getPageSize()
  {
    String pageSize = getProperty(PAGE_SIZE);
    return pageSize != null ? Integer.parseInt(pageSize) : DEFAULT_PAGE_SIZE;
  }

}
