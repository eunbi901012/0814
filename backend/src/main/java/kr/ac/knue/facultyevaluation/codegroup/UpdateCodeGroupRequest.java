package kr.ac.knue.facultyevaluation.codegroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCodeGroupRequest(
    @Size(max = 60, message = "그룹ID는 60자 이하여야 합니다.")
    String groupId,

    @NotBlank(message = "명칭은 필수입니다.")
    @Size(max = 100, message = "명칭은 100자 이하여야 합니다.")
    String groupName,

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    String description,

    @Size(max = 100, message = "관리부서는 100자 이하여야 합니다.")
    String managingDepartment,

    @NotBlank(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N이어야 합니다.")
    String useYn
) {
}
