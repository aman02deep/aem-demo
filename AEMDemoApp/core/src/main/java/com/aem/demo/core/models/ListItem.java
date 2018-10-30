package com.aem.demo.core.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.day.cq.wcm.api.Page;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListItem {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageListItemImpl.class);

  /**
   * if List item is an External link then LinkModel details will be available and properties and
   * Resource will not be available
   */
  private boolean isExternalLink;
  private String linkPath;
  private String title;
  private String targetBlank;

  protected SlingHttpServletRequest request;
  protected Page page;
  protected Resource pageContentResource;
  private List<ListItem> childListItems;

  public ListItem(SlingHttpServletRequest request, Page page) {
    this.isExternalLink = false;
    this.request = request;
    this.page = page;
    this.pageContentResource = page.getContentResource();
    this.childListItems = new ArrayList();
    setTitle("");
  }

  public ListItem(SlingHttpServletRequest request, LinkModel linkModel) {
    this.request = request;

    if (Objects.nonNull(linkModel)) {
      if (linkModel.isAbsoluteURL()) {

        isExternalLink = true;
        this.linkPath = linkModel.getLinkPath();
        this.title = linkModel.getLinkName();
        this.targetBlank = linkModel.getTargetBlank();

      } else {
        isExternalLink = false;

        ResourceResolver resolver = request.getResourceResolver();
        Resource pageResource = resolver.getResource(linkModel.getLinkPath());
        if (Objects.nonNull(pageResource)) {
          this.page = pageResource.adaptTo(Page.class);
          this.pageContentResource = page.getContentResource();
          this.childListItems = new ArrayList();
          this.targetBlank = linkModel.getTargetBlank();

          // overriding the Title with authored title
          setTitle(linkModel.getLinkName());
        }
      }
    }
  }

  public void setTitle(String title) {
      Optional<String> authoredTitle = Optional.ofNullable(title).filter(s -> !s.isEmpty());
      Optional<String> navTitle = Optional.ofNullable(page.getNavigationTitle());
      Optional<String> pageTitle = Optional.ofNullable(page.getPageTitle());
      Optional<String> pgTitle = Optional.ofNullable(page.getTitle());

      this.title = authoredTitle.orElseGet(() -> navTitle.orElseGet(() -> pageTitle
          .orElseGet(() -> pgTitle.orElseGet(() -> page.getName()))));
  }

  public String getTitle() {
    return title;
  }

  public String getPath() {
    if (this.isExternalLink) {
      return this.linkPath;
    } else {
      return page.getPath();
    }
  }

  public String getURL() {
    if (this.isExternalLink) {
      return this.linkPath;
    } else {
      String vanityURL = page.getVanityUrl();
      return StringUtils.isEmpty(vanityURL)
          ? request.getContextPath() + page.getPath() + Constants.HTML_SUFFIX
          : request.getContextPath() + vanityURL;
    }
  }

  public ValueMap getProperties() {
    return page.getProperties();
  }

  public ValueMap getItemResourceProperties() {
    return pageContentResource.getValueMap();
  }

  public Resource getItemResource() {
    return pageContentResource;
  }

  public void setChildListItems(List<ListItem> childListItems) {
    this.childListItems = childListItems;
  }

  public List<ListItem> getChildListItems() {
    return childListItems;
  }

  public String getDescription() {
    return page.getDescription();
  }

  public Calendar getLastModified() {
    return page.getLastModified();
  }
}