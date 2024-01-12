/* GoHome */

class GoHomeControl
{
  constructor(homePosition)
  {
    this.homePosition = homePosition;
  }

  onAdd(map)
  {
    const div = document.createElement("div");
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `<button><span class="fa fa-home"/></button>`;
    div.title = "Initial view";
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      map.flyTo(this.homePosition);
    });

    return div;
  }
}

export { GoHomeControl };


