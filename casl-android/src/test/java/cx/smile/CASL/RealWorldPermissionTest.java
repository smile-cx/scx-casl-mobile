package cx.smile.CASL;

import org.json.JSONException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Real-world permission tests using actual API response data.
 * Parses the full perms JSON array and checks each permission type.
 */
public class RealWorldPermissionTest {

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

    private Ability buildAbility() throws JSONException {
        List<RawRule> rules = RawRule.listFromJson(PERMS_JSON);
        return Ability.fromRules(rules);
    }

    @Test
    public void testShortenerCreateIsFalse() throws JSONException {
        Ability ability = buildAbility();
        assertFalse("Shortener.create should be false", ability.can("create", "Shortener"));
    }

    @Test
    public void testShortenerGoIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Shortener.go should be true", ability.can("go", "Shortener"));
    }

    @Test
    public void testMessageInfoIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Message.info should be true", ability.can("info", "Message"));
    }

    @Test
    public void testMessageAttachIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Message.attach should be true", ability.can("attach", "Message"));
    }

    @Test
    public void testContactMethodQueryIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("ContactMethod.query should be true", ability.can("query", "ContactMethod"));
    }

    @Test
    public void testUserTimesheetIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("User.timesheet should be true", ability.can("timesheet", "User"));
    }

    @Test
    public void testTimesheetCreateRequestsIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Timesheet.create-requests should be true", ability.can("create-requests", "Timesheet"));
    }

    @Test
    public void testTimesheetCancelRequestsIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Timesheet.cancel-requests should be true", ability.can("cancel-requests", "Timesheet"));
    }

    @Test
    public void testKpiQueryIsFalse() throws JSONException {
        Ability ability = buildAbility();
        assertFalse("Kpi.query should be false", ability.can("query", "Kpi"));
    }

    @Test
    public void testKpiAllIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Kpi.all should be true", ability.can("all", "Kpi"));
    }

    @Test
    public void testUserQueryForAppCallIdentificationIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("User.query-for-app-call-identification should be true", ability.can("query-for-app-call-identification", "User"));
    }

    @Test
    public void testUserQueryForAddressBookIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("User.query-for-address-book should be true", ability.can("query-for-address-book", "User"));
    }

    @Test
    public void testMobileDownloadAddressBookIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Mobile.download-address-book should be true", ability.can("download-address-book", "Mobile"));
    }

    @Test
    public void testVivochaSmartChannelIsFalse() throws JSONException {
        Ability ability = buildAbility();
        assertFalse("Vivocha.smartChannel should be false", ability.can("smartChannel", "Vivocha"));
    }

    @Test
    public void testMobileCheckForUpdateIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Mobile.check-for-update should be true", ability.can("check-for-update", "Mobile"));
    }

    @Test
    public void testUserGetAvatarUploadUrlIsFalse() throws JSONException {
        Ability ability = buildAbility();
        assertFalse("User.get-avatar-upload-url should be false", ability.can("get-avatar-upload-url", "User"));
    }

    @Test
    public void testMobileLearningAppIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Mobile.learning-app should be true", ability.can("learning-app", "Mobile"));
    }

    @Test
    public void testMobileFeedbackIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Mobile.feedback should be true", ability.can("feedback", "Mobile"));
    }

    @Test
    public void testNewsSendIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("News.send should be true", ability.can("send", "News"));
    }

    @Test
    public void testTimesheetBootstrapModeSuperadminIsFalse() throws JSONException {
        Ability ability = buildAbility();
        assertFalse("Timesheet.bootstrap-mode-superadmin should be false", ability.can("bootstrap-mode-superadmin", "Timesheet"));
    }

    @Test
    public void testTimesheetBootstrapModeTeamIsFalse() throws JSONException {
        Ability ability = buildAbility();
        assertFalse("Timesheet.bootstrap-mode-team should be false", ability.can("bootstrap-mode-team", "Timesheet"));
    }

    @Test
    public void testTimesheetBootstrapModeChildrenIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Timesheet.bootstrap-mode-children should be true", ability.can("bootstrap-mode-children", "Timesheet"));
    }

    @Test
    public void testTimesheetHandleMultipleRequestsIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Timesheet.handle-multiple-requests should be true", ability.can("handle-multiple-requests", "Timesheet"));
    }

    @Test
    public void testTimesheetCreateRequestsAdminIsFalse() throws JSONException {
        Ability ability = buildAbility();
        assertFalse("Timesheet.create-requests-admin should be false", ability.can("create-requests-admin", "Timesheet"));
    }

    @Test
    public void testMobileApplicationMyticketIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("MobileApplication.myticket should be true", ability.can("myticket", "MobileApplication"));
    }

    @Test
    public void testTimesheetShowBalanceToAdminIsTrue() throws JSONException {
        Ability ability = buildAbility();
        assertTrue("Timesheet.show-balance-to-admin should be true", ability.can("show-balance-to-admin", "Timesheet"));
    }

    @Test
    public void testVivochaOldSupportIsFalseDueToInversion() throws JSONException {
        // Vivocha.oldSupport has two rules: one regular and one inverted (last).
        // The inverted one comes last, so it takes precedence => false
        Ability ability = buildAbility();
        assertFalse("Vivocha.oldSupport should be false (inverted rule takes precedence)", ability.can("oldSupport", "Vivocha"));
    }
}
