package org.santfeliu.misc.mapviewer.io;

import java.io.InputStream;
import java.util.Stack;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.santfeliu.misc.mapviewer.ServiceCapabilities;
import org.santfeliu.misc.mapviewer.ServiceCapabilities.ContactInformation;
import org.santfeliu.misc.mapviewer.ServiceCapabilities.Layer;
import org.santfeliu.misc.mapviewer.ServiceCapabilities.Style;

/**
 *
 * @author realor
 */
public class ServiceCapabilitiesReader
{
  public ServiceCapabilities read(InputStream is) throws Exception
  {
    ServiceCapabilities capabilities = new ServiceCapabilities();
    Stack<String> stack = new Stack<String>();
    Layer layer = null;
    Style style = null;
    boolean inContactInfo = false;

    XMLInputFactory xmlif = XMLInputFactory.newInstance();
    XMLStreamReader reader = xmlif.createXMLStreamReader(is);
    try
    {
      while (reader.hasNext())
      {
        int eventType = reader.next();
        if (XMLStreamReader.START_ELEMENT == eventType)
        {
          String tag = reader.getLocalName();
          stack.push(tag);
          if (stack.size() == 1)
          {
            String version = reader.getAttributeValue(null, "version");
            capabilities.setVersion(version);
          }
          else if (tag.equals("Layer") && stack.size() > 3)
          {
            layer = capabilities.createLayer();
            capabilities.getLayers().add(layer);
          }
          else if (tag.equals("Style"))
          {
            style = capabilities.createStyle();
            if (layer != null)
            {
              layer.getStyles().add(style);
            }
          }
          else if (tag.equals("ContactInformation"))
          {
            inContactInfo = true;
          }
        }
        else if (XMLStreamReader.END_ELEMENT == eventType)
        {
          String tag = stack.pop();
          if (tag.equals("Layer"))
          {
            layer = null;
          }
          else if (tag.equals("Style"))
          {
            style = null;
          }
          else if (tag.equals("ContactInformation"))
          {
            inContactInfo = false;
          }
        }
        else if (XMLStreamReader.CHARACTERS == eventType)
        {
          String tag = stack.peek();
          String text = reader.getText();
          if (tag.equals("Name"))
          {
            if (style != null)
            {
              style.setName(text);
            }
            else if (layer != null)
            {
              layer.setName(text);
            }
            else if (equals(stack, null, "Service", "Name"))
            {
              capabilities.setName(text);
            }
          }
          else if (tag.equals("Title"))
          {
            if (style != null)
            {
              style.setTitle(text);
            }
            else if (layer != null)
            {
              layer.setTitle(text);
            }
            else if (equals(stack, null, "Service", "Title"))
            {
              capabilities.setTitle(text);
            }
          }
          else if (tag.equals("Abstract"))
          {
            if (style != null)
            {
              style.setAbstract(text);
            }
            else if (layer != null)
            {
              layer.setAbstract(text);
            }
            else if (equals(stack, null, "Service", "Abstract"))
            {
              capabilities.setAbstract(text);
            }
          }
          else if (tag.equals("SRS") || tag.equals("CRS"))
          {            
            if (layer != null)
            {
              layer.getSrs().add(text);
            }
            else
            {
              capabilities.getSrs().add(text);
            }
          }
          else if (inContactInfo)
          {
            ContactInformation contactInfo =
               capabilities.getContactInformation();
            if (tag.equals("ContactPerson"))
            {
              contactInfo.getPersonPrimary().setPerson(text);
            }
            else if (tag.equals("ContactOrganization"))
            {
              contactInfo.getPersonPrimary().setOrganization(text);
            }
            else if (tag.equals("ContactPosition"))
            {
              contactInfo.setPosition(text);
            }
            else if (tag.equals("AddressType"))
            {
              contactInfo.getAddress().setAddressType(text);
            }
            else if (tag.equals("Address"))
            {
              contactInfo.getAddress().setAddress(text);
            }
            else if (tag.equals("City"))
            {
              contactInfo.getAddress().setCity(text);
            }
            else if (tag.equals("StateOrProvince"))
            {
              contactInfo.getAddress().setStateOrProvince(text);
            }
            else if (tag.equals("PostCode"))
            {
              contactInfo.getAddress().setPostCode(text);
            }
            else if (tag.equals("Country"))
            {
              contactInfo.getAddress().setCountry(text);
            }
            else if (tag.equals("ContactVoiceTelephone"))
            {
              contactInfo.setVoiceTelephon(text);
            }
            else if (tag.equals("ContactFacsimileTelephone"))
            {
              contactInfo.setFacsimileTelephon(text);
            }
            else if (tag.equals("ContactElectronicMailAddress"))
            {
              contactInfo.setElectronicMailAddress(text);
            }
          }
          else if (tag.equals("Format"))
          {
            if ("GetMap".equals(stack.get(3)))
            {
              capabilities.getRequest().getGetMap().getFormats().add(text);
            }
          }
        }
      }
    }
    finally
    {
      reader.close();
    }
    return capabilities;
  }

  private boolean equals(Stack<String> stack, String ... tags)
  {
    boolean equals = stack.size() == tags.length;
    int i = 0;
    while (equals && i < tags.length && i < stack.size())
    {
      if (tags[i] != null)
      {
        equals = stack.get(i).equals(tags[i]);
      }
      i++;
    }
    return equals;
  }

  public static void main(String args[])
  {
    try
    {
      InputStream is = MapReader.class.getResourceAsStream("cap.xml");
      ServiceCapabilitiesReader reader = new ServiceCapabilitiesReader();
      ServiceCapabilities capabilities = reader.read(is);
      System.out.println("Version: " + capabilities.getVersion());
      System.out.println("Name: " + capabilities.getName());
      System.out.println("Title: " + capabilities.getTitle());
      System.out.println("Abstract: " + capabilities.getAbstract());
      ContactInformation contactInfo = capabilities.getContactInformation();
      System.out.println("Person: " + contactInfo.getPersonPrimary().getPerson());
      System.out.println("Organization: " + contactInfo.getPersonPrimary().getOrganization());
      System.out.println("GetMap formats: " + capabilities.getRequest().getGetMap().getFormats());
      for (Layer layer : capabilities.getLayers())
      {
        System.out.println("Name: " + layer.getName());
        System.out.println("Style: " + layer.getTitle());
        System.out.println("SRS: " + layer.getSrs());
        System.out.println("Styles: " + layer.getStyles());
        System.out.println("------------------------------");
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
