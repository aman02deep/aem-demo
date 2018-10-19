package com.aem.demo.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

public interface ListItem extends com.adobe.cq.wcm.core.components.models.ListItem {

  default ValueMap getProperties() {
    throw new UnsupportedOperationException();
  }

  default Resource getItemResource() {
    throw new UnsupportedOperationException();
  }

  default String getPageImage() {
    throw new UnsupportedOperationException();
  }

}
