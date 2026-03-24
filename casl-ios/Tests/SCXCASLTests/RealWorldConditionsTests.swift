import XCTest
@testable import SCXCASL

final class RealWorldConditionsTests: XCTestCase {

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

    private let options = AbilityOptions(
        conditionsMatcher: ConditionsMatcher.match,
        fieldMatcher: FieldMatcher.match
    )

    override func setUp() {
        super.setUp()
        let rules = try! RawRule.listFromJSON(Self.permsJSON)
        ability = Ability(rules: rules, options: options)
    }

    // MARK: - Helper

    /// Build a typed subject dictionary with __caslSubjectType__ set.
    private func subject(_ type: String, _ data: [String: Any] = [:]) -> [String: Any] {
        var result = data
        result["__caslSubjectType__"] = type
        return result
    }

    // MARK: - Application.query with $in conditions

    func testApplicationQuerySmartMeetIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-meet"])))
    }

    func testApplicationQuerySpartaAiIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "_sparta-ai"])))
    }

    func testApplicationQuerySpartaAi2IsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "sparta-ai-2"])))
    }

    func testApplicationQuerySmartHomeIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-home"])))
    }

    func testApplicationQuerySmartRoomsIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-rooms"])))
    }

    func testApplicationQuerySmartLearningIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-learning"])))
    }

    func testApplicationQuerySmartEmployeesIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-employees"])))
    }

    func testApplicationQuerySmartTimesheetIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-timesheet"])))
    }

    func testApplicationQuerySmartReportsIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-reports"])))
    }

    func testApplicationQueryMyticketIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "myticket"])))
    }

    func testApplicationQuerySmartNotesIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-notes"])))
    }

    func testApplicationQuerySmartAppraisalIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-appraisal"])))
    }

    func testApplicationQueryDocdigitIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "docdigit"])))
    }

    func testApplicationQuerySmartAgentDesktopIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-agent-desktop"])))
    }

    func testApplicationQueryCentralizedLoggingIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "centralized-logging"])))
    }

    func testApplicationQuerySmartCtiConfiguratorIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smart-cti-configurator"])))
    }

    func testApplicationQueryDocswebIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "docsweb"])))
    }

    func testApplicationQuerySpartawebCorp5IsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "spartaweb-corp5"])))
    }

    func testApplicationQuerySmileCxDocsIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "smile-cx-docs"])))
    }

    func testApplicationQueryMyTestAppWithIconIsTrue() {
        XCTAssertTrue(ability.can("query", subject("Application", ["id": "MyTestAppWithIcon"])))
    }

    func testApplicationQueryUnknownAppIsFalse() {
        XCTAssertFalse(ability.can("query", subject("Application", ["id": "unknown-app"])),
                       "Application.query with unknown id should be false")
    }

    func testApplicationQueryRandomIdIsFalse() {
        XCTAssertFalse(ability.can("query", subject("Application", ["id": "random-id"])),
                       "Application.query with random id should be false")
    }

    func testApplicationTrackNoConditionsIsTrue() {
        // Application.track has no conditions, so any instance matches
        XCTAssertTrue(ability.can("track", subject("Application", ["id": "anything"])))
    }

    // MARK: - Application._query with condition

    func testApplicationUnderscoreQueryRegalonataleIsTrue() {
        XCTAssertTrue(ability.can("_query", subject("Application", ["id": "regalonatale"])))
    }

    func testApplicationUnderscoreQueryOtherIsFalse() {
        XCTAssertFalse(ability.can("_query", subject("Application", ["id": "other"])),
                       "Application._query with non-matching id should be false")
    }

    // MARK: - SpartaReport.yellow with equality conditions

    func testSpartaReportYellowMatchingTeamIsTrue() {
        XCTAssertTrue(ability.can("yellow", subject("SpartaReport", ["Team": "SCX Product UX & UI"])))
    }

    func testSpartaReportYellowMatchingGruppoIsTrue() {
        XCTAssertTrue(ability.can("yellow", subject("SpartaReport", ["GruppoPianificazione": "Technology & People"])))
    }

    func testSpartaReportYellowOtherTeamIsFalse() {
        XCTAssertFalse(ability.can("yellow", subject("SpartaReport", ["Team": "Other Team"])),
                       "SpartaReport.yellow with non-matching Team should be false")
    }

    func testSpartaReportYellowOtherGruppoIsFalse() {
        XCTAssertFalse(ability.can("yellow", subject("SpartaReport", ["GruppoPianificazione": "Other Group"])),
                       "SpartaReport.yellow with non-matching GruppoPianificazione should be false")
    }

    // MARK: - PrivacyPolicy with userId condition

    func testPrivacyPolicyQueryMatchingUserIdIsTrue() {
        XCTAssertTrue(ability.can("query", subject("PrivacyPolicy", ["userId": "5f6a22bd844d146134d6f4fc"])))
    }

    func testPrivacyPolicyQueryWrongUserIdIsFalse() {
        XCTAssertFalse(ability.can("query", subject("PrivacyPolicy", ["userId": "other-user-id"])),
                       "PrivacyPolicy.query with wrong userId should be false")
    }

    func testPrivacyPolicyCreateNoConditionsIsTrue() {
        // PrivacyPolicy.create has no conditions
        XCTAssertTrue(ability.can("create", subject("PrivacyPolicy")))
    }

    // MARK: - CsvExportConfiguration with userId condition

    func testCsvExportQueryMatchingUserIdIsTrue() {
        XCTAssertTrue(ability.can("query", subject("CsvExportConfiguration", ["userId": "5f6a22bd844d146134d6f4fc"])))
    }

    func testCsvExportQueryWrongUserIdIsFalse() {
        XCTAssertFalse(ability.can("query", subject("CsvExportConfiguration", ["userId": "wrong-id"])),
                       "CsvExportConfiguration.query with wrong userId should be false")
    }

    func testCsvExportCreateMatchingUserIdIsTrue() {
        XCTAssertTrue(ability.can("create", subject("CsvExportConfiguration", ["userId": "5f6a22bd844d146134d6f4fc"])))
    }

    func testCsvExportDeleteMatchingUserIdIsTrue() {
        XCTAssertTrue(ability.can("delete", subject("CsvExportConfiguration", ["userId": "5f6a22bd844d146134d6f4fc"])))
    }

    func testCsvExportDeleteWrongUserIdIsFalse() {
        XCTAssertFalse(ability.can("delete", subject("CsvExportConfiguration", ["userId": "wrong-id"])),
                       "CsvExportConfiguration.delete with wrong userId should be false")
    }

    // MARK: - Timesheet.get-request-types with id condition

    func testTimesheetGetRequestTypesRolIsTrue() {
        XCTAssertTrue(ability.can("get-request-types", subject("Timesheet", ["id": "rol"])))
    }

    func testTimesheetGetRequestTypesFerieIsTrue() {
        XCTAssertTrue(ability.can("get-request-types", subject("Timesheet", ["id": "ferie"])))
    }

    func testTimesheetGetRequestTypesDeleteApprovedIsTrue() {
        XCTAssertTrue(ability.can("get-request-types", subject("Timesheet", ["id": "delete-approved-request"])))
    }

    func testTimesheetGetRequestTypesMalattiaIsFalse() {
        XCTAssertFalse(ability.can("get-request-types", subject("Timesheet", ["id": "malattia"])),
                       "Timesheet.get-request-types with id=malattia should be false")
    }

    func testTimesheetGetRequestTypesOtherIsFalse() {
        XCTAssertFalse(ability.can("get-request-types", subject("Timesheet", ["id": "other"])),
                       "Timesheet.get-request-types with id=other should be false")
    }

    // MARK: - Vivocha.oldSupport inverted rule precedence

    func testVivochaOldSupportTypeLevelIsFalse() {
        // Inverted rule comes last, so it takes precedence
        XCTAssertFalse(ability.can("oldSupport", "Vivocha"),
                       "Vivocha.oldSupport should be false (inverted rule takes precedence)")
    }

    func testVivochaOldSupportInstanceLevelIsFalse() {
        // Same result at instance level
        XCTAssertFalse(ability.can("oldSupport", subject("Vivocha")),
                       "Vivocha.oldSupport instance-level should be false (inverted rule takes precedence)")
    }

    // MARK: - _User subject (underscore prefix in subject name)

    func testUserUnderscoreGetHierarchyMatchingCompanyIdIsTrue() {
        // Only the rule with company.id = 30 can match (the $in: [] rule matches nothing)
        let subj = subject("_User", [
            "facets": ["sparta": ["company": ["id": 30]]]
        ])
        XCTAssertTrue(ability.can("get-hierarchy", subj))
    }

    func testUserUnderscoreGetHierarchyNonMatchingCompanyIdIsFalse() {
        // 99 is not in empty $in and not equal to 30
        let subj = subject("_User", [
            "facets": ["sparta": ["company": ["id": 99]]]
        ])
        XCTAssertFalse(ability.can("get-hierarchy", subj),
                       "_User.get-hierarchy with company.id=99 should be false")
    }

    func testUserUnderscoreGetChildrenMatchingCompanyIdIsTrue() {
        let subj = subject("_User", [
            "facets": ["sparta": ["company": ["id": 30]]]
        ])
        XCTAssertTrue(ability.can("get-children", subj))
    }

    // MARK: - User.query with dot-notation conditions (nested subject data)

    func testUserQueryWithMatchingSmartTagsIsTrue() {
        // Rule: conditions = {"facets.smart.tags": "vivochaSupport"}
        let subj = subject("User", [
            "facets": ["smart": ["tags": "vivochaSupport"]]
        ])
        XCTAssertTrue(ability.can("query", subj))
    }

    func testUserQueryWithMatchingFeatureUserIsTrue() {
        // Rule: conditions = {"featureUser": true}
        let subj = subject("User", ["featureUser": true])
        XCTAssertTrue(ability.can("query", subj))
    }

    // MARK: - User.query field-level checks

    func testUserQueryFieldIdIsTrue() {
        // _id appears in multiple non-inverted User.query rules with fields
        XCTAssertTrue(ability.can("query", subject("User"), field: "_id"))
    }

    func testUserQueryFieldFirstnameIsTrue() {
        XCTAssertTrue(ability.can("query", subject("User"), field: "firstname"))
    }

    func testUserQueryFieldContractIsFalse() {
        // facets.sparta.contract.** is in the inverted rule's fields
        XCTAssertFalse(ability.can("query", subject("User"), field: "facets.sparta.contract.something"),
                       "User.query field facets.sparta.contract.something should be blocked by inverted rule")
    }

    func testUserQueryFieldBankInfoIsFalse() {
        // facets.sparta.bankInfo.** is in the inverted rule's fields
        XCTAssertFalse(ability.can("query", subject("User"), field: "facets.sparta.bankInfo.iban"),
                       "User.query field facets.sparta.bankInfo.iban should be blocked by inverted rule")
    }

    func testUserQueryFieldGenderIsTrue() {
        // gender is in the inverted rule's fields, but a later non-inverted rule
        // also includes gender in its fields, so the last rule wins => allowed
        XCTAssertTrue(ability.can("query", subject("User"), field: "gender"))
    }
}
