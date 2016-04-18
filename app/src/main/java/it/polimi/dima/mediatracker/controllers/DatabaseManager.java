package it.polimi.dima.mediatracker.controllers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.orm.util.NamingHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.model.Category;
import it.polimi.dima.mediatracker.model.ImportanceLevel;
import it.polimi.dima.mediatracker.model.MediaItem;
import it.polimi.dima.mediatracker.model.MediaType;

/**
 * Manages the application database (e.g. import/export)
 *
 * It does NOT manage:
 * - insert/delete/update queries (taken care by the external library Sugar ORM)
 * - select queries (taken care by the implementations of {@link MediaItemsAbstractController} and by {@link CategoriesController})
 */
public class DatabaseManager
{
    private final static String EXPORT_FILE_DIR = "files_to_share";
    private final static String EXPORT_FILE_NAME = "MediaTrackerDatabase.JSON";
    private final static String FILE_PROVIDER_AUTHORITIES = "it.polimi.dima.mediatracker.fileprovider";

    private final static String META_DB_NAME = "DATABASE";
    private final static String META_DB_VERSION = "VERSION";

    private final static String MEDIA_ITEMS_FIELD_NAME = "MEDIA_ITEMS";
    private final static String ROOT_FIELD_NAME = "CATEGORIES";

    private Context context;

    private static DatabaseManager instance;

    /**
     * Private constructor
     * @param context the context
     */
    private DatabaseManager(Context context)
    {
        this.context = context;
    }

    /**
     * Singleton pattern
     */
    public static synchronized DatabaseManager getInstance(Context context)
    {
        if(instance==null) instance = new DatabaseManager(context);
        return instance;
    }

    /**
     * Function to export the application database to a JSON file. The method creates the JSON file and then
     * opens a send intent to allow the user to save the file somewhere
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void exportDatabase()
    {
        // Get database
        SQLiteDatabase db = (new DBHelper(context, getDatabaseName(), getDatabaseVersion())).getWritableDatabase();

        // Get/create directory in the internal storage (setup for sharing with the file provider)
        File directory = new File(context.getFilesDir(), EXPORT_FILE_DIR);
        directory.mkdir();

        // Convert database to JSON object
        JSONObject databaseJson = dbToJson(db);

        // Save JSON object to a file in the internal storage
        File databaseJsonFile = new File(directory, EXPORT_FILE_NAME);
        StorageManager.saveJsonObjectToInternalStorage(databaseJsonFile, databaseJson);

        // Get the file URI from the file provider
        Uri dbUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITIES, databaseJsonFile);

        // Start sharing intent setting the file URI
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_STREAM, dbUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, ""));
    }

    /**
     * Function to import the application database from a JSON file
     * @param fileUri the URL of the file
     * @throws DBImportValidationException if something went wrong with the import
     * @throws IOException if the file cannot be accessed
     */
    public void importDatabase(Uri fileUri) throws DBImportValidationException, IOException, JSONException
    {
        // Get database
        SQLiteDatabase db = (new DBHelper(context, getDatabaseName(), getDatabaseVersion())).getWritableDatabase();

        // Get JSON object from the file
        JSONObject databaseObject = StorageManager.getJsonObjectFromUri(context, fileUri);

        // Insert values from the file
        jsonToDb(db, databaseObject);
    }

