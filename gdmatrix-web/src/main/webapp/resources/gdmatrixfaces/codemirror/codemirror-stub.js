/* codemirror-stub.js */

import "./codemirror.js";

// Object used to store all instances indexed by ID.
window.cmInstances = window.cmInstances || {};

function codemirrorInit(clientId, readonly, language, showLineNumbers, completion, changeListener, activeExtensions)
{
  
  console.log("Extensiones que llegan desde Java:", activeExtensions);
  
  const editorId = clientId + "_editor";
  const inputId = clientId + "_input";

  const editorElem = document.getElementById(editorId);
  editorElem.className = "cm-editor-holder";

  const inputElem = document.getElementById(inputId);

  const {keymap, highlightSpecialChars, drawSelection,
    highlightActiveLine, dropCursor,
    rectangularSelection, crosshairCursor, EditorView,
    lineNumbers, highlightActiveLineGutter, lineWrapping} = CM["@codemirror/view"];
  const {Extension, EditorState} = CM["@codemirror/state"];
  const {defaultHighlightStyle, syntaxHighlighting, indentOnInput,
    bracketMatching, foldGutter, foldKeymap, indentUnit} = CM["@codemirror/language"]
  const {defaultKeymap, history, historyKeymap, indentWithTab} = CM["@codemirror/commands"];
  const {searchKeymap, highlightSelectionMatches} = CM["@codemirror/search"];
  const {autocompletion, completionKeymap, closeBrackets, closeBracketsKeymap} =
          CM["@codemirror/autocomplete"];
  const {lintKeymap} = CM["@codemirror/lint"];
  
  const EXTENSION_REGISTRY = {
    "indentWithTab": keymap.of([indentWithTab]),
    "lineWrapping": EditorView.lineWrapping
    // Add optional extensions here; they must also be imported as constants above.
  };
  
  let theme = EditorView.theme({
    "&.cm-focused .cm-cursor": {
      borderLeftColor: "var(--text-color)",
      borderLeftWidth: "2px"
    },
    "&.cm-focused .cm-matchingBracket": {
      "backgroundColor": "#C0C000",
      "color": "black"
    },
    "&.ͼ2 .cm-activeLine": {
      "backgroundColor": "rgba(128, 128, 128, 0.1)"
    },
    "&.ͼ2 .cm-activeLineGutter": {
      "backgroundColor": "rgba(128, 128, 128, 0.1)"
    },
    "&.ͼ2 .cm-tooltip": {
      "backgroundColor": "var(--surface-overlay)"
    },
    "&.ͼ2 .cm-gutters": {
      "backgroundColor": "var(--surface-overlay)"
    },
    "& .ͼa": {
      "color": "#666",
      "fontWeight": "bold"
    },
    "& .ͼl": {
      "color": "#808080"
    },
    "& .ͼd": {
      "color": "#2020ff"
    },
    "& .cm-line": {
      "color": "var(--text-color)"
    },
    "& .cm-wrap": {
      "height": "100%"
    },
    "& .cm-scroller": {
      "overflow": "auto"
    },
    "& .ͼb": {
      "color": "#444",
      "fontWeight": "bold"
    },
    "& .ͼe": {
      "color": "#2020ff"
    },
    "& .ͼf": {
      "color": "#8080e0"
    },
    "& .ͼg": {
      "color": "#444"
    },
    "& .ͼm": {
      "color": "#808080"
    }
  });

  let editorView = new EditorView(
          {
            parent: editorElem
          });

  let updateListenerExtension = EditorView.updateListener.of((update) => {
    if (update.docChanged) {
      const doc = update.state.doc.toString();
      inputElem.value = doc;
      if (typeof changeListener === "function")
      {
        changeListener(editorView, doc);
      }
    }
  });

  const extensions = [
    highlightActiveLineGutter(),
    highlightSpecialChars(),
    history(),
    drawSelection(),
    dropCursor(),
    EditorState.allowMultipleSelections.of(true),
    indentOnInput(),
    syntaxHighlighting(defaultHighlightStyle, {fallback: true}),
    bracketMatching(),
    closeBrackets(),
    autocompletion(),
    rectangularSelection(),
    crosshairCursor(),
    highlightActiveLine(),
    highlightSelectionMatches(),
    indentUnit.of("  "),
    keymap.of([
      ...closeBracketsKeymap,
      ...defaultKeymap,
      ...searchKeymap,
      ...historyKeymap,
      ...foldKeymap,
      ...completionKeymap,
      ...lintKeymap
    ]),
    updateListenerExtension,
    theme];
  
  let extsToLoad = [];
  if (Array.isArray(activeExtensions)) {
    extsToLoad = activeExtensions;
  }else if (typeof activeExtensions == 'string') {
    try {
      extsToLoad = JSON.parse(activeExtensions);
    }catch(e){
      console.warn("ERROR al parsear extensiones", e);
    }
  }
  
  // Load dynamic extensions
  extsToLoad.forEach(extName => {
    const ext = EXTENSION_REGISTRY[extName];
    if (ext) {
      if(Array.isArray(ext)){
        extensions.push(...ext);
      }else{
        extensions.push(ext);
      }
    }
  });
  
  switch (language)
  {
    case "html":
      const {html} = CM["@codemirror/lang-html"];
      extensions.push(html());
      break;

    case "json":
      const {json} = CM["@codemirror/lang-json"];
      extensions.push(json());
      break;

    case "xml":
      const {xml} = CM["@codemirror/lang-xml"];
      extensions.push(xml({autoCloseTags: true}));
      break;

    case "sql":
      const {sql} = CM["@codemirror/lang-sql"];
      extensions.push(sql());
      break;

    case "css":
      const {css} = CM["@codemirror/lang-css"];
      extensions.push(css());
      break;

    case "markdown":
      const {markdown} = CM["@codemirror/lang-markdown"];
      extensions.push(markdown());
      break;

    default:
      const {javascript} = CM["@codemirror/lang-javascript"];
      extensions.push(javascript());
      break;
  }

  if (showLineNumbers)
  {
    extensions.push(lineNumbers());
    extensions.push(foldGutter());
  }

  if (completion)
  {
    extensions.push(autocompletion({override: [completion]}));
  }

  let editorState = EditorState.create(
          {
            doc: inputElem.value,
            extensions: extensions
          });
          
  editorView.setState(editorState);
}

window.codemirrorInit = codemirrorInit;
