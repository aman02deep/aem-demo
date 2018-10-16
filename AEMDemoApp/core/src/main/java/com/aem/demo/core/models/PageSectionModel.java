package com.aem.demo.core.models;


import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Model(adaptables = SlingHttpServletRequest.class)
public class PageSectionModel {

  @Inject @Via("resource")
  @Optional
  private Image image;

  @ScriptVariable
  private ValueMap properties;

  private boolean hasBackgroundImage;
  private boolean hasBackgroundVideo;
  private String ratio;

  @PostConstruct public void init() {
    hasBackgroundVideo = properties.get("backgroundVideoType", String.class) != null
        && properties.get("id", String.class) != null;

    hasBackgroundImage = image != null && isNotEmpty(image.getFileReference());

    initializeRatio();
  }

  public boolean getHasBackgroundImage() {
    return hasBackgroundImage;
  }

  public boolean getHasBackgroundVideo() {
    return hasBackgroundVideo;
  }

  public boolean getHasBackgroundMedia() {
    return hasBackgroundVideo || hasBackgroundImage;
  }

  public String getRatio() {
    return ratio;
  }

  private void initializeRatio() {
    String propertyRatio = properties.get("ratio", String.class);

    if (isNotEmpty(propertyRatio)) {
      ratio = propertyRatio;
    } else if (getHasBackgroundMedia()) {
      ratio = "media";
    } else {
      ratio = "content";
    }
  }
}