/* 
 	Copyright 2010-2012 Cesar Valiente Gordo
 
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

package es.cesar.quitesleep.tasks;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import es.cesar.quitesleep.data.controllers.ClientDDBB;
import es.cesar.quitesleep.data.models.CallLog;
import es.cesar.quitesleep.ui.fragments.base.BaseListFragment;
import es.cesar.quitesleep.utils.Log;

/**
 * 
 * @author Cesar Valiente Gordo (cesar.valiente@gmail.com)
 * 
 *         This class is used to load all {@link CallLog} objects
 */
public class LoadLogsTask extends AsyncTask<Void, Void, List<String>> {

    private BaseListFragment mListener;

    /**
     * Constructor
     * 
     * @param handler
     */
    public LoadLogsTask(final BaseListFragment listener) {
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mListener.showProgressLoader();
    }

    @Override
    protected List<String> doInBackground(final Void... params) {

        List<String> callLogListString = null;

        Log.d("cesar", "loading calllogs");
        ClientDDBB clientDDBB = new ClientDDBB();
        if (!clientDDBB.isEmpty()) {

            List<CallLog> callLogList = clientDDBB.getSelects().selectAllCallLog();
            callLogListString = convertCallLogList(callLogList);

            clientDDBB.close();
        }
        return callLogListString;
    }

    @Override
    protected void onPostExecute(final List<String> result) {
        mListener.getDataInfo(result);
    }

    /**
     * 
     * @param callLogList
     * @return The callLogList
     * @see List<String>
     */
    private List<String> convertCallLogList(final List<CallLog> callLogList) {

        if (callLogList != null && callLogList.size() > 0) {

            List<String> callLogListString = new ArrayList<String>();

            for (int i = 0; i < callLogList.size(); i++) {
                String callLogString = callLogList.get(i).toString();
                if (callLogString != null) {
                    callLogListString.add(callLogString);
                }
            }
            return callLogListString;
        }
        return null;
    }

}
