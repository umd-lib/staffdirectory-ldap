package edu.umd.lib.staffdir.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communications with the LDAP server.
 */
public class Ldap {
  public static final Logger log = LoggerFactory.getLogger(Ldap.class);

  private String ldapUrl;
  private String authentication;
  private String bindDn;
  private String credentials;
  private String searchBaseDn;

  // Specifies the LDAP attributes to return
  private static final String[] LDAP_ATTRIBUTES = {
      "uid", "sn", "givenName", "telephoneNumber", "mail",
      "umOfficialTitle", "umDisplayTitle", "umPrimaryCampusRoom", "umPrimaryCampusBuilding",
      "umCatStatus", "umOptionalTitle"
  };

  public Ldap(String ldapUrl, String authentication, String bindDn, String credentials,
      String searchBaseDn) {
    this.ldapUrl = ldapUrl;
    this.authentication = authentication;
    this.bindDn = bindDn;
    this.credentials = credentials;
    this.searchBaseDn = searchBaseDn;
  }

  /**
   * Returns a list of Strings, which represent LDAP filter queries for multiple
   * uids.
   * <p>
   * This method is intended as an optimization, so that fewer LDAP queries are
   * performed.
   *
   * @param uids
   *          the List of all uids to query
   * @param batchSize
   *          the number of uids to include in each LDAP query
   * @return a List of LDAP filter query strings, containing up to the given
   *         batchSize number of uids.
   */
  protected static List<String> getQueryBatches(Set<String> uids, int batchSize) {
    List<String> queryBatches = new ArrayList<>();

    if (uids == null) {
      return queryBatches;
    }

    Iterator<String> uidIter = uids.iterator();
    int currentSize = 0;

    while (uidIter.hasNext()) {
      StringBuilder sb = new StringBuilder("(|");
      while (uidIter.hasNext() && (currentSize < batchSize)) {
        String uid = uidIter.next();
        sb.append("(uid=" + uid + ")");
        currentSize++;
      }
      sb.append(")");
      queryBatches.add(sb.toString());
      currentSize = 0;
    }
    return queryBatches;
  }

  /**
   * Returns a Map, keyed by uid, where each value is a Map containing the LDAP
   * attributes for that person (keyed by LDAP attribute).
   * <p>
   * Note: If a uid is not found in LDAP, the returned Map will not contain an
   * entry for that person.
   *
   * @param uids
   *          the Set of LDAP uids to query
   * @return a Map, keyed by uid, where each value is a Map containing the LDAP
   *         attributes for that person (keyed by LDAP attribute).
   */
  public Map<String, Map<String, String>> getUsers(Set<String> uids) {
    Map<String, Map<String, String>> results = new HashMap<>();
    Hashtable<String, Object> env = createLdapContext();
    int batchSize = 50;
    List<String> queryBatches = getQueryBatches(uids, batchSize);
    for (String uidBatchQuery : queryBatches) {
      List<SearchResult> searchResults = performSearch(env, this.searchBaseDn, uidBatchQuery);
      for (SearchResult searchResult : searchResults) {
        Map<String, String> m = asMap(searchResult);
        String uid = m.get("uid");
        results.put(uid, m);
      }
    }
    return results;
  }

  /**
   * Returns a Hashtable representing the LDAP environment context
   *
   * @param ldapUrl
   *          the LDAP URL
   * @param authentication
   *          the LDAP authentication mechanism
   * @param bindDn
   *          the DN to use for authentication
   * @param credentials
   *          the password associated with the bind DN
   * @return a Hashtable representing the LDAP environment context
   */
  private Hashtable<String, Object> createLdapContext() {
    // Set up the environment for creating the initial context
    Hashtable<String, Object> env = new Hashtable<String, Object>(11);
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, this.ldapUrl);
    env.put(Context.SECURITY_AUTHENTICATION, this.authentication);
    env.put(Context.SECURITY_PRINCIPAL, this.bindDn);
    env.put(Context.SECURITY_CREDENTIALS, this.credentials);
    return env;
  }

  /**
   * Performs the LDAP search, returning a (possible empty) List of
   * SearchResults.
   *
   * @param env
   *          the Hashtable representing the LDAP environment context
   * @param searchBaseDn
   *          the base DN at which to start the search.
   * @param uid
   *          the uid for the user to return
   * @return a (posssibly empty) List of SearchResults for the given uidQuery.
   */
  private static List<SearchResult> performSearch(Hashtable<String, Object> env, String searchBaseDn, String uidQuery) {
    List<SearchResult> searchResults = new ArrayList<>();

    try {
      // Create the initial context
      InitialLdapContext ctx = new InitialLdapContext(env, null);

      Name name = new LdapName(searchBaseDn);
      SearchControls searchControls = new SearchControls();
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      String[] returningAttributes = LDAP_ATTRIBUTES;
      searchControls.setReturningAttributes(returningAttributes);

      // Perform lookup and cast to target type
      String query = uidQuery;
      NamingEnumeration<SearchResult> results = ctx.search(name, query, searchControls);
      while ((results != null) && results.hasMore()) {
        SearchResult sr = results.next();
        searchResults.add(sr);
      }

      // Close the context when we're done
      ctx.close();
    } catch (NamingException e) {
      log.error("ERROR: Lookup failed.", e);
    }
    return searchResults;
  }

  /**
   * Converts the given SearchResult into a Map, keyed by the LDAP attribute
   * name, containing the LDAP attribute value.
   *
   * @param searchResults
   *          the List of SearchResults to convert
   * @return a Map, keyed by the LDAP attribute name, containing the LDAP
   *         attribute value.
   */
  private static Map<String, String> asMap(SearchResult searchResult) {
    return asMap(searchResult.getAttributes());
  }

  /**
   * Converts the given Attributes into a Map, keyed by the LDAP attribute name,
   * containing the LDAP attribute value.
   *
   * @param attrs
   *          the Attributes to use in creating the person Map.
   * @return a Map, keyed by the LDAP attribute name, containing the LDAP
   *         attribute value.
   */
  private static Map<String, String> asMap(Attributes attrs) {
    Map<String, String> result = new HashMap<>();
    try {
      for (String key : LDAP_ATTRIBUTES) {
        result.put(key, getAttrValue(attrs.get(key)));
      }

    } catch (NamingException ne) {
      log.error("ERROR: Error processing LDAP parameters", ne);
      return result;
    }

    return result;
  }

  /**
   * Returns a String representing the value of the given attribute.
   *
   * @param attr
   *          the attribute to retrieve the value of
   * @return a String representing the value of the given attribute.
   * @throws NamingException
   *           if a naming exception occurs when retrieving the values.
   */
  private static String getAttrValue(Attribute attr) throws NamingException {
    if (attr == null) {
      return null;
    }

    Object obj = attr.get();
    if (obj == null) {
      return null;
    }
    return obj.toString();
  }
}
