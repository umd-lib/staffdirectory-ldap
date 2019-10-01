package edu.umd.lib.staffdir.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class LdapTest {
  @Test
  public void testGetQueryBatches() {
    // Null uids list
    List<String> batches = Ldap.getQueryBatches(null, 10);
    assertTrue(batches.isEmpty());

    // Empty uids list
    batches = Ldap.getQueryBatches(new ArrayList<String>(), 10);
    assertTrue(batches.isEmpty());

    // Batches of various sizes
    List<String> uids = Arrays.asList("uid1", "uid2", "uid3", "uid4", "uid5");

    batches = Ldap.getQueryBatches(uids, 1);
    assertEquals(5, batches.size());
    assertEquals("(|(uid=uid1))", batches.get(0));
    assertEquals("(|(uid=uid2))", batches.get(1));
    assertEquals("(|(uid=uid3))", batches.get(2));
    assertEquals("(|(uid=uid4))", batches.get(3));
    assertEquals("(|(uid=uid5))", batches.get(4));

    batches = Ldap.getQueryBatches(uids, 2);
    assertEquals(3, batches.size());
    assertEquals("(|(uid=uid1)(uid=uid2))", batches.get(0));
    assertEquals("(|(uid=uid3)(uid=uid4))", batches.get(1));
    assertEquals("(|(uid=uid5))", batches.get(2));

    batches = Ldap.getQueryBatches(uids, 3);
    assertEquals(2, batches.size());
    assertEquals("(|(uid=uid1)(uid=uid2)(uid=uid3))", batches.get(0));
    assertEquals("(|(uid=uid4)(uid=uid5))", batches.get(1));

    batches = Ldap.getQueryBatches(uids, 4);
    assertEquals(2, batches.size());
    assertEquals("(|(uid=uid1)(uid=uid2)(uid=uid3)(uid=uid4))", batches.get(0));
    assertEquals("(|(uid=uid5))", batches.get(1));

    batches = Ldap.getQueryBatches(uids, 5);
    assertEquals(1, batches.size());
    assertEquals("(|(uid=uid1)(uid=uid2)(uid=uid3)(uid=uid4)(uid=uid5))", batches.get(0));

    batches = Ldap.getQueryBatches(uids, 6);
    assertEquals(1, batches.size());
    assertEquals("(|(uid=uid1)(uid=uid2)(uid=uid3)(uid=uid4)(uid=uid5))", batches.get(0));

  }
}
