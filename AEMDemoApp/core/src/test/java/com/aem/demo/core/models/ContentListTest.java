package com.aem.demo.core.models;

import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.script.Bindings;
import javax.script.SimpleBindings;

import com.adobe.cq.sightly.SightlyWCMMode;
import com.adobe.cq.sightly.WCMBindings;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.search.SimpleSearch;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.designer.Style;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static com.day.cq.wcm.api.WCMMode.REQUEST_ATTRIBUTE_NAME;

public class ContentListTest {

  @Rule
  public final AemContext context = new AemContext();

  @Before
  public void init() {
    context.addModelsForPackage("com.aem.demo.core.models");
    context.load().json("/content-list/content.json", "/content/list");
    context.load().json("/content-list/tags.json", "/etc/tags/list");
  }

  /**
   * Get the slingBinding object for the context.<br>
   *
   * @param   context The context to use
   * @return  A <code>Bindings</code> object
   */
  protected static Bindings getDefaultSlingBindings(AemContext context) {
    SlingBindings slingBindings = (SlingBindings) context.request().getAttribute(SlingBindings.class.getName());
    if (slingBindings != null) {
      return new SimpleBindings(slingBindings);
    }
    return new SimpleBindings();
  }

  /**
   * Set a resource object in the context.<br>
   *
   * @param   context         The context to use
   * @param   page    The page of the resource that has to be set in the context
   */
  public static void setResource(AemContext context, String page) {
    setResource(context, page, "", WCMMode.EDIT);
  }

  /**
   * Set a resource object in the context.<br>
   *
   * @param   context         The context to use
   * @param   resourcePath    The resourcePath of the resource that has to be set in the context
   */
  public static void setResource(AemContext context, String page, String resourcePath) {
    setResource(context, page, resourcePath, WCMMode.EDIT);
  }
  /**
   * Set a resource object in the context.<br>
   *
   * @param   context         The context to use
   * @param   resourcePath    The resourcePath of the resource that has to be set in the context
   * @param   wcmmode         Set a specified wcmmode in the resource
   */
  public static void setResource(AemContext context, String page, String resourcePath, WCMMode wcmmode) {
    context.currentPage(page);
    context.request().setAttribute(REQUEST_ATTRIBUTE_NAME, wcmmode);

    Resource resource = context.currentResource(page + resourcePath);
    Bindings bindings = getDefaultSlingBindings(context);
    ValueMap properties = resource.adaptTo(ValueMap.class);
    PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
    HierarchyNodeInheritanceValueMap
        inVm = new HierarchyNodeInheritanceValueMap(context.currentPage().getContentResource());

    bindings.put(WCMBindings.WCM_MODE, new SightlyWCMMode(context.request()));
    bindings.put(WCMBindings.PROPERTIES, properties);
    bindings.put(WCMBindings.PAGE_MANAGER, pageManager);
    bindings.put(WCMBindings.PAGE_PROPERTIES, inVm);
    bindings.put(WCMBindings.CURRENT_PAGE, context.currentPage());
    bindings.put(WCMBindings.CURRENT_STYLE, Mockito.mock(Style.class));
  }

  /**
   * Test: Invalid List type
   */
  @Test
  public void testInvalidListType() {
    setResource(context, "/content/list", "/listTypes/illegalListType");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();
    Assert.assertEquals(0, itemList.size());
  }

  /**
   * Test: List of fixed number of pages
   */
  @Test
  public void testStaticList() {
    setResource(context, "/content/list", "/listTypes/staticListType");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(3, itemList.size());

    /* Page 1 details check */
    Assert.assertNotNull(itemList.get(0).getPath());
    Assert.assertEquals("Google", itemList.get(0).getTitle());
    Assert.assertEquals("true", itemList.get(0).getTargetBlank());

    /* Page 2 details check */
    Assert.assertNotNull(itemList.get(1).getPath());
    Assert.assertEquals("Authored Title", itemList.get(1).getTitle());
    Assert.assertNotNull(itemList.get(1).getProperties());

    /* Page 3 details check */
    Assert.assertNotNull(itemList.get(2).getPath());
    Assert.assertEquals("Page 2", itemList.get(2).getTitle());
    Assert.assertTrue(itemList.get(2).getChildListItems().isEmpty());
  }

