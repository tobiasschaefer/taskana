package pro.taskana.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

/**
 * Represents a group with a name and a set of members.
 */
public class GroupPrincipal implements Group {

    private String name;
    private Set<Principal> members;

    public GroupPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean addMember(Principal user) {
        return this.members.add(user);
    }

    @Override
    public boolean removeMember(Principal user) {
        return this.members.remove(user);
    }

    @Override
    public boolean isMember(Principal member) {
        return this.members.contains(member);
    }

    @Override
    public Enumeration<? extends Principal> members() {
        return Collections.enumeration(this.members);
    }

}
