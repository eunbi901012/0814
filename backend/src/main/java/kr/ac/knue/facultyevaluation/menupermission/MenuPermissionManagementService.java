package kr.ac.knue.facultyevaluation.menupermission;

import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyevaluation.menupermission.mapper.MenuPermissionManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuPermissionManagementService {

    private final MenuPermissionManagementMapper menuPermissionManagementMapper;

    public MenuPermissionManagementService(MenuPermissionManagementMapper menuPermissionManagementMapper) {
        this.menuPermissionManagementMapper = menuPermissionManagementMapper;
    }

    @Transactional(readOnly = true)
    public MenuPermissionSearchResult list(MenuPermissionSearchCriteria criteria) {
        validateTarget(criteria.targetType(), criteria.targetId());
        List<MenuPermissionSummary> items = menuPermissionManagementMapper.list(criteria).stream()
            .map(MenuPermissionRow::toSummary)
            .toList();
        long total = menuPermissionManagementMapper.count(criteria);
        return new MenuPermissionSearchResult(items, total, criteria.safePage(), criteria.safeSize(), buildPreview(items));
    }

    @Transactional
    public MenuPermissionSearchResult save(SaveMenuPermissionRequest request) {
        validateTarget(request.targetType(), request.targetId());
        for (SaveMenuPermissionItem item : request.permissions()) {
            validatePermissionItem(item);
            String permissionId = menuPermissionManagementMapper.findPermissionId(request.targetType(), request.targetId(), item.menuId());
            if (permissionId == null) {
                permissionId = "PERM-" + UUID.randomUUID();
            }
            menuPermissionManagementMapper.upsertPermission(
                permissionId,
                request.targetType(),
                request.targetId(),
                item.menuId(),
                item.canRead(),
                item.canWrite(),
                item.effect()
            );
        }
        return list(new MenuPermissionSearchCriteria(request.targetType(), request.targetId(), null, 0, 100));
    }

    private PermissionPreview buildPreview(List<MenuPermissionSummary> items) {
        return new PermissionPreview(
            items.stream().filter(item -> item.url() != null && item.canRead() && "ALLOW".equals(item.effect())).map(MenuPermissionSummary::url).toList(),
            items.stream().filter(item -> item.url() != null && item.canWrite() && "ALLOW".equals(item.effect())).map(MenuPermissionSummary::url).toList(),
            items.stream().filter(item -> item.url() != null && (!item.canRead() || "DENY".equals(item.effect()))).map(MenuPermissionSummary::url).toList()
        );
    }

    private void validateTarget(String targetType, String targetId) {
        if (targetType == null || targetType.isBlank() || targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("대상 유형과 대상 ID는 필수입니다.");
        }
        boolean exists = switch (targetType) {
            case "ROLE" -> menuPermissionManagementMapper.existsRole(targetId);
            case "ORGANIZATION" -> menuPermissionManagementMapper.existsOrganization(targetId);
            case "USER" -> menuPermissionManagementMapper.existsUser(targetId);
            default -> throw new IllegalArgumentException("대상 유형은 ROLE, ORGANIZATION, USER만 허용됩니다.");
        };
        if (!exists) {
            throw new IllegalArgumentException("권한 대상을 찾을 수 없습니다: " + targetId);
        }
    }

    private void validatePermissionItem(SaveMenuPermissionItem item) {
        if (!menuPermissionManagementMapper.existsMenu(item.menuId())) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + item.menuId());
        }
        if (Boolean.TRUE.equals(item.canWrite()) && !Boolean.TRUE.equals(item.canRead())) {
            throw new IllegalArgumentException("변경허용은 조회허용 없이 설정할 수 없습니다.");
        }
        if ("DENY".equals(item.effect()) && Boolean.TRUE.equals(item.canWrite())) {
            throw new IllegalArgumentException("DENY 권한은 변경허용을 함께 설정할 수 없습니다.");
        }
    }
}
