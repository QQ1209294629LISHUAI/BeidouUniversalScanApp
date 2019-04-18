package com.beidouspatial.universalscanapp.entity;

public class OneParcelModel {
    private String xqdm;
    private String djq;
    private String djzq;
    private String zddm;
    private String tdqlr;

    public OneParcelModel(String xqdm, String djq, String djzq, String zddm, String tdqlr) {
        this.xqdm = xqdm;
        this.djq = djq;
        this.djzq = djzq;
        this.zddm = zddm;
        this.tdqlr = tdqlr;
    }

    public String getXqdm() {
        return xqdm;
    }

    public void setXqdm(String xqdm) {
        this.xqdm = xqdm;
    }

    public String getDjq() {
        return djq;
    }

    public void setDjq(String djq) {
        this.djq = djq;
    }

    public String getDjzq() {
        return djzq;
    }

    public void setDjzq(String djzq) {
        this.djzq = djzq;
    }

    public String getZddm() {
        return zddm;
    }

    public void setZddm(String zddm) {
        this.zddm = zddm;
    }

    public String getTdqlr() {
        return tdqlr;
    }

    public void setTdqlr(String tdqlr) {
        this.tdqlr = tdqlr;
    }
}
