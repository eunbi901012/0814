import { describe, expect, it } from "vitest";
import { createAdminCodeGroupsViewModel } from "./main";

describe("/admin/code-groups browser contract", () => {
  it("renders SCR-CMN-CODE-GROUP search/list/detail contract and detail-code navigation action", () => {
    const viewModel = createAdminCodeGroupsViewModel({
      path: "/admin/code-groups",
      codeGroups: [
        {
          groupId: "EMPLOYMENT_STATUS",
          groupName: "재직상태",
          description: "사용자 검색과 KORUS snapshot 검증에 필요한 재직 상태",
          managingDepartment: "교수지원과",
          useYn: "Y",
          updatedAt: "2026-01-01T09:00:00",
        },
      ],
      selectedGroupId: "EMPLOYMENT_STATUS",
      status: "success",
      message: "코드그룹이 저장되었습니다.",
    });

    expect(viewModel.route).toBe("/admin/code-groups");
    expect(viewModel.screenId).toBe("SCR-CMN-CODE-GROUP");
    expect(viewModel.menuPath).toBe(
      "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    );
    expect(viewModel.filters).toEqual([
      "그룹ID",
      "명칭",
      "관리부서",
      "사용여부",
    ]);
    expect(viewModel.columns).toEqual([
      "그룹ID",
      "명칭",
      "설명",
      "관리부서",
      "사용여부",
    ]);
    expect(viewModel.detailFields).toEqual([
      "group_id readonly on edit",
      "명칭",
      "설명",
      "관리부서",
      "사용여부",
    ]);
    expect(viewModel.actions).toEqual([
      "/api/code-groups",
      "/api/code-groups/{groupId}",
      "/admin/code-details?groupId={groupId}",
    ]);
    expect(viewModel.selectedCodeGroup?.groupId).toBe("EMPLOYMENT_STATUS");
    expect(viewModel.detailNavigationUrl).toBe(
      "/admin/code-details?groupId=EMPLOYMENT_STATUS",
    );
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state when the code group screen is protected", () => {
    const viewModel = createAdminCodeGroupsViewModel({
      path: "/admin/code-groups",
      codeGroups: [],
      selectedGroupId: null,
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
  });
});
