import { describe, expect, it } from "vitest";
import { createAdminMenuPermissionsViewModel } from "./main";

describe("/admin/menu-permissions browser contract", () => {
  it("renders SCR-CMN-MENU-PERM permission matrix with target selector, effect controls, and server preview", () => {
    const viewModel = createAdminMenuPermissionsViewModel({
      path: "/admin/menu-permissions",
      permissions: [
        {
          permissionId: "PERM-R09-M-SYS-USER",
          targetType: "ROLE",
          targetId: "R09",
          menuId: "M-SYS-USER",
          topMenuName: "시스템 관리",
          middleMenuName: "사용자·조직 관리",
          screenMenuName: "사용자 관리",
          screenId: "SCR-CMN-USER",
          url: "/admin/users",
          canRead: true,
          canWrite: true,
          effect: "ALLOW",
          updatedAt: "2026-01-01T09:00:00",
        },
        {
          permissionId: "PERM-R09-M-SYS-MENUPERM",
          targetType: "ROLE",
          targetId: "R09",
          menuId: "M-SYS-MENUPERM",
          topMenuName: "시스템 관리",
          middleMenuName: "역할·권한 관리",
          screenMenuName: "메뉴 권한 관리",
          screenId: "SCR-CMN-MENU-PERM",
          url: "/admin/menu-permissions",
          canRead: true,
          canWrite: true,
          effect: "ALLOW",
          updatedAt: "2026-01-01T09:00:00",
        },
      ],
      preview: {
        visibleMenuUrls: ["/admin/users", "/admin/menu-permissions"],
        writableMenuUrls: ["/admin/users", "/admin/menu-permissions"],
        deniedMenuUrls: [],
      },
      status: "success",
      message: "메뉴 권한이 저장되었습니다.",
    });

    expect(viewModel.route).toBe("/admin/menu-permissions");
    expect(viewModel.screenId).toBe("SCR-CMN-MENU-PERM");
    expect(viewModel.menuPath).toBe(
      "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    );
    expect(viewModel.filters).toEqual(["대상유형", "대상ID"]);
    expect(viewModel.columns).toEqual([
      "대메뉴",
      "중메뉴",
      "화면",
      "조회허용",
      "변경허용",
      "effect",
    ]);
    expect(viewModel.actions).toEqual(["/api/menu-permissions"]);
    expect(viewModel.permissions[0].topMenuName).toBe("시스템 관리");
    expect(viewModel.permissions[1].screenMenuName).toBe("메뉴 권한 관리");
    expect(viewModel.preview.visibleMenuUrls).toContain(
      "/admin/menu-permissions",
    );
    expect(viewModel.preview.writableMenuUrls).toContain("/admin/users");
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state when the protected menu is denied or unavailable", () => {
    const viewModel = createAdminMenuPermissionsViewModel({
      path: "/admin/menu-permissions",
      permissions: [],
      preview: {
        visibleMenuUrls: [],
        writableMenuUrls: [],
        deniedMenuUrls: ["/admin/menu-permissions"],
      },
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
    expect(viewModel.preview.deniedMenuUrls).toContain(
      "/admin/menu-permissions",
    );
  });
});
