package cx.smile.CASL;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Instance-level condition tests using actual API response data.
 * Verifies that conditions work when passing subject data dictionaries.
 */
public class RealWorldConditionsTest {

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

    // --- Helper ---

    /** Build a typed subject Map with __caslSubjectType__ set. */
    private Map<String, Object> subject(String type) {
        return subject(type, new HashMap<>());
    }

    private Map<String, Object> subject(String type, Map<String, Object> data) {
        Map<String, Object> result = new LinkedHashMap<>(data);
        result.put("__caslSubjectType__", type);
        return result;
    }

    /** Convenience for building a map inline. */
    @SafeVarargs
    private static <K, V> Map<K, V> mapOf(Object... keyValues) {
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            @SuppressWarnings("unchecked")
            K key = (K) keyValues[i];
            @SuppressWarnings("unchecked")
            V value = (V) keyValues[i + 1];
            map.put(key, value);
        }
        return map;
    }

    // ---- Application.query with $in conditions ----

    @Test
    public void testApplicationQuerySmartMeetIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-meet"))));
    }

    @Test
    public void testApplicationQuerySpartaAiIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "_sparta-ai"))));
    }

    @Test
    public void testApplicationQuerySpartaAi2IsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "sparta-ai-2"))));
    }

    @Test
    public void testApplicationQuerySmartHomeIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-home"))));
    }

    @Test
    public void testApplicationQuerySmartRoomsIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-rooms"))));
    }

    @Test
    public void testApplicationQuerySmartLearningIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-learning"))));
    }

    @Test
    public void testApplicationQuerySmartEmployeesIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-employees"))));
    }

    @Test
    public void testApplicationQuerySmartTimesheetIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-timesheet"))));
    }

    @Test
    public void testApplicationQuerySmartReportsIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-reports"))));
    }

    @Test
    public void testApplicationQueryMyticketIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "myticket"))));
    }

    @Test
    public void testApplicationQuerySmartNotesIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-notes"))));
    }

    @Test
    public void testApplicationQuerySmartAppraisalIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-appraisal"))));
    }

    @Test
    public void testApplicationQueryDocdigitIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "docdigit"))));
    }

    @Test
    public void testApplicationQuerySmartAgentDesktopIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-agent-desktop"))));
    }

    @Test
    public void testApplicationQueryCentralizedLoggingIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "centralized-logging"))));
    }

    @Test
    public void testApplicationQuerySmartCtiConfiguratorIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smart-cti-configurator"))));
    }

    @Test
    public void testApplicationQueryDocswebIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "docsweb"))));
    }

    @Test
    public void testApplicationQuerySpartawebCorp5IsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "spartaweb-corp5"))));
    }

    @Test
    public void testApplicationQuerySmileCxDocsIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "smile-cx-docs"))));
    }

    @Test
    public void testApplicationQueryMyTestAppWithIconIsTrue() {
        assertTrue(ability.can("query", subject("Application", mapOf("id", "MyTestAppWithIcon"))));
    }

    @Test
    public void testApplicationQueryUnknownAppIsFalse() {
        assertFalse("Application.query with unknown id should be false",
                ability.can("query", subject("Application", mapOf("id", "unknown-app"))));
    }

    @Test
    public void testApplicationQueryRandomIdIsFalse() {
        assertFalse("Application.query with random id should be false",
                ability.can("query", subject("Application", mapOf("id", "random-id"))));
    }

    @Test
    public void testApplicationTrackNoConditionsIsTrue() {
        // Application.track has no conditions, so any instance matches
        assertTrue(ability.can("track", subject("Application", mapOf("id", "anything"))));
    }

    // ---- Application._query with condition ----

    @Test
    public void testApplicationUnderscoreQueryRegalonataleIsTrue() {
        assertTrue(ability.can("_query", subject("Application", mapOf("id", "regalonatale"))));
    }

    @Test
    public void testApplicationUnderscoreQueryOtherIsFalse() {
        assertFalse("Application._query with non-matching id should be false",
                ability.can("_query", subject("Application", mapOf("id", "other"))));
    }

    // ---- SpartaReport.yellow with equality conditions ----

    @Test
    public void testSpartaReportYellowMatchingTeamIsTrue() {
        assertTrue(ability.can("yellow", subject("SpartaReport", mapOf("Team", "SCX Product UX & UI"))));
    }

    @Test
    public void testSpartaReportYellowMatchingGruppoIsTrue() {
        assertTrue(ability.can("yellow", subject("SpartaReport", mapOf("GruppoPianificazione", "Technology & People"))));
    }

    @Test
    public void testSpartaReportYellowOtherTeamIsFalse() {
        assertFalse("SpartaReport.yellow with non-matching Team should be false",
                ability.can("yellow", subject("SpartaReport", mapOf("Team", "Other Team"))));
    }

    @Test
    public void testSpartaReportYellowOtherGruppoIsFalse() {
        assertFalse("SpartaReport.yellow with non-matching GruppoPianificazione should be false",
                ability.can("yellow", subject("SpartaReport", mapOf("GruppoPianificazione", "Other Group"))));
    }

    // ---- PrivacyPolicy with userId condition ----

    @Test
    public void testPrivacyPolicyQueryMatchingUserIdIsTrue() {
        assertTrue(ability.can("query", subject("PrivacyPolicy", mapOf("userId", "5f6a22bd844d146134d6f4fc"))));
    }

    @Test
    public void testPrivacyPolicyQueryWrongUserIdIsFalse() {
        assertFalse("PrivacyPolicy.query with wrong userId should be false",
                ability.can("query", subject("PrivacyPolicy", mapOf("userId", "other-user-id"))));
    }

    @Test
    public void testPrivacyPolicyCreateNoConditionsIsTrue() {
        // PrivacyPolicy.create has no conditions
        assertTrue(ability.can("create", subject("PrivacyPolicy")));
    }

    // ---- CsvExportConfiguration with userId condition ----

    @Test
    public void testCsvExportQueryMatchingUserIdIsTrue() {
        assertTrue(ability.can("query", subject("CsvExportConfiguration", mapOf("userId", "5f6a22bd844d146134d6f4fc"))));
    }

    @Test
    public void testCsvExportQueryWrongUserIdIsFalse() {
        assertFalse("CsvExportConfiguration.query with wrong userId should be false",
                ability.can("query", subject("CsvExportConfiguration", mapOf("userId", "wrong-id"))));
    }

    @Test
    public void testCsvExportCreateMatchingUserIdIsTrue() {
        assertTrue(ability.can("create", subject("CsvExportConfiguration", mapOf("userId", "5f6a22bd844d146134d6f4fc"))));
    }

    @Test
    public void testCsvExportDeleteMatchingUserIdIsTrue() {
        assertTrue(ability.can("delete", subject("CsvExportConfiguration", mapOf("userId", "5f6a22bd844d146134d6f4fc"))));
    }

    @Test
    public void testCsvExportDeleteWrongUserIdIsFalse() {
        assertFalse("CsvExportConfiguration.delete with wrong userId should be false",
                ability.can("delete", subject("CsvExportConfiguration", mapOf("userId", "wrong-id"))));
    }

    // ---- Timesheet.get-request-types with id condition ----

    @Test
    public void testTimesheetGetRequestTypesRolIsTrue() {
        assertTrue(ability.can("get-request-types", subject("Timesheet", mapOf("id", "rol"))));
    }

    @Test
    public void testTimesheetGetRequestTypesFerieIsTrue() {
        assertTrue(ability.can("get-request-types", subject("Timesheet", mapOf("id", "ferie"))));
    }

    @Test
    public void testTimesheetGetRequestTypesDeleteApprovedIsTrue() {
        assertTrue(ability.can("get-request-types", subject("Timesheet", mapOf("id", "delete-approved-request"))));
    }

    @Test
    public void testTimesheetGetRequestTypesMalattiaIsFalse() {
        assertFalse("Timesheet.get-request-types with id=malattia should be false",
                ability.can("get-request-types", subject("Timesheet", mapOf("id", "malattia"))));
    }

    @Test
    public void testTimesheetGetRequestTypesOtherIsFalse() {
        assertFalse("Timesheet.get-request-types with id=other should be false",
                ability.can("get-request-types", subject("Timesheet", mapOf("id", "other"))));
    }

    // ---- Vivocha.oldSupport inverted rule precedence ----

    @Test
    public void testVivochaOldSupportTypeLevelIsFalse() {
        // Inverted rule comes last, so it takes precedence
        assertFalse("Vivocha.oldSupport should be false (inverted rule takes precedence)",
                ability.can("oldSupport", "Vivocha"));
    }

    @Test
    public void testVivochaOldSupportInstanceLevelIsFalse() {
        // Same result at instance level
        assertFalse("Vivocha.oldSupport instance-level should be false (inverted rule takes precedence)",
                ability.can("oldSupport", subject("Vivocha")));
    }

    // ---- _User subject (underscore prefix in subject name) ----

    @Test
    public void testUserUnderscoreGetHierarchyMatchingCompanyIdIsTrue() {
        // Only the rule with company.id = 30 can match (the $in: [] rule matches nothing)
        Map<String, Object> subj = subject("_User", mapOf(
                "facets", mapOf(
                        "sparta", mapOf(
                                "company", mapOf("id", 30)
                        )
                )
        ));
        assertTrue(ability.can("get-hierarchy", subj));
    }

    @Test
    public void testUserUnderscoreGetHierarchyNonMatchingCompanyIdIsFalse() {
        // 99 is not in empty $in and not equal to 30
        Map<String, Object> subj = subject("_User", mapOf(
                "facets", mapOf(
                        "sparta", mapOf(
                                "company", mapOf("id", 99)
                        )
                )
        ));
        assertFalse("_User.get-hierarchy with company.id=99 should be false",
                ability.can("get-hierarchy", subj));
    }

    @Test
    public void testUserUnderscoreGetChildrenMatchingCompanyIdIsTrue() {
        Map<String, Object> subj = subject("_User", mapOf(
                "facets", mapOf(
                        "sparta", mapOf(
                                "company", mapOf("id", 30)
                        )
                )
        ));
        assertTrue(ability.can("get-children", subj));
    }

    // ---- User.query with dot-notation conditions (nested subject data) ----

    @Test
    public void testUserQueryWithMatchingSmartTagsIsTrue() {
        // Rule: conditions = {"facets.smart.tags": "vivochaSupport"}
        Map<String, Object> subj = subject("User", mapOf(
                "facets", mapOf(
                        "smart", mapOf("tags", "vivochaSupport")
                )
        ));
        assertTrue(ability.can("query", subj));
    }

    @Test
    public void testUserQueryWithMatchingFeatureUserIsTrue() {
        // Rule: conditions = {"featureUser": true}
        Map<String, Object> subj = subject("User", mapOf("featureUser", true));
        assertTrue(ability.can("query", subj));
    }

    // ---- User.query field-level checks ----

    @Test
    public void testUserQueryFieldIdIsTrue() {
        // _id appears in multiple non-inverted User.query rules with fields
        assertTrue(ability.can("query", subject("User"), "_id"));
    }

    @Test
    public void testUserQueryFieldFirstnameIsTrue() {
        assertTrue(ability.can("query", subject("User"), "firstname"));
    }

    @Test
    public void testUserQueryFieldContractIsFalse() {
        // facets.sparta.contract.** is in the inverted rule's fields
        assertFalse("User.query field facets.sparta.contract.something should be blocked by inverted rule",
                ability.can("query", subject("User"), "facets.sparta.contract.something"));
    }

    @Test
    public void testUserQueryFieldBankInfoIsFalse() {
        // facets.sparta.bankInfo.** is in the inverted rule's fields
        assertFalse("User.query field facets.sparta.bankInfo.iban should be blocked by inverted rule",
                ability.can("query", subject("User"), "facets.sparta.bankInfo.iban"));
    }

    @Test
    public void testUserQueryFieldGenderIsTrue() {
        // gender is in the inverted rule's fields, but a later non-inverted rule
        // also includes gender in its fields, so the last rule wins => allowed
        assertTrue(ability.can("query", subject("User"), "gender"));
    }
}
