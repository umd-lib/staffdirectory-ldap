package edu.umd.lib.staffdir.ldap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

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
  
  public static void main(String[] args) {
    Properties props = null;
    try {
      FileInputStream propFile =
          new FileInputStream( "ldap.properties");
      props = new Properties(System.getProperties());
      props.load(propFile);
    } catch(IOException ioe) {
      System.out.println(ioe);
      System.exit(1);
    }
    
    String ldapUrl = props.getProperty("ldap.url");
    String authentication = props.getProperty("ldap.authentication");
    String bindDn = props.getProperty("ldap.bindDn");
    String credentials = props.getProperty("ldap.credentials");
    String searchBaseDn = props.getProperty("ldap.searchBaseDn");
    
    List<SearchResult> searchResults = ldapSearch(ldapUrl, authentication, bindDn, credentials, searchBaseDn);
    List<Person> persons = getPersons(searchResults);
        
    // Sort the persons by last name
    Collections.sort(persons, (Person p1, Person p2)-> p1.getLastName().compareTo(p2.getLastName()));
    for(Person p : persons) {
      System.out.println(p);
    }

    System.out.println("persons = " + persons.size());

  }
  
  public static List<SearchResult> ldapSearch(String ldapUrl, String authentication, String bindDn, String credentials, String searchBaseDn) {
    // Set up the environment for creating the initial context
    Hashtable<String, Object> env = new Hashtable<String, Object>(11);
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, ldapUrl);
    env.put(Context.SECURITY_AUTHENTICATION, authentication);
    env.put(Context.SECURITY_PRINCIPAL, bindDn);
    env.put(Context.SECURITY_CREDENTIALS, credentials);

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
      
      for(int i = 0; i < initialLetters.length(); i++) {
        String initialLetter = initialLetters.substring(i,i+1);
        String query = "(&(ou=LIBR-Libraries)(uid=" + initialLetter + "*))";
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
    
    
  public static List<Person> getPersons(List<SearchResult> searchResults) {
    List<Person> persons = new ArrayList<>();
    for(SearchResult sr : searchResults) {
      Person p = createPerson(sr.getAttributes());
      if (p != null) {
        persons.add(p);
      }
    }
        
    return persons;
  }
  
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
        .location((String) "TODO")
        .fte((String) "TODO")
        .categoryStatuses(Arrays.asList("TODO"))
        .facultyPermanentStatus(false) // TODO
        .descriptiveTitle(getAttrValue(attrs.get("umOptionalTitle")))
        .costCenter(membershipInfo.getCostCenter());
    } catch(NamingException ne) {
      log.error("Error processing LDAP parameters", ne);
      return null;
    }
    
    return pb.getPerson();
  }
  
  private static List<String> getMemberships(Attribute memberOfAttr) throws NamingException {
    List<String> memberships = new ArrayList<>();
    
    if (memberOfAttr != null) {
      NamingEnumeration ne = memberOfAttr.getAll();
      while(ne.hasMore()) {
        Object obj = ne.next();
        String str = obj.toString();
        memberships.add(str);
      }
    }
    return memberships;
  }
  
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
