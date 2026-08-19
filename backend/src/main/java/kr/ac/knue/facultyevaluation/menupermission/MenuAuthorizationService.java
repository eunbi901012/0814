package kr.ac.knue.facultyevaluation.menupermission;

import java.util.List;
import kr.ac.knue.facultyevaluation.auth.AuthenticatedUser;
import kr.ac.knue.facultyevaluation.menupermission.mapper.MenuPermissionManagementMapper;
import org.springframework.stereotype.Service;

@Service
public class MenuAuthorizationService {

    private final MenuPermissionManagementMapper menuPermissionManagementMapper;

    public MenuAuthorizationService(MenuPermissionManagementMapper menuPermissionManagementMapper) {
        this.menuPermissionManagementMapper = menuPermissionManagementMapper;
    }

    public MenuAccessDecision decide(AuthenticatedUser user, String requestUri, String method) {
        String menuUrl = mapApiPathToMenuUrl(requestUri);
        if (menuUrl == null) {
            return MenuAccessDecision.unmatched();
        }
        MenuPermissionRow decision = menuPermissionManagementMapper.findEffectivePermission(user.userId(), user.departmentCode(), user.roles(), menuUrl);
        if (decision == null) {
            return new MenuAccessDecision(true, false, false, "DENY");
        }
        boolean readAllowed = decision.canRead() && "ALLOW".equals(decision.effect());
        boolean writeAllowed = decision.canWrite() && "ALLOW".equals(decision.effect());
        boolean mutating = !("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method));
        return new MenuAccessDecision(true, mutating ? writeAllowed : readAllowed, writeAllowed, decision.effect());
    }

    private String mapApiPathToMenuUrl(String requestUri) {
        if (requestUri.startsWith("/api/users")) {
            return "/admin/users";
        }
        if (requestUri.startsWith("/api/organizations")) {
            return "/admin/organizations";
        }
        if (requestUri.startsWith("/api/roles")) {
            return "/admin/roles";
        }
        if (requestUri.startsWith("/api/user-roles")) {
            return "/admin/user-roles";
        }
        if (requestUri.startsWith("/api/menu-permissions")) {
            return "/admin/menu-permissions";
        }
        if (requestUri.startsWith("/api/menus/tree")) {
            return "/admin/menus/tree";
        }
        if (requestUri.startsWith("/api/menus")) {
            return "/admin/menus";
        }
        if (requestUri.startsWith("/api/code-groups")) {
            return "/admin/code-groups";
        }
        if (requestUri.startsWith("/api/code-details")) {
            return "/admin/code-details";
        }
        return null;
    }
}
