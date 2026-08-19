package kr.ac.knue.facultyevaluation.userrole;

import java.time.LocalDate;

public record UserRoleSearchCriteria(
    String employeeNo,
    String name,
    String roleCode,
    LocalDate validOn,
    String assignmentType,
    String status,
    Integer page,
    Integer size
) {
    public int safePage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int safeSize() {
        if (size == null) {
            return 20;
        }
        return Math.min(Math.max(size, 1), 100);
    }

    public int offset() {
        return safePage() * safeSize();
    }
}
