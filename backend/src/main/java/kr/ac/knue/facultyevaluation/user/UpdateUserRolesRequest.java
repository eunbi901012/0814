package kr.ac.knue.facultyevaluation.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

public record UpdateUserRolesRequest(
    @NotEmpty(message = "업무 역할은 하나 이상 선택해야 합니다.")
    List<@Pattern(regexp = "R0[1-9]", message = "역할코드는 R01~R09만 허용됩니다.") String> roleCodes,

    @NotBlank(message = "부여구분은 필수입니다.")
    @Pattern(regexp = "POSITION_BASED|MANUAL", message = "부여구분은 POSITION_BASED 또는 MANUAL이어야 합니다.")
    String assignmentType,

    @NotBlank(message = "승인자는 필수입니다.")
    String approverUserId,

    @NotNull(message = "유효 시작일은 필수입니다.")
    LocalDate effectiveStartDate,

    LocalDate effectiveEndDate
) {
    @AssertTrue(message = "유효 시작일은 종료일보다 늦을 수 없습니다.")
    public boolean isValidPeriod() {
        return effectiveEndDate == null || effectiveStartDate == null || !effectiveStartDate.isAfter(effectiveEndDate);
    }
}