    /**
     * Getter
     * @return the database name (retrieved from the Sugar ORM manifest metadata)
     */
    private String getDatabaseName()
    {
        try
        {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            return bundle.getString(META_DB_NAME);
        }
        catch(PackageManager.NameNotFoundException | NullPointerException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Getter
     * @return the database version (retrieved from the Sugar ORM manifest metadata)
     */
    private int getDatabaseVersion()
    {
        try
        {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            return bundle.getInt(META_DB_VERSION);
        }
        catch(PackageManager.NameNotFoundException | NullPointerException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Helper that creates a JSON object from the database
     * @param db the database
     * @return the JSON object representing a database: {TABLE_NAME1=>ROWS,TABLE_NAME2=>ROWS,...} where ROWS={{COLUMN_NAME1=>VALUE1, COLUMN_NAME2=>VALUE2,...},...}
     */
    private JSONObject dbToJson(SQLiteDatabase db)
    {
        try
        {
            // Variables used in the function
            JSONObject databaseObject = new JSONObject();
            JSONObject categoryObject;
            JSONObject mediaItemObject;
            JSONArray categoriesArray;
            JSONArray mediaItemsArray;
            Cursor categoriesCursor;
            Cursor mediaItemsCursor;
            int columnIndex;
            String table;
            Long categoryId;
            MediaType mediaType;
            String value;

            // Category table
            String categoryTableName = getCategoryTableName();

            // Category column names
            List<String> categoriesColumnNames = getCategoryTableColumnNames(db);

            // Loop all media types
            HashMap<MediaType, String> mediaItemTableNames = new HashMap<>();
            HashMap<MediaType, List<String>> mediaItemColumnNames = new HashMap<>();
            for(MediaType mt: MediaType.values())
            {
                // Media type table
                table = getMediaTypeTableName(mt);
                mediaItemTableNames.put(mt, table);

                // Media type columns
                mediaItemColumnNames.put(mt, getMediaTypeTableColumnNames(db, mt));
            }

            // Get all rows in the categories table
            categoriesCursor = db.query(categoryTableName, null, null, null, null, null, null);

            // Loop all categories
            categoriesArray = new JSONArray();
            while(categoriesCursor.moveToNext())
            {
                // Initialize row object
                categoryObject = new JSONObject();

                // Loop all column names
                for(String columnName: categoriesColumnNames)
                {
                    // Put data in the row (columnName => value)
                    columnIndex = categoriesCursor.getColumnIndexOrThrow(columnName);
                    value = categoriesCursor.getString(columnIndex);
                    categoryObject.put(columnName, manageSpecialColumns(true, columnName, value));
                }

                // Get category ID
                columnIndex = categoriesCursor.getColumnIndexOrThrow(Category.COLUMN_ID);
                categoryId = Long.valueOf(categoriesCursor.getString(columnIndex));

                // Get category media type
                columnIndex = categoriesCursor.getColumnIndexOrThrow(Category.COLUMN_MEDIA_TYPE_NAME);
                mediaType = MediaType.valueOf(categoriesCursor.getString(columnIndex));

                // Get all media items in the category
                mediaItemsCursor = db.query(mediaItemTableNames.get(mediaType), null, MediaItem.COLUMN_CATEGORY + " = ?", new String[]{String.valueOf(categoryId)}, null, null, null);

                // Loop all media items
                mediaItemsArray = new JSONArray();
                while(mediaItemsCursor.moveToNext())
                {
                    // Initialize row object
                    mediaItemObject = new JSONObject();

                    // Loop all column names
                    for(String columnName: mediaItemColumnNames.get(mediaType))
                    {
                        // Put data in the row (columnName => value)
                        columnIndex = mediaItemsCursor.getColumnIndexOrThrow(columnName);
                        value = mediaItemsCursor.getString(columnIndex);
                        mediaItemObject.put(columnName, manageSpecialColumns(true, columnName, value));
                    }

                    // Add media item to array
                    mediaItemsArray.put(mediaItemObject);
                }
                mediaItemsCursor.close();

                // Add media items to category object
                categoryObject.put(MEDIA_ITEMS_FIELD_NAME, mediaItemsArray);

                // Add row object to the rows array
                categoriesArray.put(categoryObject);
            }
            categoriesCursor.close();

            // Add categories array to final object
            databaseObject.put(ROOT_FIELD_NAME, categoriesArray);

            // Return final object
            return databaseObject;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    /**
     * Deletes current database content and inserts all rows represented in the JSON object
     * @param db the database
     * @param databaseObject the JSON object
     * @throws DBImportValidationException if something went wrong with the import
     * @throws JSONException if the JSON file has a wrong structure
     */
    private void jsonToDb(SQLiteDatabase db, JSONObject databaseObject) throws DBImportValidationException, JSONException
    {
        // Variables used in the function
        JSONObject rowObject;
        HashMap<String, Object> insertValues;
        CategoriesController categoriesController = CategoriesController.getInstance();
        MediaItemsAbstractController mediaItemsController;
        String validationError;

        // Start a DB transaction
        db.beginTransaction();
        try
        {
            // Category table
            String categoryTableName = getCategoryTableName();

            // Category column names
            List<String> categoriesColumnNames = getCategoryTableColumnNames(db);

            // Category ID
            int categoryId = 1;

            // Loop all media types
            HashMap<MediaType, String> mediaItemTableNames = new HashMap<>();
            HashMap<MediaType, List<String>> mediaItemColumnNames = new HashMap<>();
            HashMap<MediaType, Integer> mediaItemIds = new HashMap<>();
            for(MediaType mediaType: MediaType.values())
            {
                // Media type table
                mediaItemTableNames.put(mediaType, getMediaTypeTableName(mediaType));

                // Media type columns
                mediaItemColumnNames.put(mediaType, getMediaTypeTableColumnNames(db, mediaType));

                // Media item ID
                mediaItemIds.put(mediaType, 1);
            }

            // Empty all tables
            for(String t: mediaItemTableNames.values())
            {
                db.execSQL("DELETE FROM " + t);
            }
            db.execSQL("DELETE FROM "+categoryTableName);

            // Get all categories in the JSON object
            JSONArray categoriesArray = databaseObject.getJSONArray(ROOT_FIELD_NAME);
            JSONObject categoryObject;
            if(categoriesArray!=null)
            {
                // Loop categories
                for(int i=0; i<categoriesArray.length(); i++)
                {
                    // Get current category
                    categoryObject = categoriesArray.getJSONObject(i);

                    // Get media type (must be correct!)
                    MediaType mediaType;
                    try
                    {
                        mediaType = MediaType.valueOf((String) categoryObject.get(Category.COLUMN_MEDIA_TYPE_NAME));
                        if(mediaType==null)
                        {
                            throw new Exception();
                        }
                    }
                    catch(Exception e)
                    {
                        throw new DBImportValidationException(categoryId, -1, context.getString(R.string.validation_category_no_media_type));
                    }

                    // Setup insert map
                    insertValues = new HashMap<>();
                    setupInsertHashMapForImport(categoryObject, categoriesColumnNames, insertValues);

                    // Add the ID (generated dynamically during import)
                    insertValues.put(Category.COLUMN_ID, categoryId);

                    // Validate category
                    validationError = categoriesController.validateCategoryDbRow(context, insertValues);
                    if(validationError!=null)
                    {
                        throw new DBImportValidationException(categoryId, -1, validationError);
                    }

                    // Insert category
                    insertRowForImport(db, categoryTableName, insertValues);

                    // Get media type controller
                    mediaItemsController = mediaType.getController();

                    // Get media items in this category (if any)
                    JSONArray mediaItemsArray;
                    try
                    {
                        mediaItemsArray = categoryObject.getJSONArray(MEDIA_ITEMS_FIELD_NAME);
                    }
                    catch(JSONException e)
                    {
                        mediaItemsArray = null;
                    }
                    if(mediaItemsArray!=null && mediaItemsArray.length()>0)
                    {
                        // Loop media items
                        for(int j=0; j<mediaItemsArray.length(); j++)
                        {
                            // Get current media item
                            rowObject = mediaItemsArray.getJSONObject(j);

                            // Setup insert lists
                            insertValues = new HashMap<>();
                            setupInsertHashMapForImport(rowObject, mediaItemColumnNames.get(mediaType), insertValues);

                            // Add the ID (generated dynamically during import)
                            insertValues.put(MediaItem.COLUMN_ID, mediaItemIds.get(mediaType));

                            // Add the category ID
                            insertValues.put(MediaItem.COLUMN_CATEGORY, categoryId);

                            // Validate media item
                            validationError = mediaItemsController.validateMediaItemDbRow(context, insertValues);
                            if(validationError!=null)
                            {
                                throw new DBImportValidationException(categoryId, j+1, validationError);
                            }

                            // Insert media item
                            insertRowForImport(db, mediaItemTableNames.get(mediaType), insertValues);

                            // Increase media item ID
                            mediaItemIds.put(mediaType, mediaItemIds.get(mediaType)+1);
                        }
                    }

                    // Increase category ID
                    categoryId++;
                }
            }

            // If we are here, transaction is successful
            db.setTransactionSuccessful();
        }
        finally
        {
            // End transaction (commit only if we called setTransactionSuccessful)
            db.endTransaction();
        }
    }

    /**
     * Helper for DB import to build the insert lists
     * @param object contains data to import
     * @param columnNames all column names
     * @param insertValues map column => value
     */
    private void setupInsertHashMapForImport(JSONObject object, List<String> columnNames, HashMap<String, Object> insertValues)
    {
        // Loop all column names
        for(String column: columnNames)
        {
            // Add column => value in the map
            try
            {
                insertValues.put(column, manageSpecialColumns(false, column, object.get(column)));
            }
            catch(JSONException e)
            {
                insertValues.put(column, null);
            }
        }
    }

    /**
     * Helper for DB import to add a row
     * @param db the DB
     * @param table the table
     * @param insertValues map column => value
     */
    private void insertRowForImport(SQLiteDatabase db, String table, HashMap<String, Object> insertValues)
    {
        // If we have data...
        if(insertValues.size()>0)
        {
            // Get columns, placeholders and values
            List<String> insertColumnsList = new ArrayList<>();
            List<String> insertPlaceholdersList = new ArrayList<>();
            List<Object> insertValuesList = new ArrayList<>();
            for(Map.Entry<String, Object> pair: insertValues.entrySet())
            {
                insertColumnsList.add(pair.getKey());
                insertPlaceholdersList.add("?");
                insertValuesList.add(pair.getValue());
            }

            // Insert row
            db.execSQL("INSERT OR REPLACE INTO " + table + " (" +
                            TextUtils.join(", ", insertColumnsList) + ") " +
                            "VALUES (" + TextUtils.join(", ", insertPlaceholdersList) + ") ",
                    insertValuesList.toArray());
        }
    }

    /**
     * Getter
     * @return the name of the categories table
     */
    private String getCategoryTableName()
    {
        return NamingHelper.toSQLName(Category.class);
    }

    /**
     * Getter
     * @param db the DB
     * @return all columns to import/export in the categories table
     */
    private List<String> getCategoryTableColumnNames(SQLiteDatabase db)
    {
        Cursor rowsCursor = db.query(getCategoryTableName(), null, null, null, null, null, null);
        List<String> columnNames = new ArrayList<>(Arrays.asList(rowsCursor.getColumnNames()));

        columnNames.remove(Category.COLUMN_ID);

        rowsCursor.close();
        return columnNames;
    }

    /**
     * Getter
     * @param mediaType the media type
     * @return the name of the media item table linked with the given media type
     */
    private String getMediaTypeTableName(MediaType mediaType)
    {
        return NamingHelper.toSQLName(mediaType.getController().getModelClass());
    }

    /**
     * Getter
     * @param db the DB
     * @param mediaType the media type
     * @return all columns to import/export in the media item table linked with the given media type
     */
    private List<String> getMediaTypeTableColumnNames(SQLiteDatabase db, MediaType mediaType)
    {
        Cursor rowsCursor = db.query(getMediaTypeTableName(mediaType), null, null, null, null, null, null);
        List<String> columnNames = new ArrayList<>(Arrays.asList(rowsCursor.getColumnNames()));

        columnNames.remove(MediaItem.COLUMN_ID);
        columnNames.remove(MediaItem.COLUMN_CATEGORY);

        rowsCursor.close();
        return columnNames;
    }

    /**
     * Manages special columns that are not exported/imported exactly as they are in the database
     * @param fromDbToJson true if we are translating from the DB to a JSON file, false otherwise
     * @param column the column name
     * @param value the column value
     * @return the new value of the object (possibly different from the one passed as argument)
     */
    private Object manageSpecialColumns(boolean fromDbToJson, String column, Object value)
    {
        // Switch special columns
        switch(column)
        {
            // Importance level
            case MediaItem.COLUMN_IMPORTANCE_LEVEL:
                if(value!=null && value instanceof String)
                {
                    // Integer value -> name
                    if(fromDbToJson)
                    {
                        int dbValue = Integer.parseInt((String) value);
                        for(ImportanceLevel il: ImportanceLevel.values())
                        {
                            if(dbValue==il.getDbValue()) return il.name();
                        }
                    }

                    // Name -> integer value
                    else
                    {
                        String ilName = (String) value;
                        for(ImportanceLevel il: ImportanceLevel.values())
                        {
                            if(ilName.equals(il.name())) return String.valueOf(il.getDbValue());
                        }
                    }
                }
        }

        // No change
        return value;
    }

    /**
     * Helper to get the application database
     */
    private static class DBHelper extends SQLiteOpenHelper
    {
        public DBHelper(Context context, String databaseName, int databaseVersion)
        {
            super(context, databaseName, null, databaseVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db){}

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){}
    }

    /**
     * An exception thrown during DB import if data has some validation error
     */
    public class DBImportValidationException extends Exception
    {
        private String error;
        private int invalidCategoryPosition;
        private int invalidMediaItemPosition;

        /**
         * Constructor
         * @param invalidCategoryPosition the linked category position (starting from 1)
         * @param invalidMediaItemPosition the linked media item position (starting from 1), set <=0 if no linked media item
         * @param error the error to be shown to the user
         */
        public DBImportValidationException(int invalidCategoryPosition, int invalidMediaItemPosition, String error)
        {
            super();

            this.error = error;
            this.invalidCategoryPosition = invalidCategoryPosition;
            this.invalidMediaItemPosition = invalidMediaItemPosition;
        }

        /**
         * Getter
         * @return the error to be shown to the user
         */
        public String getError(Context context)
        {
            if(invalidMediaItemPosition<=0)
            {
                return context.getString(R.string.import_database_validation_error_category, invalidCategoryPosition, error);
            }
            else
            {
                return context.getString(R.string.import_database_validation_error_media_item, invalidCategoryPosition, invalidMediaItemPosition, error);
            }
        }
    }
}
