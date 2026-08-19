CREATE TABLE IF NOT EXISTS organization (
    org_code varchar(40) PRIMARY KEY,
    org_name varchar(200) NOT NULL,
    org_type varchar(40) NOT NULL CHECK (org_type IN ('UNIVERSITY', 'GRADUATE_SCHOOL', 'COLLEGE', 'DEPARTMENT', 'OFFICE')),
    parent_org_code varchar(40) REFERENCES organization(org_code),
    use_yn char(1) NOT NULL CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE organization IS 'KORUS Mock snapshot 조직 원천값의 조회 기준이다. 조직 관계 변경은 organization_relation_history 보정 이력으로 저장한다.';
COMMENT ON COLUMN organization.org_type IS 'UNIVERSITY:대학|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|OFFICE:부서';
COMMENT ON COLUMN organization.parent_org_code IS 'organization.org_code 참조 의도 (자기참조 FK 선언)';
COMMENT ON COLUMN organization.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS internal_account (
    user_id varchar(40) PRIMARY KEY,
    login_id varchar(100) NOT NULL UNIQUE,
    password_hash varchar(128) NOT NULL,
    name varchar(100) NOT NULL,
    department_code varchar(40) REFERENCES organization(org_code),
    position_name varchar(100),
    employment_status varchar(30) CHECK (employment_status IN ('ACTIVE', 'RETIRED', 'LEAVE')),
    system_use_yn char(1) NOT NULL CHECK (system_use_yn IN ('Y', 'N')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE internal_account IS '내부 로그인과 시스템 사용여부의 로컬 원천 테이블이다. KORUS 인사 원천정보는 snapshot에서 조회하고 이 테이블은 인증과 사용여부를 관리한다.';
COMMENT ON COLUMN internal_account.department_code IS 'organization.org_code 참조 의도 (FK 선언)';
COMMENT ON COLUMN internal_account.employment_status IS 'ACTIVE:재직|RETIRED:퇴직|LEAVE:휴직';
COMMENT ON COLUMN internal_account.system_use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS korus_faculty_snapshot (
    snapshot_id varchar(40) PRIMARY KEY,
    employee_no varchar(40) NOT NULL UNIQUE,
    name varchar(100) NOT NULL,
    org_code varchar(40) REFERENCES organization(org_code),
    rank_name varchar(100),
    retirement_date date,
    last_synced_at timestamp NOT NULL,
    status varchar(30) NOT NULL CHECK (status IN ('ACTIVE', 'RETIRED', 'LEAVE')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE korus_faculty_snapshot IS '외부 KORUS 접속 없이 로컬 Docker Compose에서 사용하는 조회 전용 교직원 Mock snapshot이다. 애플리케이션은 직접 수정 API를 제공하지 않는다.';
COMMENT ON COLUMN korus_faculty_snapshot.org_code IS 'organization.org_code 참조 의도 (FK 선언)';
COMMENT ON COLUMN korus_faculty_snapshot.status IS 'ACTIVE:재직|RETIRED:퇴직|LEAVE:휴직';
COMMENT ON COLUMN korus_faculty_snapshot.last_synced_at IS 'Flyway seed 또는 향후 snapshot 동기화 어댑터가 snapshot 적재 시 갱신';

CREATE TABLE IF NOT EXISTS organization_relation_history (
    history_id varchar(40) PRIMARY KEY,
    org_code varchar(40) NOT NULL REFERENCES organization(org_code),
    parent_org_code varchar(40) REFERENCES organization(org_code),
    effective_start_date date NOT NULL,
    effective_end_date date,
    reason varchar(500),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_org_relation_period CHECK (effective_end_date IS NULL OR effective_start_date <= effective_end_date)
);

COMMENT ON TABLE organization_relation_history IS '조직 개편 시 상위조직 관계와 적용기간의 로컬 보정 이력을 보존한다. KORUS 원천 조직 필드는 변경하지 않는다.';
COMMENT ON COLUMN organization_relation_history.org_code IS 'organization.org_code 참조 의도 (FK 선언)';
COMMENT ON COLUMN organization_relation_history.parent_org_code IS 'organization.org_code 참조 의도 (FK 선언)';

CREATE TABLE IF NOT EXISTS role (
    role_code varchar(10) PRIMARY KEY,
    role_name varchar(100) NOT NULL,
    purpose varchar(500) NOT NULL,
    grant_criteria varchar(500),
    data_scope_default varchar(200),
    use_yn char(1) NOT NULL CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_role_code_seed CHECK (role_code IN ('R01', 'R02', 'R03', 'R04', 'R05', 'R06', 'R07', 'R08', 'R09'))
);

COMMENT ON TABLE role IS 'R01~R09 역할코드를 PK로 유지하고 역할명, 목적, 부여기준, 데이터 범위 기본값을 관리한다. 역할코드는 변경하지 않는다.';
COMMENT ON COLUMN role.role_code IS 'R01:교원|R02:학과장|R03:단과대학(원) 행정실|R04:교수지원과|R05:산학협력단|R06:입학인재관리과|R07:실적부서|R08:점수산출 감사자|R09:시스템관리자';
COMMENT ON COLUMN role.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS user_role (
    user_role_id varchar(40) PRIMARY KEY,
    user_id varchar(40) NOT NULL REFERENCES internal_account(user_id),
    role_code varchar(10) NOT NULL REFERENCES role(role_code),
    assignment_type varchar(30) NOT NULL CHECK (assignment_type IN ('POSITION_BASED', 'MANUAL')),
    approver_user_id varchar(40) REFERENCES internal_account(user_id),
    effective_start_date date NOT NULL,
    effective_end_date date,
    status varchar(30) NOT NULL CHECK (status IN ('ACTIVE', 'REVOKED')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_role_period CHECK (effective_end_date IS NULL OR effective_start_date <= effective_end_date)
);

COMMENT ON TABLE user_role IS '사용자별 역할, 유효기간, 승인자, 보직 기반/수동 구분을 저장한다. 회수는 물리삭제하지 않고 상태로 표시한다.';
COMMENT ON COLUMN user_role.user_id IS 'internal_account.user_id 참조 의도 (FK 선언)';
COMMENT ON COLUMN user_role.role_code IS 'role.role_code 참조 의도 (FK 선언)';
COMMENT ON COLUMN user_role.assignment_type IS 'POSITION_BASED:보직기반|MANUAL:수동';
COMMENT ON COLUMN user_role.approver_user_id IS 'internal_account.user_id 참조 의도 (FK 선언)';
COMMENT ON COLUMN user_role.status IS 'ACTIVE:활성|REVOKED:회수';

CREATE TABLE IF NOT EXISTS menu (
    menu_id varchar(40) PRIMARY KEY,
    parent_menu_id varchar(40) REFERENCES menu(menu_id),
    menu_name varchar(100) NOT NULL,
    screen_id varchar(80) UNIQUE,
    url varchar(200) UNIQUE,
    icon varchar(80),
    business_type varchar(80),
    description varchar(500),
    display_order integer NOT NULL,
    use_yn char(1) NOT NULL CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE menu IS '시스템 관리 영역의 3단계 메뉴 구조와 화면 실행정보를 관리한다. 사용 중인 메뉴는 물리삭제보다 비활성화를 우선한다.';
COMMENT ON COLUMN menu.parent_menu_id IS 'menu.menu_id 참조 의도 (자기참조 FK 선언)';
COMMENT ON COLUMN menu.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS menu_permission (
    permission_id varchar(40) PRIMARY KEY,
    target_type varchar(30) NOT NULL CHECK (target_type IN ('ROLE', 'ORGANIZATION', 'USER')),
    target_id varchar(40) NOT NULL,
    menu_id varchar(40) NOT NULL REFERENCES menu(menu_id),
    can_read boolean NOT NULL,
    can_write boolean NOT NULL,
    effect varchar(20) NOT NULL CHECK (effect IN ('ALLOW', 'DENY')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (target_type, target_id, menu_id)
);

COMMENT ON TABLE menu_permission IS '역할, 조직, 사용자 단위의 메뉴 접근 권한과 조회/변경 권한을 저장한다. 명시 DENY는 ALLOW보다 우선한다.';
COMMENT ON COLUMN menu_permission.target_type IS 'ROLE:역할|ORGANIZATION:조직|USER:사용자';
COMMENT ON COLUMN menu_permission.target_id IS 'target_type에 따라 role.role_code, organization.org_code, internal_account.user_id 참조 의도 (다형 참조로 FK 미선언)';
COMMENT ON COLUMN menu_permission.menu_id IS 'menu.menu_id 참조 의도 (FK 선언)';
COMMENT ON COLUMN menu_permission.effect IS 'ALLOW:허용|DENY:차단';

CREATE TABLE IF NOT EXISTS code_group (
    group_id varchar(60) PRIMARY KEY,
    group_name varchar(100) NOT NULL,
    description varchar(500),
    managing_department varchar(100),
    use_yn char(1) NOT NULL CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE code_group IS '공통코드 그룹의 그룹ID, 명칭, 설명, 관리부서와 사용여부를 관리한다. 사용 중인 그룹은 물리삭제보다 미사용 처리를 우선한다.';
COMMENT ON COLUMN code_group.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS code_detail (
    group_id varchar(60) NOT NULL REFERENCES code_group(group_id),
    code_value varchar(60) NOT NULL,
    code_name varchar(100) NOT NULL,
    parent_code_value varchar(60),
    sort_order integer NOT NULL,
    extra_attributes jsonb,
    use_yn char(1) NOT NULL CHECK (use_yn IN ('Y', 'N')),
    valid_from date,
    valid_to date,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, code_value),
    CONSTRAINT ck_code_detail_period CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_from <= valid_to)
);

COMMENT ON TABLE code_detail IS '코드그룹별 상세코드, 상위코드, 정렬순서, 추가속성, 유효기간을 관리한다. 사용 중인 코드는 물리삭제보다 미사용 처리를 우선한다.';
COMMENT ON COLUMN code_detail.group_id IS 'code_group.group_id 참조 의도 (FK 선언)';
COMMENT ON COLUMN code_detail.parent_code_value IS '같은 group_id의 code_detail.code_value 참조 의도 (계층 관계, 복합 FK 미선언)';
COMMENT ON COLUMN code_detail.extra_attributes IS 'CodeDetailService.create/update 시 애플리케이션에서 갱신하는 연계 코드 매핑용 JSON 속성';
COMMENT ON COLUMN code_detail.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS session (
    session_id varchar(80) PRIMARY KEY,
    user_id varchar(40) NOT NULL REFERENCES internal_account(user_id),
    expires_at timestamp NOT NULL,
    status varchar(30) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED')),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE session IS 'HttpOnly SameSite=Lax 쿠키 기반 내부 인증 세션을 저장한다. 로그아웃 또는 만료 시 상태를 EXPIRED로 변경한다.';
COMMENT ON COLUMN session.user_id IS 'internal_account.user_id 참조 의도 (FK 선언)';
COMMENT ON COLUMN session.status IS 'ACTIVE:활성|EXPIRED:만료';

CREATE INDEX IF NOT EXISTS idx_internal_account_login_id ON internal_account(login_id);
CREATE INDEX IF NOT EXISTS idx_internal_account_department ON internal_account(department_code);
CREATE INDEX IF NOT EXISTS idx_korus_faculty_employee_no ON korus_faculty_snapshot(employee_no);
CREATE INDEX IF NOT EXISTS idx_korus_faculty_org_code ON korus_faculty_snapshot(org_code);
CREATE INDEX IF NOT EXISTS idx_organization_parent ON organization(parent_org_code);
CREATE INDEX IF NOT EXISTS idx_org_relation_org_period ON organization_relation_history(org_code, effective_start_date);
CREATE INDEX IF NOT EXISTS idx_user_role_user_status ON user_role(user_id, status);
CREATE INDEX IF NOT EXISTS idx_user_role_role_status ON user_role(role_code, status);
CREATE INDEX IF NOT EXISTS idx_menu_parent_order ON menu(parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_menu_permission_target ON menu_permission(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_code_detail_group_sort ON code_detail(group_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_session_user_status ON session(user_id, status);
CREATE INDEX IF NOT EXISTS idx_session_expires_at ON session(expires_at);
