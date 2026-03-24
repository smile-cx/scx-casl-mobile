import XCTest
@testable import SCXCASL

final class RealWorldPermissionTests: XCTestCase {

    // Full perms JSON array from real API response
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

    private func buildAbility() throws -> Ability {
        let rules = try RawRule.listFromJSON(Self.permsJSON)
        return Ability.fromRules(rules)
    }

    func testShortenerCreateIsFalse() throws {
        let ability = try buildAbility()
        XCTAssertFalse(ability.can("create", "Shortener"), "Shortener.create should be false")
    }

    func testShortenerGoIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("go", "Shortener"), "Shortener.go should be true")
    }

    func testMessageInfoIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("info", "Message"), "Message.info should be true")
    }

    func testMessageAttachIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("attach", "Message"), "Message.attach should be true")
    }

    func testContactMethodQueryIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("query", "ContactMethod"), "ContactMethod.query should be true")
    }

    func testUserTimesheetIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("timesheet", "User"), "User.timesheet should be true")
    }

    func testTimesheetCreateRequestsIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("create-requests", "Timesheet"), "Timesheet.create-requests should be true")
    }

    func testTimesheetCancelRequestsIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("cancel-requests", "Timesheet"), "Timesheet.cancel-requests should be true")
    }

    func testKpiQueryIsFalse() throws {
        let ability = try buildAbility()
        XCTAssertFalse(ability.can("query", "Kpi"), "Kpi.query should be false")
    }

    func testKpiAllIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("all", "Kpi"), "Kpi.all should be true")
    }

    func testUserQueryForAppCallIdentificationIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("query-for-app-call-identification", "User"), "User.query-for-app-call-identification should be true")
    }

    func testUserQueryForAddressBookIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("query-for-address-book", "User"), "User.query-for-address-book should be true")
    }

    func testMobileDownloadAddressBookIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("download-address-book", "Mobile"), "Mobile.download-address-book should be true")
    }

    func testVivochaSmartChannelIsFalse() throws {
        let ability = try buildAbility()
        XCTAssertFalse(ability.can("smartChannel", "Vivocha"), "Vivocha.smartChannel should be false")
    }

    func testMobileCheckForUpdateIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("check-for-update", "Mobile"), "Mobile.check-for-update should be true")
    }

    func testUserGetAvatarUploadUrlIsFalse() throws {
        let ability = try buildAbility()
        XCTAssertFalse(ability.can("get-avatar-upload-url", "User"), "User.get-avatar-upload-url should be false")
    }

    func testMobileLearningAppIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("learning-app", "Mobile"), "Mobile.learning-app should be true")
    }

    func testMobileFeedbackIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("feedback", "Mobile"), "Mobile.feedback should be true")
    }

    func testNewsSendIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("send", "News"), "News.send should be true")
    }

    func testTimesheetBootstrapModeSuperadminIsFalse() throws {
        let ability = try buildAbility()
        XCTAssertFalse(ability.can("bootstrap-mode-superadmin", "Timesheet"), "Timesheet.bootstrap-mode-superadmin should be false")
    }

    func testTimesheetBootstrapModeTeamIsFalse() throws {
        let ability = try buildAbility()
        XCTAssertFalse(ability.can("bootstrap-mode-team", "Timesheet"), "Timesheet.bootstrap-mode-team should be false")
    }

    func testTimesheetBootstrapModeChildrenIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("bootstrap-mode-children", "Timesheet"), "Timesheet.bootstrap-mode-children should be true")
    }

    func testTimesheetHandleMultipleRequestsIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("handle-multiple-requests", "Timesheet"), "Timesheet.handle-multiple-requests should be true")
    }

    func testTimesheetCreateRequestsAdminIsFalse() throws {
        let ability = try buildAbility()
        XCTAssertFalse(ability.can("create-requests-admin", "Timesheet"), "Timesheet.create-requests-admin should be false")
    }

    func testMobileApplicationMyticketIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("myticket", "MobileApplication"), "MobileApplication.myticket should be true")
    }

    func testTimesheetShowBalanceToAdminIsTrue() throws {
        let ability = try buildAbility()
        XCTAssertTrue(ability.can("show-balance-to-admin", "Timesheet"), "Timesheet.show-balance-to-admin should be true")
    }

    func testVivochaOldSupportIsFalseDueToInversion() throws {
        // Vivocha.oldSupport has two rules: one regular and one inverted (last).
        // The inverted one comes last, so it takes precedence => false
        let ability = try buildAbility()
        XCTAssertFalse(ability.can("oldSupport", "Vivocha"), "Vivocha.oldSupport should be false (inverted rule takes precedence)")
    }
}
