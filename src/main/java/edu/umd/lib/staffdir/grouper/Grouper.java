package edu.umd.lib.staffdir.grouper;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;

/**
 * Thin wrapper around the Internet2 GrouperClient library, for defining the
 * necessary configuration and providing simplified access to some basic
 * commands.
 */
public class Grouper {
  /**
   * Configuration for accessing the Grouper web services endpoint.
   */
  public static class Config {
    public final String wsEndpoint;
    public final String wsUser;
    public final String wsPassword;

    /**
     * Constructor for the Grouper configuration
     *
     * @param wsEndpoint the full URL to the Grouper web services endpoint
     * @param wsUser the Grouper web services username
     * @param wsPassword the Grouper web services password
     */
    public Config(String wsEndpoint, String wsUser, String wsPassword) {
      this.wsEndpoint = wsEndpoint;
      this.wsUser = wsUser;
      this.wsPassword = wsPassword;
    }
  }

  /**
   * Returns the WsGetMembershipsResults of members for the given Grouper group
   *
   * @param config the Grouper.Config configuration for the Grouper endpoint
   * @param groupName the Grouper name to retrieve the members of, for example,
   * "Departmental_Groups:Libraries:Employees:Libraries-Staff-Exempt"
   * @return the WsGetMembershipsResults for the given Grouper group name
   */
  public static WsGetMembershipsResults getMemberships(Grouper.Config config, String groupName) {
    GcGetMemberships getMemberships = new GcGetMemberships();
    getMemberships.assignWsEndpoint(config.wsEndpoint)
                  .assignWsUser(config.wsUser)
                  .assignWsPass(config.wsPassword)
                  .assignIncludeSubjectDetail(true)
                  .addGroupName(groupName);
    return getMemberships.execute();
  }

  /**
   * Adds one or more members to a Grouper group
   *
   * @param config the Grouper.Config configuration for the Grouper endpoint
   * @param groupName the Grouper name to add members to, for example,
   * "Departmental_Groups:Libraries:Employees:Libraries-Staff-Exempt"
   * @param subjectIds an array of one or more "subjectIds" to add to the
   * Grouper group. A subjectId is equivalent to the LDAP "employeeNumber" field
   * @return the WsAddMemberResults indicating the success/failure of adding
   * members to the group
   */
  public static WsAddMemberResults addMembers(Grouper.Config config, String groupName, String[] subjectIds) {
    GcAddMember addMember = new GcAddMember();
    addMember.assignWsEndpoint(config.wsEndpoint)
            .assignWsUser(config.wsUser)
            .assignWsPass(config.wsPassword)
            .assignGroupName(groupName);

    for(String subjectId: subjectIds) {
      addMember.addSubjectId(subjectId);
    }

    return addMember.execute();
  }

  /**
   * Deletes one or more members from a Grouper group
   *
   * @param config the Grouper.Config configuration for the Grouper endpoint
   * @param groupName the Grouper name to delete members from, for example,
   * "Departmental_Groups:Libraries:Employees:Libraries-Staff-Exempt"
   * @param subjectIds an array of one or more "subjectIds" to delete from the
   * Grouper group. A subjectId is equivalent to the LDAP "employeeNumber" field
   * @return the WsDeleteMemberResults indicating the success/failure of
   * deleting members from the group
   */
  public static WsDeleteMemberResults deleteMembers(Grouper.Config config, String groupName, String[] subjectIds) {
    GcDeleteMember deleteMember = new GcDeleteMember();
    deleteMember.assignWsEndpoint(config.wsEndpoint)
            .assignWsUser(config.wsUser)
            .assignWsPass(config.wsPassword)
            .assignGroupName(groupName);

    for(String subjectId: subjectIds) {
      deleteMember.addSubjectId(subjectId);
    }

    return deleteMember.execute();
  }
}
