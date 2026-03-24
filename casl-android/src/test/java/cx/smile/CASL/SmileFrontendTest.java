package cx.smile.CASL;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Smile Frontend permission tests using actual API response data.
 * Checks a large set of frontend permissions against the real perms JSON.
 */
public class SmileFrontendTest {

    private static final String PERMS_JSON = "[\n" +
            "{\"subject\": \"Mobile\", \"action\": \"check-for-update\"},\n" +
            "{\"subject\": \"Employee\", \"action\": \"roles\"},\n" +
            "{\"subject\": \"User\", \"action\": [\"get-hierarchy\", \"get-children\"], \"_conditions\": {\"facets.sparta.corporate.id\": {\"$in\": [1, 2]}}, \"fields\": [\"_id\", \"firstname\", \"lastname\", \"facets.sparta.idSparta\", \"facets.sparta.company.**\", \"facets.sparta.corporate.**\", \"facets.sparta.team.**\", \"facets.sparta.site.**\", \"facets.sparta.country.**\", \"roles.**\", \"isActive\"]},\n" +
            "{\"subject\": \"_User\", \"action\": [\"get-hierarchy\", \"get-children\"], \"conditions\": {\"facets.sparta.company.id\": {\"$in\": []}}, \"fields\": [\"_id\", \"firstname\", \"lastname\", \"facets.sparta.idSparta\", \"facets.sparta.company.**\", \"facets.sparta.corporate.**\", \"facets.sparta.team.**\", \"facets.sparta.site.**\", \"facets.sparta.country.**\", \"roles.**\", \"isActive\"]},\n" +
            "{\"subject\": \"_User\", \"action\": [\"get-hierarchy\", \"get-children\"], \"conditions\": {\"facets.sparta.company.id\": 30}, \"fields\": [\"_id\", \"firstname\", \"lastname\", \"facets.sparta.idSparta\", \"facets.sparta.company.**\", \"facets.sparta.corporate.**\", \"facets.sparta.team.**\", \"facets.sparta.site.**\", \"facets.sparta.country.**\", \"roles.**\", \"isActive\"]},\n" +
            "{\"subject\": \"User\", \"action\": \"query\", \"conditions\": {\"facets.smart.tags\": \"vivochaSupport\"}, \"fields\": [\"_id\", \"firstname\", \"lastname\", \"isActive\"]},\n" +
            "{\"subject\": \"User\", \"action\": \"query\", \"fields\": [\"facets.sparta.contract.**\", \"facets.sparta.bankInfo.**\", \"facets.sparta.campaigns.**\", \"facets.sparta.serial\", \"facets.sparta.logs.**\", \"facets.sparta.notes\", \"facets.sparta.qualification.**\", \"facets.sparta.hashedObj\", \"facets.sparta.tfrDocumentationDeliveryDate\", \"facets.sparta.oldUserId\", \"gender\", \"email\"], \"inverted\": true},\n" +
            "{\"subject\": \"Role\", \"action\": [\"query\", \"read\"]},\n" +
            "{\"subject\": \"Sparta\", \"action\": [\"services\", \"index\"]},\n" +
            "{\"subject\": \"Docs\", \"action\": \"assets\"},\n" +
            "{\"subject\": \"Script\", \"action\": [\"legacyList\", \"legacyGet\"]},\n" +
            "{\"subject\": \"Channel\", \"action\": \"create-token\"},\n" +
            "{\"subject\": \"SpartaReport\", \"action\": \"yellow\", \"conditions\": {\"Team\": \"SCX Product UX & UI\"}},\n" +
            "{\"subject\": \"SpartaReport\", \"action\": \"yellow\", \"conditions\": {\"GruppoPianificazione\": \"Technology & People\"}},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": {\"$in\": [\"_sparta-ai\", \"smart-meet\"]}}},\n" +
            "{\"subject\": \"RAS\", \"action\": \"producer-legacy\"},\n" +
            "{\"subject\": \"User\", \"action\": \"query-read\", \"fields\": [\"_id\", \"firstname\", \"lastname\", \"facets.sparta.idSparta\", \"facets.sparta.corporate.**\", \"facets.smart.tags\"]},\n" +
            "{\"subject\": \"Audio\", \"action\": \"transcribe\"},\n" +
            "{\"subject\": \"User\", \"action\": [\"kpi-dashboards\", \"set-workplace\"]},\n" +
            "{\"subject\": \"Timing\", \"action\": [\"get-time\", \"get-start-time\"]},\n" +
            "{\"subject\": \"Portfolio\", \"action\": [\"query\", \"scripts\"]},\n" +
            "{\"subject\": \"Vivocha\", \"action\": \"oldSupport\"},\n" +
            "{\"subject\": \"Badge\", \"action\": \"create\"},\n" +
            "{\"subject\": \"Timesheet\", \"action\": [\"approve-requests\", \"reject-requests\"]},\n" +
            "{\"subject\": \"Kpi\", \"action\": [\"get-action-reporting\", \"get-user-action-reporting\"]},\n" +
            "{\"subject\": \"StringOperation\", \"action\": \"query\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"sparta-ai-2\"}},\n" +
            "{\"subject\": \"Apps\", \"action\": \"sparta2\"},\n" +
            "{\"subject\": \"RAS\", \"action\": [\"producer\"]},\n" +
            "{\"subject\": \"Script\", \"action\": [\"list\", \"read\"]},\n" +
            "{\"subject\": \"Campaign\", \"action\": [\"legacyList\", \"query-personal\"]},\n" +
            "{\"subject\": \"Customer\", \"action\": [\"read-customers\", \"upsert\", \"update-status\"]},\n" +
            "{\"subject\": \"KPIPredictive\", \"action\": \"query\"},\n" +
            "{\"subject\": \"User\", \"action\": \"query\", \"conditions\": {\"featureUser\": true}, \"fields\": [\"_id\", \"firstname\", \"lastname\", \"fullname\", \"isActive\", \"featureUser\", \"facets.smart.tags\"]},\n" +
            "{\"subject\": \"User\", \"action\": \"can\"},\n" +
            "{\"subject\": \"Environment\", \"action\": \"prod\"},\n" +
            "{\"subject\": \"User\", \"action\": [\"me\", \"me-light\", \"session-and-redirect\"]},\n" +
            "{\"subject\": \"User\", \"action\": \"patch\", \"fields\": [\"settings.**\"]},\n" +
            "{\"subject\": \"User\", \"action\": [\"get-participants\", \"get-admins\"], \"fields\": [\"_id\", \"firstname\", \"lastname\", \"gender\", \"facets.sparta.idSparta\", \"roles.roleName\"]},\n" +
            "{\"subject\": \"Status\", \"action\": [\"post\", \"get\", \"get-multiple\"]},\n" +
            "{\"subject\": \"Channel\", \"action\": [\"query\", \"get-statuses\", \"read\"]},\n" +
            "{\"subject\": \"Channel\", \"action\": [\"get-participants\", \"get-admins\"], \"fields\": [\"_id\", \"firstname\", \"lastname\", \"gender\", \"isGuest\", \"roles.roleName\"]},\n" +
            "{\"subject\": \"Message\", \"action\": [\"get-channel-messages\", \"get-private-messages\", \"get-notifications\", \"clean-notifications\", \"get-last-channels-interactions\", \"get-last-direct-interactions\", \"attach\", \"send-message\", \"send-ack\", \"feed-query\", \"info\"]},\n" +
            "{\"subject\": \"String\", \"action\": [\"query\", \"read\"]},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": {\"$in\": [\"smart-home\", \"smart-rooms\"]}}},\n" +
            "{\"subject\": \"UserDevice\", \"action\": [\"register\", \"unregister\"]},\n" +
            "{\"subject\": \"Watermark\", \"action\": \"read\"},\n" +
            "{\"subject\": \"UserSettings\", \"action\": [\"read\", \"update\", \"patch\"]},\n" +
            "{\"subject\": \"Shortener\", \"action\": [\"read\", \"go\"]},\n" +
            "{\"subject\": \"Transcription\", \"action\": \"attach\"},\n" +
            "{\"subject\": \"ContactMethod\", \"action\": [\"query\", \"upsert\", \"confirm\", \"remove\"]},\n" +
            "{\"subject\": \"Application\", \"action\": \"track\"},\n" +
            "{\"subject\": \"ApplicationUserSettings\", \"action\": [\"read\", \"update\", \"remove\", \"create\"]},\n" +
            "{\"subject\": \"Mobile\", \"action\": \"check-for-update\"},\n" +
            "{\"subject\": \"KpisConfiguration\", \"action\": [\"query\"]},\n" +
            "{\"subject\": \"Development\", \"action\": \"test-timing\"},\n" +
            "{\"subject\": \"PrivacyPolicy\", \"action\": \"query\", \"conditions\": {\"userId\": \"5f6a22bd844d146134d6f4fc\"}},\n" +
            "{\"subject\": \"PrivacyPolicy\", \"action\": \"create\"},\n" +
            "{\"subject\": \"Redirect\", \"action\": \"redirect\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smart-learning\"}},\n" +
            "{\"subject\": \"Moodle\", \"action\": [\"_get-plans\", \"_get-courses\", \"_get-course-content\", \"bootstrap\", \"get-modules-by-course\"]},\n" +
            "{\"subject\": \"Mobile\", \"action\": \"learning-app\"},\n" +
            "{\"subject\": \"EmployeesOptionField\", \"action\": [\"corporate\", \"country\", \"company\", \"planningGroup\", \"site\", \"team\", \"teamType\", \"role\", \"spartarole\", \"single\", \"spartaid\", \"tag\", \"relationshipType\", \"taxCode\", \"area\"]},\n" +
            "{\"subject\": \"User\", \"action\": \"options\"},\n" +
            "{\"subject\": \"User\", \"action\": \"query\", \"fields\": [\"_id\", \"firstname\", \"lastname\", \"fullname\", \"facets.sparta.idSparta\", \"facets.sparta.company.**\", \"facets.sparta.corporate.**\", \"facets.sparta.team.**\", \"facets.sparta.site.**\", \"facets.sparta.country.**\", \"facets.extras.**\", \"facets.sparta.division.**\", \"facets.sparta.department.**\", \"facets.sparta.businessUnit.**\", \"roles.**\", \"isActive\", \"email\", \"phoneNumber\", \"facets.smart.tags\"]},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smart-employees\"}},\n" +
            "{\"subject\": \"CsvExportConfiguration\", \"action\": [\"query\", \"read\", \"update\", \"create\", \"delete\", \"remove\"], \"conditions\": {\"userId\": \"5f6a22bd844d146134d6f4fc\"}},\n" +
            "{\"subject\": \"User\", \"action\": \"query\", \"fields\": [\"_id\", \"firstname\", \"lastname\", \"fullname\", \"featureUser\", \"email\", \"gender\", \"roles.**\", \"isActive\", \"dateOfBirth\", \"featureUser\", \"facets.sparta.role.**\", \"facets.sparta.idSparta\", \"facets.sparta.country.**\", \"facets.smart.tags\"]},\n" +
            "{\"subject\": \"User\", \"action\": [\"options\", \"options-read\"]},\n" +
            "{\"subject\": \"Role\", \"action\": [\"create\", \"update\"]},\n" +
            "{\"subject\": \"Channel\", \"action\": [\"remove\", \"create\", \"update\"]},\n" +
            "{\"subject\": \"ChannelOptionField\", \"action\": [\"corporate\", \"country\", \"company\", \"planningGroup\", \"site\", \"team\", \"role\", \"spartarole\", \"single\", \"spartaid\", \"tag\", \"relationshipType\"]},\n" +
            "{\"subject\": \"String\", \"action\": [\"create\", \"remove\", \"update\"]},\n" +
            "{\"subject\": \"Proxy\", \"action\": [\"read\", \"query\", \"remove\", \"patch\", \"update\"]},\n" +
            "{\"subject\": \"ReportDashboard\", \"action\": [\"read-personal\", \"query-personal\", \"_describe\"]},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smart-reports\"}},\n" +
            "{\"subject\": \"Channel\", \"action\": \"get-all-feed\"},\n" +
            "{\"subject\": \"News\", \"action\": \"send\"},\n" +
            "{\"subject\": \"Channel\", \"action\": \"no-limits\"},\n" +
            "{\"subject\": \"Message\", \"action\": [\"_feed-query\", \"proxy-content\"]},\n" +
            "{\"subject\": \"Recupera\", \"action\": \"services\"},\n" +
            "{\"subject\": \"MFA\", \"action\": \"force\"},\n" +
            "{\"subject\": \"SmartSpartaWeb\", \"action\": \"manage\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"_query\", \"conditions\": {\"id\": \"regalonatale\"}},\n" +
            "{\"subject\": \"ProxyExternalApp\", \"action\": \"regalonatale\"},\n" +
            "{\"subject\": \"User\", \"action\": \"timesheet\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smart-timesheet\"}},\n" +
            "{\"subject\": \"Timesheet\", \"action\": [\"get-requests\", \"get-status\", \"cancel-requests\", \"delete-approved-requests\", \"approve-requests\", \"reject-requests\", \"create-requests\", \"calculate-estimate\", \"bootstrap-user\"]},\n" +
            "{\"subject\": \"Link\", \"action\": \"payroll\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smart-notes\"}},\n" +
            "{\"subject\": \"Note\", \"action\": \"manage\"},\n" +
            "{\"subject\": \"EmployeesOptionField\", \"action\": [\"isActive\"]},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smart-appraisal\"}},\n" +
            "{\"subject\": \"Appraisal\", \"action\": [\"upsert\", \"get\"]},\n" +
            "{\"subject\": \"Appraisal\", \"action\": \"peer\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"docdigit\"}},\n" +
            "{\"subject\": \"ProxyExternalApp\", \"action\": \"docdigit\"},\n" +
            "{\"subject\": \"User\", \"action\": \"query-for-address-book\", \"fields\": [\"_id\", \"firstname\", \"lastname\", \"email\", \"phoneNumber\", \"facets.avatar.**\", \"facets.sparta.idSparta\", \"facets.sparta.corporate.**\", \"facets.sparta.company.**\", \"facets.sparta.country.**\", \"facets.sparta.site.**\", \"facets.sparta.team.**\", \"isActive\"]},\n" +
            "{\"subject\": \"Mobile\", \"action\": \"download-address-book\"},\n" +
            "{\"subject\": \"User\", \"action\": \"query-for-app-call-identification\"},\n" +
            "{\"subject\": \"Timesheet\", \"action\": \"get-request-types\", \"conditions\": {\"id\": \"rol\"}},\n" +
            "{\"subject\": \"Timesheet\", \"action\": \"get-request-types\", \"conditions\": {\"id\": \"ferie\"}},\n" +
            "{\"subject\": \"Timesheet\", \"action\": \"get-request-types\", \"conditions\": {\"id\": \"delete-approved-request\"}},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"myticket\"}},\n" +
            "{\"subject\": \"ProxyExternalApp\", \"action\": \"myticket\"},\n" +
            "{\"subject\": \"MobileApplication\", \"action\": \"myticket\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smart-agent-desktop\"}},\n" +
            "{\"subject\": \"Portfolio\", \"action\": [\"bootstrap\", \"scripts\", \"all-skills\"]},\n" +
            "{\"subject\": \"Kpi\", \"action\": [\"all\"]},\n" +
            "{\"subject\": \"Vivocha\", \"action\": \"oldSupport\", \"inverted\": true},\n" +
            "{\"subject\": \"KPIPredictive\", \"action\": \"query\"},\n" +
            "{\"subject\": \"AgentStatuses\", \"action\": \"query\"},\n" +
            "{\"subject\": \"Campaign\", \"action\": [\"query-personal\", \"read\"]},\n" +
            "{\"subject\": \"Environment\", \"action\": \"mobile\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"centralized-logging\"}},\n" +
            "{\"subject\": \"ProxyExternalApp\", \"action\": \"logging\"},\n" +
            "{\"subject\": \"Mobile\", \"action\": \"feedback\"},\n" +
            "{\"subject\": \"JiraTicketMobile\", \"action\": \"create\"},\n" +
            "{\"subject\": \"VPN\", \"action\": \"manage\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smart-cti-configurator\"}},\n" +
            "{\"subject\": \"EmployeesOptionField\", \"action\": [\"single\"]},\n" +
            "{\"subject\": \"User\", \"action\": [\"query\", \"patch\"], \"fields\": [\"facets.cti.**\"]},\n" +
            "{\"subject\": \"User\", \"action\": \"query\", \"fields\": [\"_id\", \"firstname\", \"lastname\", \"fullname\", \"facets.sparta.idSparta\", \"facets.sparta.company.**\", \"facets.sparta.corporate.**\", \"facets.sparta.team.**\", \"facets.sparta.site.**\", \"facets.sparta.country.**\", \"roles.**\", \"isActive\", \"facets.cti.**\"]},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"parking-reservation-tab\"}},\n" +
            "{\"subject\": \"Realtime\", \"action\": [\"details\"]},\n" +
            "{\"subject\": \"CtiMonitorConfig\", \"action\": \"query\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"docsweb\"}},\n" +
            "{\"subject\": \"Mobile\", \"action\": \"download-address-book-ios\"},\n" +
            "{\"subject\": \"Timesheet\", \"action\": \"bootstrap-admin\"},\n" +
            "{\"subject\": \"Timesheet\", \"action\": \"handle-multiple-requests\"},\n" +
            "{\"subject\": \"Timesheet\", \"action\": \"bootstrap-mode-children\"},\n" +
            "{\"subject\": \"Timesheet\", \"action\": \"show-balance-to-admin\"},\n" +
            "{\"subject\": \"Realtime\", \"action\": [\"kpi-panels\"]},\n" +
            "{\"subject\": \"Mails\", \"action\": \"send\"},\n" +
            "{\"subject\": \"Channel\", \"action\": \"exists\"},\n" +
            "{\"subject\": \"Channel\", \"action\": \"allowed-email-domains\"},\n" +
            "{\"subject\": \"Avatar\", \"action\": \"avatar_show\"},\n" +
            "{\"subject\": \"SmartSpartaWeb-C5\", \"action\": \"services\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"spartaweb-corp5\"}},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"smile-cx-docs\"}},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"centro-risorse-smilecx-gaia\"}},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"sales-marketing-intelligence-hub\"}},\n" +
            "{\"subject\": \"MobileApplication\", \"action\": \"sales-marketing-intelligence-hub\"},\n" +
            "{\"subject\": \"SpartaTimesheet\", \"action\": \"can-edit-sickness-hours\"},\n" +
            "{\"subject\": \"SpartaTimesheet\", \"action\": \"can-edit-recovery-hours\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"MyTestAppWithIcon\"}},\n" +
            "{\"subject\": \"OpenIDClient\", \"action\": \"studio-dev-sso\"},\n" +
            "{\"subject\": \"OpenIDClient\", \"action\": \"studio-next-sso\"},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"desk-reservation-tab\"}},\n" +
            "{\"subject\": \"Application\", \"action\": \"query\", \"conditions\": {\"id\": \"myticketglpi\"}},\n" +
            "{\"subject\": \"ProxyExternalApp\", \"action\": \"myticketglpi\"},\n" +
            "{\"subject\": \"OpenIDClient\", \"action\": \"studio-cx-sso\"},\n" +
            "{\"subject\": \"OpenIDClient\", \"action\": \"studio-it-1-sso\"}\n" +
            "]";

    private Ability ability;

    @Before
    public void setUp() throws JSONException {
        List<RawRule> rules = RawRule.listFromJson(PERMS_JSON);
        ability = Ability.fromRules(rules);
    }

    // AdditionalUserInfo

    @Test
    public void testAdditionalUserInfoPermissions() {
        assertFalse(ability.can("login-application", "AdditionalUserInfo"));
        assertFalse(ability.can("skills", "AdditionalUserInfo"));
        assertFalse(ability.can("trade-unions", "AdditionalUserInfo"));
    }

    // AiStudio

    @Test
    public void testAiStudioPermissions() {
        assertFalse(ability.can("architect", "AiStudio"));
        assertFalse(ability.can("configurator", "AiStudio"));
    }

    // Analytics

    @Test
    public void testAnalyticsPermissions() {
        assertFalse(ability.can("ga-track", "Analytics"));
        assertFalse(ability.can("ga-ask-consent", "Analytics"));
        assertFalse(ability.can("hj-track", "Analytics"));
        assertFalse(ability.can("hj-ask-consent", "Analytics"));
    }

    // Application

    @Test
    public void testApplicationPermissions() {
        assertTrue(ability.can("query", "Application"));
        assertFalse(ability.can("remove", "Application"));
    }

    // AppraisalConfig

    @Test
    public void testAppraisalConfigPermissions() {
        assertFalse(ability.can("create", "AppraisalConfig"));
    }

    // Audio

    @Test
    public void testAudioPermissions() {
        assertFalse(ability.can("remoteListen", "Audio"));
        assertFalse(ability.can("implicit-agree-remoteListen", "Audio"));
        assertTrue(ability.can("transcribe", "Audio"));
        assertFalse(ability.can("transcribe-call", "Audio"));
        assertFalse(ability.can("queue", "Audio"));
        assertFalse(ability.can("recording", "Audio"));
        assertFalse(ability.can("autoRecording", "Audio"));
    }

    // Avatar

    @Test
    public void testAvatarPermissions() {
        assertTrue(ability.can("avatar_show", "Avatar"));
    }

    // Channel

    @Test
    public void testChannelPermissions() {
        assertTrue(ability.can("create", "Channel"));
        assertTrue(ability.can("create-token", "Channel"));
        assertTrue(ability.can("no-limits", "Channel"));
    }

    // ChannelOptionField

    @Test
    public void testChannelOptionFieldPermissions() {
        assertTrue(ability.can("corporate", "ChannelOptionField"));
        assertTrue(ability.can("country", "ChannelOptionField"));
        assertTrue(ability.can("company", "ChannelOptionField"));
        assertTrue(ability.can("planningGroup", "ChannelOptionField"));
        assertTrue(ability.can("site", "ChannelOptionField"));
        assertTrue(ability.can("team", "ChannelOptionField"));
        assertTrue(ability.can("role", "ChannelOptionField"));
        assertTrue(ability.can("spartarole", "ChannelOptionField"));
        assertTrue(ability.can("single", "ChannelOptionField"));
        assertTrue(ability.can("spartaid", "ChannelOptionField"));
        assertTrue(ability.can("tag", "ChannelOptionField"));
        assertTrue(ability.can("relationshipType", "ChannelOptionField"));
    }

    // Chat

    @Test
    public void testChatPermissions() {
        assertFalse(ability.can("hide", "Chat"));
    }

    // ContactMethod

    @Test
    public void testContactMethodPermissions() {
        assertFalse(ability.can("otp-supervisor", "ContactMethod"));
        assertTrue(ability.can("query", "ContactMethod"));
    }

    // DynamicRole

    @Test
    public void testDynamicRolePermissions() {
        assertFalse(ability.can("query", "DynamicRole"));
        assertFalse(ability.can("update", "DynamicRole"));
        assertFalse(ability.can("patch", "DynamicRole"));
    }

    // Employee

    @Test
    public void testEmployeePermissions() {
        assertFalse(ability.can("contract-history", "Employee"));
        assertFalse(ability.can("dynamic-role-json", "Employee"));
        assertFalse(ability.can("info", "Employee"));
        assertFalse(ability.can("permissions", "Employee"));
        assertFalse(ability.can("reports", "Employee"));
        assertTrue(ability.can("roles", "Employee"));
        assertFalse(ability.can("site-assignment", "Employee"));
        assertFalse(ability.can("team-assignment", "Employee"));
    }

    // EmployeesOptionField

    @Test
    public void testEmployeesOptionFieldPermissions() {
        assertTrue(ability.can("area", "EmployeesOptionField"));
        assertTrue(ability.can("company", "EmployeesOptionField"));
        assertTrue(ability.can("corporate", "EmployeesOptionField"));
        assertTrue(ability.can("country", "EmployeesOptionField"));
        assertTrue(ability.can("planningGroup", "EmployeesOptionField"));
        assertTrue(ability.can("relationshipType", "EmployeesOptionField"));
        assertTrue(ability.can("role", "EmployeesOptionField"));
        assertTrue(ability.can("site", "EmployeesOptionField"));
        assertTrue(ability.can("tag", "EmployeesOptionField"));
        assertTrue(ability.can("team", "EmployeesOptionField"));
        assertTrue(ability.can("single", "EmployeesOptionField"));
        assertTrue(ability.can("spartaid", "EmployeesOptionField"));
        assertTrue(ability.can("spartarole", "EmployeesOptionField"));
        assertTrue(ability.can("taxCode", "EmployeesOptionField"));
        assertTrue(ability.can("isActive", "EmployeesOptionField"));
    }

    // Environment

    @Test
    public void testEnvironmentPermissions() {
        assertTrue(ability.can("mobile", "Environment"));
        assertFalse(ability.can("dev", "Environment"));
        assertTrue(ability.can("prod", "Environment"));
        assertFalse(ability.can("stage", "Environment"));
    }

    // ExtrasOptionField

    @Test
    public void testExtrasOptionFieldPermissions() {
        assertFalse(ability.can("wildCard", "ExtrasOptionField"));
    }

    // Kpi

    @Test
    public void testKpiPermissions() {
        assertTrue(ability.can("all", "Kpi"));
        assertFalse(ability.can("query", "Kpi"));
    }

    // KpisConfiguration

    @Test
    public void testKpisConfigurationPermissions() {
        assertFalse(ability.can("create", "KpisConfiguration"));
        assertFalse(ability.can("update", "KpisConfiguration"));
        assertFalse(ability.can("delete", "KpisConfiguration"));
    }

    // kpi-dashboards (as subject)

    @Test
    public void testKpiDashboardsPermissions() {
        assertFalse(ability.can("create", "kpi-dashboards"));
        assertFalse(ability.can("update", "kpi-dashboards"));
        assertFalse(ability.can("delete", "kpi-dashboards"));
    }

    // Layout

    @Test
    public void testLayoutPermissions() {
        assertFalse(ability.can("new", "Layout"));
    }

    // Library

    @Test
    public void testLibraryPermissions() {
        assertFalse(ability.can("library", "Library"));
    }

    // Link

    @Test
    public void testLinkPermissions() {
        assertTrue(ability.can("payroll", "Link"));
    }

    // Mails

    @Test
    public void testMailsPermissions() {
        assertTrue(ability.can("send", "Mails"));
    }

    // Message

    @Test
    public void testMessagePermissions() {
        assertTrue(ability.can("attach", "Message"));
        assertTrue(ability.can("feed-query", "Message"));
    }

    // Moodle

    @Test
    public void testMoodlePermissions() {
        assertTrue(ability.can("bootstrap", "Moodle"));
        assertFalse(ability.can("tab-competencies", "Moodle"));
        assertFalse(ability.can("get-tenants", "Moodle"));
        assertFalse(ability.can("get-all-courses", "Moodle"));
        assertFalse(ability.can("get-all-plans", "Moodle"));
        assertFalse(ability.can("get-courses-by-tenant", "Moodle"));
        assertFalse(ability.can("get-plans-by-tenant", "Moodle"));
    }

    // MoodlePipeline

    @Test
    public void testMoodlePipelinePermissions() {
        assertFalse(ability.can("create", "MoodlePipeline"));
        assertFalse(ability.can("update", "MoodlePipeline"));
        assertFalse(ability.can("query", "MoodlePipeline"));
        assertFalse(ability.can("run", "MoodlePipeline"));
        assertFalse(ability.can("remove", "MoodlePipeline"));
    }

    // News

    @Test
    public void testNewsPermissions() {
        assertTrue(ability.can("send", "News"));
    }

    // Passepartout

    @Test
    public void testPassepartoutPermissions() {
        assertFalse(ability.can("query", "Passepartout"));
        assertFalse(ability.can("create", "Passepartout"));
    }

    // Realtime

    @Test
    public void testRealtimePermissions() {
        assertTrue(ability.can("details", "Realtime"));
        assertTrue(ability.can("kpi-panels", "Realtime"));
        assertFalse(ability.can("agents-table", "Realtime"));
    }

    // ReportDashboard

    @Test
    public void testReportDashboardPermissions() {
        assertFalse(ability.can("create", "ReportDashboard"));
        assertFalse(ability.can("manage-quicksight", "ReportDashboard"));
        assertFalse(ability.can("manage-superset", "ReportDashboard"));
        assertFalse(ability.can("manage-grafana", "ReportDashboard"));
    }

    // Role

    @Test
    public void testRolePermissions() {
        assertTrue(ability.can("create", "Role"));
        assertTrue(ability.can("update", "Role"));
    }

    // ScriptExternal

    @Test
    public void testScriptExternalPermissions() {
        assertFalse(ability.can("create", "ScriptExternal"));
    }

    // Shortener

    @Test
    public void testShortenerPermissions() {
        assertFalse(ability.can("create-temporary-meet", "Shortener"));
    }

    // Sparta

    @Test
    public void testSpartaPermissions() {
        assertTrue(ability.can("index", "Sparta"));
    }

    // Timesheet

    @Test
    public void testTimesheetPermissions() {
        assertFalse(ability.can("viewer", "Timesheet"));
        assertTrue(ability.can("create-requests", "Timesheet"));
        assertTrue(ability.can("cancel-requests", "Timesheet"));
        assertFalse(ability.can("query-planned-workplace", "Timesheet"));
        assertTrue(ability.can("bootstrap-admin", "Timesheet"));
        assertFalse(ability.can("bootstrap-mode-team", "Timesheet"));
        assertTrue(ability.can("bootstrap-mode-children", "Timesheet"));
        assertFalse(ability.can("bootstrap-mode-superAdmin", "Timesheet"));
        assertFalse(ability.can("create-requests-admin", "Timesheet"));
        assertTrue(ability.can("show-balance-to-admin", "Timesheet"));
    }

    // Transcription

    @Test
    public void testTranscriptionPermissions() {
        assertFalse(ability.can("editScore", "Transcription"));
        assertFalse(ability.can("hear-audio", "Transcription"));
        assertFalse(ability.can("pipeline", "Transcription"));
        assertFalse(ability.can("query", "Transcription"));
        assertFalse(ability.can("read", "Transcription"));
        assertFalse(ability.can("view-json", "Transcription"));
        assertFalse(ability.can("not_disguise", "Transcription"));
    }

    // UploaderConfig

    @Test
    public void testUploaderConfigPermissions() {
        assertFalse(ability.can("download-data", "UploaderConfig"));
    }

    // User

    @Test
    public void testUserPermissions() {
        assertFalse(ability.can("patch-extras", "User"));
        assertFalse(ability.can("patch-perms", "User"));
        assertFalse(ability.can("query-for-app-role", "User"));
        assertTrue(ability.can("query", "User"));
        assertFalse(ability.can("reset-auth", "User"));
        assertFalse(ability.can("update-role", "User"));
        assertFalse(ability.can("delete", "User"));
        assertFalse(ability.can("dynamic-field", "User"));
        assertFalse(ability.can("update-uid", "User"));
    }

    // UserImport

    @Test
    public void testUserImportPermissions() {
        assertFalse(ability.can("create-request", "UserImport"));
        assertFalse(ability.can("confirm-request", "UserImport"));
    }

    // UserTemplate

    @Test
    public void testUserTemplatePermissions() {
        assertFalse(ability.can("query", "UserTemplate"));
    }

    // Vivocha

    @Test
    public void testVivochaPermissions() {
        assertFalse(ability.can("oldSupport", "Vivocha"));
        assertFalse(ability.can("smartChannel", "Vivocha"));
    }

    // VPN

    @Test
    public void testVPNPermissions() {
        assertTrue(ability.can("manage", "VPN"));
    }

    // VpnGrant

    @Test
    public void testVpnGrantPermissions() {
        assertFalse(ability.can("manage", "VpnGrant"));
        assertFalse(ability.can("create", "VpnGrant"));
        assertFalse(ability.can("update", "VpnGrant"));
        assertFalse(ability.can("remove", "VpnGrant"));
    }

    // VPNServer

    @Test
    public void testVPNServerPermissions() {
        assertFalse(ability.can("manage", "VPNServer"));
        assertFalse(ability.can("query", "VPNServer"));
        assertFalse(ability.can("create", "VPNServer"));
        assertFalse(ability.can("create-network", "VPNServer"));
        assertFalse(ability.can("sync", "VPNServer"));
        assertFalse(ability.can("sync-all", "VPNServer"));
        assertFalse(ability.can("update", "VPNServer"));
        assertFalse(ability.can("remove", "VPNServer"));
        assertFalse(ability.can("update-network", "VPNServer"));
        assertFalse(ability.can("delete-network", "VPNServer"));
    }
}
