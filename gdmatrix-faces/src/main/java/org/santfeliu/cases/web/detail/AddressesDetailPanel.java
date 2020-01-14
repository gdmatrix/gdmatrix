package org.santfeliu.cases.web.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.kernel.Address;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.misc.mapviewer.util.MapImageGenerator;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.util.ResultsManager;
/**
 *
 * @author blanquepa
 */
public class AddressesDetailPanel extends TabulatedDetailPanel
{
  private static final String LOCAL_CITY_NAME = "localCityName";
  private static final String LOCAL_MAP_CODE = "localMapCode";
  private static final String EXTERNAL_MAP_CODE = "externalMapCode";
  private static final String FILTER_COMMENTS = "filterComments";

  //OGC Server properties
  private static final String OGC_SERVER_URL_PROPERTY = "ogcServerURL";
  private static final String LAYERS_PROPERTY = "layers";
  private static final String SEARCH_LAYER_PROPERTY = "searchLayer";
  private static final String SRS_PROPERTY = "srs"; //EPSG:23031;
  private static final String BUFFER_SIZE_PROPERTY = "bufferSize";
  private static final String IMAGE_SIZE_PROPERTY = "imageSize";
  private static final String IMAGE_FORMAT_PROPERTY = "imageFormat";

  protected List<CaseAddressView> caseAddresses;
  private ResultsManager resultsManager;
  private boolean collapsed = true;

  public AddressesDetailPanel()
  {
    resultsManager =
      new ResultsManager(
          "org.santfeliu.cases.web.resources.CaseBundle", "caseAddresses_");
    resultsManager.addDefaultColumn("addressView.description");
  }

  public boolean isCollapsed()
  {
    return collapsed;
  }

  public void setCollapsed(boolean collapsed)
  {
    this.collapsed = collapsed;
  }

  @Override
  public void loadData(DetailBean detailBean)
  {
    resultsManager.setColumns(getMid());
    caseAddresses = new ArrayList();
    try
    {
      String caseId = ((CaseDetailBean) detailBean).getCaseId();
      CaseAddressFilter filter = new CaseAddressFilter();
      filter.setCaseId(caseId);
      List<CaseAddressView> addresses =
        CaseConfigBean.getPort().findCaseAddressViews(filter);
      for (CaseAddressView address : addresses)
      {
        String filterComments = getProperty(FILTER_COMMENTS);
        if (filterComments == null || filterComments.equals(address.getComments()))
        {
          caseAddresses.add(address);
        }
      }

      List<String> orderBy = getMultivaluedProperty(ResultsManager.ORDERBY);
      if (orderBy != null && !orderBy.isEmpty())
        resultsManager.sort(caseAddresses, orderBy);
    }
    catch(Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public boolean isRenderContent()
  {
    return (caseAddresses != null && !caseAddresses.isEmpty());
  }

  @Override
  public String getType()
  {
    return "addresses";
  }

  public List<CaseAddressView> getCaseAddresses()
  {
    return caseAddresses;
  }

  public void setCaseAddresses(List<CaseAddressView> caseAddresses)
  {
    this.caseAddresses = caseAddresses;
  }

  public ResultsManager getResultsManager()
  {
    return resultsManager;
  }

  public void setResultsManager(ResultsManager resultsManager)
  {
    this.resultsManager = resultsManager;
  }

  public String getImageMapURL()
  {
    CaseAddressView caseAddressView = (CaseAddressView)getValue("#{row}");
    return getImageMapURL(caseAddressView);
  }

  public String getImageMapURL(CaseAddressView caseAddressView)
  {
    String ogcServerUrl = getProperty(OGC_SERVER_URL_PROPERTY);
    String layers = getProperty(LAYERS_PROPERTY);
    String searchLayer = getProperty(SEARCH_LAYER_PROPERTY);
    String bufferSize = getProperty(BUFFER_SIZE_PROPERTY, "300");
    String srs = getProperty(SRS_PROPERTY, "EPSG:23031");
    String imageSize = getProperty(IMAGE_SIZE_PROPERTY, "300");
    String imageFormat = getProperty(IMAGE_FORMAT_PROPERTY, "image/png");

    String addressId = caseAddressView.getAddressView().getAddressId();
    Address address = KernelConfigBean.getPort().loadAddress(addressId);
    String gisReference = address.getGisReference();
    String query = "RF like '" + gisReference + "'";

    MapImageGenerator gen = new MapImageGenerator();
    gen.setOgcServerUrl(ogcServerUrl);
    gen.setLayers(layers); // layer list (comma separated)
    gen.setSearchLayer(searchLayer);
    gen.setBufferSize(Integer.valueOf(bufferSize));
    gen.setSrs(srs);
    gen.setFormat(imageFormat);

    return getContextPath() + "/" +
      gen.getImageUri(query, Integer.valueOf(imageSize));
  }

  public String getMapCode()
  {
    CaseAddressView caseAddressView = (CaseAddressView)getValue("#{row}");
    return getMapCode(caseAddressView);
  }

  public String getMapCode(CaseAddressView caseAddressView)
  {
    String localCityName = getProperty(LOCAL_CITY_NAME);
    String city = caseAddressView.getAddressView().getCity();
    if (city == null || (city != null && city.equalsIgnoreCase(localCityName)))
      return mergeProperties(caseAddressView, getProperty(LOCAL_MAP_CODE));
    else
      return mergeProperties(caseAddressView, getProperty(EXTERNAL_MAP_CODE));
  }

  private String mergeProperties(CaseAddressView caseAddressView,
    String templateString)
  {
    if (templateString == null)
      return null;

    Map properties = new HashMap();
    properties.put("caseAddressView", caseAddressView);
    properties.put("cas", caseAddressView.getCaseObject());
    properties.put("userSessionBean", UserSessionBean.getCurrentInstance());

    String caseName = caseAddressView.getCaseObject().getTitle();
    properties.put("name", caseName);
    String addressId = caseAddressView.getAddressView().getAddressId();
    properties.put("addressId", addressId);

    String description = caseAddressView.getAddressView().getDescription();
    String[] parts = getAddressParts(description);
    properties.put("streetName", parts[0]);
    if (parts.length > 1)
      properties.put("streetNumber", parts[1]);

    description = caseAddressView.getAddressView().getDescription() + "," +
      caseAddressView.getAddressView().getCity() + "," +
      caseAddressView.getAddressView().getCountry();
    properties.put("description", description);

    return WebTemplate.create(templateString).merge(properties);
  }

  public String sort()
  {
    resultsManager.sort(caseAddresses);
    return null;
  }

  public boolean isLocalAddress()
  {
    CaseAddressView caseAddressView = (CaseAddressView)getValue("#{row}");
    if (caseAddressView == null)
      return false;
    String city = caseAddressView.getAddressView().getCity();
    String localCityName = getProperty(LOCAL_CITY_NAME);
    return (city != null && city.equalsIgnoreCase(localCityName));
  }

  private String[] getAddressParts(String address)
  {
    String[] parts = address.split("\\s\\d");
    if (parts.length > 1)
    {
      address = address.substring(parts[0].length() + 1);
      parts[1] = address.split("\\D")[0];
    }
    return parts;
  }
}
