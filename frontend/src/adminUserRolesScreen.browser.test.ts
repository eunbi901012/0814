import { describe, expect, it } from "vitest";
import { createAdminUserRolesViewModel } from "./main";

describe("/admin/user-roles browser contract", () => {
  it("renders the SCR-CMN-USER-ROLE route with current role list, effective period, approver, assignment type, and mutating actions", () => {
    const viewModel = createAdminUserRolesViewModel({
      path: "/admin/user-roles",
      userRoles: [
        {
          userRoleId: "UR-FAC-0001-R01",
          userId: "FAC-0001",
          userName: "김교원",
          roleCode: "R01",
          roleName: "교원",
          effectiveStartDate: "2026-01-01",
          effectiveEndDate: null,
          approverUserId: "admin",
          approverName: "시스템관리자",
          assignmentType: "POSITION_BASED",
          status: "ACTIVE",
          updatedAt: "2026-01-01T09:00:00",
        },
      ],
      selectedUserRoleId: "UR-FAC-0001-R01",
      status: "success",
      message: "사용자 역할이 저장되었습니다.",
    });

    expect(viewModel.route).toBe("/admin/user-roles");
    expect(viewModel.screenId).toBe("SCR-CMN-USER-ROLE");
    expect(viewModel.menuPath).toBe(
      "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    );
    expect(viewModel.filters).toEqual([
      "교번",
      "성명",
      "역할",
      "유효기간",
      "부여구분",
    ]);
    expect(viewModel.columns).toEqual([
      "교번",
      "성명",
      "현재 역할",
      "시작일",
      "종료일",
      "승인자",
      "부여구분",
      "상태",
    ]);
    expect(viewModel.detailFields).toEqual([
      "역할",
      "시작일",
      "종료일",
      "승인자",
      "부여구분",
      "상태",
    ]);
    expect(viewModel.actions).toEqual([
      "/api/user-roles",
      "/api/user-roles/{userRoleId}",
    ]);
    expect(viewModel.selectedUserRole?.approverUserId).toBe("admin");
    expect(viewModel.selectedUserRole?.assignmentType).toBe("POSITION_BASED");
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state when R09 permission is absent", () => {
    const viewModel = createAdminUserRolesViewModel({
      path: "/forbidden",
      userRoles: [],
      selectedUserRoleId: null,
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
  });
});
