package com.example.springboot.dto;

public class E2EEKeyDto {
    private String publicKey;
    private Boolean e2eeEnabled;

    public E2EEKeyDto() {}

    public E2EEKeyDto(String publicKey, Boolean e2eeEnabled) {
        this.publicKey = publicKey;
        this.e2eeEnabled = e2eeEnabled;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Boolean getE2eeEnabled() {
        return e2eeEnabled;
    }

    public void setE2eeEnabled(Boolean e2eeEnabled) {
        this.e2eeEnabled = e2eeEnabled;
    }
}
