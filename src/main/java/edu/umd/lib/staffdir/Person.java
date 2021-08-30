package edu.umd.lib.staffdir;

import java.util.Comparator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates information for a single person.
 */
public class Person {
  public static final Logger log = LoggerFactory.getLogger(Person.class);

  /**
   * The unique identifier of the person associated with this object.
   */
  public final String uid;

  /**
   * The Map of sources for this person.
   */
  public Map<String, Map<String, String>> sources;

  /**
   * Constructs a Person object with the given UID and Map of sources. The
   * sources Map will typically include a "Staff", and "LDAP" Map derived from
   * the Google sheets document containing information about a single person.
   *
   * @param uid
   *          the unique identifier for the person
   * @param sources
   *          a Map of Map<String, String>, keyed by a source identifier such as
   *          "Staff", or "LDAP".
   */
  @JsonCreator
  public Person(@JsonProperty("uid") String uid,
      @JsonProperty("sources") Map<String, Map<String, String>> sources) {
    if (uid == null) {
      throw new IllegalArgumentException("uid is null.");
    }

    if (sources == null) {
      throw new IllegalArgumentException("sources is null.");
    }

    this.uid = uid;
    this.sources = sources;
  }

  /**
   * Returns the value from the given source and field, or an empty String.
   *
   * @param source
   *          the key of the source Map to retrieve from the "sources" Map
   * @param field
   *          the key for the field to retrieve from the source Map
   * @return the value from the given source and field, or an empty String.
   */
  public String get(String source, String field) {
    Map<String, String> src = sources.get(source);
    if (src == null) {
      log.warn("WARNING: uid: '{}' - Source '{}' is null. Returning empty string.", uid, source);
      return "";
    }

    if (src.containsKey(field)) {
      String value = src.get(field);
      if (value == null) {
        log.warn("WARNING: uid: '{}' - Value for field '{}' in source '{}' is null. Returning empty string.", uid,
            field, source);
        return "";
      }
      return value;

    } else {
      log.warn("WARNING: uid: '{}' - Field '{}' not found in source '{}'. Returning empty string.", uid, field, source);
      return "";
    }

  }

  /**
   * Returns the value from the given source and field, or null if either the
   * source or field is not found (or if the actual value is null).
   *
   * @param source
   *          the key of the source Map to retrieve from the "sources" Map
   * @param field
   *          the key for the field to retrieve from the source Map
   * @return the value from the given source and field, or null if either the
   *         source or field is not found (or if the actual value is null).
   */
  public String getAllowNull(String source, String field) {
    Map<String, String> src = sources.get(source);
    if (src == null) {
      log.warn("WARNING: uid: '{}' - Source '{}' is null. Returning null.", uid, source);
      return null;
    }

    return src.getOrDefault(field, null);
  }

  /**
   * @return a String representation of this object.
   */
  @Override
  public String toString() {
    String str = String.format("Person@%s[uid: %s]",
        Integer.toHexString(System.identityHashCode(this)),
        uid);
    return str;
  }

  /**
   * Sorts Person objects by Last Name and First Name, based on LDAP attributes
   */
  public static class LastNameFirstNameComparator implements Comparator<Person> {
    @Override
    public int compare(Person person1, Person person2) {
      if (person1 == person2) {
        return 0;
      }

      if (person1 == null) {
        return -1;
      }

      if (person2 == null) {
        return 1;
      }

      String person1SortKey = (person1.get("LDAP", "sn") + person1.get("LDAP", "givenName")).toLowerCase();
      String person2SortKey = (person2.get("LDAP", "sn") + person2.get("LDAP", "givenName")).toLowerCase();

      return person1SortKey.compareTo(person2SortKey);
    }
  }
}
