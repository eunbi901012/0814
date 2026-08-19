import { describe, expect, it } from "vitest";
import { createAdminOrganizationsViewModel } from "./main";

describe("/admin/organizations browser contract", () => {
  it("renders the SCR-CMN-ORG route with source-backed filters, tree hierarchy, readonly source info, and relationship save action", () => {
    const viewModel = createAdminOrganizationsViewModel({
      path: "/admin/organizations",
      organizations: [
        {
          orgCode: "KNUE",
          orgName: "한국교원대학교",
          orgType: "UNIVERSITY",
          parentOrgCode: null,
          parentOrgName: null,
          useYn: "Y",
          depth: 0,
          childOrgCodes: ["KNUE-DEPT-COMMON"],
          updatedAt: "2026-01-01T09:00:00",
          currentRelationship: null,
        },
        {
          orgCode: "KNUE-DEPT-COMMON",
          orgName: "공통기능학과",
          orgType: "DEPARTMENT",
          parentOrgCode: "KNUE",
          parentOrgName: "한국교원대학교",
          useYn: "Y",
          depth: 1,
          childOrgCodes: [],
          updatedAt: "2026-01-01T09:00:00",
          currentRelationship: {
            parentOrgCode: "KNUE",
            parentOrgName: "한국교원대학교",
            effectiveStartDate: "2026-01-01",
            effectiveEndDate: null,
            reason: "초기 조직 계층 seed",
          },
        },
      ],
      selectedOrgCode: "KNUE-DEPT-COMMON",
      status: "success",
      message: "관계/기간이 저장되었습니다.",
    });

    expect(viewModel.route).toBe("/admin/organizations");
    expect(viewModel.screenId).toBe("SCR-CMN-ORG");
    expect(viewModel.filters).toEqual(["조직코드", "조직구분"]);
    expect(viewModel.sourceColumns).toEqual([
      "조직코드",
      "조직명",
      "조직구분",
      "상위조직",
      "사용여부",
    ]);
    expect(viewModel.actions).toEqual([
      "/api/organizations",
      "/api/organizations/{orgCode}/relationships",
    ]);
    expect(viewModel.selectedOrganization?.orgCode).toBe("KNUE-DEPT-COMMON");
    expect(viewModel.treeRows[1].depth).toBe(1);
    expect(viewModel.status).toBe("success");
  });

  it("shows permission state outside the R09 organization route", () => {
    const viewModel = createAdminOrganizationsViewModel({
      path: "/forbidden",
      organizations: [],
      selectedOrgCode: null,
      status: "permission",
    });

    expect(viewModel.status).toBe("permission");
    expect(viewModel.permissionMessage).toBe("시스템관리자 권한이 필요합니다.");
  });
});
