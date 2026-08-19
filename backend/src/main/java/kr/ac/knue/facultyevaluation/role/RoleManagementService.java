package kr.ac.knue.facultyevaluation.role;

import java.util.List;
import kr.ac.knue.facultyevaluation.role.mapper.RoleManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleManagementService {

    private final RoleManagementMapper roleManagementMapper;

    public RoleManagementService(RoleManagementMapper roleManagementMapper) {
        this.roleManagementMapper = roleManagementMapper;
    }

    @Transactional(readOnly = true)
    public RoleSearchResult list(RoleSearchCriteria criteria) {
        List<RoleSummary> items = roleManagementMapper.list(criteria).stream()
            .map(RoleRow::toSummary)
            .toList();
        long total = roleManagementMapper.count(criteria);
        return new RoleSearchResult(items, total, criteria.safePage(), criteria.safeSize());
    }

    @Transactional
    public RoleSummary updateRole(String roleCode, UpdateRoleRequest request) {
        if (request.roleCode() != null && !request.roleCode().isBlank() && !roleCode.equals(request.roleCode())) {
            throw new IllegalArgumentException("역할코드는 변경할 수 없습니다.");
        }
        ensureRoleExists(roleCode);
        roleManagementMapper.updateRole(
            roleCode,
            request.roleName(),
            request.purpose(),
            normalizeBlank(request.grantCriteria()),
            normalizeBlank(request.dataScopeDefault()),
            request.useYn()
        );
        return getRole(roleCode);
    }

    private void ensureRoleExists(String roleCode) {
        if (!roleManagementMapper.existsRole(roleCode)) {
            throw new IllegalArgumentException("역할을 찾을 수 없습니다: " + roleCode);
        }
    }

    private RoleSummary getRole(String roleCode) {
        RoleRow roleRow = roleManagementMapper.findByRoleCode(roleCode);
        if (roleRow == null) {
            throw new IllegalArgumentException("역할을 찾을 수 없습니다: " + roleCode);
        }
        return roleRow.toSummary();
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
