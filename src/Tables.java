import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

public class Tables {
    public static String[] dbTables = {
            "Endorsement", "Review", "Attendance", "Movie", "Customer"
    };
    //todo: update names of dbFunctions, + chuhan's functions name
    private static String[] dbFunctions = {"isValidEmail", "isValidAttendanceDate", "checkReviewDate", "isReviewAllowed", "isFirstReview", "isValidRating", " isEndorseAllowed"};

    /** (yuting)
     * Create a derby connection for iRate database
     * @return Connection conn
     */
    public static Connection newConnection(){
        String protocol = "jdbc:derby:";
        String dbName = "iRate";
        String connStr = protocol + dbName + ";create=true";


        Connection conn = null;
        Properties props = new Properties();
        props.put("user", "user1");
        props.put("password", "user1");

        try {
            conn = DriverManager.getConnection(connStr, props);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database " + dbName + " not created.");
        }

        if (conn == null) System.err.println("Failed in connection.");
        else System.out.println("Connected to and created database " + dbName);

        return conn;
    }

    /**
     * Create all tables and functions.
     * Drop them first if there is any table/function exist
     * @param stmt statement
     */
    public static void initializeAll(Statement stmt){
        dropConstraints(stmt);
        dropFunctions(stmt);
        dropTables(stmt);
        createFunctions(stmt);
        createTables(stmt);
    }

    /**
     * Drop constraints in the tables:
     * Customer:valid_email
     * Review: valid_firstReview, valid_rating
     * Endorsement: valid_endorsement
     * @param stmt derby statement
     */
    private static void dropConstraints(Statement stmt) {
        String[] tables = {"Customer", "Attendance", "Review", "Endorsement"};
        String[][] constraints = {{"valid_email"}, {"valid_attendanceDate"}, {"valid_reviewDate", "valid_firstReview",
                "valid_rating"}, {"valid_endorsement"}};

        for(int i=0; i<tables.length; i++) {
            for (String constraint: constraints[i]) {
                try {
                    stmt.executeUpdate("alter table " + tables[i] + " drop constraint " + constraint);
                } catch (SQLException e) {
                }
            }
        }
        System.out.println("Dropped all constraints.");
    }


    private static void dropTables(Statement stmt) {
        for (String tbl : dbTables) {
            try {
                stmt.executeUpdate("drop table " + tbl);
            } catch (SQLException ex) {
            }
        }
        System.out.println("Dropped all tables.");
    }

    private static void createTables(Statement stmt) {
        try {
            // create the Customer table
            String createTable_Customer =
                    "create table Customer ("
                            + "  Name varchar(32) not null,"
                            + "  Email varchar(64) not null,"
                            + "  JoinedDate date not null,"
                            + "  CustomerID int not null,"
                            + "  CONSTRAINT valid_email check(isValidEmail(Email)),"
                            + "  primary key (CustomerID)"
                            + ")";
            stmt.executeUpdate(createTable_Customer);
//            System.out.println("Created entity table Customer");

            // create the Movie table
            String createTable_Movie =
                    "create table Movie ("
                            + "  Title varchar(32) not null,"
                            + "  MovieID int not null,"
                            + "  primary key (MovieID)"
                            + ")";
            stmt.executeUpdate(createTable_Movie);
//            System.out.println("Created entity table Movie");

            // create the Attendance table: this is a record of a movie seen by a customer on a given date
            String createTable_Attendance =
                    "create table Attendance ("
                            + "  MovieID int not null,"
                            + "  AttendanceDATE date not null,"
                            + "  CustomerID int not null,"
                            + "  CONSTRAINT valid_attendanceDate check(isValidAttendanceDate(CustomerID, AttendanceDATE)),"
                            + "  primary key (MovieID, CustomerID, AttendanceDATE),"
                            + "  foreign key (MovieID) references Movie(MovieID) on delete cascade,"
                            + "  foreign key (CustomerID) references Customer(CustomerID) on delete cascade"
                            + ")";
            stmt.executeUpdate(createTable_Attendance);
//            System.out.println("Created relation table Attendance");

            // create the Review table: this is a review of a particular movie attended by a customer within the last week
            String createTable_Review =
                    "create table Review ("
                            + "  CustomerID int not null,"
                            + "  MovieID int not null,"
                            + "  ReviewDate date not null,"
                            + "  Rating int not null,"
                            + "  Review varchar(1000) not null,"
                            + "  ReviewID int not null,"
                            + "  CONSTRAINT valid_reviewDate check(checkReviewDate(CustomerID, MovieID, ReviewDate)),"
                            + "  CONSTRAINT valid_firstReview check(isFirstReview(CustomerID, MovieID)),"
                            + "  CONSTRAINT valid_rating check(isValidRating(Rating)),"
                            + "  primary key (ReviewID),"
                            + "  foreign key (MovieID) references Movie(MovieID) on delete cascade,"
                            + "  foreign key (CustomerID) references Customer(CustomerID) on delete cascade"
                            + "  )";
            stmt.executeUpdate(createTable_Review);
//            System.out.println("Created relation table Review");

            // create the Endorsement table: this is an endorsement of a movie review by a customer

            String createTable_Endorsement =
                    "create table Endorsement ("
                            + "  ReviewID int,"
                            + "  CustomerID int,"
                            + "  EndorsementDate date not null,"
                            + "  CONSTRAINT valid_endorsement check(isEndorseAllowed(ReviewID, CustomerID, EndorsementDate)),"
                            + "  primary key (ReviewID , CustomerID),"
                            + "  foreign key (ReviewID) references Review (ReviewID) on delete cascade,"
                            + "  foreign key (CustomerID) references Customer (CustomerID) on delete cascade"
                            + " )";
            stmt.executeUpdate(createTable_Endorsement);
//            System.out.println("Created relation table Endorsement");

        } catch (SQLException ex) {
            System.err.println("Error in Tables.createTables()");
            ex.printStackTrace();
        }

        System.out.println("Created all tables: " + Arrays.toString(dbTables));
    }


