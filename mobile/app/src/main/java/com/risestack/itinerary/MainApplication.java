package com.risestack.itinerary;

// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import android.app.Application;

import okhttp3.MediaType;

public class MainApplication extends Application {

    public static final String LOG_TAG = "Itinerary";
    public static final String DEBUG_URL = "http://10.0.2.2:8080/";
    public static final String PROD_URL = "https://final-project-186205.appspot.com/";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //current endpoint
    public static final String URL = DEBUG_URL;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
