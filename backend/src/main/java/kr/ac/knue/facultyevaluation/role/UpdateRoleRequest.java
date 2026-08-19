package kr.ac.knue.facultyevaluation.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
    @Pattern(regexp = "R0[1-9]", message = "역할코드는 R01~R09만 허용됩니다.")
    String roleCode,

    @NotBlank(message = "역할명은 필수입니다.")
    @Size(max = 100, message = "역할명은 100자 이하여야 합니다.")
    String roleName,

    @NotBlank(message = "목적은 필수입니다.")
    @Size(max = 500, message = "목적은 500자 이하여야 합니다.")
    String purpose,

    @Size(max = 500, message = "부여 기준은 500자 이하여야 합니다.")
    String grantCriteria,

    @Size(max = 200, message = "데이터 범위 기본값은 200자 이하여야 합니다.")
    String dataScopeDefault,

    @NotBlank(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N이어야 합니다.")
    String useYn
) {
}
