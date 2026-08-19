import { describe, expect, it } from "vitest";
import { createVisibleAdminNavigation } from "./main";

describe("frontend menu hiding contract", () => {
  it("hides protected admin menus that are not present in the permission preview", () => {
    const navigation = createVisibleAdminNavigation([
      "/admin/users",
      "/admin/code-groups",
    ]);

    expect(
      navigation.flatMap((group) => group.items.map((item) => item.href)),
    ).toEqual(["/admin/users", "/admin/code-groups"]);
    expect(
      navigation.flatMap((group) => group.items.map((item) => item.label)),
    ).not.toContain("메뉴 권한 관리");
    expect(
      navigation.flatMap((group) => group.items.map((item) => item.label)),
    ).not.toContain("상세코드 관리");
  });

  it("keeps only groups with at least one visible child menu", () => {
    const navigation = createVisibleAdminNavigation([
      "/admin/menu-permissions",
    ]);

    expect(navigation).toEqual([
      {
        label: "역할·권한 관리",
        items: [{ label: "메뉴 권한 관리", href: "/admin/menu-permissions" }],
      },
    ]);
  });
});
