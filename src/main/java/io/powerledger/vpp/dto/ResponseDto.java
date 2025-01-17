package io.powerledger.vpp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseDto {
    private String status;
    private String message;
    private Object data;
    private String errorCode;
    private LocalDateTime timestamp;

    public ResponseDto(String status, String message, Object data, String errorCode) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public static ResponseDto success(String message, Object data) {
        return new ResponseDto("SUCCESS", message, data, null);
    }

    public static ResponseDto error(String message, String errorCode) {
        return new ResponseDto("ERROR", message, null, errorCode);
    }
}
