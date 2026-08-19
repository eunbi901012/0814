package kr.ac.knue.facultyevaluation.menupermission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record SaveMenuPermissionRequest(
    @NotBlank(message = "대상 유형은 필수입니다.")
    @Pattern(regexp = "ROLE|ORGANIZATION|USER", message = "대상 유형은 ROLE, ORGANIZATION, USER만 허용됩니다.")
    String targetType,

    @NotBlank(message = "대상 ID는 필수입니다.")
    String targetId,

    @Valid
    @NotEmpty(message = "저장할 메뉴 권한은 하나 이상이어야 합니다.")
    List<SaveMenuPermissionItem> permissions
) {
}
