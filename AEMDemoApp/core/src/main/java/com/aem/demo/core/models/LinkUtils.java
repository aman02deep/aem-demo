package com.aem.demo.core.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.adobe.cq.social.community.api.CommunityConstants;
import com.day.cq.commons.Externalizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * The Class LinkUtils.
 */
public final class LinkUtils {

    /**
     * Instantiates a new link utils.
     */
    private LinkUtils() {
    }

    /**
     * Check a path is external link.
     *
     * @param resourceResolver the resource resolver
     * @param path the path
     * @return true/false
     */
    public static boolean isExternalLink(final ResourceResolver resourceResolver,
        final String path) {
        if (resourceResolver == null || StringUtils.isBlank(path)) {
            return true;
        }
        Resource resource = resourceResolver.getResource(path);
        return resource == null;
    }

    /**
     * Get External URL. From resource/slingrequest
     *
     * @param resourceResolver ResourceResolver
     * @param path String
     * @return String
     */
    public static String getExternalUrl(final ResourceResolver resourceResolver,
            final String path) {
        Externalizer externalizer = resourceResolver.adaptTo(Externalizer.class);
        String url = getProperURL(path, resourceResolver);
        String urlTemp = path;
        // checking for the url under content node
        if (urlTemp != null && StringUtils.isNotBlank(urlTemp)
                && urlTemp.startsWith(CommunityConstants.CONTENT_ROOT_PATH)
                && !urlTemp.startsWith(CommunityConstants.DAM_ROOT_PATH)) {
            urlTemp += Constants.HTML_SUFFIX;
        }
        if (externalizer != null) {
            url = externalizer.externalLink(resourceResolver, Externalizer.PUBLISH, urlTemp);

        }
        return url;
    }

    /**
     * Add Extension.
     *
     * @param url String
     * @param resolver ResourceResolver
     * @return String
     */
    private static String addExtension(final String url, final ResourceResolver resolver) {
        // add the code to split the URL so that .html can be added
        // at proper place and then query string will be appended
        String newURL;
        if (url.contains("?")) {
            String[] urlSplit = url.split("\\?");
            newURL = urlSplit[0] + ".html?" + urlSplit[1];
        } else if (url.contains("#")) {
            String[] urlSplit = url.split("#");
            newURL = urlSplit[0] + ".html#" + urlSplit[1];
        } else {
            newURL = url + Constants.HTML_SUFFIX;
        }
        if (StringUtils.isNotBlank(newURL)) {
            newURL = resolver.map(newURL);
        }
        return newURL;
    }

    /**
     * Get Property Url.
     *
     * @param url String
     * @param resolver ResourceResolver
     * @return String
     */
    public static String getProperURL(final String url, final ResourceResolver resolver) {
        String properURL = url;
        // checking for non blank URL or not null request and resource resolver
        if (properURL == null || StringUtils.isBlank(url) || resolver == null) {
            return properURL;
        }

        // checking for the url under content node
        if (url.startsWith(CommunityConstants.CONTENT_ROOT_PATH)
                && !url.startsWith(CommunityConstants.DAM_ROOT_PATH)) {
            // when author have added the extension and query parameters in the
            // URL
            if (url.contains(".")) {
                properURL = resolver.map(url);
            } else {
                properURL = addExtension(properURL, resolver);
            }
        }
        return properURL;
    }

    /**
     * Read the values from resource node and create list of LinkModel
     * @param linksResource
     * @return
     */
    public static List<LinkModel> getLinksFromResource(Resource linksResource) {
        List<LinkModel> linksList = new ArrayList<>();
        if (Objects.nonNull(linksResource) && linksResource.hasChildren()) {
            linksResource.getChildren().forEach(res -> {
                String linkName = res.getValueMap().get(Constants.LINK_NAME, String.class);
                String linkPath = res.getValueMap().get(Constants.LINK_PATH, String.class);
                String targetBlank = res.getValueMap().get(Constants.TARGET_BLANK, String.class);
                if (Objects.nonNull(linkName)) {
                    linksList.add(new LinkModel(linkName, linkPath, targetBlank, ""));
                }
            });
        } else {
            //LOG.debug("Links not available");
        }
        return linksList;
    }

}
