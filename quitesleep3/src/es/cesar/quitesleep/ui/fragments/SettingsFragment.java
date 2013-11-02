/* 
 	Copyright 2010 Cesar Valiente Gordo
 
 	This file is part of QuiteSleep.

    QuiteSleep is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    QuiteSleep is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with QuiteSleep.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.cesar.quitesleep.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;

import es.cesar.quitesleep.R;
import es.cesar.quitesleep.application.QuiteSleepApp;
import es.cesar.quitesleep.components.listeners.ContactDialogListener;
import es.cesar.quitesleep.data.controllers.ClientDDBB;
import es.cesar.quitesleep.data.models.MuteOrHangUp;
import es.cesar.quitesleep.data.models.Settings;
import es.cesar.quitesleep.operations.StartStopServicesOperations;
import es.cesar.quitesleep.settings.ConfigAppValues;
import es.cesar.quitesleep.ui.activities.BlockCallsActivity;
import es.cesar.quitesleep.ui.activities.MailSettings;
import es.cesar.quitesleep.ui.activities.SmsSettings;
import es.cesar.quitesleep.ui.notifications.QuiteSleepNotification;
import es.cesar.quitesleep.utils.ExceptionUtils;
import es.cesar.quitesleep.utils.Log;

/**
 * 
 * @author Cesar Valiente Gordo
 * @mail cesar.valiente@mgail.com
 * 
 */
public class SettingsFragment extends SherlockFragment implements OnClickListener, ContactDialogListener {

    private final String CLASS_NAME = getClass().getName();

    // Activity buttons
    private Button mailButton;
    private Button smsButton;
    private Button blockOtherCalls;
    private RadioButton muteRButton;
    private RadioButton hangUpRButton;
    private ToggleButton serviceToggleButton;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        return inflater.inflate(R.layout.settingstab, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);

        // All Activity buttons
        mailButton = (Button) getSherlockActivity().findViewById(R.id.settings_button_mail);
        smsButton = (Button) getSherlockActivity().findViewById(R.id.settings_button_sms);
        blockOtherCalls = (Button) getSherlockActivity().findViewById(R.id.settings_button_blockCallsConfiguration);
        muteRButton = (RadioButton) getSherlockActivity().findViewById(R.id.settings_radiobutton_mute);
        hangUpRButton = (RadioButton) getSherlockActivity().findViewById(R.id.settings_radiobutton_hangup);
        serviceToggleButton = (ToggleButton) getSherlockActivity().findViewById(R.id.settings_togglebutton_service);

        // Define all button listeners
        mailButton.setOnClickListener(this);
        smsButton.setOnClickListener(this);
        blockOtherCalls.setOnClickListener(this);
        muteRButton.setOnClickListener(this);
        hangUpRButton.setOnClickListener(this);
        serviceToggleButton.setOnClickListener(this);

