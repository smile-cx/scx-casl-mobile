import XCTest
@testable import SCXCASL

final class SmileFrontendTests: XCTestCase {

    // Full perms JSON array from real API response (same as RealWorldPermissionTests)
    private static let permsJSON = """
    [
        {"subject": "Mobile", "action": "check-for-update"},
        {"subject": "Employee", "action": "roles"},
        {"subject": "User", "action": ["get-hierarchy", "get-children"], "_conditions": {"facets.sparta.corporate.id": {"$in": [1, 2]}}, "fields": ["_id", "firstname", "lastname", "facets.sparta.idSparta", "facets.sparta.company.**", "facets.sparta.corporate.**", "facets.sparta.team.**", "facets.sparta.site.**", "facets.sparta.country.**", "roles.**", "isActive"]},
        {"subject": "_User", "action": ["get-hierarchy", "get-children"], "conditions": {"facets.sparta.company.id": {"$in": []}}, "fields": ["_id", "firstname", "lastname", "facets.sparta.idSparta", "facets.sparta.company.**", "facets.sparta.corporate.**", "facets.sparta.team.**", "facets.sparta.site.**", "facets.sparta.country.**", "roles.**", "isActive"]},
        {"subject": "_User", "action": ["get-hierarchy", "get-children"], "conditions": {"facets.sparta.company.id": 30}, "fields": ["_id", "firstname", "lastname", "facets.sparta.idSparta", "facets.sparta.company.**", "facets.sparta.corporate.**", "facets.sparta.team.**", "facets.sparta.site.**", "facets.sparta.country.**", "roles.**", "isActive"]},
        {"subject": "User", "action": "query", "conditions": {"facets.smart.tags": "vivochaSupport"}, "fields": ["_id", "firstname", "lastname", "isActive"]},
        {"subject": "User", "action": "query", "fields": ["facets.sparta.contract.**", "facets.sparta.bankInfo.**", "facets.sparta.campaigns.**", "facets.sparta.serial", "facets.sparta.logs.**", "facets.sparta.notes", "facets.sparta.qualification.**", "facets.sparta.hashedObj", "facets.sparta.tfrDocumentationDeliveryDate", "facets.sparta.oldUserId", "gender", "email"], "inverted": true},
        {"subject": "Role", "action": ["query", "read"]},
        {"subject": "Sparta", "action": ["services", "index"]},
        {"subject": "Docs", "action": "assets"},
        {"subject": "Script", "action": ["legacyList", "legacyGet"]},
        {"subject": "Channel", "action": "create-token"},
        {"subject": "SpartaReport", "action": "yellow", "conditions": {"Team": "SCX Product UX & UI"}},
        {"subject": "SpartaReport", "action": "yellow", "conditions": {"GruppoPianificazione": "Technology & People"}},
        {"subject": "Application", "action": "query", "conditions": {"id": {"$in": ["_sparta-ai", "smart-meet"]}}},
        {"subject": "RAS", "action": "producer-legacy"},
        {"subject": "User", "action": "query-read", "fields": ["_id", "firstname", "lastname", "facets.sparta.idSparta", "facets.sparta.corporate.**", "facets.smart.tags"]},
        {"subject": "Audio", "action": "transcribe"},
        {"subject": "User", "action": ["kpi-dashboards", "set-workplace"]},
        {"subject": "Timing", "action": ["get-time", "get-start-time"]},
        {"subject": "Portfolio", "action": ["query", "scripts"]},
        {"subject": "Vivocha", "action": "oldSupport"},
        {"subject": "Badge", "action": "create"},
        {"subject": "Timesheet", "action": ["approve-requests", "reject-requests"]},
        {"subject": "Kpi", "action": ["get-action-reporting", "get-user-action-reporting"]},
        {"subject": "StringOperation", "action": "query"},
        {"subject": "Application", "action": "query", "conditions": {"id": "sparta-ai-2"}},
        {"subject": "Apps", "action": "sparta2"},
        {"subject": "RAS", "action": ["producer"]},
        {"subject": "Script", "action": ["list", "read"]},
        {"subject": "Campaign", "action": ["legacyList", "query-personal"]},
        {"subject": "Customer", "action": ["read-customers", "upsert", "update-status"]},
        {"subject": "KPIPredictive", "action": "query"},
        {"subject": "User", "action": "query", "conditions": {"featureUser": true}, "fields": ["_id", "firstname", "lastname", "fullname", "isActive", "featureUser", "facets.smart.tags"]},
        {"subject": "User", "action": "can"},
        {"subject": "Environment", "action": "prod"},
        {"subject": "User", "action": ["me", "me-light", "session-and-redirect"]},
        {"subject": "User", "action": "patch", "fields": ["settings.**"]},
        {"subject": "User", "action": ["get-participants", "get-admins"], "fields": ["_id", "firstname", "lastname", "gender", "facets.sparta.idSparta", "roles.roleName"]},
        {"subject": "Status", "action": ["post", "get", "get-multiple"]},
        {"subject": "Channel", "action": ["query", "get-statuses", "read"]},
        {"subject": "Channel", "action": ["get-participants", "get-admins"], "fields": ["_id", "firstname", "lastname", "gender", "isGuest", "roles.roleName"]},
        {"subject": "Message", "action": ["get-channel-messages", "get-private-messages", "get-notifications", "clean-notifications", "get-last-channels-interactions", "get-last-direct-interactions", "attach", "send-message", "send-ack", "feed-query", "info"]},
        {"subject": "String", "action": ["query", "read"]},
        {"subject": "Application", "action": "query", "conditions": {"id": {"$in": ["smart-home", "smart-rooms"]}}},
        {"subject": "UserDevice", "action": ["register", "unregister"]},
        {"subject": "Watermark", "action": "read"},
        {"subject": "UserSettings", "action": ["read", "update", "patch"]},
        {"subject": "Shortener", "action": ["read", "go"]},
        {"subject": "Transcription", "action": "attach"},
        {"subject": "ContactMethod", "action": ["query", "upsert", "confirm", "remove"]},
        {"subject": "Application", "action": "track"},
        {"subject": "ApplicationUserSettings", "action": ["read", "update", "remove", "create"]},
        {"subject": "Mobile", "action": "check-for-update"},
        {"subject": "KpisConfiguration", "action": ["query"]},
        {"subject": "Development", "action": "test-timing"},
        {"subject": "PrivacyPolicy", "action": "query", "conditions": {"userId": "5f6a22bd844d146134d6f4fc"}},
        {"subject": "PrivacyPolicy", "action": "create"},
        {"subject": "Redirect", "action": "redirect"},
        {"subject": "Application", "action": "query", "conditions": {"id": "smart-learning"}},
        {"subject": "Moodle", "action": ["_get-plans", "_get-courses", "_get-course-content", "bootstrap", "get-modules-by-course"]},
        {"subject": "Mobile", "action": "learning-app"},
        {"subject": "EmployeesOptionField", "action": ["corporate", "country", "company", "planningGroup", "site", "team", "teamType", "role", "spartarole", "single", "spartaid", "tag", "relationshipType", "taxCode", "area"]},
        {"subject": "User", "action": "options"},
        {"subject": "User", "action": "query", "fields": ["_id", "firstname", "lastname", "fullname", "facets.sparta.idSparta", "facets.sparta.company.**", "facets.sparta.corporate.**", "facets.sparta.team.**", "facets.sparta.site.**", "facets.sparta.country.**", "facets.extras.**", "facets.sparta.division.**", "facets.sparta.department.**", "facets.sparta.businessUnit.**", "roles.**", "isActive", "email", "phoneNumber", "facets.smart.tags"]},
        {"subject": "Application", "action": "query", "conditions": {"id": "smart-employees"}},
        {"subject": "CsvExportConfiguration", "action": ["query", "read", "update", "create", "delete", "remove"], "conditions": {"userId": "5f6a22bd844d146134d6f4fc"}},
        {"subject": "User", "action": "query", "fields": ["_id", "firstname", "lastname", "fullname", "featureUser", "email", "gender", "roles.**", "isActive", "dateOfBirth", "featureUser", "facets.sparta.role.**", "facets.sparta.idSparta", "facets.sparta.country.**", "facets.smart.tags"]},
        {"subject": "User", "action": ["options", "options-read"]},
        {"subject": "Role", "action": ["create", "update"]},
        {"subject": "Channel", "action": ["remove", "create", "update"]},
        {"subject": "ChannelOptionField", "action": ["corporate", "country", "company", "planningGroup", "site", "team", "role", "spartarole", "single", "spartaid", "tag", "relationshipType"]},
        {"subject": "String", "action": ["create", "remove", "update"]},
        {"subject": "Proxy", "action": ["read", "query", "remove", "patch", "update"]},
        {"subject": "ReportDashboard", "action": ["read-personal", "query-personal", "_describe"]},
        {"subject": "Application", "action": "query", "conditions": {"id": "smart-reports"}},
        {"subject": "Channel", "action": "get-all-feed"},
        {"subject": "News", "action": "send"},
        {"subject": "Channel", "action": "no-limits"},
        {"subject": "Message", "action": ["_feed-query", "proxy-content"]},
        {"subject": "Recupera", "action": "services"},
        {"subject": "MFA", "action": "force"},
        {"subject": "SmartSpartaWeb", "action": "manage"},
        {"subject": "Application", "action": "_query", "conditions": {"id": "regalonatale"}},
        {"subject": "ProxyExternalApp", "action": "regalonatale"},
        {"subject": "User", "action": "timesheet"},
        {"subject": "Application", "action": "query", "conditions": {"id": "smart-timesheet"}},
        {"subject": "Timesheet", "action": ["get-requests", "get-status", "cancel-requests", "delete-approved-requests", "approve-requests", "reject-requests", "create-requests", "calculate-estimate", "bootstrap-user"]},
        {"subject": "Link", "action": "payroll"},
        {"subject": "Application", "action": "query", "conditions": {"id": "smart-notes"}},
        {"subject": "Note", "action": "manage"},
        {"subject": "EmployeesOptionField", "action": ["isActive"]},
        {"subject": "Application", "action": "query", "conditions": {"id": "smart-appraisal"}},
        {"subject": "Appraisal", "action": ["upsert", "get"]},
        {"subject": "Appraisal", "action": "peer"},
        {"subject": "Application", "action": "query", "conditions": {"id": "docdigit"}},
        {"subject": "ProxyExternalApp", "action": "docdigit"},
        {"subject": "User", "action": "query-for-address-book", "fields": ["_id", "firstname", "lastname", "email", "phoneNumber", "facets.avatar.**", "facets.sparta.idSparta", "facets.sparta.corporate.**", "facets.sparta.company.**", "facets.sparta.country.**", "facets.sparta.site.**", "facets.sparta.team.**", "isActive"]},
        {"subject": "Mobile", "action": "download-address-book"},
        {"subject": "User", "action": "query-for-app-call-identification"},
        {"subject": "Timesheet", "action": "get-request-types", "conditions": {"id": "rol"}},
        {"subject": "Timesheet", "action": "get-request-types", "conditions": {"id": "ferie"}},
        {"subject": "Timesheet", "action": "get-request-types", "conditions": {"id": "delete-approved-request"}},
        {"subject": "Application", "action": "query", "conditions": {"id": "myticket"}},
        {"subject": "ProxyExternalApp", "action": "myticket"},
        {"subject": "MobileApplication", "action": "myticket"},
        {"subject": "Application", "action": "query", "conditions": {"id": "smart-agent-desktop"}},
        {"subject": "Portfolio", "action": ["bootstrap", "scripts", "all-skills"]},
        {"subject": "Kpi", "action": ["all"]},
        {"subject": "Vivocha", "action": "oldSupport", "inverted": true},
        {"subject": "KPIPredictive", "action": "query"},
        {"subject": "AgentStatuses", "action": "query"},
        {"subject": "Campaign", "action": ["query-personal", "read"]},
        {"subject": "Environment", "action": "mobile"},
        {"subject": "Application", "action": "query", "conditions": {"id": "centralized-logging"}},
        {"subject": "ProxyExternalApp", "action": "logging"},
        {"subject": "Mobile", "action": "feedback"},
        {"subject": "JiraTicketMobile", "action": "create"},
        {"subject": "VPN", "action": "manage"},
        {"subject": "Application", "action": "query", "conditions": {"id": "smart-cti-configurator"}},
        {"subject": "EmployeesOptionField", "action": ["single"]},
        {"subject": "User", "action": ["query", "patch"], "fields": ["facets.cti.**"]},
        {"subject": "User", "action": "query", "fields": ["_id", "firstname", "lastname", "fullname", "facets.sparta.idSparta", "facets.sparta.company.**", "facets.sparta.corporate.**", "facets.sparta.team.**", "facets.sparta.site.**", "facets.sparta.country.**", "roles.**", "isActive", "facets.cti.**"]},
        {"subject": "Application", "action": "query", "conditions": {"id": "parking-reservation-tab"}},
        {"subject": "Realtime", "action": ["details"]},
        {"subject": "CtiMonitorConfig", "action": "query"},
        {"subject": "Application", "action": "query", "conditions": {"id": "docsweb"}},
        {"subject": "Mobile", "action": "download-address-book-ios"},
        {"subject": "Timesheet", "action": "bootstrap-admin"},
        {"subject": "Timesheet", "action": "handle-multiple-requests"},
        {"subject": "Timesheet", "action": "bootstrap-mode-children"},
        {"subject": "Timesheet", "action": "show-balance-to-admin"},
        {"subject": "Realtime", "action": ["kpi-panels"]},
        {"subject": "Mails", "action": "send"},
        {"subject": "Channel", "action": "exists"},
        {"subject": "Channel", "action": "allowed-email-domains"},
        {"subject": "Avatar", "action": "avatar_show"},
        {"subject": "SmartSpartaWeb-C5", "action": "services"},
        {"subject": "Application", "action": "query", "conditions": {"id": "spartaweb-corp5"}},
        {"subject": "Application", "action": "query", "conditions": {"id": "smile-cx-docs"}},
        {"subject": "Application", "action": "query", "conditions": {"id": "centro-risorse-smilecx-gaia"}},
        {"subject": "Application", "action": "query", "conditions": {"id": "sales-marketing-intelligence-hub"}},
        {"subject": "MobileApplication", "action": "sales-marketing-intelligence-hub"},
        {"subject": "SpartaTimesheet", "action": "can-edit-sickness-hours"},
        {"subject": "SpartaTimesheet", "action": "can-edit-recovery-hours"},
        {"subject": "Application", "action": "query", "conditions": {"id": "MyTestAppWithIcon"}},
        {"subject": "OpenIDClient", "action": "studio-dev-sso"},
        {"subject": "OpenIDClient", "action": "studio-next-sso"},
        {"subject": "Application", "action": "query", "conditions": {"id": "desk-reservation-tab"}},
        {"subject": "Application", "action": "query", "conditions": {"id": "myticketglpi"}},
        {"subject": "ProxyExternalApp", "action": "myticketglpi"},
        {"subject": "OpenIDClient", "action": "studio-cx-sso"},
        {"subject": "OpenIDClient", "action": "studio-it-1-sso"}
    ]
    """

