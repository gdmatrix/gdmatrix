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

function quillInit(clientId, disabled)
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

  const updateText = () =>
  {
    var html = document.getElementById(editorId).firstElementChild.innerHTML;
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

  quill.on('text-change', (delta, oldDelta, source) => updateText());
  updateText();
  if (disabled) quill.disable();
}
