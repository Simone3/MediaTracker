package it.polimi.dima.mediatracker.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import it.polimi.dima.mediatracker.R;
import it.polimi.dima.mediatracker.alarms.AlarmScheduler;
import it.polimi.dima.mediatracker.controllers.DatabaseManager;
import it.polimi.dima.mediatracker.controllers.SettingsManager;
import it.polimi.dima.mediatracker.utils.GlobalConstants;
import it.polimi.dima.mediatracker.utils.Utils;

/**
 * Manages the application settings
 */
public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Manage database import, if any
        final Uri data = getIntent().getData();
        if(data!=null)
        {
            // Dynamic permissions
            LinearLayout settingsMainContainer = (LinearLayout) findViewById(R.id.settings_main_container);
            Utils.askPermissionsAtRuntimeIfNeeded(this, Manifest.permission.READ_EXTERNAL_STORAGE, settingsMainContainer, R.string.permission_request_external_storage, GlobalConstants.PERMISSION_EXTERNAL_STORAGE_CODE);

            // Remove data
            getIntent().setData(null);

            // Show confirmation message
            new AlertDialog.Builder(this)
                    .setMessage(R.string.import_database_confirm_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            try
                            {
                                DatabaseManager.getInstance(SettingsActivity.this).importDatabase(data);
                                Toast.makeText(getApplicationContext(), R.string.import_database_success, Toast.LENGTH_SHORT).show();
                            }
                            catch(JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.import_database_json_error, Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            catch(DatabaseManager.DBImportValidationException e)
                            {
                                Toast.makeText(getApplicationContext(), e.getError(SettingsActivity.this), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            catch(IOException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.import_database_file_error, Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                    }).show();
        }

        // Set title
        setTitle(R.string.title_activity_settings);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();

        // Enable the Up button
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * PreferenceFragment automatically manages preferences
     */
    public static class PreferencesFragment extends PreferenceFragment
    {
        private SettingsManager settingsManager;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // Get settings manager
            settingsManager = SettingsManager.getInstance(getActivity());

            // Setup special preferences
            setupNewReleasesNotificationsActivePreference();
            setupNotificationsTimePreference();
            setupExportDatabasePreference();
            setupImportDatabasePreference();
        }

        /**
         * Manages special preference for new releases notification
         */
        private void setupNewReleasesNotificationsActivePreference()
        {
            SwitchPreference newReleasesActivePreference = (SwitchPreference) findPreference(getString(R.string.key_receive_new_releases_notifications));
            newReleasesActivePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    // When the value change, call the AlarmScheduler to start/stop the notification alarms
                    Boolean checked = (Boolean) newValue;
                    if (checked)
                    {
                        AlarmScheduler.getInstance(getActivity()).startNewReleasesAlarm();
                    }
                    else
                    {
                        AlarmScheduler.getInstance(getActivity()).stopNewReleasesAlarm();
                    }
                    return true;
                }
            });
        }

        /**
         * Manages special preference for notification time
         */
        private void setupNotificationsTimePreference()
        {
            // Get button and calendar
            final Preference newReleasesNotificationsTime = findPreference(getString(R.string.key_new_releases_notification_time_button));
            final Calendar newReleasesNotificationsCalendar = Calendar.getInstance();

            // Add onClick on the button: open dialog
            newReleasesNotificationsTime.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(final Preference preference)
                {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                    {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                        {
                            // Save values
                            settingsManager.setNewReleasesNotificationHour(hourOfDay);
                            settingsManager.setNewReleasesNotificationMinutes(minute);

                            // Update button text
                            newReleasesNotificationsCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            newReleasesNotificationsCalendar.set(Calendar.MINUTE, minute);
                            setTimePickerButtonSummary(newReleasesNotificationsTime, newReleasesNotificationsCalendar);

                            // Update notification alarms
                            AlarmScheduler.getInstance(getActivity()).updateNewReleasesAlarm();
                        }
                    }, newReleasesNotificationsCalendar.get(Calendar.HOUR_OF_DAY), newReleasesNotificationsCalendar.get(Calendar.MINUTE), android.text.format.DateFormat.is24HourFormat(getActivity()));

                    timePickerDialog.show();

                    return true;
                }
            });

            // Set initial summary for hours picker button
            newReleasesNotificationsCalendar.set(Calendar.HOUR_OF_DAY, settingsManager.getNewReleasesNotificationHour());
            newReleasesNotificationsCalendar.set(Calendar.MINUTE, settingsManager.getNewReleasesNotificationMinutes());
            setTimePickerButtonSummary(newReleasesNotificationsTime, newReleasesNotificationsCalendar);
        }

        /**
         * Manages special preference for database export
         */
        private void setupExportDatabasePreference()
        {
            // Get button
            final Preference exportButton = findPreference(getString(R.string.key_export_db_button));

            // Add onClick on the button
            exportButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(final Preference preference)
                {
                    // Call the database manager
                    DatabaseManager.getInstance(getActivity()).exportDatabase();
                    return true;
                }
            });
        }

        /**
         * Manages special preference for database import
         */
        private void setupImportDatabasePreference()
        {
            // Get button
            final Preference importButton = findPreference(getString(R.string.key_import_db_button));

            // Add onClick on the button
            importButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(final Preference preference)
                {
                    // Just show a message (import is actually performed when the user opens (e.g. from mail attachment, file manager, etc.) a JSON file
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.import_database_notice)
                            .show();

                    return true;
                }
            });
        }

        /**
         * Helper to update the time picker button with the currently selected value
         */
        private void setTimePickerButtonSummary(Preference preference, Calendar calendar)
        {
            DateFormat format = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
            preference.setSummary(format.format(calendar.getTime()));
        }
    }
}
