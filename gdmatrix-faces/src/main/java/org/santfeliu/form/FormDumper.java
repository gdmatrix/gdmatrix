package org.santfeliu.form;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public class FormDumper
{
  public FormDumper()
  {
  }

  public void dump(Form form, OutputStream out)
  {
    dump(form, new PrintWriter(out));
  }

  public void dump(Form form, PrintStream out)
  {
    dump(form, new PrintWriter(out));
  }

  public void dump(Form form, Writer out)
  {
    dump(form, new PrintWriter(out));
  }

  private void dump(Form form, PrintWriter out)
  {
    try
    {
      dumpHeader(form, out);

      for (Field field : form.getFields())
      {
        dumpField(field, out);
      }
      View view = form.getRootView();
      dumpView(view, "", out);
    }
    finally
    {
      out.close();
    }
  }

  protected void dumpHeader(Form form, PrintWriter out)
  {
    out.println("Id: " + form.getId());
    out.println("Title: " + form.getTitle());
    out.println("Language: " + form.getLanguage());
    out.println("Class: " + form.getClass().getName());
    out.println("Context: " + form.getContext());
    out.println("--------------------------------");
  }

  protected void dumpField(Field field, PrintWriter out)
  {
    out.println("Label: " + field.getLabel());
    out.println("Reference: " + field.getReference());
    out.println("Type: " + field.getType());
    out.println("Class: " + field.getClass().getName());
    out.println("ReadOnly: " + field.isReadOnly());
    out.println("MinOccurs: " + field.getMinOccurs());
    out.println("MaxOccurs: " + field.getMaxOccurs());
    out.println("--------------------------------");
  }

  protected void dumpView(View view, String indent, PrintWriter out)
  {
    out.println(indent + "--------------------------------");
    out.println(indent + "Type: " + view.getViewType());
    out.println(indent + "Class: " + view.getClass().getName());
    out.println(indent + "NativeType: " + view.getNativeViewType());
    if (view.getId() != null)
      out.println(indent + "Id: " + view.getId());
    if (view.getReference() != null)
      out.println(indent + "Reference: " + view.getReference());
    out.println(indent + "Properties:");
    for (String propertyName : view.getPropertyNames())
    {
      Object value = view.getProperty(propertyName);
      out.println(indent + "-> " + propertyName + "=" + value);
    }
    for (View child : view.getChildren())
    {
      dumpView(child, indent + "  ", out);
    }    
  }

  public static void main(String[] args)
  {
    try
    {
      HtmlForm form = new HtmlForm();
      form.read(new FileInputStream("c:/sample1.html"));

      FormDumper dumper = new FormDumper();
      dumper.dump(form, System.out);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