        /*
         * Set the previous saved state in the ddbb in the widgets that need the
         * saved state, such as toggle button.
         */
        initActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        initActivity();
    }

    /**
     * Listener for all buttons in this Activity
     */
    @Override
    public void onClick(final View view) {

        int viewId = view.getId();

        switch (viewId) {

            case R.id.settings_button_mail:
                Intent intentMailSettings = new Intent(QuiteSleepApp.getContext(), MailSettings.class);
                startActivityForResult(intentMailSettings, ConfigAppValues.REQCODE_MAIL_SETTINGS);
                break;

            case R.id.settings_button_sms:
                Intent intentSmsSettings = new Intent(QuiteSleepApp.getContext(), SmsSettings.class);
                startActivityForResult(intentSmsSettings, ConfigAppValues.REQCODE_SMS_SETTINGS);
                break;

            case R.id.settings_radiobutton_mute:
                /*
                 * false (although the mute option is check to true) indicates
                 * that the user has selected mute option
                 */
                saveMuteOrHangUpOption(false);
                break;

            case R.id.settings_radiobutton_hangup:
                // true indicates that the user has selected hang up option
                saveMuteOrHangUpOption(true);
                break;

            case R.id.settings_button_blockCallsConfiguration:
                Intent intentBlockOtherCalls = new Intent(QuiteSleepApp.getContext(), BlockCallsActivity.class);
                startActivityForResult(intentBlockOtherCalls, ConfigAppValues.REQCODE_BLOCK_OTHER_CALLS);
                break;

            case R.id.settings_togglebutton_service:
                startStopServiceProcess();
                QuiteSleepNotification
                        .showNotification(QuiteSleepApp.getContext(), serviceToggleButton.isChecked());
                break;

            default:
                break;
        }
    }

    /**
     * This function save the user option regarding both mute or hang up in the
     * ddbb.
     * 
     * @param optionValue
     */
    private void saveMuteOrHangUpOption(final boolean optionValue) {

        try {

            ClientDDBB clientDDBB = new ClientDDBB();
            MuteOrHangUp muteOrHangup = clientDDBB.getSelects().selectMuteOrHangUp();
            if (muteOrHangup != null) {

                // If is true, then the hangup option is established
                if (optionValue) {
                    muteOrHangup.setHangUp(true);
                    ConfigAppValues.setMuteOrHangup(true);
                }
                // If is false, then the mute mode is set to true
                else {
                    muteOrHangup.setMute(true);
                    ConfigAppValues.setMuteOrHangup(false);
                }

                clientDDBB.getUpdates().insertMuteOrHangUp(muteOrHangup);
                clientDDBB.commit();
            }

            // If MuteOrHangUp object is not created.
            else {
                muteOrHangup = new MuteOrHangUp();
                clientDDBB.getInserts().insertMuteOrHangUp(muteOrHangup);
                clientDDBB.commit();
            }
            clientDDBB.close();

        } catch (Exception e) {
            Log.e(CLASS_NAME, ExceptionUtils.getString(e));
        }
    }

    /**
     * Put the ddbb saved data in the activity widgets
     */
    private void initActivity() {

        try {
            ClientDDBB clientDDBB = new ClientDDBB();

            // Togglebutton (QuuiteSleep service) check
            Settings settings = clientDDBB.getSelects().selectSettings();
            if (settings != null) {
                serviceToggleButton.setChecked(settings.isQuiteSleepServiceState());
            } else {
                serviceToggleButton.setChecked(false);
            }

            // Mute or hangup radio buttons check
            if (ConfigAppValues.getMuteOrHangup() == null) {
                MuteOrHangUp muteOrHangup = clientDDBB.getSelects().selectMuteOrHangUp();
                if (muteOrHangup != null) {
                    if (muteOrHangup.isMute()) {
                        muteRButton.setChecked(true);
                    } else if (muteOrHangup.isHangUp()) {
                        hangUpRButton.setChecked(true);
                    }
                }
                // If MuteOrHangUp object is not created. Should not occur
                else {
                    muteOrHangup = new MuteOrHangUp();
                    clientDDBB.getInserts().insertMuteOrHangUp(muteOrHangup);
                    clientDDBB.commit();
                }

            } else {
                if (ConfigAppValues.getMuteOrHangup()) {
                    hangUpRButton.setChecked(true);
                } else {
                    muteRButton.setChecked(true);
                }
            }
            clientDDBB.close();

        } catch (Exception e) {
            Log.e(CLASS_NAME, ExceptionUtils.getString(e));
        }
    }

    /**
     * Function that depends the serviceToggleButton state try to start the
     * QuiteSleep incoming call service or stop it.
     */
    private void startStopServiceProcess() {

        try {

            boolean result = StartStopServicesOperations
                    .startStopQuiteSleepService(serviceToggleButton.isChecked());

            if (serviceToggleButton.isChecked()) {

                /*
                 * Deactivate the notification toast because now use the status
                 * bar notification
                 */
                /*
                 * if (result)
                 * 
                 * //All right, start the service was ok! Toast.makeText( this,
                 * this.getString( R.string.settings_toast_start_service),
                 * Toast.LENGTH_SHORT).show();
                 * 
                 * else //An error has ocurred!! Toast.makeText( this,
                 * this.getString( R.string.settings_toast_fail_service),
                 * Toast.LENGTH_SHORT).show();
                 */
            } else {
                if (result) {
                    // All right, stop the service was ok!
                    es.cesar.quitesleep.utils.Toast.r(QuiteSleepApp.getContext(),
                            this.getString(R.string.settings_toast_stop_service), Toast.LENGTH_SHORT);
                } else {
                    // An error has occurred!!
                    es.cesar.quitesleep.utils.Toast.r(QuiteSleepApp.getContext(),
                            this.getString(R.string.settings_toast_fail_service), Toast.LENGTH_SHORT);
                }
            }
        } catch (Exception e) {
            Log.e(CLASS_NAME, ExceptionUtils.getString(e));
        }
    }

    @Override
    public void clickYes() {
        // TODO Auto-generated method stub

    }
}
