/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.localizacao;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * AsyncTask for reverse geocoding coordinates into a physical address.
 */
class FetchAddressTask extends AsyncTask<Location, Void, String[]> {

    private Context mContext;
    private OnTaskCompleted mListener;


    FetchAddressTask(Context applicationContext, OnTaskCompleted listener) {
        mContext = applicationContext;
        mListener = listener;
    }

    private final String TAG = FetchAddressTask.class.getSimpleName();

    @Override
    protected String[] doInBackground(Location... params) {
        // Set up the geocoder
        Geocoder geocoder = new Geocoder(mContext,
                Locale.getDefault());

        // Obtem o valor informado pelo dispositivo referente a geolocalização
        Location location = params[0];
        List<Address> addresses = null;
        String[] resultMessage = new String[3];
        resultMessage[0]="";
        resultMessage[1]="";
        resultMessage[2]="";

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address
                    1);
            resultMessage[1]=String.valueOf(location.getLatitude());
            resultMessage[2]=String.valueOf( location.getLongitude());
        } catch (IOException ioException) {
            // Catch network or other I/O problems
            resultMessage[0] = mContext.getString(R.string.service_not_available);
            Log.e(TAG, resultMessage[0], ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values
            resultMessage[0] = mContext.getString(R.string.invalid_lat_long_used);
            Log.e(TAG, resultMessage[0] + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // If no addresses found, print an error message.
        if (addresses == null || addresses.size() == 0) {
            if (resultMessage[0].isEmpty()) {
                resultMessage[0] = mContext.getString(R.string.no_address_found);
                Log.e(TAG, resultMessage[0]);
            }
        } else {
            // If an address is found, read it into resultMessage
            Address address = addresses.get(0);
            ArrayList<String> addressParts = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressParts.add(address.getAddressLine(i));
            }

            resultMessage[0] = TextUtils.join(
                    "\n",
                    addressParts);

        }

        return resultMessage;
    }

    /**
     * Called once the background thread is finished and updates the
     * UI with the result.
     * @param address The resulting reverse geocoded address, or error
     *                message if the task failed.
     */
    @Override
    protected void onPostExecute(String[] address) {
        mListener.onTaskCompleted(address);
        super.onPostExecute(address);
    }

    interface OnTaskCompleted {
        void onTaskCompleted(String[] result);
    }
}
