package it.polimi.dima.mediatracker.controllers;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
* Manages creation and deletion of files on the device storage
*/
public class StorageManager
{
    /**
     * Saves a JSON object to the device internal storage
     * @param file the file where we need to write
     * @param jsonObject the JSON object to be written in the file
     */
    public static void saveJsonObjectToInternalStorage(File file, JSONObject jsonObject)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(file);

            OutputStreamWriter outputWriter = new OutputStreamWriter(fos);
            outputWriter.write(jsonObject.toString());
            outputWriter.close();

            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Gets a JSON object from a JSON file located at the given Uri
     * @param context the context
     * @param uri the file Uri
     * @return the JSONObject contained in the file
     * @throws JSONException if the JSON object is not valid
     * @throws IOException if the file cannot be accessed
     */
    public static JSONObject getJsonObjectFromUri(Context context, Uri uri) throws JSONException, IOException
    {
        final String scheme = uri.getScheme();

        if(ContentResolver.SCHEME_FILE.equals(scheme))
        {
            ContentResolver cr = context.getContentResolver();
            InputStream is = cr.openInputStream(uri);

            if (is == null) throw new IOException();

            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null)
            {
                total.append(line);
            }

            return new JSONObject(total.toString());
        }
        else
        {
            throw new IOException();
        }
    }

    /**
     * Deletes a file from internal storage
     * @param context activity context
     * @param filename the name of the stored file
     */
    public static void deleteFileFromInternalStorage(Context context, String filename)
    {
        context.deleteFile(filename);
    }
}
