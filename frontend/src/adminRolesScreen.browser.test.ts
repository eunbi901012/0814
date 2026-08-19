import { describe, expect, it } from "vitest";
import { createAdminRolesViewModel } from "./main";

describe("/admin/roles browser contract", () => {
  it("renders the SCR-CMN-ROLE route with R01 to R09 list, readonly role_code detail, and role save action", () => {
    const viewModel = createAdminRolesViewModel({
      path: "/admin/roles",
      roles: [
        {
          roleCode: "R01",
          roleName: "교원",
          purpose: "본인 관련 업무를 수행하는 일반 사용자 역할",
          grantCriteria: "교원 재직자",
          dataScopeDefault: "SELF",
          useYn: "Y",
          updatedAt: "2026-01-01T09:00:00",
        },
        {
          roleCode: "R09",
          roleName: "시스템관리자",
          purpose: "사용자·조직·메뉴·권한·코드 관리를 수행하는 관리자 역할",
          grantCriteria: "시스템 관리자",
          dataScopeDefault: "ALL",
          useYn: "Y",
          updatedAt: "2026-01-01T09:00:00",
        },
      ],
      selectedRoleCode: "R09",
      status: "success",
      message: "역할 기준이 저장되었습니다.",
    });

    expect(viewModel.route).toBe("/admin/roles");
    expect(viewModel.screenId).toBe("SCR-CMN-ROLE");
    expect(viewModel.menuPath).toBe("시스템 관리 > 역할·권한 관리 > 역할 관리");
    expect(viewModel.filters).toEqual(["역할코드", "역할명", "사용여부"]);
    expect(viewModel.columns).toEqual([
      "역할코드",
      "역할명",
      "목적",
      "사용여부",
    ]);
    expect(viewModel.detailFields).toContain("role_code readonly");
    expect(viewModel.actions).toEqual(["/api/roles", "/api/roles/{roleCode}"]);
    expect(viewModel.selectedRole?.roleCode).toBe("R09");
    expect(viewModel.selectedRole?.dataScopeDefault).toBe("ALL");
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state outside the R09 roles route", () => {
    const viewModel = createAdminRolesViewModel({
      path: "/forbidden",
      roles: [],
      selectedRoleCode: null,
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
  });
});
