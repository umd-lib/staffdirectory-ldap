package edu.umd.lib.staffdir.ldap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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

import edu.umd.lib.staffdir.Person;
import edu.umd.lib.staffdir.PersonBuilder;

public class Ldap {
  public static final Logger log = LoggerFactory.getLogger(Ldap.class);

  // Specifies the LDAP attributes to return. Need to create a specific
  // list, as "memberOf" isn't returned, unless specifically requested.
  private static final String[] LDAP_ATTRIBUTES = {
      "uid", "sn", "givenName", "telephoneNumber", "mail",
      "umOfficialTitle", "umDisplayTitle", "umPrimaryCampusRoom", "umPrimaryCampusBuilding",
      "umCatStatus", "umOptionalTitle", "memberOf"
  };

  /**
   * Queries LDAP and returns a list of Persons matching the search.
   *
   * @param ldapUrl
   *          the LDAP URL
   * @param authentication
   *          the LDAP authentication mechanism
   * @param bindDn
   *          the DN to use for authentication
   * @param credentials
   *          the password associated with the bind DN
   * @param searchBaseDn
   *          the base DN at which to start the search.
   * @return a a list of Persons matching the search.
   */
  public static List<Person> ldapSearch(String ldapUrl, String authentication, String bindDn, String credentials,
      String searchBaseDn) {
    // Set up the environment for creating the initial context
    Hashtable<String, Object> env = createLdapContext(ldapUrl, authentication, bindDn, credentials);

    List<SearchResult> searchResults = performSearch(env, searchBaseDn);
    List<Person> persons = getPersons(searchResults);

    return persons;
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
  private static Hashtable<String, Object> createLdapContext(String ldapUrl, String authentication, String bindDn,
      String credentials) {
    // Set up the environment for creating the initial context
    Hashtable<String, Object> env = new Hashtable<String, Object>(11);
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, ldapUrl);
    env.put(Context.SECURITY_AUTHENTICATION, authentication);
    env.put(Context.SECURITY_PRINCIPAL, bindDn);
    env.put(Context.SECURITY_CREDENTIALS, credentials);
    return env;
  }

  /**
   * Performs the LDAP search, returning a list of SearchResults matching the
   * search.
   *
   * @param env
   *          the Hashtable representing the LDAP environment context
   * @param searchBaseDn
   *          the base DN at which to start the search.
   * @return a list of SearchResults matching the search.
   */
  private static List<SearchResult> performSearch(Hashtable<String, Object> env, String searchBaseDn) {
    List<SearchResult> searchResults = new ArrayList<>();
    try {
      // Create the initial context
      InitialLdapContext ctx = new InitialLdapContext(env, null);

      Name name = new LdapName(searchBaseDn);
      SearchControls searchControls = new SearchControls();
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      String[] returningAttributes = LDAP_ATTRIBUTES;
      searchControls.setReturningAttributes(returningAttributes);
      String initialLetters = "abcdefghijklmnopqrstuvwxyz";

      // Perform lookup and cast to target type

      for (int i = 0; i < initialLetters.length(); i++) {
        String initialLetter = initialLetters.substring(i, i + 1);
        String query = String.format(
            "(&(ou=LIBR-Libraries)(uid=%s*)(umHourlyStudentEmployee=FALSE))",
            initialLetter);
        NamingEnumeration<SearchResult> results = ctx.search(name, query, searchControls);

        while ((results != null) && results.hasMore()) {
          SearchResult sr = results.next();
          searchResults.add(sr);
        }
      }
      // Close the context when we're done
      ctx.close();
    } catch (NamingException e) {
      log.error("Lookup failed.", e);
    }
    return searchResults;
  }

  /**
   * Returns a List of Persons, converted from the given List of SearchResults
   *
   * @param searchResults
   *          the List of SearchResults to convert
   * @return a List of Persons, converted from the given List of SearchResults
   */
  private static List<Person> getPersons(List<SearchResult> searchResults) {
    List<Person> persons = new ArrayList<>();
    for (SearchResult sr : searchResults) {
      Person p = createPerson(sr.getAttributes());
      if (p != null) {
        persons.add(p);
      }
    }

    return persons;
  }

  /**
   * Returns a Person, created from the given list of Attributes
   *
   * @param attrs
   *          the Attributes to use in creating the Person.
   * @return a Person, created from the given list of Attributes
   */
  private static Person createPerson(Attributes attrs) {
    PersonBuilder pb = null;
    try {
      pb = new PersonBuilder();

      Attribute memberOf = attrs.get("memberOf");
      List<String> memberships = getMemberships(memberOf);
      MembershipInfo membershipInfo = new MembershipInfo(memberships);

      pb.uid(getAttrValue(attrs.get("uid")))
          .lastName(getAttrValue(attrs.get("sn")))
          .firstName(getAttrValue(attrs.get("givenName")))
          .phoneNumber(getAttrValue(attrs.get("telephoneNumber")))
          .email(getAttrValue(attrs.get("mail")))
          .officialTitle(getAttrValue(attrs.get("umOfficialTitle")))
          .jobTitle(getAttrValue(attrs.get("umDisplayTitle")))
          .roomNumber(getAttrValue(attrs.get("umPrimaryCampusRoom")))
          .building(getAttrValue(attrs.get("umPrimaryCampusBuilding")))
          .division(membershipInfo.getDivision())
          .department(membershipInfo.getDepartment())
          .unit(membershipInfo.getUnit())
          .fte(membershipInfo.getFte())
          .categoryStatus(getAttrValue(attrs.get("umCatStatus")))
          .facultyPermanentStatus(membershipInfo.isFacultyPermanentStatus())
          .descriptiveTitle(getAttrValue(attrs.get("umOptionalTitle")))
          .costCenter(membershipInfo.getCostCenter());
    } catch (NamingException ne) {
      log.error("Error processing LDAP parameters", ne);
      return null;
    }

    return pb.getPerson();
  }

  /**
   * Returns a List of String representing the "memberOf" values from the given
   * attribute.
   *
   * @param memberOfAttr
   *          the "memberOf" attribute to retrieve the values of.
   * @return a List of String representing the "memberOf" values from the given
   *         attribute.
   * @throws NamingException
   *           if a naming exception occurs when retrieving the values.
   */
  private static List<String> getMemberships(Attribute memberOfAttr) throws NamingException {
    List<String> memberships = new ArrayList<>();

    if (memberOfAttr != null) {
      NamingEnumeration<?> ne = memberOfAttr.getAll();
      while (ne.hasMore()) {
        Object obj = ne.next();
        String str = obj.toString();
        memberships.add(str);
      }
    }
    return memberships;
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
