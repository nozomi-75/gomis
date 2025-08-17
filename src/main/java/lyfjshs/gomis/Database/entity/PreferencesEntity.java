/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;

public class PreferencesEntity {

    private int id;
    private int userId;
    private String category;
    private String PREFERENCES_KEY;
    private String value;
    private String dataType;
    private byte[] file;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;  

    public PreferencesEntity(int id, int userId, String category, String PREFERENCES_KEY, String value, String dataType, byte[] file, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.PREFERENCES_KEY= PREFERENCES_KEY;
        this.value = value;
        this.dataType = dataType;
        this.file = file;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getKey() {
        return PREFERENCES_KEY;
    }
    public void setKey(String PREFERENCES_KEY) {
        this.PREFERENCES_KEY= PREFERENCES_KEY;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    public byte[] getFile() {
        return file;
    }
    public void setFile(byte[] file) {
        this.file = file;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }   
}