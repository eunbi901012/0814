package kr.ac.knue.facultyevaluation.role;

public record RoleSearchCriteria(
    String roleCode,
    String roleName,
    String useYn,
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
        return Math.max(1, Math.min(size, 100));
    }

    public int offset() {
        return safePage() * safeSize();
    }

    public int getSafeSize() {
        return safeSize();
    }

    public int getOffset() {
        return offset();
    }
}
