<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >


    <t:div styleClass="galleryPanel">
      <sf:imagesCarousel id="carousel" imageId="#{panel.imageIds}"
        thumbnailWidth="#{panel.thumbnailWidth}"
        thumbnailHeight="#{panel.thumbnailHeight}" thumbnailCrop="auto"
        thumbnailClickMode="#{thumbnailClickMode != null ? thumbnailClickMode : 'select'}"
        thumbnailHoverMode="#{panel.thumbnailHoverMode}"
        mainImageWidth="#{panel.imageWidth}"
        mainImageHeight="#{panel.imageHeight}" mainImageCrop="auto"
        mainImageClickMode="#{panel.mainImageClickMode != null ? mainImageClickMode : 'open'}"
        renderMainImage="#{panel.renderMainImage}"
        renderNavLinks="#{panel.renderNavLinks}"
        renderThumbnails="#{panel.renderThumbnails}"
        thumbnailShiftMode="#{panel.thumbnailShiftMode != null ? thumbnailShiftMode : 'thumbnail'}"
        thumbnailCount="#{panel.thumbnailCount}"
        thumbnailWindow="#{panel.thumbnailWindow}"
        continueTime="#{panel.continueTime}"
        transitionTime="#{panel.transitionTime}"  />
    </t:div>

</jsp:root>