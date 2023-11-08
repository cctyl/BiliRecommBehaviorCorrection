package io.github.cctyl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerException extends RuntimeException {

    private Integer code;

    private String message;

}
