package com.example.stayfit.dtos;

public class ResponseDto {
    private String message;
    private Object content;
    private Boolean isSuccessful;

    public ResponseDto(String message, Object content, Boolean isSuccessful) {
        this.message = message;
        this.content = content;
        this.isSuccessful = isSuccessful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Boolean getSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(Boolean successful) {
        isSuccessful = successful;
    }
}