  /**
   * Test: List of fixed number of pages with invalid and valid page paths
   */
  @Test
  public void testStaticListWithInvalidAndValidPagePaths() {
    setResource(context, "/content/list", "/listTypes/staticListWithInvalidPagePaths");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(2, itemList.size());
  }

  /**
   * Test: List of fixed number of pages
   */
  @Test
  public void testEmptyStaticList() {
    setResource(context, "/content/list", "/listTypes/emptyStaticListType");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(0, itemList.size());
  }

  /**
   * Test: List of child pages when root/parent page not provided in dialog
   */
  @Test
  public void testChildrenList() {
    setResource(context, "/content/list", "/listTypes/childrenListType");

    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertNotNull(itemList);
    Assert.assertEquals(3, itemList.size());
  }

  /**
   * Test: List of child pages when root/parent page not provided in dialog
   */
  @Test
  public void testChildrenListHierarchicalWithDepth() {
    String jsonResp = "[{\"isExternalLink\":false,\"title\":\"Page 1.1\",\"path\":\"/content/list/pages/page_1/page_1_1\",\"url\":\"null/content/list/pages/page_1/page_1_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.1\"},\"childListItems\":[]},{\"isExternalLink\":false,\"title\":\"Page 1.2\",\"path\":\"/content/list/pages/page_1/page_1_2\",\"url\":\"null/content/list/pages/page_1/page_1_2.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2\"},\"childListItems\":[{\"isExternalLink\":false,\"title\":\"Page 1.2.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2.1\"},\"childListItems\":[{\"isExternalLink\":false,\"title\":\"Page 1.2.1.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_1/page_1_2_1_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_1/page_1_2_1_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2.1.1\"},\"childListItems\":[]}]},{\"isExternalLink\":false,\"title\":\"B Page 1.2.2\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_2\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_2.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"B Page 1.2.2\"},\"childListItems\":[{\"isExternalLink\":false,\"title\":\"A Page 1.2.2.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_2/page_1_2_2_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_2/page_1_2_2_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"A Page 1.2.2.1\"},\"childListItems\":[]}]}]},{\"isExternalLink\":false,\"title\":\"Page 1.3\",\"path\":\"/content/list/pages/page_1/page_1_3\",\"url\":\"null/content/list/pages/page_1/page_1_3.html\",\"properties\":{\"cq:tags\":[\"list:test_category/test_tag\"],\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.3\"},\"childListItems\":[]}]";

    setResource(context, "/content/list", "/listTypes/childrenListTypeWithDepth");

    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItemsHierarchical();

    Assert.assertNotNull(itemList);
    Assert.assertEquals(3, itemList.size());
    Assert.assertEquals(2, itemList.get(1).getChildListItems().size());

    Assert.assertEquals(jsonResp, listAdapter.getListItemsHierarchicalJson());
  }

