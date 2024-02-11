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
package org.santfeliu.pdfgen;

import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.bridge.AbstractSVGBridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author realor
 */
public class PdfGenerator
{
  static SAXSVGDocumentFactory factory;
  static ThreadLocal<PdfGenerator> generatorThreadLocal = new ThreadLocal<>();

  private Document document;
  private PdfWriter writer;
  private final HashMap context = new HashMap();
  private final ArrayList<Class> bridgeRegistry = new ArrayList<>();

  static
  {
    String parser = XMLResourceDescriptor.getXMLParserClassName();
    factory = new SAXSVGDocumentFactory(parser);
  }

  public PdfGenerator()
  {
  }

  public Map getContext()
  {
    return context;
  }

  public static PdfGenerator getCurrentInstance()
  {
    PdfGenerator gen = generatorThreadLocal.get();
    if (gen == null)
    {
      gen = new PdfGenerator();
      generatorThreadLocal.set(gen);
    }
    return gen;
  }

  public void registerBridge(Class cls)
  {
    bridgeRegistry.add(cls);
  }

  public void unregisterBridge(Class cls)
  {
    bridgeRegistry.add(cls);
  }

  public void registerBridge(String className)
    throws ClassNotFoundException
  {
    Class cls = Class.forName(className);
    bridgeRegistry.add(cls);
  }

  public void open(OutputStream os) throws Exception
  {
    document = new Document();
    writer = PdfWriter.getInstance(document, os);
    String author = (String)context.get("author");
    if (author != null)
    {
      document.addAuthor(author);
    }
    String title = (String)context.get("title");
    if (title != null)
    {
     document.addTitle(title);
    }
    document.open();
  }

  public Document getDocument()
  {
    return document;
  }

  public PdfWriter getWriter()
  {
    return writer;
  }

  public void addPage(File svgTemplate) throws Exception
  {
    addPage(svgTemplate.toURI().toString());
  }

  public void addPage(String svgTemplateUri) throws Exception
  {
    SVGDocument svg = factory.createSVGDocument(svgTemplateUri);

    float factor = 72.0f / 90.0f; // auto detect from svg
    float pageWidth =
      factor * svg.getRootElement().getWidth().getBaseVal().getValue();
    float pageHeight =
      factor * svg.getRootElement().getHeight().getBaseVal().getValue();

    document.setPageSize(new Rectangle(pageWidth, pageHeight));
    document.newPage();

    UserAgent userAgent = new UserAgentAdapter();
    DocumentLoader loader = new DocumentLoader(userAgent);
    BridgeContext ctx = new BridgeContext(userAgent, loader)
    {
      @Override
      public void registerSVGBridges()
      {
        super.registerSVGBridges();
        for (Class cls : bridgeRegistry)
        {
          try
          {
            AbstractSVGBridge bridge =
              (AbstractSVGBridge)cls.getConstructor().newInstance();
            putBridge(bridge);
          }
          catch (Exception ex)
          {
          }
        }
      }
    };

    GVTBuilder builder = new GVTBuilder();

    GraphicsNode graphicsNode = builder.build(ctx, svg);

    PdfContentByte cb = writer.getDirectContent();

    PdfGraphics2D g2d = new PdfGraphics2D(cb, pageWidth, pageHeight);

    g2d.scale(factor, factor);

    graphicsNode.paint(g2d);

    g2d.dispose();

    ctx.dispose();
  }

  public void close() throws DocumentException
  {
    generatorThreadLocal.remove();
    if (writer.isPageEmpty())
    {
      document.newPage();
      document.add(new Paragraph("Document is empty."));
    }
    document.close();
  }
}