    private static void dropFunctions(Statement stmt) {
        for (String func : dbFunctions) {
            try {
                stmt.executeUpdate("drop function " + func);
            } catch (SQLException ex) {
                //ex.printStackTrace();
            }
        }
        System.out.println("Dropped all functions.");
    }

    public static void createFunctions(Statement stmt) {
        try {
            // create function isValidEmail
            String createFunction_isValidEmail =
                    "CREATE FUNCTION isValidEmail(email varchar(64))"
                            + " RETURNS BOOLEAN "
                            + " PARAMETER STYLE JAVA "
                            + " LANGUAGE JAVA "
                            + " NO SQL "
                            + " EXTERNAL NAME "
                            + "'DBFunctions.isValidEmail'";
            stmt.executeUpdate(createFunction_isValidEmail);

            // create function isValidAttendanceDate
            String createFunction_isValidAttendanceDate =
                    "CREATE FUNCTION isValidAttendanceDate(cid int, attendanceDate date)"
                            + " RETURNS BOOLEAN "
                            + " PARAMETER STYLE JAVA "
                            + " LANGUAGE JAVA "
                            + " NO SQL "
                            + " EXTERNAL NAME "
                            + "'DBFunctions.isValidAttendanceDate'";
            stmt.executeUpdate(createFunction_isValidAttendanceDate);

            // create function checkReviewDate
            String createFunction_checkReviewDate=
                    "CREATE FUNCTION checkReviewDate(cid int, mid int, reviewDate date)"
                            + " RETURNS BOOLEAN "
                            + " PARAMETER STYLE JAVA "
                            + " LANGUAGE JAVA "
                            + " NO SQL "
                            + " EXTERNAL NAME "
                            + "'DBFunctions.checkReviewDate'";
            stmt.executeUpdate(createFunction_checkReviewDate);

            // create function isFirstReview
            String createFunction_isFirstReview =
                    "CREATE FUNCTION isFirstReview(cid int, mid int)"
                            + " RETURNS BOOLEAN "
                            + " PARAMETER STYLE JAVA "
                            + " LANGUAGE JAVA "
                            + " NO SQL "
                            + " EXTERNAL NAME "
                            + "'DBFunctions.isFirstReview'";
            stmt.executeUpdate(createFunction_isFirstReview);

            // create function isValidRating
            String createFunction_isValidRating =
                    "CREATE FUNCTION isValidRating(rating int)"
                            + " RETURNS BOOLEAN "
                            + " PARAMETER STYLE JAVA "
                            + " LANGUAGE JAVA "
                            + " NO SQL "
                            + " EXTERNAL NAME "
                            + "'DBFunctions.isValidRating'";
            stmt.executeUpdate(createFunction_isValidRating);


            // create function isEndorseAllowed
            String createFunction_isEndorseAllowed =
                    "CREATE FUNCTION isEndorseAllowed(rid int, cid int, endorseDate date)"
                            + " RETURNS BOOLEAN "
                            + " PARAMETER STYLE JAVA "
                            + " LANGUAGE JAVA "
                            + " NO SQL "
                            + " EXTERNAL NAME "
                            + "'DBFunctions.isEndorseAllowed'";
            stmt.executeUpdate(createFunction_isEndorseAllowed);

        } catch (SQLException ex) {
            System.err.println("Error in Tables.createFunctions()");
            ex.printStackTrace();
        }
        System.out.println("Created all functions: " + Arrays.toString(dbFunctions));
    }
}

