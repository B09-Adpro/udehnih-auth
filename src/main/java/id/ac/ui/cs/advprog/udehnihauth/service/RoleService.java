package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;

import java.util.Set;

public interface RoleService {
    boolean addRoleToUser(Long userId, RoleType roleType, Long staffId);
    boolean userHasRole(Long userId, RoleType roleType);
    Set<RoleType> getUserRoles(Long userId);
    boolean removeRoleFromUser(Long userId, RoleType roleType);
}