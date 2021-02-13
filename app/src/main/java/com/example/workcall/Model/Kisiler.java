package com.example.workcall.Model;

public class Kisiler {

    String isim,durum,resim;

    public Kisiler(String isim, String durum, String resim) {
        this.isim = isim;
        this.durum = durum;
        this.resim = resim;
    }

    public Kisiler() {

    }

    public String getIsim() {
        return isim;
    }

    public void setIsim(String isim) {
        this.isim = isim;
    }

    public String getDurum() {
        return durum;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }

    public String getResim() {
        return resim;
    }

    public void setResim(String resim) {
        this.resim = resim;
    }
}
