/* Locator.js */

import { Search } from "./Search.js";

class Locator // abstract class
{
  constructor(params)
  {
  }

  getTitle()
  {
    return "title";
  }

  onCreate(map)
  {
    // create form fields into elem
  }
}

function init(map)
{
  if (map.searchControl)
  {
    map.searchControl.addLocator(new Locator());
  }
}

export { Locator };