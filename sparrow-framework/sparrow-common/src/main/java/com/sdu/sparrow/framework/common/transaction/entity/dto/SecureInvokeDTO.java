package com.sdu.sparrow.framework.common.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecureInvokeDTO {

    private String className;

    private String methodName;

    private String parameterTypes;

    private Object[] args;
}

