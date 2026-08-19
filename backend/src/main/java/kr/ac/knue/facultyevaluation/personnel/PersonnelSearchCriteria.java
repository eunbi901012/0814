package kr.ac.knue.facultyevaluation.personnel;

public record PersonnelSearchCriteria(
    String employeeNo,
    String name,
    String orgCode,
    String rankName,
    String status
) {
}
