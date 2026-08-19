import { describe, expect, it } from "vitest";
import { createAdminUsersViewModel } from "./main";

describe("/admin/users browser contract", () => {
  it("renders the SCR-CMN-USER route with search filters, readonly KORUS columns, and user save actions", () => {
    const viewModel = createAdminUsersViewModel({
      path: "/admin/users",
      users: [
        {
          userId: "FAC-0001",
          loginId: "fac0001",
          name: "김교원",
          departmentCode: "KNUE-DEPT-COMMON",
          departmentName: "공통기능학과",
          rankName: "교수",
          employmentStatus: "ACTIVE",
          roles: ["R01"],
          systemUseYn: "Y",
          positionName: "학과장",
          retirementDate: null,
          lastSyncedAt: "2026-01-01T09:00:00",
        },
      ],
      selectedUserId: "FAC-0001",
      status: "success",
      message: "저장되었습니다.",
    });

    expect(viewModel.route).toBe("/admin/users");
    expect(viewModel.screenId).toBe("SCR-CMN-USER");
    expect(viewModel.filters).toEqual([
      "교번",
      "성명",
      "소속",
      "직급",
      "재직상태",
      "역할",
      "사용여부",
    ]);
    expect(viewModel.columns).toContain("보직");
    expect(viewModel.columns).toContain("퇴직일자");
    expect(viewModel.columns).toContain("최종 동기화일시");
    expect(viewModel.actions).toEqual([
      "/api/users",
      "/api/users/{userId}/usage",
      "/api/users/{userId}/roles",
    ]);
    expect(viewModel.selectedUser?.roles).toEqual(["R01"]);
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state outside the R09 admin users route", () => {
    const viewModel = createAdminUsersViewModel({
      path: "/forbidden",
      users: [],
      selectedUserId: null,
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
  });
});
