package kr.ac.knue.facultyevaluation.codedetail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import kr.ac.knue.facultyevaluation.codedetail.mapper.CodeDetailManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeDetailManagementService {

    private final CodeDetailManagementMapper codeDetailManagementMapper;
    private final ObjectMapper objectMapper;

    public CodeDetailManagementService(CodeDetailManagementMapper codeDetailManagementMapper, ObjectMapper objectMapper) {
        this.codeDetailManagementMapper = codeDetailManagementMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CodeDetailSearchResult list(CodeDetailSearchCriteria criteria) {
        List<CodeDetailSummary> items = codeDetailManagementMapper.list(criteria).stream()
            .map(CodeDetailRow::toSummary)
            .toList();
        long total = codeDetailManagementMapper.count(criteria);
        return new CodeDetailSearchResult(items, total, criteria.safePage(), criteria.safeSize());
    }

    @Transactional
    public CodeDetailSummary create(CreateCodeDetailRequest request) {
        String groupId = normalizeRequired(request.groupId(), "그룹ID는 필수입니다.");
        String codeValue = normalizeRequired(request.codeValue(), "코드값은 필수입니다.");
        validateGroupExists(groupId);
        validateCodeAvailable(groupId, codeValue);
        validateParent(groupId, codeValue, request.parentCodeValue());
        validatePeriod(request.validFrom(), request.validTo());
        codeDetailManagementMapper.insertCodeDetail(
            groupId,
            codeValue,
            normalizeRequired(request.codeName(), "코드명은 필수입니다."),
            normalizeBlank(request.parentCodeValue()),
            request.sortOrder(),
            normalizeJson(request.extraAttributes()),
            request.useYn(),
            request.validFrom(),
            request.validTo()
        );
        return getCodeDetail(groupId, codeValue);
    }

    @Transactional
    public CodeDetailSummary update(String groupId, String codeValue, UpdateCodeDetailRequest request) {
        String normalizedGroupId = normalizeRequired(groupId, "그룹ID는 필수입니다.");
        String normalizedCodeValue = normalizeRequired(codeValue, "코드값은 필수입니다.");
        if (request.groupId() != null && !request.groupId().isBlank() && !normalizedGroupId.equals(request.groupId().trim())) {
            throw new IllegalArgumentException("그룹ID와 코드값은 변경할 수 없습니다.");
        }
        if (request.codeValue() != null && !request.codeValue().isBlank() && !normalizedCodeValue.equals(request.codeValue().trim())) {
            throw new IllegalArgumentException("그룹ID와 코드값은 변경할 수 없습니다.");
        }
        validateCodeExists(normalizedGroupId, normalizedCodeValue);
        validateParent(normalizedGroupId, normalizedCodeValue, request.parentCodeValue());
        validatePeriod(request.validFrom(), request.validTo());
        codeDetailManagementMapper.updateCodeDetail(
            normalizedGroupId,
            normalizedCodeValue,
            normalizeRequired(request.codeName(), "코드명은 필수입니다."),
            normalizeBlank(request.parentCodeValue()),
            request.sortOrder(),
            normalizeJson(request.extraAttributes()),
            request.useYn(),
            request.validFrom(),
            request.validTo()
        );
        return getCodeDetail(normalizedGroupId, normalizedCodeValue);
    }

    private CodeDetailSummary getCodeDetail(String groupId, String codeValue) {
        CodeDetailRow codeDetailRow = codeDetailManagementMapper.findById(groupId, codeValue);
        if (codeDetailRow == null) {
            throw new IllegalArgumentException("상세코드를 찾을 수 없습니다: " + groupId + "/" + codeValue);
        }
        return codeDetailRow.toSummary();
    }

    private void validateGroupExists(String groupId) {
        if (!codeDetailManagementMapper.existsGroup(groupId)) {
            throw new IllegalArgumentException("코드그룹을 찾을 수 없습니다: " + groupId);
        }
    }

    private void validateCodeExists(String groupId, String codeValue) {
        if (!codeDetailManagementMapper.existsCode(groupId, codeValue)) {
            throw new IllegalArgumentException("상세코드를 찾을 수 없습니다: " + groupId + "/" + codeValue);
        }
    }

    private void validateCodeAvailable(String groupId, String codeValue) {
        if (codeDetailManagementMapper.existsCode(groupId, codeValue)) {
            throw new IllegalArgumentException("이미 등록된 코드값입니다: " + groupId + "/" + codeValue);
        }
    }

    private void validateParent(String groupId, String codeValue, String parentCodeValue) {
        String normalizedParentCodeValue = normalizeBlank(parentCodeValue);
        if (normalizedParentCodeValue == null) {
            return;
        }
        if (codeValue.equals(normalizedParentCodeValue)) {
            throw new IllegalArgumentException("상위코드는 자기 자신일 수 없습니다.");
        }
        if (!codeDetailManagementMapper.existsCode(groupId, normalizedParentCodeValue)) {
            throw new IllegalArgumentException("상위코드를 찾을 수 없습니다: " + normalizedParentCodeValue);
        }
    }

    private void validatePeriod(LocalDate validFrom, LocalDate validTo) {
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("유효 시작일은 종료일보다 늦을 수 없습니다.");
        }
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeJson(String value) {
        String normalizedValue = normalizeBlank(value);
        String json = normalizedValue == null ? "{}" : normalizedValue;
        try {
            objectMapper.readTree(json);
            return json;
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("추가속성은 유효한 JSON이어야 합니다.");
        }
    }
}
