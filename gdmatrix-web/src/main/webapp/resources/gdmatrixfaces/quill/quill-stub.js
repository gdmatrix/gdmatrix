/* quill-stub.js */

function quillInit(clientId, readonly, maxLength)
{
  const editorId = clientId + "_editor";
  const inputId = clientId + "_input";
    
  const rootElem = document.getElementById(clientId);    
    
  const toolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'], // toggled buttons
    ['blockquote', 'code-block'],

    [{'list': 'ordered'}, {'list': 'bullet'}],
    [{'script': 'sub'}, {'script': 'super'}], // superscript/subscript
    [{'indent': '-1'}, {'indent': '+1'}], // outdent/indent
    [{'direction': 'rtl'}], // text direction

    [{'size': ['small', false, 'large', 'huge']}], // custom dropdown
    [{'header': [1, 2, 3, 4, 5, 6, false]}],
    ['link', 'image', 'video'], // add's image support
    [{'color': []}, {'background': []}], // dropdown with defaults from theme
    [{'font': []}],
    [{'align': []}],

    ['clean'] // remove formatting button
  ];

  const updateText = (delta, oldDelta, source) =>
  {     
    var html = quill.getSemanticHTML(0,quill.getLength());
    if (source === 'user') //maxLength control: Only when is a user input
    {
      var isDelete = delta.ops[delta.ops.length - 1].delete === 1;
      var length = html.trim().length;    
      if (!isDelete && maxLength && maxLength < length && html !== '<p><br></p>') 
      {
        quill.setContents(oldDelta);  
        html = quill.getSemanticHTML(0,quill.getLength()); //Set old value          
      }
    } 
    document.getElementById(inputId).value = html;    
  };

  const handleImage = (quill) =>
  {
    let url = "";
    let width = "200px";
    let alt = "image";

    let range = quill.getSelection();    
    let delta = quill.getContents(range.index, range.length);
    let ops = delta.ops;
    let op = null;

    let isUpdate = false;
    
    if (ops.length === 1)
    {
      op = ops[0];
      if (typeof op.insert === "object")
      {
        if (op.insert.image)
        {
          isUpdate = true;
          url = op.insert.image;
          width = op.attributes.width;
          alt = op.attributes.alt;
        }
      }
    }
    else if (ops.length > 1) return;
    
    if (typeof width === 'undefined') width = "";
    
    quill.setSelection(0, 0);
    
    let dialogElem = document.createElement("div");
    dialogElem.className = "ql-dialog";
    document.body.appendChild(dialogElem);
      
    dialogElem.innerHTML = `
      <label for="ql_img_url">URL:</label><input id="ql_img_url" type="text" value="${url}">
      <label for="ql_img_width">Width:</label><input id="ql_img_width" type="text" value="${width}">
      <label for="ql_img_alt">Alt:</label><input id="ql_img_alt" type="text" value="${alt}">
      <div class="text-center"><button id="ql_img_save">Save</button><button id="ql_img_cancel">Cancel</button></div>
    `;
   
    document.getElementById("ql_img_save").addEventListener("click", () => 
    {
      let url = document.getElementById("ql_img_url").value;
      let width = document.getElementById("ql_img_width").value;
      let alt = document.getElementById("ql_img_alt").value;
      
      if (url && width && alt)
      {
        if (!isUpdate)
        {
          quill.insertEmbed(range.index, 'image', url, Quill.sources.USER);
          quill.formatText(range.index, range.index + 1, 'width', width);
          quill.formatText(range.index, range.index + 1, 'alt', alt);        
        }
        else
        {
          let content = quill.getContents();
          op.insert.image = url;
          op.attributes.width = width;
          op.attributes.alt = alt;
          
          quill.setText("");
          quill.updateContents(content);
        }
        dialogElem.parentNode.removeChild(dialogElem);
      }
    });

    document.getElementById("ql_img_cancel").addEventListener("click", () => 
    {
      dialogElem.parentNode.removeChild(dialogElem);
    });    
  };

  const editorElem = document.getElementById(editorId);

  const quill = new Quill(editorElem, {
    theme: 'snow',
    modules: {
      toolbar: {
        container: toolbarOptions,
        handlers: {
          image: () => handleImage(quill)
        }
      }
    }
  });
  
  const elems = rootElem.getElementsByClassName("ql-toolbar");
  if (elems.length > 0)
  {
    const toolbarElem = elems[0];
    toolbarElem.classList.add("ui-editor-toolbar");
  }

  quill.on('text-change', updateText);
  updateText();
  
  if (readonly) quill.disable();
}
