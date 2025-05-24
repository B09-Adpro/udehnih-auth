package id.ac.ui.cs.advprog.udehnihauth.repository;

import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.users WHERE r.name = :name")
    Optional<Role> findByNameWithUsers(@Param("name") RoleType name);

    @Query("SELECT COUNT(ur) FROM User u JOIN u.roles ur WHERE ur.name = :roleName")
    Long countUsersByRoleName(@Param("roleName") RoleType roleName);
}