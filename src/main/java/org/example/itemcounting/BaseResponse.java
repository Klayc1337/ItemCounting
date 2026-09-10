package org.example.itemcounting;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BaseResponse<T> {
    private String status;
    private T data;
    private List<String> errors;
}