    private var ability: Ability!

    override func setUp() {
        super.setUp()
        let rules = try! RawRule.listFromJSON(Self.permsJSON)
        ability = Ability.fromRules(rules)
    }

    // MARK: - AdditionalUserInfo

    func testAdditionalUserInfoPermissions() {
        XCTAssertFalse(ability.can("login-application", "AdditionalUserInfo"))
        XCTAssertFalse(ability.can("skills", "AdditionalUserInfo"))
        XCTAssertFalse(ability.can("trade-unions", "AdditionalUserInfo"))
    }

    // MARK: - AiStudio

    func testAiStudioPermissions() {
        XCTAssertFalse(ability.can("architect", "AiStudio"))
        XCTAssertFalse(ability.can("configurator", "AiStudio"))
    }

    // MARK: - Analytics

    func testAnalyticsPermissions() {
        XCTAssertFalse(ability.can("ga-track", "Analytics"))
        XCTAssertFalse(ability.can("ga-ask-consent", "Analytics"))
        XCTAssertFalse(ability.can("hj-track", "Analytics"))
        XCTAssertFalse(ability.can("hj-ask-consent", "Analytics"))
    }

    // MARK: - Application

    func testApplicationPermissions() {
        XCTAssertTrue(ability.can("query", "Application"))
        XCTAssertFalse(ability.can("remove", "Application"))
    }

