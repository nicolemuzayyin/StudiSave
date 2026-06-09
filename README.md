# StudiSave

### Grade: 77/100

### Youtube Run-through Video Link: https://www.youtube.com/watch?v=D9Yq5WvE7L0

### Run Application

The default login is fred@uni.uk, admin.
Application should run from Main.java, or alternatively, with mvn clean javafx:run

### Dependencies

| Library                            | Purpose                                     |
|------------------------------------|---------------------------------------------|
| org.openjfx:javafx-controls:21.0.6 | JavaFX controls (TableView, ComboBox, etc.) |
| org.openjfx:javafx-fxml:21.0.6     | FXMLLoader and @FXML annotation processing  |
| org.openjfx:javafx-graphics:21.0.6 | JavaFX rendering and scene graph            |
| org.xerial:sqlite-jdbc:3.45.3.0    | SQLite JDBC driver                          |


### Structure of Packages

| Package           | Purpose                           |
|-------------------|-----------------------------------|
| core/ui           | JavaFX controllers                |
| core/db_functions | Interact with the database        |
| core/db_library   | SQL abstraction                   |
| core/tables       | Data classes for database tables  |
| resoruces/core/ui | FXML layout files                 |
| resources/sql     | SQL files to be loaded as strings |

### Key Classes

| Class            | Summary                                                                                                                                                                                                                                                                                                                      |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Query            | Stores a single SQL statement, the expected result column names, the results rows, and optionally any bind parameters (to replace ? in SQL strings)                                                                                                                                                                          |
| QueryLibrary     | Constructs Query objects from SQL strings or files. (Files processed into strings using Query.fileToString())                                                                                                                                                                                                                |
| Action           | Creates lists of Query objects for each page or user action and passes them to ManageConnection to be executed. Returns the queries so the results can be used.                                                                                                                                                              |
| ManageConnection | Opens one connection per execute call, iterates over the list of queries and executes their SQL in order. Uses runRead() or runWrite() based on wether the resultsColumns is null (write queries have no results). PreparedStatement is used when parameters are not null (any ? in the SQL must be defined as a parameter). |
| Table Classes    | Plain data objects used for field access - from results rows or user input                                                                                                                                                                                                                                                   |
 

