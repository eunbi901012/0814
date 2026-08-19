package kr.ac.knue.facultyevaluation.menupermission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.ac.knue.facultyevaluation.auth.AuthenticatedUser;
import kr.ac.knue.facultyevaluation.menupermission.mapper.MenuPermissionManagementMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuAuthorizationServiceTest {

    @Mock
    MenuPermissionManagementMapper mapper;

    @Test
    void deniesUnreadablePermissionForEveryProtectedAdminApiGroup() {
        MenuAuthorizationService service = new MenuAuthorizationService(mapper);
        AuthenticatedUser user = new AuthenticatedUser("FAC-0001", "faculty", "교원", "KNUE-DEPT-EDU", "Y", List.of("R01"));
        List<ApiMenuCase> cases = List.of(
            new ApiMenuCase("/api/users", "/admin/users"),
            new ApiMenuCase("/api/organizations", "/admin/organizations"),
            new ApiMenuCase("/api/roles", "/admin/roles"),
            new ApiMenuCase("/api/user-roles", "/admin/user-roles"),
            new ApiMenuCase("/api/menu-permissions", "/admin/menu-permissions"),
            new ApiMenuCase("/api/menus/tree", "/admin/menus/tree"),
            new ApiMenuCase("/api/menus", "/admin/menus"),
            new ApiMenuCase("/api/code-groups", "/admin/code-groups"),
            new ApiMenuCase("/api/code-details", "/admin/code-details")
        );
        cases.forEach(apiMenuCase -> when(mapper.findEffectivePermission("FAC-0001", "KNUE-DEPT-EDU", List.of("R01"), apiMenuCase.menuUrl()))
            .thenReturn(new MenuPermissionRow(null, "ROLE", "R01", "MENU", null, null, null, null, apiMenuCase.menuUrl(), false, false, "DENY", null)));

        List<MenuAccessDecision> decisions = cases.stream()
            .map(apiMenuCase -> service.decide(user, apiMenuCase.apiPath(), "GET"))
            .toList();

        assertThat(decisions).allSatisfy(decision -> {
            assertThat(decision.matched()).isTrue();
            assertThat(decision.allowed()).isFalse();
        });
    }

    @Test
    void deniesWriteWhenMenuIsReadOnlyForMutatingRequest() {
        MenuAuthorizationService service = new MenuAuthorizationService(mapper);
        AuthenticatedUser user = new AuthenticatedUser("FAC-0001", "faculty", "교원", "KNUE-DEPT-EDU", "Y", List.of("R01"));
        when(mapper.findEffectivePermission("FAC-0001", "KNUE-DEPT-EDU", List.of("R01"), "/admin/code-groups"))
            .thenReturn(new MenuPermissionRow(null, "ROLE", "R01", "MENU", null, null, null, null, "/admin/code-groups", true, false, "ALLOW", null));

        MenuAccessDecision decision = service.decide(user, "/api/code-groups", "POST");

        assertThat(decision.matched()).isTrue();
        assertThat(decision.allowed()).isFalse();
    }

    private record ApiMenuCase(String apiPath, String menuUrl) {
    }
}
