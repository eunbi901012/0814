import { describe, expect, it } from "vitest";
import { createAdminMenusViewModel } from "./main";

describe("/admin/menus browser contract", () => {
  it("renders SCR-CMN-MENU-INFO search/list/detail contract with execution information actions", () => {
    const viewModel = createAdminMenusViewModel({
      path: "/admin/menus",
      menus: [
        {
          menuId: "M-SYS-MENUINFO",
          parentMenuId: "M-SYS-MENU",
          menuName: "메뉴 정보 관리",
          screenId: "SCR-CMN-MENU-INFO",
          url: "/admin/menus",
          icon: "menu-info",
          businessType: "SYSTEM",
          description: "메뉴 실행정보 관리",
          displayOrder: 2,
          useYn: "Y",
          updatedAt: "2026-01-01T09:00:00",
        },
      ],
      selectedMenuId: "M-SYS-MENUINFO",
      status: "success",
      message: "메뉴 실행정보가 저장되었습니다.",
    });

    expect(viewModel.route).toBe("/admin/menus");
    expect(viewModel.screenId).toBe("SCR-CMN-MENU-INFO");
    expect(viewModel.menuPath).toBe("시스템 관리 > 메뉴 관리 > 메뉴 정보 관리");
    expect(viewModel.filters).toEqual(["메뉴명", "화면ID", "URL", "업무구분"]);
    expect(viewModel.columns).toEqual([
      "메뉴명",
      "화면ID",
      "URL",
      "아이콘",
      "업무구분",
      "설명",
      "사용여부",
    ]);
    expect(viewModel.detailFields).toEqual([
      "menu_id",
      "부모메뉴",
      "메뉴명",
      "화면ID",
      "URL",
      "아이콘",
      "업무구분",
      "설명",
      "표시순서",
      "사용여부",
    ]);
    expect(viewModel.actions).toEqual(["/api/menus", "/api/menus/{menuId}"]);
    expect(viewModel.selectedMenu?.screenId).toBe("SCR-CMN-MENU-INFO");
    expect(viewModel.selectedMenu?.url).toBe("/admin/menus");
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state when menu information screen is protected", () => {
    const viewModel = createAdminMenusViewModel({
      path: "/admin/menus",
      menus: [],
      selectedMenuId: null,
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
  });
});
