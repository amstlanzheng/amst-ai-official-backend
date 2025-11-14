package com.amst.api.common.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author lanzhs
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}
