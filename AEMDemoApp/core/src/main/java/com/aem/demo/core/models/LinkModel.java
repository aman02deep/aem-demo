package com.aem.demo.core.models;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

@Model(adaptables = Resource.class)
public class LinkModel {

  @Inject
  @Optional
  @Default(values = StringUtils.EMPTY)
  private String linkPath;

  @Inject
  @Optional
  @Default(values = Constants.CONST_FALSE)
  private String targetBlank;

  @Inject
  @Optional
  @Default(values = StringUtils.EMPTY)
  private String linkName;

  private Resource resource;

  public LinkModel(Resource resource) {
    this.resource = resource;
  }

  @PostConstruct
  private void init() {
  }

  /**
   * Constructor for Link Model
   * @param linkName
   * @param linkPath
   * @param targetBlank true or false
   * @param appendURLParam set Null or Empty if you don't want to append any parameter to URL
   */
  public LinkModel(String linkName, String linkPath, String targetBlank, String appendURLParam) {
    this.linkName = linkName;
    this.linkPath = linkPath;
    this.targetBlank = targetBlank;

    if (StringUtils.isNotBlank(linkPath) && !isAbsoluteURL(linkPath)) {
      if (!linkPath.contains(".html") && linkPath.startsWith("/")
          && !StringUtils.endsWithAny(linkPath, "#", "/", "?")) {
        this.linkPath = StringUtils.join(linkPath, ".html");
      }
      if (StringUtils.isNotBlank(appendURLParam)) {
        this.linkPath = StringUtils.join(this.linkPath, appendURLParam);
      }
    } else {
      this.linkPath = linkPath;
    }
  }

  public String getLinkPath() {
    return linkPath;
  }

  public void setLinkPath(String linkPath) {
    this.linkPath = linkPath;
  }

  public String getTargetBlank() {
    return targetBlank;
  }

  public void setTargetBlank(String targetBlank) {
    this.targetBlank = targetBlank;
  }

  public String getLinkName() {
    return linkName;
  }

  public void setLinkName(String linkName) {
    this.linkName = linkName;
  }

  /**
   * Objects content as String
   * @return String with Object's content
   */
  @Override
  public String toString() {
    return "LinkModel{" + "linkName='" + linkName + '\'' + ", linkPath='" + linkPath + '\''
        + ", targetBlank=" + targetBlank + '}';
  }

  /**
   * this method check, if the input URL contains //, it can be a absolute URL
   * @param linkPathParam
   * @return boolean true for absolute URL
   */
  public static boolean isAbsoluteURL(String linkPathParam) {
    return StringUtils.isNotBlank(linkPathParam) && linkPathParam.contains("//");
  }

  public boolean isAbsoluteURL(){
    return isAbsoluteURL(this.linkPath);
  }
}

