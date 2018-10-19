package com.aem.demo.core.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

public class PageListItemImpl implements ListItem {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageListItemImpl.class);

  protected SlingHttpServletRequest request;
  protected Page page;
  protected Resource pageContentResource;
  private java.util.List childListItems;

  public PageListItemImpl(SlingHttpServletRequest request,  Page page) {
    this.request = request;
    this.page = page;
    this.pageContentResource = page.getContentResource();
    this.childListItems = new ArrayList();

    Page redirectTarget = getRedirectTarget(page);
    if (redirectTarget != null && !redirectTarget.equals(page)) {
      this.page = redirectTarget;
    }
  }

  @Override
  public String getURL() {
    String vanityURL = page.getVanityUrl();
    return StringUtils.isEmpty(vanityURL) ? request.getContextPath() + page.getPath() + ".html" : request.getContextPath() + vanityURL;
  }

  @Override
  public String getTitle() {
    String title = page.getNavigationTitle();
    if (title == null) {
      title = page.getPageTitle();
    }
    if (title == null) {
      title = page.getTitle();
    }
    if (title == null) {
      title = page.getName();
    }
    return title;
  }

  @Override
  public String getDescription() {
    return page.getDescription();
  }

  @Override
  public Calendar getLastModified() {
    return page.getLastModified();
  }

  @Override
  public String getPath() {
    return page.getPath();
  }

  @Override
  public ValueMap getProperties() {
    return page.getProperties();
  }

  @Override
  public Resource getItemResource() {
    return pageContentResource;
  }

  @Override
  public String getPageImage() {
    if (pageContentResource.hasChildren()) {
      Resource imageRes = pageContentResource.getChild("root/hero_image");
      if (Objects.nonNull(imageRes)) {
        return imageRes.getValueMap().get("fileReference", String.class);
      }
    }
    return "";
  }

  public void setChildListItems(java.util.List childListItems) {
    this.childListItems = childListItems;
  }

  @Override
  public java.util.List<ListItem> getChildPageList() {
    return this.childListItems;
  }

  private Page getRedirectTarget(Page page) {
    Page result = page;
    String redirectTarget;
    PageManager pageManager = page.getPageManager();
    Set<String> redirectCandidates = new LinkedHashSet<>();
    redirectCandidates.add(page.getPath());
    while (result != null && StringUtils.isNotEmpty((redirectTarget = result.getProperties().get("cq:redirectTarget", String.class)))) {
      result = pageManager.getPage(redirectTarget);
      if (result != null) {
        if (!redirectCandidates.add(result.getPath())) {
          LOGGER.warn("Detected redirect loop for the following pages: {}.", redirectCandidates.toString());
          break;
        }
      }
    }
    return result;
  }

}