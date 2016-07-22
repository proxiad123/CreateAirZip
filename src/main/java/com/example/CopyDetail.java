package com.example;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by C5232064 on 7/1/2016.
 */
public class CopyDetail {
    private String target;
    private String responsible;
    private boolean backCopy;
    private String type;
    private String category;
    private String source;
    private String destination;

    private List<String> permittedFileTypes;

    public CopyDetail(String csvLine) {
        String[] rowDetail = csvLine.split(",");

        this.target = rowDetail[0];
        this.responsible = rowDetail[1];
        this.backCopy = Boolean.parseBoolean(rowDetail[2]);
        this.type = rowDetail[3];
        this.category = rowDetail[4];
        this.source = rowDetail[5];
        if(rowDetail.length == 6) {
            this.destination = null; // if destination is missing => set it manually to null
        }
        else{
            this.destination = rowDetail[6];
        }

        if(!StringUtils.isEmpty(rowDetail[4])){
            String[] permitedExtensions = rowDetail[4].split(" ");
            permittedFileTypes = new ArrayList<String>();
            for(int i = 0; i < permitedExtensions.length; i++){
                permittedFileTypes.add(permitedExtensions[i].substring(permitedExtensions[i].lastIndexOf(".") + 1));
            }
        }
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isBackCopy() {
        return backCopy;
    }

    public void setBackCopy(boolean backCopy) {
        this.backCopy = backCopy;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<String> getPermittedFileTypes() {
        return permittedFileTypes;
    }

    public void setPermittedFileTypes(List<String> permittedFileTypes) {
        this.permittedFileTypes = permittedFileTypes;
    }

}
