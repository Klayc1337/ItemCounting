package org.example.itemcounting.rest.dto;

import java.util.List;

public record ResponseValue(String status, List<String> errors){
}
