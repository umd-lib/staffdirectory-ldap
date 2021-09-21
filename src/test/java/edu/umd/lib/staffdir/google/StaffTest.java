package edu.umd.lib.staffdir.google;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class StaffTest {
  @Test
  public void testWhitespaceTrimmedFromRawCells() {
    Map<String, String> entry1 = new HashMap<>();
    entry1.put("uid", "noTrimmingNeeded");
    entry1.put("Cost Center", "12345");

    Map<String, String> entry2 = new HashMap<>();
    entry2.put("uid", " spaceOnLeft");
    entry2.put("Cost Center", " 45678");

    Map<String, String> entry3 = new HashMap<>();
    entry3.put("uid", "spaceOnRight ");
    entry3.put("Cost Center", "98765 ");

    List<Map<String, String>> rawEntries = new ArrayList<>();
    rawEntries.add(entry1);
    rawEntries.add(entry2);
    rawEntries.add(entry3);

    Staff staff = new Staff(rawEntries, "uid");

    Set<String> uids = staff.getUids();
    // Uids should not have spaces
    assertThat(uids, CoreMatchers.hasItems("noTrimmingNeeded", "spaceOnLeft", "spaceOnRight"));

    // Values should not have spaces
    assertEquals("12345", staff.get("noTrimmingNeeded").get("Cost Center"));
    assertEquals("45678", staff.get("spaceOnLeft").get("Cost Center"));
    assertEquals("98765", staff.get("spaceOnRight").get("Cost Center"));
  }
}
