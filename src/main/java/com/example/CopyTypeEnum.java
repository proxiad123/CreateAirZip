package com.example;

/**
 * Created by C5246734 on 7/4/2016.
 */
public enum CopyTypeEnum {

    DIR_TO_DIR("DirToDir"),
    FILE_TO_DIR("FileToDir"),
	FILE_TO_FILE("FileToFile");

    private String type;

    CopyTypeEnum(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }
}
