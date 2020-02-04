/**
 * Copyright (C) 2020, ControlThings Oy Ab
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * @license Apache-2.0
 */
package mist.api.ui;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import wish.Peer;

/**
 * Created by jan on 11/1/16.
 */

class MistCustomApi {

    private MistCustomApi() {
    }


    private static class MistCustomApiHolder {
        private static final MistCustomApi INSTANCE = new MistCustomApi();
    }

    public static MistCustomApi getInstance() {
        return MistCustomApiHolder.INSTANCE;
    }

    //synchronized native void addCustomUi(InputStream stream, byte[] configBson, CustomUi.AddUiResponse response);
    synchronized native boolean removeCustomUi(String md5);

    //synchronized native String[] listCustomUis();
    //synchronized native InputStream getRawCustomUi(String md5);
    synchronized native String[] getCustomUi(Peer peer);
    //synchronized native int loadCustomUi(String md5, List<Peer> filter);


    File SDCardRoot = Environment.getExternalStorageDirectory();
    File file = new File(SDCardRoot, "file.tmp");

    List<String> list = new ArrayList<String>();

    public void addCustomUi(InputStream stream, byte[] configBson, CustomUi.AddUiResponse response) {
        Log.d("log", "addcustomui: ");
        try {
            FileOutputStream fileOutput = new FileOutputStream(file);
            IOUtils.copy(stream, fileOutput);
            fileOutput.close();
        } catch (Exception e) {
            Log.d("log", "addcustomui: " + e);
        }

        response.added();
    }

    public InputStream getRawCustomUi(String md5) {

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return fileInputStream;
        } catch (Exception e) {
            return null;
        }
    }

    ;


    public int loadCustomUi(String md5, List<Peer> filter) {
        return 21;
    }

    // String md5 for testing;
    public String[] listCustomUis(String md5) {
        list.add(md5);
        String[] strings = new String[list.size()];
        return list.toArray(strings);
    }

}