  /**
   * Test: List of child pages when root/parent page not provided in dialog
   */
  @Test
  public void testChildrenListWithDepth() {
    String jsonResp = "[{\"isExternalLink\":false,\"title\":\"Page 1.1\",\"path\":\"/content/list/pages/page_1/page_1_1\",\"url\":\"null/content/list/pages/page_1/page_1_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.1\"},\"childListItems\":[]},{\"isExternalLink\":false,\"title\":\"Page 1.2\",\"path\":\"/content/list/pages/page_1/page_1_2\",\"url\":\"null/content/list/pages/page_1/page_1_2.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2\"},\"childListItems\":[{\"isExternalLink\":false,\"title\":\"Page 1.2.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2.1\"},\"childListItems\":[{\"isExternalLink\":false,\"title\":\"Page 1.2.1.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_1/page_1_2_1_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_1/page_1_2_1_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2.1.1\"},\"childListItems\":[]}]},{\"isExternalLink\":false,\"title\":\"B Page 1.2.2\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_2\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_2.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"B Page 1.2.2\"},\"childListItems\":[{\"isExternalLink\":false,\"title\":\"A Page 1.2.2.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_2/page_1_2_2_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_2/page_1_2_2_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"A Page 1.2.2.1\"},\"childListItems\":[]}]}]},{\"isExternalLink\":false,\"title\":\"Page 1.3\",\"path\":\"/content/list/pages/page_1/page_1_3\",\"url\":\"null/content/list/pages/page_1/page_1_3.html\",\"properties\":{\"cq:tags\":[\"list:test_category/test_tag\"],\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.3\"},\"childListItems\":[]},{\"isExternalLink\":false,\"title\":\"Page 1.2.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2.1\"},\"childListItems\":[{\"isExternalLink\":false,\"title\":\"Page 1.2.1.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_1/page_1_2_1_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_1/page_1_2_1_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2.1.1\"},\"childListItems\":[]}]},{\"isExternalLink\":false,\"title\":\"B Page 1.2.2\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_2\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_2.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"B Page 1.2.2\"},\"childListItems\":[{\"isExternalLink\":false,\"title\":\"A Page 1.2.2.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_2/page_1_2_2_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_2/page_1_2_2_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"A Page 1.2.2.1\"},\"childListItems\":[]}]},{\"isExternalLink\":false,\"title\":\"Page 1.2.1.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_1/page_1_2_1_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_1/page_1_2_1_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"Page 1.2.1.1\"},\"childListItems\":[]},{\"isExternalLink\":false,\"title\":\"A Page 1.2.2.1\",\"path\":\"/content/list/pages/page_1/page_1_2/page_1_2_2/page_1_2_2_1\",\"url\":\"null/content/list/pages/page_1/page_1_2/page_1_2_2/page_1_2_2_1.html\",\"properties\":{\"jcr:primaryType\":\"cq:PageContent\",\"jcr:title\":\"A Page 1.2.2.1\"},\"childListItems\":[]}]";
    setResource(context, "/content/list", "/listTypes/childrenListTypeWithDepth");

    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertNotNull(itemList);
    Assert.assertEquals(7, itemList.size());
    Assert.assertEquals(jsonResp, listAdapter.getListItemsJson());
  }

  /**
   * Test: List of child pages when root/parent page is provided in dialog and
   * results limit is set
   */
  @Test
  public void testChildrenListWithLimit() {
    setResource(context, "/content/list", "/listTypes/childrenListTypeWithLimit");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(2, itemList.size());
  }

  /**
   * Test: List of child pages when root/parent page is provided in dialog and
   * results limit is set to more then the number of results
   */
  @Test
  public void testChildrenListWithLimit2() {
    setResource(context, "/content/list", "/listTypes/childrenListTypeWithLimitMoreThanResults");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(3, itemList.size());
  }

  /**
   * Test: List of child pages should be zero (0), when root/parent page not
   * provided in dialog
   */
  @Test
  public void testChildrenListWithoutParentPageValue() {
    setResource(context, "/content/list", "/listTypes/childrenListTypeWithoutParentPage");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(0, itemList.size());
  }

  /**
   * Test: List of child pages should be zero (0), when root/parent page is
   * incorrect in dialog
   */
  @Test
  public void testChildrenListWithIncorrectParentPageValue() {
    setResource(context, "/content/list", "/listTypes/childrenListTypeWithIncorrectParentPage");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(0, itemList.size());
  }

  /**
   * Test: List of child pages Ordered by title
   */
  @Test
  public void testChildrenListOrderByTitle() {
    setResource(context, "/content/list", "/listTypes/ChildListWithOrderByTitle");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals("A Page 1.2.2.1", itemList.get(0).getTitle());
    Assert.assertEquals("B Page 1.2.2", itemList.get(1).getTitle());
  }

