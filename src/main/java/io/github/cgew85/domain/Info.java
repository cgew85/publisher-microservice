package io.github.cgew85.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by cgew85 on 13.04.2016.
 */
@NoArgsConstructor
@AllArgsConstructor
public class Info implements Serializable{
    @Getter @Setter private String processId;
    @Getter @Setter private String errorStatus;
    @Getter @Setter private String result;
    @Getter @Setter private String message;
    @Getter @Setter private String finished;
}