    // MARK: - AppraisalConfig

    func testAppraisalConfigPermissions() {
        XCTAssertFalse(ability.can("create", "AppraisalConfig"))
    }

    // MARK: - Audio

    func testAudioPermissions() {
        XCTAssertFalse(ability.can("remoteListen", "Audio"))
        XCTAssertFalse(ability.can("implicit-agree-remoteListen", "Audio"))
        XCTAssertTrue(ability.can("transcribe", "Audio"))
        XCTAssertFalse(ability.can("transcribe-call", "Audio"))
        XCTAssertFalse(ability.can("queue", "Audio"))
        XCTAssertFalse(ability.can("recording", "Audio"))
        XCTAssertFalse(ability.can("autoRecording", "Audio"))
    }

    // MARK: - Avatar

    func testAvatarPermissions() {
        XCTAssertTrue(ability.can("avatar_show", "Avatar"))
    }

    // MARK: - Channel

    func testChannelPermissions() {
        XCTAssertTrue(ability.can("create", "Channel"))
        XCTAssertTrue(ability.can("create-token", "Channel"))
        XCTAssertTrue(ability.can("no-limits", "Channel"))
    }

    // MARK: - ChannelOptionField

    func testChannelOptionFieldPermissions() {
        XCTAssertTrue(ability.can("corporate", "ChannelOptionField"))
        XCTAssertTrue(ability.can("country", "ChannelOptionField"))
        XCTAssertTrue(ability.can("company", "ChannelOptionField"))
        XCTAssertTrue(ability.can("planningGroup", "ChannelOptionField"))
        XCTAssertTrue(ability.can("site", "ChannelOptionField"))
        XCTAssertTrue(ability.can("team", "ChannelOptionField"))
        XCTAssertTrue(ability.can("role", "ChannelOptionField"))
        XCTAssertTrue(ability.can("spartarole", "ChannelOptionField"))
        XCTAssertTrue(ability.can("single", "ChannelOptionField"))
        XCTAssertTrue(ability.can("spartaid", "ChannelOptionField"))
        XCTAssertTrue(ability.can("tag", "ChannelOptionField"))
        XCTAssertTrue(ability.can("relationshipType", "ChannelOptionField"))
    }