  /**
   * Test: List of child pages Ordered by Modified date
   */
  @Test
  public void testChildrenListOrderByModifiedDate() {
    setResource(context, "/content/list", "/listTypes/ChildListWithOrderByModifiedDate");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertTrue(itemList.get(0).getLastModified().after(itemList.get(1).getLastModified()));
  }

  @Test
  public void testChildrenListOrderByOther() {
    setResource(context, "/content/list", "/listTypes/ChildListWithOrderByOther");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals("A Page 2 3", itemList.get(0).getTitle());
    Assert.assertEquals("Blog Page 2 2", itemList.get(1).getTitle());
  }

  /**
   * Test: Tags are selected within dialog and rootPath/Parent path is also
   * authored
   */
  @Test
  public void testListWithTag() {
    setResource(context, "/content/list", "/listTypes/tagsListType");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(2, itemList.size());
  }

  /**
   * Test: Take tags from Page properties as no tag selected in component
   */
  @Test
  public void testListWithTagsFromPageProperties() {
    setResource(context, "/content/list", "/listTypes/tagsListWithTagsFromPageProperties");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(itemList.size(), 4);
  }

  /**
   * Test: Take tags from Page properties as no tag selected in component
   */
  @Test
  public void testListTestWithoutRootPath() {
    setResource(context, "/content/list", "/listTypes/tagsListWithoutRootPath");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(0, itemList.size());
  }

  /**
   * Test: Invalid root Path
   */
  @Test
  public void testListTestWithInvalidRootPath() {
    setResource(context, "/content/list", "/listTypes/tagsListWithInvalidRootPath");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(0, itemList.size());
  }

  /**
   * Test: match all tags case
   */
  @Test
  public void testListWithTagsFromPagePropertiesWillAllTagsMatch() {
    setResource(context, "/content/list", "/listTypes/tagsListWithTagsFromPagePropertiesAllTagsMatch");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(2, itemList.size());
  }

  /**
   * Test: Tags are selected within dialog and rootPath/Parent path is also
   * authored and then tag deleted by author
   */
  @Test
  public void testListWhenTagDoesNotExist() {
    setResource(context, "/content/list", "/listTypes/tagsListWithTagThatDoesNotExist");
    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(0, itemList.size());
  }

  @Test
  public void testListWithValidSearchKeyword() throws RepositoryException {
    setResource(context, "/content/list", "/listTypes/searchListWithValidKey");
    List<Hit> hits = new ArrayList<Hit>() {{
      add(getMockedHit(context.resourceResolver().getResource("/content/list/pages/page_1"
          + "/page_1_1")));
      add(getMockedHit(context.resourceResolver().getResource("/content/list/pages/page_1"
          + "/page_1_2/page_1_2_1/page_1_2_1_1")));
    }};

    setSearchMock(hits);

    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(2, itemList.size());
  }

  @Test
  public void testListWithInValidSearchKeyword() throws RepositoryException {
    setResource(context, "/content/list", "/listTypes/searchListWithValidKey");
    setSearchMock(new ArrayList<>());

    ContentList listAdapter = context.request().adaptTo(ContentList.class);
    List<ListItem> itemList = listAdapter.getListItems();

    Assert.assertEquals(0, itemList.size());
  }

  private void setSearchMock(List<Hit> hits) throws RepositoryException{
    SimpleSearch mockSimpleSearch = Mockito.mock(SimpleSearch.class);
    SearchResult searchResult = Mockito.mock(SearchResult.class);
    Hit hit = Mockito.mock(Hit.class);

    context.registerAdapter(Resource.class, SimpleSearch.class, mockSimpleSearch);
    Mockito.when(mockSimpleSearch.getResult()).thenReturn(searchResult);
    Mockito.when(searchResult.getHits()).thenReturn(Collections.singletonList(hit));
    Mockito.when(searchResult.getHits()).thenReturn(hits);
  }

  private Hit getMockedHit(Resource resource) throws RepositoryException{
    Hit hit = Mockito.mock(Hit.class);
    Mockito.when(hit.getResource()).thenReturn(resource);
    return hit;
  }
}
