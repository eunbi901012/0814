package kr.ac.knue.facultyevaluation.menupermission;

public record MenuAccessDecision(
    boolean matched,
    boolean allowed,
    boolean writable,
    String effect
) {
    public static MenuAccessDecision unmatched() {
        return new MenuAccessDecision(false, true, true, "ALLOW");
    }
}
