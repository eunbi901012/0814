package kr.ac.knue.facultyevaluation.menustructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.ac.knue.facultyevaluation.menustructure.mapper.MenuStructureManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuStructureManagementService {

    private final MenuStructureManagementMapper menuStructureManagementMapper;

    public MenuStructureManagementService(MenuStructureManagementMapper menuStructureManagementMapper) {
        this.menuStructureManagementMapper = menuStructureManagementMapper;
    }

    @Transactional(readOnly = true)
    public MenuTreeSearchResult getTree(MenuTreeSearchCriteria criteria) {
        List<MenuTreeRow> rows = menuStructureManagementMapper.listTree(criteria);
        Map<String, List<String>> childMenuIdsByParent = new LinkedHashMap<>();
        for (MenuTreeRow row : rows) {
            if (row.parentMenuId() != null) {
                childMenuIdsByParent.computeIfAbsent(row.parentMenuId(), ignored -> new ArrayList<>()).add(row.menuId());
            }
        }
        List<MenuTreeSummary> items = rows.stream()
            .map(row -> row.toSummary(childMenuIdsByParent.getOrDefault(row.menuId(), List.of())))
            .toList();
        return new MenuTreeSearchResult(items, menuStructureManagementMapper.countActiveMenus(), criteria.safePage(), criteria.safeSize());
    }

    @Transactional
    public MenuTreeSearchResult updateParent(String menuId, UpdateMenuParentRequest request) {
        String nextParentMenuId = normalizeParentMenuId(request.parentMenuId());
        validateMenuExists(menuId);
        if (nextParentMenuId != null) {
            validateMenuExists(nextParentMenuId);
            if (menuId.equals(nextParentMenuId)) {
                throw new IllegalArgumentException("자기 자신을 부모메뉴로 지정할 수 없습니다.");
            }
            if (menuStructureManagementMapper.isDescendant(menuId, nextParentMenuId)) {
                throw new IllegalArgumentException("하위 메뉴를 부모메뉴로 지정할 수 없습니다.");
            }
        }
        menuStructureManagementMapper.updateParent(menuId, nextParentMenuId);
        return getTree(new MenuTreeSearchCriteria(menuId, 0, 100));
    }

    @Transactional
    public MenuTreeSearchResult updateOrder(String menuId, UpdateMenuOrderRequest request) {
        validateMenuExists(menuId);
        menuStructureManagementMapper.updateDisplayOrder(menuId, request.displayOrder());
        return getTree(new MenuTreeSearchCriteria(menuId, 0, 100));
    }

    private String normalizeParentMenuId(String parentMenuId) {
        if (parentMenuId == null || parentMenuId.isBlank()) {
            return null;
        }
        return parentMenuId.trim();
    }

    private void validateMenuExists(String menuId) {
        if (menuId == null || menuId.isBlank() || !menuStructureManagementMapper.existsMenu(menuId)) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId);
        }
    }
}
