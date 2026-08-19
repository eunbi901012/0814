import { describe, expect, it } from "vitest";
import { createAdminCodeDetailsViewModel } from "./main";

describe("/admin/code-details browser contract", () => {
  it("renders SCR-CMN-CODE-DETAIL group-scoped search/list/detail contract", () => {
    const viewModel = createAdminCodeDetailsViewModel({
      path: "/admin/code-details?groupId=EMPLOYMENT_STATUS",
      codeDetails: [
        {
          groupId: "EMPLOYMENT_STATUS",
          codeValue: "ACTIVE",
          codeName: "재직",
          parentCodeValue: null,
          sortOrder: 1,
          extraAttributes: '{"source":"local-seed"}',
          useYn: "Y",
          validFrom: "2026-01-01",
          validTo: null,
          depth: 0,
          updatedAt: "2026-01-01T09:00:00",
        },
      ],
      selectedCodeKey: "EMPLOYMENT_STATUS::ACTIVE",
      status: "success",
      message: "상세코드가 저장되었습니다.",
    });

    expect(viewModel.route).toBe(
      "/admin/code-details?groupId=EMPLOYMENT_STATUS",
    );
    expect(viewModel.screenId).toBe("SCR-CMN-CODE-DETAIL");
    expect(viewModel.menuPath).toBe(
      "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    );
    expect(viewModel.filters).toEqual([
      "그룹ID",
      "코드값/코드명",
      "사용여부",
      "유효일자",
    ]);
    expect(viewModel.columns).toEqual([
      "그룹ID",
      "코드값",
      "코드명",
      "상위코드",
      "정렬순서",
      "추가속성",
      "사용여부",
      "유효기간",
    ]);
    expect(viewModel.detailFields).toEqual([
      "group_id readonly on edit",
      "code_value readonly on edit",
      "코드명",
      "상위코드",
      "정렬순서",
      "추가속성",
      "사용여부",
      "유효 시작일",
      "유효 종료일",
    ]);
    expect(viewModel.actions).toEqual([
      "/api/code-details",
      "/api/code-details/{groupId}/{codeValue}",
      "/admin/code-groups",
    ]);
    expect(viewModel.selectedCodeDetail?.codeValue).toBe("ACTIVE");
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state when the code detail screen is protected", () => {
    const viewModel = createAdminCodeDetailsViewModel({
      path: "/admin/code-details",
      codeDetails: [],
      selectedCodeKey: null,
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
  });
});
