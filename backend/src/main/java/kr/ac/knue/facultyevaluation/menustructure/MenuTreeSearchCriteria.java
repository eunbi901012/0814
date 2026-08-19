package kr.ac.knue.facultyevaluation.menustructure;

public record MenuTreeSearchCriteria(
    String selectedMenuId,
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
        return Math.min(Math.max(size, 1), 100);
    }

    public int offset() {
        return safePage() * safeSize();
    }
}
