package com.aem.demo.core.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.google.gson.annotations.Expose;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListItem {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListItem.class);

  /**
   * if List item is an External link then LinkModel details will be available and properties and
   * Resource will not be available
   */
  @Expose
  private boolean isExternalLink;
  @Expose
  private String linkPath;
  @Expose
  private String title;
  @Expose
  private String targetBlank;
  @Expose
  private String path;
  @Expose
  private String url;
  @Expose
  private ValueMap properties;

  private SlingHttpServletRequest request;
  private Page page;
  private Resource pageContentResource;

  @Expose
  private List<ListItem> childListItems;

  public ListItem(SlingHttpServletRequest request, Page page) {
    this.isExternalLink = false;
    this.request = request;
    this.page = page;
    this.pageContentResource = page.getContentResource();
    this.childListItems = new ArrayList();
    this.properties = page.getProperties();
    setTitle("");
    setPath();
    setUrl();
  }

  public ListItem(SlingHttpServletRequest request, LinkModel linkModel) {
    this.request = request;
    ResourceResolver resolver = request.getResourceResolver();

    if (isValidLinkModel(linkModel, resolver)) {
      if (linkModel.isAbsoluteURL()) {
        isExternalLink = true;
        this.linkPath = linkModel.getLinkPath();
        this.title = linkModel.getLinkName();
        this.targetBlank = linkModel.getTargetBlank();
      } else {
        isExternalLink = false;
        Resource pageResource = resolver.getResource(linkModel.getLinkPath());
        this.page = pageResource.adaptTo(Page.class);
        this.pageContentResource = page.getContentResource();
        this.childListItems = new ArrayList();
        this.targetBlank = linkModel.getTargetBlank();
        this.properties = page.getProperties();
        // overriding the Title with authored title
        setTitle(linkModel.getLinkName());
      }
      setPath();
      setUrl();
    }
  }

  public static boolean isValidLinkModel(LinkModel linkModel, ResourceResolver resolver) {
    return Objects.nonNull(linkModel) &&
        (linkModel.isAbsoluteURL() || Objects.nonNull(resolver.getResource(linkModel.getLinkPath())));
  }

  public void setTitle(String title) {
      Optional<String> authoredTitle = Optional.ofNullable(title).filter(s -> !s.isEmpty());
      Optional<String> navTitle = Optional.ofNullable(page.getNavigationTitle());
      Optional<String> pageTitle = Optional.ofNullable(page.getPageTitle());
      Optional<String> pgTitle = Optional.ofNullable(page.getTitle());

      this.title = authoredTitle.orElseGet(() -> navTitle.orElseGet(() -> pageTitle
          .orElseGet(() -> pgTitle.orElseGet(() -> page.getName()))));
  }

  public void setPath() {
    if (this.isExternalLink) {
      this.path = this.linkPath;
    } else {
      this.path = page.getPath();
    }
  }

  public void setUrl() {
    if (this.isExternalLink) {
      this.url = this.linkPath;
    } else {
      String vanityURL = page.getVanityUrl();
      this.url = StringUtils.isEmpty(vanityURL)
          ? request.getContextPath() + page.getPath() + Constants.HTML_SUFFIX
          : request.getContextPath() + vanityURL;
    }
  }

  public String getTitle() {
    return title;
  }

  public String getPath() {
    return this.path;
  }

  public String getUrl() {
    return url;
  }

  public String getTargetBlank() {
    return targetBlank;
  }

  public ValueMap getProperties() {
    return this.properties;
  }

  public ValueMap getItemResourceProperties() {
    return Optional.ofNullable(pageContentResource.getValueMap()).orElse(ValueMap.EMPTY);
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
    return properties.get(NameConstants.PN_PAGE_LAST_MOD, Calendar.class);
  }
}