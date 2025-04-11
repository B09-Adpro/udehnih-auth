package id.ac.ui.cs.advprog.udehnihauth.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTypeTest {

    @Test
    void testRoleTypeValues() {
        assertEquals("STUDENT", RoleType.STUDENT.name());
        assertEquals("TUTOR", RoleType.TUTOR.name());
        assertEquals("STAFF", RoleType.STAFF.name());
    }

    @Test
    void testRoleTypeValuesCount() {
        assertEquals(3, RoleType.values().length);
    }
}