    // MARK: - Chat

    func testChatPermissions() {
        XCTAssertFalse(ability.can("hide", "Chat"))
    }

    // MARK: - ContactMethod

    func testContactMethodPermissions() {
        XCTAssertFalse(ability.can("otp-supervisor", "ContactMethod"))
        XCTAssertTrue(ability.can("query", "ContactMethod"))
    }

    // MARK: - DynamicRole

    func testDynamicRolePermissions() {
        XCTAssertFalse(ability.can("query", "DynamicRole"))
        XCTAssertFalse(ability.can("update", "DynamicRole"))
        XCTAssertFalse(ability.can("patch", "DynamicRole"))
    }

    // MARK: - Employee

    func testEmployeePermissions() {
        XCTAssertFalse(ability.can("contract-history", "Employee"))
        XCTAssertFalse(ability.can("dynamic-role-json", "Employee"))
        XCTAssertFalse(ability.can("info", "Employee"))
        XCTAssertFalse(ability.can("permissions", "Employee"))
        XCTAssertFalse(ability.can("reports", "Employee"))
        XCTAssertTrue(ability.can("roles", "Employee"))
        XCTAssertFalse(ability.can("site-assignment", "Employee"))
        XCTAssertFalse(ability.can("team-assignment", "Employee"))
    }

