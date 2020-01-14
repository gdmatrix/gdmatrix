package org.santfeliu.faces.browser.encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.santfeliu.faces.Translator;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.util.net.HttpClient;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;


public class HtmlEncoder extends ContentEncoder
{
  public HtmlEncoder()
  {
  }
  
  public void encode(HtmlBrowser browser, HttpClient httpClient, 
    ResponseWriter writer, Translator translator, String translationGroup)
    throws IOException
  {
    InputStream input = null;

    String charset = httpClient.getContentEncoding();
    if (charset == null) charset = Charset.defaultCharset().name();

    // translate document first
    if (translator != null)
    {
      Reader in = new InputStreamReader(
        httpClient.getContentInputStream(), charset);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Writer out = new OutputStreamWriter(bos, charset);
      translator.translate(in, out, "text/html",
        getLanguage(), translationGroup);
      input = new ByteArrayInputStream(bos.toByteArray());
    }
    else input = httpClient.getContentInputStream();

    Tidy tidy = new Tidy();
    if (browser.hasAllowedTags())
    {
      if (browser.isHeadTagAllowed())
        tidy.setPrintBodyOnly(true);
      String allowedTags = browser.getAllowedHtmlTags();
      Properties props = new Properties();
      props.put("new-blocklevel-tags", allowedTags);
      tidy.setConfigurationFromProps(props);
    }
      
    tidy.setInputEncoding(charset);
    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    PrintWriter errorWriter = new PrintWriter(errorStream, true);
    tidy.setErrout(errorWriter);
    errorWriter.flush();

    // parse document with Tidy
    Document document = tidy.parseDOM(input, null);
    // write to output
    writeDocumentBody(browser, document, writer, null);
    // TODO: show errors
    //writeErrors(browser, errorStream.toByteArray(), writer);
  }

  private void writeDocumentBody(HtmlBrowser browser, Document document, 
    ResponseWriter writer, String actionURL)
    throws IOException
  {
    // look for html element
    Node html = findNode(document.getFirstChild(), "html");
    if (html == null) throw new IOException("Invalid html document.");
    
    // look for head element
    if (browser.isHeadTagAllowed())
    {
      Node head = findNode(html.getFirstChild(), "head");
      // write head contents
      if (head != null)
      {
        Node node = head.getFirstChild();
        while (node != null)
        {
          writeNode(browser, node, writer, actionURL);
          node = node.getNextSibling();
        }    
      }
    }
    
    // look for body element
    Node body = findNode(html.getFirstChild(), "body");
    if (body == null) throw new IOException("Invalid html document.");

    // write body contents
    Node node = body.getFirstChild();
    while (node != null)
    {
      writeNode(browser, node, writer, actionURL);
      node = node.getNextSibling();
    }
  }
  
  private Node findNode(Node node, String nodeName)
  {
    Node foundNode = null;
    Node currentNode = node;
    while (foundNode == null && currentNode != null)
    {
      String name = currentNode.getNodeName();
      if (nodeName.equalsIgnoreCase(name))
      {
        foundNode = currentNode;
      }
      else currentNode = currentNode.getNextSibling();
    }
    return foundNode;
  }

  private void writeNode(HtmlBrowser browser, Node node, 
    ResponseWriter writer, String actionURL) throws IOException
  {
    if (node instanceof Text)
    {
      String text = node.getNodeValue();
      writer.writeText(text, null);
    }
    else if (node instanceof Comment)
    {
      writer.writeComment(node.getNodeValue());
    }
    else if (node instanceof Element)// normal tag
    {
      Element element = (Element)node;
      String tag = element.getNodeName().toLowerCase();
      if (tag.equals("form")) // it's a form
      {
        actionURL = element.getAttribute("action");
        writeChildren(browser, node, writer, actionURL);
        return;
      }
      else if (tag.equals("img")) // it's a image
      {
        String src = element.getAttribute("src");
        src = browser.getAbsoluteHRef(src, false, true);
        element.setAttribute("src", src);
      }
      else if (tag.equals("a")) // it's a link
      {
        String href = element.getAttribute("href");
        if (!href.startsWith("javascript:") && 
            !href.startsWith("mailto:") &&
            !href.startsWith("mms:") &&
            !href.startsWith("ftp:") &&
            !href.equals("#"))
        {
          String target = element.getAttribute("target");
          if (target.length() == 0)
          {
            element.setAttribute("href", "#");
            element.setAttribute("onclick", browser.getOnclickLink(href));
          }
          else
          {
            element.setAttribute("href", 
              browser.getAbsoluteHRef(href, false, false));
          }
        }
      }
      else if (tag.equals("input")) // it's a input
      {
        String type = element.getAttribute("type");
        if (type.equals("submit"))
        {
          element.setAttribute("type", "button");
          element.setAttribute("onclick", browser.getOnclickSubmit(actionURL));
        }
      }
      writer.startElement(tag, browser);
      writeAttributes(element, writer);
      writeChildren(browser, node, writer, actionURL);
      writer.endElement(tag);
    }
  }

  private void writeChildren(HtmlBrowser browser, Node node,
    ResponseWriter writer, String actionURL) throws IOException
  {
    // render children
    Node child = node.getFirstChild();
    while (child != null)
    {
      writeNode(browser, child, writer, actionURL);
      child = child.getNextSibling();
    }
  }
  
  private void writeAttributes(Element element, ResponseWriter writer)
    throws IOException
  {
    NamedNodeMap map = element.getAttributes();
    int count = map.getLength();
    for (int i = 0; i < count; i++)
    {
      Node attribute = map.item(i);
      String name = attribute.getNodeName();
      String value = attribute.getNodeValue();
      writer.writeAttribute(name, value, null);
    }
  }

  private void writeErrors(HtmlBrowser browser,
    byte[] errors, ResponseWriter writer) throws IOException
  {
    String[] lines = new String(errors).split("\n");
    writer.startElement("div", browser);
    writer.writeAttribute("style", "width:100%", null);
    writer.writeAttribute("class", "outputBox", null);
    for (String line : lines)
    {
      writer.writeText(line, null);
      writer.startElement("br", browser);
      writer.endElement("br");
    }
    writer.endElement("div");
  }

  private String getLanguage()
  {
    Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    return locale.getLanguage();
  }
}
