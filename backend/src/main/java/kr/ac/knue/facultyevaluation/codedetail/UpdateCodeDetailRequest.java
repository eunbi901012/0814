package kr.ac.knue.facultyevaluation.codedetail;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateCodeDetailRequest(
    @Size(max = 60, message = "그룹ID는 60자 이하여야 합니다.")
    String groupId,

    @Size(max = 60, message = "코드값은 60자 이하여야 합니다.")
    String codeValue,

    @NotBlank(message = "코드명은 필수입니다.")
    @Size(max = 100, message = "코드명은 100자 이하여야 합니다.")
    String codeName,

    @Size(max = 60, message = "상위코드는 60자 이하여야 합니다.")
    String parentCodeValue,

    @NotNull(message = "정렬순서는 필수입니다.")
    @Min(value = 1, message = "정렬순서는 1 이상이어야 합니다.")
    Integer sortOrder,

    String extraAttributes,

    @NotBlank(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N이어야 합니다.")
    String useYn,

    java.time.LocalDate validFrom,
    java.time.LocalDate validTo
) {
}