    // MARK: - EmployeesOptionField

    func testEmployeesOptionFieldPermissions() {
        XCTAssertTrue(ability.can("area", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("company", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("corporate", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("country", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("planningGroup", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("relationshipType", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("role", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("site", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("tag", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("team", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("single", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("spartaid", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("spartarole", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("taxCode", "EmployeesOptionField"))
        XCTAssertTrue(ability.can("isActive", "EmployeesOptionField"))
    }

    // MARK: - Environment

    func testEnvironmentPermissions() {
        XCTAssertTrue(ability.can("mobile", "Environment"))
        XCTAssertFalse(ability.can("dev", "Environment"))
        XCTAssertTrue(ability.can("prod", "Environment"))
        XCTAssertFalse(ability.can("stage", "Environment"))
    }

    // MARK: - ExtrasOptionField

    func testExtrasOptionFieldPermissions() {
        XCTAssertFalse(ability.can("wildCard", "ExtrasOptionField"))
    }

    // MARK: - Kpi

    func testKpiPermissions() {
        XCTAssertTrue(ability.can("all", "Kpi"))
        XCTAssertFalse(ability.can("query", "Kpi"))
    }

    // MARK: - KpisConfiguration

    func testKpisConfigurationPermissions() {
        XCTAssertFalse(ability.can("create", "KpisConfiguration"))
        XCTAssertFalse(ability.can("update", "KpisConfiguration"))
        XCTAssertFalse(ability.can("delete", "KpisConfiguration"))
    }

    // MARK: - kpi-dashboards (as subject)

    func testKpiDashboardsPermissions() {
        XCTAssertFalse(ability.can("create", "kpi-dashboards"))
        XCTAssertFalse(ability.can("update", "kpi-dashboards"))
        XCTAssertFalse(ability.can("delete", "kpi-dashboards"))
    }

    // MARK: - Layout

    func testLayoutPermissions() {
        XCTAssertFalse(ability.can("new", "Layout"))
    }

    // MARK: - Library

    func testLibraryPermissions() {
        XCTAssertFalse(ability.can("library", "Library"))
    }

    // MARK: - Link

    func testLinkPermissions() {
        XCTAssertTrue(ability.can("payroll", "Link"))
    }

    // MARK: - Mails

    func testMailsPermissions() {
        XCTAssertTrue(ability.can("send", "Mails"))
    }

    // MARK: - Message

    func testMessagePermissions() {
        XCTAssertTrue(ability.can("attach", "Message"))
        XCTAssertTrue(ability.can("feed-query", "Message"))
    }

    // MARK: - Moodle

    func testMoodlePermissions() {
        XCTAssertTrue(ability.can("bootstrap", "Moodle"))
        XCTAssertFalse(ability.can("tab-competencies", "Moodle"))
        XCTAssertFalse(ability.can("get-tenants", "Moodle"))
        XCTAssertFalse(ability.can("get-all-courses", "Moodle"))
        XCTAssertFalse(ability.can("get-all-plans", "Moodle"))
        XCTAssertFalse(ability.can("get-courses-by-tenant", "Moodle"))
        XCTAssertFalse(ability.can("get-plans-by-tenant", "Moodle"))
    }

    // MARK: - MoodlePipeline

    func testMoodlePipelinePermissions() {
        XCTAssertFalse(ability.can("create", "MoodlePipeline"))
        XCTAssertFalse(ability.can("update", "MoodlePipeline"))
        XCTAssertFalse(ability.can("query", "MoodlePipeline"))
        XCTAssertFalse(ability.can("run", "MoodlePipeline"))
        XCTAssertFalse(ability.can("remove", "MoodlePipeline"))
    }

    // MARK: - News

    func testNewsPermissions() {
        XCTAssertTrue(ability.can("send", "News"))
    }

    // MARK: - Passepartout

    func testPassepartoutPermissions() {
        XCTAssertFalse(ability.can("query", "Passepartout"))
        XCTAssertFalse(ability.can("create", "Passepartout"))
    }

    // MARK: - Realtime

    func testRealtimePermissions() {
        XCTAssertTrue(ability.can("details", "Realtime"))
        XCTAssertTrue(ability.can("kpi-panels", "Realtime"))
        XCTAssertFalse(ability.can("agents-table", "Realtime"))
    }

    // MARK: - ReportDashboard

    func testReportDashboardPermissions() {
        XCTAssertFalse(ability.can("create", "ReportDashboard"))
        XCTAssertFalse(ability.can("manage-quicksight", "ReportDashboard"))
        XCTAssertFalse(ability.can("manage-superset", "ReportDashboard"))
        XCTAssertFalse(ability.can("manage-grafana", "ReportDashboard"))
    }

    // MARK: - Role

    func testRolePermissions() {
        XCTAssertTrue(ability.can("create", "Role"))
        XCTAssertTrue(ability.can("update", "Role"))
    }

    // MARK: - ScriptExternal

    func testScriptExternalPermissions() {
        XCTAssertFalse(ability.can("create", "ScriptExternal"))
    }

    // MARK: - Shortener

    func testShortenerPermissions() {
        XCTAssertFalse(ability.can("create-temporary-meet", "Shortener"))
    }

    // MARK: - Sparta

    func testSpartaPermissions() {
        XCTAssertTrue(ability.can("index", "Sparta"))
    }

    // MARK: - Timesheet

    func testTimesheetPermissions() {
        XCTAssertFalse(ability.can("viewer", "Timesheet"))
        XCTAssertTrue(ability.can("create-requests", "Timesheet"))
        XCTAssertTrue(ability.can("cancel-requests", "Timesheet"))
        XCTAssertFalse(ability.can("query-planned-workplace", "Timesheet"))
        XCTAssertTrue(ability.can("bootstrap-admin", "Timesheet"))
        XCTAssertFalse(ability.can("bootstrap-mode-team", "Timesheet"))
        XCTAssertTrue(ability.can("bootstrap-mode-children", "Timesheet"))
        XCTAssertFalse(ability.can("bootstrap-mode-superAdmin", "Timesheet"))
        XCTAssertFalse(ability.can("create-requests-admin", "Timesheet"))
        XCTAssertTrue(ability.can("show-balance-to-admin", "Timesheet"))
    }

    // MARK: - Transcription

    func testTranscriptionPermissions() {
        XCTAssertFalse(ability.can("editScore", "Transcription"))
        XCTAssertFalse(ability.can("hear-audio", "Transcription"))
        XCTAssertFalse(ability.can("pipeline", "Transcription"))
        XCTAssertFalse(ability.can("query", "Transcription"))
        XCTAssertFalse(ability.can("read", "Transcription"))
        XCTAssertFalse(ability.can("view-json", "Transcription"))
        XCTAssertFalse(ability.can("not_disguise", "Transcription"))
    }

    // MARK: - UploaderConfig

    func testUploaderConfigPermissions() {
        XCTAssertFalse(ability.can("download-data", "UploaderConfig"))
    }

    // MARK: - User

    func testUserPermissions() {
        XCTAssertFalse(ability.can("patch-extras", "User"))
        XCTAssertFalse(ability.can("patch-perms", "User"))
        XCTAssertFalse(ability.can("query-for-app-role", "User"))
        XCTAssertTrue(ability.can("query", "User"))
        XCTAssertFalse(ability.can("reset-auth", "User"))
        XCTAssertFalse(ability.can("update-role", "User"))
        XCTAssertFalse(ability.can("delete", "User"))
        XCTAssertFalse(ability.can("dynamic-field", "User"))
        XCTAssertFalse(ability.can("update-uid", "User"))
    }

    // MARK: - UserImport

    func testUserImportPermissions() {
        XCTAssertFalse(ability.can("create-request", "UserImport"))
        XCTAssertFalse(ability.can("confirm-request", "UserImport"))
    }

    // MARK: - UserTemplate

    func testUserTemplatePermissions() {
        XCTAssertFalse(ability.can("query", "UserTemplate"))
    }

    // MARK: - Vivocha

    func testVivochaPermissions() {
        XCTAssertFalse(ability.can("oldSupport", "Vivocha"))
        XCTAssertFalse(ability.can("smartChannel", "Vivocha"))
    }

    // MARK: - VPN

    func testVPNPermissions() {
        XCTAssertTrue(ability.can("manage", "VPN"))
    }

    // MARK: - VpnGrant

    func testVpnGrantPermissions() {
        XCTAssertFalse(ability.can("manage", "VpnGrant"))
        XCTAssertFalse(ability.can("create", "VpnGrant"))
        XCTAssertFalse(ability.can("update", "VpnGrant"))
        XCTAssertFalse(ability.can("remove", "VpnGrant"))
    }

    // MARK: - VPNServer

    func testVPNServerPermissions() {
        XCTAssertFalse(ability.can("manage", "VPNServer"))
        XCTAssertFalse(ability.can("query", "VPNServer"))
        XCTAssertFalse(ability.can("create", "VPNServer"))
        XCTAssertFalse(ability.can("create-network", "VPNServer"))
        XCTAssertFalse(ability.can("sync", "VPNServer"))
        XCTAssertFalse(ability.can("sync-all", "VPNServer"))
        XCTAssertFalse(ability.can("update", "VPNServer"))
        XCTAssertFalse(ability.can("remove", "VPNServer"))
        XCTAssertFalse(ability.can("update-network", "VPNServer"))
        XCTAssertFalse(ability.can("delete-network", "VPNServer"))
    }
}
