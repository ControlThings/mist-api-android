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

import org.json.JSONObject;

import java.io.File;

/**
 * Created by jeppe on 11/21/16.
 */

public class Ui {

    private File logo = null;
    private String md5 = null;
    private String name = null;
    private JSONObject info = null;

    public File getLogo() {
        return logo;
    }

    void setLogo(File logo) {
        this.logo = logo;
    }

    public String getMd5() {
        return md5;
    }

    void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public JSONObject getInfo() {
        return info;
    }

    void setInfo(JSONObject info) {
        this.info = info;
    }
}
