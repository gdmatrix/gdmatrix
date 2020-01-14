package org.santfeliu.kernel.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.TypeFilter;
import org.matrix.kernel.KernelConstants;

import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.dic.web.TypeSearchBean;
import org.santfeliu.web.obj.BasicSearchBean;


public class RoomSearchBean extends BasicSearchBean
{
  private List<SelectItem> typeSelectItems;
  private RoomFilter filter;
  private Integer capacity;
  private String addressId;

  public RoomSearchBean()
  {
    typeSelectItems = null;
    filter = new RoomFilter();
  }
  
  public void setFilter(RoomFilter filter)
  {
    this.filter = filter;
  }

  public RoomFilter getFilter()
  {
    return filter;
  }

  public Integer getCapacity()
  {
    return capacity;
  }

  public void setCapacity(Integer capacity)
  {
    this.capacity = capacity;
  }

  public void setRoomId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getRoomId()
  {
    return addressId;
  }

  public int countResults()
  {
    try
    {
      filter.setCapacity(capacity != null ? capacity.intValue() : 0);
      return KernelConfigBean.getPort().countRooms(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      filter.setCapacity(capacity != null ? capacity.intValue() : 0);
      return KernelConfigBean.getPort().findRoomViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String showRoom()
  {
    return getControllerBean().showObject("Room",
      (String)getValue("#{row.roomId}"));
  }

  public String selectRoom()
  {
    return getControllerBean().select((String)getValue("#{row.roomId}"));
  }

  public String show()
  {
    return "room_search";
  }

  public String searchType()
  {
    TypeSearchBean typeSearchBean = (TypeSearchBean)getBean("typeSearchBean");
    if (typeSearchBean == null)
      typeSearchBean = new TypeSearchBean();

    typeSearchBean.setRootTypeId(DictionaryConstants.ROOM_TYPE);
    typeSearchBean.setFilter(new TypeFilter());
    typeSearchBean.search();

    return getControllerBean().searchObject("Type",
      "#{roomSearchBean.filter.roomTypeId}");
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      TypeBean typeBean = (TypeBean)getBean("typeBean");
      String[] actions = {DictionaryConstants.READ_ACTION};
      typeSelectItems = typeBean.getAllSelectItems(DictionaryConstants.ROOM_TYPE,
        KernelConstants.KERNEL_ADMIN_ROLE, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  }

  public String getTypeDescription()
  {
    String description = null;
    RoomView row = (RoomView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if (row != null)
    {
      String typeId = row.getRoomTypeId();
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        description = type != null && type.getDescription() != null ?
          type.getDescription() : typeId;
      }
    }
    return description;
  }

}
