package io.github.cgew85.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by cgew85 on 13.04.2016.
 */
public class Info {

    @Getter @Setter private String processId;
    @Getter @Setter private String errorStatus;
    @Getter @Setter private String result;
    @Getter @Setter private String message;
    @Getter @Setter private String finished;

    public Info(String processId, String errorStatus, String result, String message, String finished) {
        this.processId = processId;
        this.errorStatus = errorStatus;
        this.result = result;
        this.message = message;
        this.finished = finished;
    }
}
