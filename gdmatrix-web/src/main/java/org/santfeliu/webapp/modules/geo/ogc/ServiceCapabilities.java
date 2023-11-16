/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.webapp.modules.geo.ogc;

import org.santfeliu.misc.mapviewer.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author realor
 */
public class ServiceCapabilities implements Serializable
{
  private String version;
  private String name;
  private String title;
  private String _abstract;
  private final ContactInformation contactInformation = new ContactInformation();
  private final List<String> srs = new ArrayList<>();
  private final List<Layer> layers = new ArrayList<>();
  private final Request request = new Request();

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getAbstract()
  {
    return _abstract;
  }

  public void setAbstract(String _abstract)
  {
    this._abstract = _abstract;
  }

  public ContactInformation getContactInformation()
  {
    return contactInformation;
  }

  public List<String> getSrs()
  {
    return srs;
  }

  public Request getRequest()
  {
    return request;
  }

  public List<Layer> getLayers()
  {
    return layers;
  }

  public class ContactInformation
  {
    private final PersonPrimary personPrimary = new PersonPrimary();
    private String position;
    private final Address address = new Address();
    private String voiceTelephon;
    private String facsimileTelephon;
    private String electronicMailAddress;

    public PersonPrimary getPersonPrimary()
    {
      return personPrimary;
    }

    public String getPosition()
    {
      return position;
    }

    public void setPosition(String position)
    {
      this.position = position;
    }

    public Address getAddress()
    {
      return address;
    }

    public String getVoiceTelephon()
    {
      return voiceTelephon;
    }

    public void setVoiceTelephon(String phone)
    {
      this.voiceTelephon = phone;
    }

    public String getFacsimileTelephon()
    {
      return facsimileTelephon;
    }

    public void setFacsimileTelephon(String fax)
    {
      this.facsimileTelephon = fax;
    }

    public String getElectronicMailAddress()
    {
      return electronicMailAddress;
    }

    public void setElectronicMailAddress(String email)
    {
      this.electronicMailAddress = email;
    }
  }

  public class PersonPrimary
  {
    private String person;
    private String organization;

    public String getOrganization()
    {
      return organization;
    }

    public void setOrganization(String organization)
    {
      this.organization = organization;
    }

    public String getPerson()
    {
      return person;
    }

    public void setPerson(String person)
    {
      this.person = person;
    }
  }

  public class Address
  {
    private String addressType;
    private String address;
    private String city;
    private String stateOrProvince;
    private String postCode;
    private String country;

    public String getAddressType()
    {
      return addressType;
    }

    public void setAddressType(String addressType)
    {
      this.addressType = addressType;
    }

    public String getAddress()
    {
      return address;
    }

    public void setAddress(String address)
    {
      this.address = address;
    }

    public String getCity()
    {
      return city;
    }

    public void setCity(String city)
    {
      this.city = city;
    }

    public String getPostCode()
    {
      return postCode;
    }

    public void setPostCode(String postCode)
    {
      this.postCode = postCode;
    }

    public String getStateOrProvince()
    {
      return stateOrProvince;
    }

    public void setStateOrProvince(String stateOrProvince)
    {
      this.stateOrProvince = stateOrProvince;
    }

    public String getCountry()
    {
      return country;
    }

    public void setCountry(String country)
    {
      this.country = country;
    }
  }

  public class Request
  {
    private GetMap getMap = new GetMap();

    public GetMap getGetMap()
    {
      return getMap;
    }
  }

  public class GetMap
  {
    private List<String> formats = new ArrayList<String>();

    public List<String> getFormats()
    {
      return formats;
    }
  }

  public class Layer
  {
    private String name;
    private String title;
    private String _abstract;
    private final List<String> srs = new ArrayList<>();
    private final List<Style> styles = new ArrayList<>();
    private final Map<String, Bounds> boundingBoxes = new HashMap<>();

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getAbstract()
    {
      return _abstract;
    }

    public void setAbstract(String _abstract)
    {
      this._abstract = _abstract;
    }

    public List<String> getSrs()
    {
      return srs;
    }

    public Map<String, Bounds> getBoundingBoxes()
    {
      return boundingBoxes;
    }

    public List<Style> getStyles()
    {
      return styles;
    }

    @Override
    public String toString()
    {
      return "Layer(" + name + ", " + title + ", " + _abstract + ")\n";
    }
  }

  public class Style
  {
    private String name;
    private String title;
    private String _abstract;

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getAbstract()
    {
      return _abstract;
    }

    public void setAbstract(String _abstract)
    {
      this._abstract = _abstract;
    }

    @Override
    public String toString()
    {
      return name;
    }
  }

  public Layer createLayer()
  {
    return new Layer();
  }

  public Style createStyle()
  {
    return new Style();
  }
}
