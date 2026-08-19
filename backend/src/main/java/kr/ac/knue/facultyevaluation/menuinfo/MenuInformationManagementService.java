package kr.ac.knue.facultyevaluation.menuinfo;

import java.util.List;
import kr.ac.knue.facultyevaluation.menuinfo.mapper.MenuInformationManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuInformationManagementService {

    private final MenuInformationManagementMapper menuInformationManagementMapper;

    public MenuInformationManagementService(MenuInformationManagementMapper menuInformationManagementMapper) {
        this.menuInformationManagementMapper = menuInformationManagementMapper;
    }

    @Transactional(readOnly = true)
    public MenuInformationSearchResult list(MenuInformationSearchCriteria criteria) {
        List<MenuInformationSummary> items = menuInformationManagementMapper.list(criteria).stream()
            .map(MenuInformationRow::toSummary)
            .toList();
        long total = menuInformationManagementMapper.count(criteria);
        return new MenuInformationSearchResult(items, total, criteria.safePage(), criteria.safeSize());
    }

    @Transactional
    public MenuInformationSummary create(CreateMenuRequest request) {
        String menuId = normalizeRequired(request.menuId(), "메뉴ID는 필수입니다.");
        validateMenuIdAvailable(menuId);
        String parentMenuId = normalizeBlank(request.parentMenuId());
        validateParentMenu(parentMenuId);
        String screenId = normalizeBlank(request.screenId());
        String url = normalizeBlank(request.url());
        validateExecutionLink(screenId, url);
        validateUniqueScreenAndUrl(menuId, screenId, url);
        menuInformationManagementMapper.insertMenu(
            menuId,
            parentMenuId,
            normalizeRequired(request.menuName(), "메뉴명은 필수입니다."),
            screenId,
            url,
            normalizeBlank(request.icon()),
            normalizeBlank(request.businessType()),
            normalizeBlank(request.description()),
            request.displayOrder(),
            request.useYn()
        );
        return getMenu(menuId);
    }

    @Transactional
    public MenuInformationSummary update(String menuId, UpdateMenuRequest request) {
        String normalizedMenuId = normalizeRequired(menuId, "메뉴ID는 필수입니다.");
        validateMenuExists(normalizedMenuId);
        String parentMenuId = normalizeBlank(request.parentMenuId());
        if (normalizedMenuId.equals(parentMenuId)) {
            throw new IllegalArgumentException("자기 자신을 부모메뉴로 지정할 수 없습니다.");
        }
        validateParentMenu(parentMenuId);
        if (parentMenuId != null && menuInformationManagementMapper.isDescendant(normalizedMenuId, parentMenuId)) {
            throw new IllegalArgumentException("하위 메뉴를 부모메뉴로 지정할 수 없습니다.");
        }
        String screenId = normalizeBlank(request.screenId());
        String url = normalizeBlank(request.url());
        validateExecutionLink(screenId, url);
        validateUniqueScreenAndUrl(normalizedMenuId, screenId, url);
        menuInformationManagementMapper.updateMenu(
            normalizedMenuId,
            parentMenuId,
            normalizeRequired(request.menuName(), "메뉴명은 필수입니다."),
            screenId,
            url,
            normalizeBlank(request.icon()),
            normalizeBlank(request.businessType()),
            normalizeBlank(request.description()),
            request.displayOrder(),
            request.useYn()
        );
        return getMenu(normalizedMenuId);
    }

    private MenuInformationSummary getMenu(String menuId) {
        MenuInformationRow menuInformationRow = menuInformationManagementMapper.findByMenuId(menuId);
        if (menuInformationRow == null) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId);
        }
        return menuInformationRow.toSummary();
    }

    private void validateMenuIdAvailable(String menuId) {
        if (menuInformationManagementMapper.existsMenu(menuId)) {
            throw new IllegalArgumentException("이미 등록된 메뉴ID입니다: " + menuId);
        }
    }

    private void validateMenuExists(String menuId) {
        if (!menuInformationManagementMapper.existsMenu(menuId)) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId);
        }
    }

    private void validateParentMenu(String parentMenuId) {
        if (parentMenuId != null && !menuInformationManagementMapper.existsMenu(parentMenuId)) {
            throw new IllegalArgumentException("부모메뉴를 찾을 수 없습니다: " + parentMenuId);
        }
    }

    private void validateExecutionLink(String screenId, String url) {
        if ((screenId == null) != (url == null)) {
            throw new IllegalArgumentException("화면ID와 URL은 함께 입력해야 합니다.");
        }
        if (screenId != null && !screenId.startsWith("SCR-")) {
            throw new IllegalArgumentException("화면ID는 SCR- 접두어로 시작해야 합니다.");
        }
        if (url != null && !url.startsWith("/admin/")) {
            throw new IllegalArgumentException("URL은 /admin/ 경로로 시작해야 합니다.");
        }
    }

    private void validateUniqueScreenAndUrl(String menuId, String screenId, String url) {
        if (screenId != null && menuInformationManagementMapper.existsScreenIdForOtherMenu(screenId, menuId)) {
            throw new IllegalArgumentException("이미 연결된 화면ID입니다: " + screenId);
        }
        if (url != null && menuInformationManagementMapper.existsUrlForOtherMenu(url, menuId)) {
            throw new IllegalArgumentException("이미 연결된 URL입니다: " + url);
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
}
