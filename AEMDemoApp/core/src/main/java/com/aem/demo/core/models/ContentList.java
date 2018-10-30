package com.aem.demo.core.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;

import com.day.cq.commons.RangeIterator;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.Predicate;
import com.day.cq.search.SimpleSearch;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.designer.Style;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(adaptables = {SlingHttpServletRequest.class}) public class ContentList {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentList.class);

  @ScriptVariable
  private PageManager pageManager;

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;

  @Self
  private SlingHttpServletRequest request;

  @ScriptVariable
  private ValueMap properties;

  @ScriptVariable
  private Style currentStyle;

  @ScriptVariable
  private Page currentPage;

  @ValueMapValue(name = Constants.PN_SOURCE, injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = StringUtils.EMPTY)
  private String listFrom;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(intValues = Constants.LIMIT_DEFAULT)
  private int limit;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(intValues = Constants.PN_DEPTH_DEFAULT)
  private int childDepth;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = StringUtils.EMPTY)
  private String query;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(intValues = 0)
  private int maxItems;

  @ValueMapValue(name = Constants.PN_TAGS, injectionStrategy = InjectionStrategy.OPTIONAL)
  private String[] tags;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = Constants.TAGS_MATCH_ANY_VALUE)
  private String tagsMatch;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = StringUtils.EMPTY)
  private String parentPage;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = StringUtils.EMPTY)
  private String tagSearchRoot;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = StringUtils.EMPTY)
  private String searchIn;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = Constants.PN_NO_ORDER)
  private String orderBy;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = StringUtils.EMPTY)
  private String orderByKeyword;

  @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = Constants.PN_SORT_ORDER_ASC)
  private String sortOrder;

  private List<ListItem> listItems;

  @PostConstruct
  private void init() {
  }

  public List<ListItem> getListItemsHierarchical() {
    List<ListItem> listItems = populateChildListItemsHierarchical();
    listItems = sortListItems(listItems);
    listItems = setMaxItems(listItems);
    return listItems;
  }

  public List<ListItem> getListItems() {
    List<ListItem> listItems = populateListItems(Source.fromString(listFrom));
    return listItems;
  }

  public static Stream<ListItem> toFlatList(List<ListItem> collection) {
    return Stream.concat(collection.stream(),
        collection.stream().flatMap(listItem -> toFlatList(listItem.getChildListItems())));
  }

  private List<ListItem> populateListItems(Source listType) {
    List<ListItem> itemsList;
    switch (listType) {
      case STATIC:
        itemsList = populateStaticListItems();
        break;
      case CHILDREN:
        itemsList = populateChildListItemsHierarchical();
        itemsList = toFlatList(itemsList).collect(Collectors.toList());
        break;
      case TAGS:
        itemsList = populateTagListItems();
        break;
      case SEARCH:
        itemsList = populateSearchListItems();
        break;
      default:
        itemsList = new ArrayList<>();
        break;
    }
    if (!Source.STATIC.equals(listType)) {
      itemsList = sortListItems(itemsList);
      itemsList = setMaxItems(itemsList);
    }
    return itemsList;
  }

  private List<ListItem> populateStaticListItems() {
    List listItems = new ArrayList<ListItem>();
    Resource staticListRoot = resource.getChild(Constants.PN_PAGES);
    if (Objects.nonNull(staticListRoot) && staticListRoot.hasChildren()) {
      staticListRoot.getChildren().forEach(child -> {
        LinkModel item = child.adaptTo(LinkModel.class);
        listItems.add(new ListItem(request, item));
      });
    }
    return listItems;
  }

  private List<ListItem> populateChildListItemsHierarchical() {
    Page rootPage = getRootPage(parentPage);
    if (rootPage != null) {
      Iterator<Page> childIterator = rootPage.listChildren();
      return iterateChildren(rootPage.getDepth(), childIterator);
    }
    return new ArrayList<>();
  }

  private List<ListItem> iterateChildren(int startLevel, Iterator<Page> childIterator) {
    List<ListItem> items = new ArrayList<>();
    while (childIterator.hasNext()) {
      Page child = childIterator.next();
      ListItem item = new ListItem(request, child);
      if (child.getDepth() - startLevel < childDepth) {
        Iterator<Page> iterator = child.listChildren();
        List<ListItem> childList = iterateChildren(startLevel, iterator);
        item.setChildListItems(childList);
      }
      items.add(item);
    }
    return items;
  }

  private List<ListItem> populateTagListItems() {
    List<ListItem> listItems = new ArrayList<>();
    Page rootPage = getRootPage(tagSearchRoot);
    if (Objects.isNull(rootPage)) {
      return listItems;
    }

    TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
    if (ArrayUtils.isEmpty(tags)) {
      Tag[] tagsArray = tagManager.getTags(currentPage.getContentResource());
      tags = Arrays.stream(tagsArray).map(Tag::getTagID).toArray(String[]::new);
    }

    boolean matchAny = Constants.TAGS_MATCH_ANY_VALUE.equals(tagsMatch);
    RangeIterator<Resource> resourceRangeIterator = tagManager.find(rootPage.getPath(), tags, matchAny);
    if (Objects.nonNull(resourceRangeIterator)) {
      while (resourceRangeIterator.hasNext()) {
        Page containingPage = pageManager.getContainingPage(resourceRangeIterator.next());
        Optional.ofNullable(containingPage)
            .ifPresent(page -> listItems.add(new ListItem(request, containingPage)));
      }
    }
    return listItems;
  }

  private List<ListItem> populateSearchListItems() {
    List<ListItem> listItems = new ArrayList<>();
    if (StringUtils.isBlank(query)) {
      return listItems;
    }

    SimpleSearch search = resource.adaptTo(SimpleSearch.class);
    if (Objects.nonNull(search)) {
      search.setQuery(query);
      search.setSearchIn(searchIn);
      search.addPredicate(new Predicate("type", "type").set("type", NameConstants.NT_PAGE));
      search.setHitsPerPage(limit);
      try {
        listItems.addAll(collectSearchResults(search.getResult()));
      } catch (RepositoryException e) {
        LOGGER.error("Unable to retrieve search results for query.", e);
      }
    }
    return listItems;
  }

  private List<ListItem> collectSearchResults(SearchResult result) throws RepositoryException {
    List<ListItem> listItems = new ArrayList<>();
    for (Hit hit : result.getHits()) {
      Page containingPage = pageManager.getContainingPage(hit.getResource());
      if (Objects.nonNull(containingPage)) {
        listItems.add(new ListItem(request, containingPage));
      }
    }
    return listItems;
  }

  private Function getListItemSortFunction() {
    OrderBy order = OrderBy.fromString(orderBy);
    if (OrderBy.MODIFIED.equals(order)) {
      return (Function<ListItem, Calendar>) listItem1 -> {
        if (Objects.nonNull(listItem1.getLastModified())) {
          return listItem1.getLastModified();
        } else {
          return listItem1.getProperties().get(NameConstants.PN_CREATED, Calendar.class);
        }
      };
    }
    if (order.equals(OrderBy.TITLE)) {
      return (Function<ListItem, String>) ListItem::getTitle;
    }
    if (order.equals(OrderBy.OTHER)) {
      return (Function<ListItem, String>) listItem -> listItem.getItemResourceProperties().get(orderByKeyword, "");
    }
    return null;
  }

  private List<ListItem> sortListItems(List<ListItem> listItems) {
    if (StringUtils.isNotBlank(orderBy) && !OrderBy.fromString(orderBy).equals(OrderBy.NO_ORDER)) {
      Comparator<ListItem> itemComparator = Comparator.comparing(getListItemSortFunction());
      if (SortOrder.fromString(sortOrder).equals(SortOrder.ASC)) {
        listItems.sort(Comparator.nullsLast(itemComparator));
      } else {
        listItems.sort(Comparator.nullsLast(itemComparator.reversed()));
      }
    }
    return listItems;
  }

  private List<ListItem> setMaxItems(List<ListItem> listItems) {
    if (maxItems != 0 && listItems.size() > maxItems) {
      return listItems.subList(0, maxItems);
    }
    return listItems;
  }

  private Page getRootPage(String pagePath) {
    String parentPath = Optional.ofNullable(pagePath).orElseGet(() -> currentPage.getPath());
    return pageManager.getContainingPage(resourceResolver.getResource(parentPath));
  }


  protected enum Source {
    CHILDREN("children"), STATIC("static"), SEARCH("search"), TAGS("tags"), EMPTY(
        StringUtils.EMPTY);

    private String value;

    Source(String value) {
      this.value = value;
    }

    public static Source fromString(String value) {
      for (Source s : values()) {
        if (StringUtils.equals(value, s.value)) {
          return s;
        }
      }
      return EMPTY;
    }
  }

  private enum SortOrder {
    ASC("asc"), DESC("desc");

    private String value;

    SortOrder(String value) {
      this.value = value;
    }

    public static SortOrder fromString(String value) {
      for (SortOrder s : values()) {
        if (StringUtils.equals(value, s.value)) {
          return s;
        }
      }
      return ASC;
    }
  }

  private enum OrderBy {
    NO_ORDER("noOrder"), TITLE("title"), MODIFIED("modified"), OTHER("other");

    private String value;

    OrderBy(String value) {
      this.value = value;
    }

    public static OrderBy fromString(String value) {
      for (OrderBy s : values()) {
        if (StringUtils.equals(value, s.value)) {
          return s;
        }
      }
      return null;
    }
  }

}
