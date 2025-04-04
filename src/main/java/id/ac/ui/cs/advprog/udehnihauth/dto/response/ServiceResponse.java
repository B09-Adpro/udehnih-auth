package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ServiceResponse<T> success(T data) {
        return ServiceResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ServiceResponse<T> success(T data, String message) {
        return ServiceResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ServiceResponse<T> error(String message) {
        return ServiceResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}