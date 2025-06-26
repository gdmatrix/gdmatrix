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
    [{ 'align': ['', 'center', 'right'] }],

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
  
  const handleLink = (quill) =>
  {
    const range = quill.getSelection();
    if (!range) return;
    
    const existingDialog = document.getElementById('link-dialog');
    if (existingDialog) existingDialog.remove();    

    const getLinkAtCursor = (quill, range) => 
    {
      const format = quill.getFormat(range);
      if (format.link) {
        const Link = Quill.import('formats/link');
        const [blot] = quill.scroll.descendant(Link, range.index);
        return blot?.domNode || null;
      }
      return null;
    };

    const existingLink = getLinkAtCursor(quill, range);
    const url = existingLink?.getAttribute('href') || '';
    const target = existingLink?.getAttribute('target') || '_self';

    const dialogElem = document.createElement("div");
    dialogElem.className = "ql-dialog";
    dialogElem.id = 'link-dialog';
    dialogElem.style.padding = "10px";

    dialogElem.innerHTML = `
      <div>
        <label for="link-url">URL:</label><br />
        <div style="display: flex; gap: 5px; align-items: stretch;">
          <input type="text" id="link-url" value="${url}" style="flex: 1;" />
          <button id="link-open" title="Show" style="white-space: nowrap;margin:0">Show</button>
        </div>
      </div>
      <div>
        <label for="link-target">Target:</label><br />
        <select id="link-target" style="width: 100%;">
          <option value="_self" ${target === '_self' ? 'selected' : ''}></option>
          <option value="_blank" ${target === '_blank' ? 'selected' : ''}>_blank</option>
        </select>
      </div>
      <div style="text-align: right; margin-top: 10px;">
        <button id="link-submit">Save</button>
        ${existingLink ? '<button id="link-remove">Remove</button>' : ''}    
        <button id="link-cancel">Cancel</button>
      </div>
    `;

    document.body.appendChild(dialogElem);

    const urlInput = document.getElementById('link-url');
    const targetSelect = document.getElementById('link-target');
    const open = document.getElementById('link-open');
    if (open) 
    {
      open.onclick = () => {
        let val = urlInput.value.trim();
        if (!val) return;

        let urlToOpen = '';

        if (/^https?:\/\//i.test(val)) {
          // Absolute URL with http or https prefix
          urlToOpen = val;
        } else if (val.startsWith('/')) {
          // Relative to current location
          urlToOpen = window.location.origin + val;
        } else {
          urlToOpen = 'http://' + val;
        }

        window.open(urlToOpen, '_blank');
      };
    }    
    const submit = document.getElementById('link-submit');
    const cancel = document.getElementById('link-cancel');
    const remove = document.getElementById('link-remove');

    //Save click
    submit.onclick = () => 
    {
      const linkUrl = urlInput.value.trim();
      const linkTarget = targetSelect.value;

      if (linkUrl) 
      {
        quill.formatText(range.index, range.length, 'link', linkUrl);

        setTimeout(() => 
        {
          const node = getLinkAtCursor(quill, range);
          if (node) 
            node.setAttribute('target', linkTarget);
        }, 0);
      }

      dialogElem.remove();
    };

    //Cancel click
    cancel.onclick = () => 
    {
      dialogElem.remove();
    };

    //Remove click
    if (remove) 
    {
      remove.onclick = () => 
      {
        quill.formatText(range.index, range.length, 'link', false);
        dialogElem.remove();
      };
    }
  }

  const editorElem = document.getElementById(editorId);

  const quill = new Quill(editorElem, {
    theme: 'snow',
    modules: {
      toolbar: {
        container: toolbarOptions,
        handlers: {
          image: () => handleImage(quill),
          link: () => handleLink(quill)
        }
      }
    }
  });
  
  //Remove native tooltip
  const tooltip = document.querySelector('.ql-tooltip');
  if (tooltip) tooltip.remove();

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
