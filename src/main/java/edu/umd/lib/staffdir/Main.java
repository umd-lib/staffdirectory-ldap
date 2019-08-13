package edu.umd.lib.staffdir;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import edu.umd.lib.staffdir.excel.ExcelGenerator;
import edu.umd.lib.staffdir.ldap.Ldap;

public class Main {
  public static void main(String[] args) {
    Properties props = null;
    try {
      FileInputStream propFile = new FileInputStream("ldap.properties");
      props = new Properties(System.getProperties());
      props.load(propFile);
    } catch (IOException ioe) {
      System.out.println(ioe);
      System.exit(1);
    }

    String ldapUrl = props.getProperty("ldap.url");
    String authentication = props.getProperty("ldap.authentication");
    String bindDn = props.getProperty("ldap.bindDn");
    String credentials = props.getProperty("ldap.credentials");
    String searchBaseDn = props.getProperty("ldap.searchBaseDn");

    List<Person> persons = Ldap.ldapSearch(ldapUrl, authentication, bindDn, credentials, searchBaseDn);

    // Sort the persons by last name
    Collections.sort(persons, (Person p1, Person p2) -> p1.getLastName().compareTo(p2.getLastName()));

    ExcelGenerator.generate("/Users/dsteelma/Desktop/test.xlsx", persons, "abcd");
  }
}
