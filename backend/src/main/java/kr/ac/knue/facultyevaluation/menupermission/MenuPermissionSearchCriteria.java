package kr.ac.knue.facultyevaluation.menupermission;

public record MenuPermissionSearchCriteria(
    String targetType,
    String targetId,
    String menuId,
    Integer page,
    Integer size
) {
    public int safePage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int safeSize() {
        if (size == null) {
            return 100;
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
