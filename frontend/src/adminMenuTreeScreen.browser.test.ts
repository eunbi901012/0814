import { describe, expect, it } from "vitest";
import { createAdminMenuTreeViewModel } from "./main";

describe("/admin/menus/tree browser contract", () => {
  it("renders SCR-CMN-MENU-TREE hierarchy with parent and display-order edit actions", () => {
    const viewModel = createAdminMenuTreeViewModel({
      path: "/admin/menus/tree",
      menus: [
        {
          menuId: "M-SYS",
          parentMenuId: null,
          menuName: "시스템 관리",
          screenId: null,
          url: null,
          displayOrder: 1,
          useYn: "Y",
          depth: 0,
          childMenuIds: ["M-SYS-USERORG", "M-SYS-ROLEAUTH", "M-SYS-MENU"],
          updatedAt: "2026-01-01T09:00:00",
        },
        {
          menuId: "M-SYS-MENU",
          parentMenuId: "M-SYS",
          menuName: "메뉴 관리",
          screenId: null,
          url: null,
          displayOrder: 3,
          useYn: "Y",
          depth: 1,
          childMenuIds: ["M-SYS-MENUTREE"],
          updatedAt: "2026-01-01T09:00:00",
        },
        {
          menuId: "M-SYS-MENUTREE",
          parentMenuId: "M-SYS-MENU",
          menuName: "메뉴 구조 관리",
          screenId: "SCR-CMN-MENU-TREE",
          url: "/admin/menus/tree",
          displayOrder: 1,
          useYn: "Y",
          depth: 2,
          childMenuIds: [],
          updatedAt: "2026-01-01T09:00:00",
        },
      ],
      selectedMenuId: "M-SYS-MENUTREE",
      status: "success",
      message: "표시순서가 저장되었습니다.",
    });

    expect(viewModel.route).toBe("/admin/menus/tree");
    expect(viewModel.screenId).toBe("SCR-CMN-MENU-TREE");
    expect(viewModel.menuPath).toBe("시스템 관리 > 메뉴 관리 > 메뉴 구조 관리");
    expect(viewModel.treeLabels).toEqual([
      "시스템 관리",
      "메뉴 관리",
      "메뉴 구조 관리",
    ]);
    expect(viewModel.selectedMenu?.menuId).toBe("M-SYS-MENUTREE");
    expect(viewModel.detailFields).toEqual([
      "menu_id readonly",
      "현재 부모메뉴",
      "부모메뉴",
      "표시순서",
    ]);
    expect(viewModel.actions).toEqual([
      "/api/menus/tree",
      "/api/menus/{menuId}/parent",
      "/api/menus/{menuId}/order",
    ]);
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state when the protected menu tree is unavailable", () => {
    const viewModel = createAdminMenuTreeViewModel({
      path: "/admin/menus/tree",
      menus: [],
      selectedMenuId: null,
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
  });
});
