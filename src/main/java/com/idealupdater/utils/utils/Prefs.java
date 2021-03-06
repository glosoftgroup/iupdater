package com.idealupdater.utils.utils;

import java.util.prefs.Preferences;

public class Prefs {
    public Preferences prefs;
    private static Prefs instance = null;

    public static Prefs getInstance(){
        return instance == null ? new Prefs() : instance;
    }

    public Prefs() {
        this.prefs = Preferences.userNodeForPackage(Prefs.class);
        instance = this;
    }

    public String getSideBarTargetBtn() {
        return  prefs.get("TargetBtn", "status");
    }

    public void setSideBarTargetBtn(String target) {
        prefs.put("TargetBtn", target);
    }

    public String getLocalServerPath() {
        String path =
                System.getenv("ProgramFiles(X86)") + "\\ClassicPOS Server\\ClassicPOS Server";
        return  prefs.get("LocalServerPath", path);
    }

    public void setLocalServerPath(String target) {
        prefs.put("LocalServerPath", target);
    }


    public String getRemoteServerPath() {
        String path = "https://raw.githubusercontent.com/glosoftgroup/classic-pck/master/backend_updater_config.json";
        return  prefs.get("RemoteServerPath", path);
    }

    public void setRemoteServerPath(String target) {
        prefs.put("RemoteServerPath", target);
    }

    public String getLocalClientPath() {
        String path =
                System.getenv("ProgramFiles(X86)") + "\\ClassicPOS Client\\ClassicPOS Client";
        return  prefs.get("LocalClientPath", path);
    }

    public void setLocalClientPath(String target) {
        prefs.put("LocalClientPath", target);
    }

    public String getRemoteClientPath() {
        String path = "https://raw.githubusercontent.com/glosoftgroup/updaterconfig/" +
                "master/frontend_updater_config.json";
        return  prefs.get("RemoteClientPath", path);
    }

    public void setRemoteClientPath(String target) {
        prefs.put("RemoteClientPath", target);
    }

    public String getLocalServerFile() {
        String path = getLocalServerPath() + "/backend_updater_config.json";
        return  prefs.get("LocalServerFile", path);
    }

    public String getLocalClientFile() {
        String path = getLocalClientPath() + "/frontend_updater_config.json";
        return  prefs.get("LocalClientFile", path);
    }

    public Integer getTimeOut(){
        return prefs.getInt("timeout", 1800000);
    }

    public void setTimeOut(int timeInSecends){
        prefs.putInt("timeout", timeInSecends);
    }
}



