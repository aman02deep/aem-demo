package com.aem.demo.core.models;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;


/**
 * Used for building up the mata information in the header of the page.<br>
 */
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PagePropertiesModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(PagePropertiesModel.class);

  @Inject
  private Page currentPage;

  @Inject
  @Default(values = "")
  private String path;

  private String tagsName;

  @PostConstruct
  protected void init() {

    if(StringUtils.isEmpty(path)) {
      path = currentPage.getPath();
    }

    /* Tag Names in String */
    tagsName = Arrays.asList(currentPage.getTags()).stream().map(Tag::getName).collect(Collectors.joining(","));
    LOGGER.debug("tagsName {}",tagsName);
  }

  public String getTagsName() {
    return tagsName;
  }

  public String getPath() {
    return path;
  }
}