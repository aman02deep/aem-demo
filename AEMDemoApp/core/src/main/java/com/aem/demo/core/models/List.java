package com.aem.demo.core.models;

import java.util.Collection;

import com.day.cq.wcm.api.Page;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface List {
  String PN_SOURCE = "listFrom";
  String PN_PAGES = "pages";
  String PN_PARENT_PAGE = "parentPage";
  String PN_TAGS_PARENT_PAGE = "tagsSearchRoot";
  String PN_TAGS = "tags";
  String PN_TAGS_MATCH = "tagsMatch";
  String PN_SHOW_DESCRIPTION = "showDescription";
  String PN_SHOW_MODIFICATION_DATE = "showModificationDate";
  String PN_LINK_ITEMS = "linkItems";
  String PN_SEARCH_IN = "searchIn";
  String PN_SORT_ORDER = "sortOrder";
  String PN_ORDER_BY = "orderBy";
  String PN_DATE_FORMAT = "dateFormat";


  default boolean linkItems() {
    throw new UnsupportedOperationException();
  }

  default boolean showDescription() {
    throw new UnsupportedOperationException();
  }

  default boolean showModificationDate() {
    throw new UnsupportedOperationException();
  }

  default String getDateFormatString() {
    throw new UnsupportedOperationException();
  }

  default String getExportedType() {
    throw new UnsupportedOperationException();
  }

  default Collection<ListItem> getListItems() {
    throw new UnsupportedOperationException();
  }
}

