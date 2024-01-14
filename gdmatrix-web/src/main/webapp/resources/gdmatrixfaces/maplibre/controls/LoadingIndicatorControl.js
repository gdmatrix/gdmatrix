/* LoadingIndicatorControl.js */

class LoadingIndicatorControl
{
  constructor()
  {
    this.tileKeys = new Set();
    this.errorCount = 0;
  }

  onLoadingStarted(event)
  {
    let key = event?.tile?.tileID?.canonical?.key;
    if (key)
    {
      this.tileKeys.add(key);
      this.updateProgress();
    }
  }

  onLoadingCompleted(event)
  {
    let key = event?.tile?.tileID?.canonical?.key;
    if (key)
    {
      this.tileKeys.delete(key);
      this.updateProgress();
    }
  }

  onError(event)
  {
    let key = event?.tile?.tileID?.canonical?.key;
    if (key)
    {
      this.errorCount++;
      console.error(event.error.message + ": " + event.source.tiles);
      this.tileKeys.delete(key);
      this.updateProgress();
    }
  }

  updateProgress()
  {
    let pendingCount = this.tileKeys.size;
    this.div.textContent = pendingCount;
    if (pendingCount > 0)
    {
      this.div.classList.add("flash");
    }
    else
    {
      this.div.classList.remove("flash");
    }
    if (this.errorCount > 0)
    {
      this.div.style.color = "red";
    }
    else
    {
      this.div.style.color = "black";
    }
  }

  onAdd(map)
  {
    map.on("dataloading", (e) => this.onLoadingStarted(e));
    map.on("data", (e) => this.onLoadingCompleted(e));
    map.on("dataabort", (e) => this.onLoadingCompleted(e));
    map.on("error", (e) => this.onError(e));

    const div = document.createElement("div");
    this.div = div;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
    div.style.width = "29px";
    div.style.height = "29px";
    div.style.userSelect = "none";
    div.style.fontFamily = "var(--font-family)";
    div.title = "Pending tiles";

    this.updateProgress();
    return div;
  }
}

export { LoadingIndicatorControl };


