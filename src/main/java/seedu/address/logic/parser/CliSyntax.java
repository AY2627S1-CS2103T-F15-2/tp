package seedu.address.logic.parser;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_TAG = new Prefix("t/");

    /* Laplace prefix definitions. The legacy AB3 prefixes above remain unchanged. */
    public static final Prefix PREFIX_NUS_ID = new Prefix("nus/");
    public static final Prefix PREFIX_MEMBER_NAME = new Prefix("name/");
    public static final Prefix PREFIX_MEMBER_PHONE = new Prefix("phone/");
    public static final Prefix PREFIX_MEMBER_EMAIL = new Prefix("email/");
    public static final Prefix PREFIX_EQUIPMENT_UUID = new Prefix("uuid/");
    public static final Prefix PREFIX_CATEGORY = new Prefix("category/");
    public static final Prefix PREFIX_CONDITION = new Prefix("condition/");
    public static final Prefix PREFIX_NOTES = new Prefix("notes/");
    public static final Prefix PREFIX_ASSIGNED_DATE = new Prefix("assigned/");
    public static final Prefix PREFIX_EXPECTED_RETURN_DATE = new Prefix("expected/");
    public static final Prefix PREFIX_RETURNED_DATE = new Prefix("returned/");

}
