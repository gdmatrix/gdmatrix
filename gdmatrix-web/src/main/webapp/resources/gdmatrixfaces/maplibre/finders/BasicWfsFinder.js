/* BasicWfsFinder.js */

import { WfsFinder } from "./WfsFinder.js";
import { Bundle } from "../i18n/Bundle.js";

class BasicWfsFinder extends WfsFinder
{
  constructor(options)
  {
    super(options);

    this.fields = options.fields || [
      // { 
      //   id: "id", 
      //   type: "number|text|date",
      //   value: "value", 
      //   options: [value1, value2, ..., valueN],
      //   name: "name", // layer field name
      //   operator: "=|>|<|>=|<=|<>|LIKE|ILIKE",
      //   wildcard: true|false,
      //   condition: fn(value)
      // } 
    ];
    this.bundle = new Bundle(this.name, options.translations);
  }
  
  getTitle()
  {
    return this.bundle.get(this.name);
  }
  
  populateForm(element)
  {
    element.innerHTML = `<div class="formgrid grid"></div>`;
    const grid = element.firstChild;
    const fields = this.fields;
    
    for (let i = 0; i < fields.length; i++)
    {
      let field = fields[i];
      let id = field.id || "f" + i;
      let label = this.bundle.get(id);
      let value = field.value || "";
      if (typeof value === "function") value = value();

      if (field.options)
      {
        this.createSelectField(grid, id, label, value, field.options);
      }
      else
      {
        let type = field.type || "text";
        switch (type)
        {
          case "text":
            this.createTextField(grid, id, label, value);
            break;
          case "number":
            this.createNumberField(grid, id, label, value);
            break;
          case "date":
            this.createDateField(grid, id, label, value);
            break;
        }
      }
    }
  }
  
  createSelectField(grid, id, label, value, options)
  {
    const div = document.createElement("div");
    div.className = "field col-12";
    div.innerHTML = `
      <label for="bf_${id}">${label}:</label>
      <select id="bf_${id}" class="w-full"></select>
    `;
    const select = div.firstElementChild.nextElementSibling;
    for (let option of options)
    {
      let optionElement = document.createElement("option");
      optionElement.value = String(option);
      let label = this.bundle.get(id + "." + option);
      if (label === id + "." + option) label = option;
      optionElement.textContent = label;
      select.appendChild(optionElement);
    }
    if (value === "" && options.length > 0)
    {
      value = options[0];
    }
    select.value = value;
    grid.appendChild(div);
  }

  createTextField(grid, id, label, value)
  {    
    const div = document.createElement("div");
    div.className = "field col-12";
    div.innerHTML = `
      <label for="bf_${id}">${label}:</label>
      <input id="bf_${id}" type="text" class="w-full" value="${value}" />
    `;
    grid.appendChild(div);
  }

  createNumberField(grid, id, label, value)
  {
    const div = document.createElement("div");
    div.className = "field col-12";
    div.innerHTML = `
      <label for="bf_${id}">${label}:</label>
      <input id="bf_${id}" type="number" class="code w-full" value="${value}" />
    `;
    grid.appendChild(div);
  }

  createDateField(grid, id, label, value)
  {
    const div = document.createElement("div");
    div.className = "field col-12";
    div.innerHTML = `
      <label for="bf_${id}">${label}:</label>
      <input id="bf_${id}" type="date" class="code w-full" value="${value}" />
    `;
    grid.appendChild(div);
  }

  async find()
  {
    let filterArray = [];
    let fields = this.fields;
    for (let field of fields)
    {
      let value = document.getElementById("bf_" + field.id).value;
      let condition = field.condition;
      
      if (value === null || value.length === 0)
      {
        // skip
      }
      else if (typeof condition === "function")
      {
        filterArray.push(condition(value));
      }
      else if (field.name)
      {
        if (field.type === "number")
        {
          let op = field.operator || "=";
          filterArray.push(`${field.name} ${op} ${value}`);
        }
        else if (field.type === "date")
        {
          // convert yyyy-MM-dd -> yyyyMMdd
          value = value.replaceAll('-', '');
          let op = field.operator || "=";
          filterArray.push(`${field.name} ${op} '${value}'`);
        }
        else // type == "text"
        {
          value = field.wildcard ? '%' + value + '%' : value;
          let op = field.operator || "LIKE";
          filterArray.push(`${field.name} ${op} '${value}'`);
        }
      }
    }
    let cqlFilter = filterArray.join(" AND ");
    console.info("cqlFilter: " + cqlFilter);

    return await this.findWfs(cqlFilter); 
  }
}

export { BasicWfsFinder };