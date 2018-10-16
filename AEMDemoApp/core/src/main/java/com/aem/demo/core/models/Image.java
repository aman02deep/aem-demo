package com.aem.demo.core.models;

import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
public class Image {

  @Inject
  private String fileReference;

  public String getFileReference() {
    return fileReference;
  }
}