### 服务提供方如何通过反射调用具体实现的方法

```
private Object doInvoke(RequestPayload requestPayload) {
    String targetInterfaceName = requestPayload.getInterfaceName();
    String methodName = requestPayload.getMethodName();
    Class<?>[] parametersType = requestPayload.getParametersType();
    Object[] parametersValue = requestPayload.getParametersValue();

    // 寻找服务的具体实现
    ServiceConfig<?> serviceConfig = IRpcBootstrap.SERVICE_MAP.get(targetInterfaceName);
    Object referenceImpl = serviceConfig.getReference();
    // 获取方法对象 通过反射调用invoke方法
    Object returnValue;
    try {
        Class<?> clazz = referenceImpl.getClass();
        Method method = clazz.getMethod(methodName, parametersType);
        returnValue = method.invoke(referenceImpl, parametersValue);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        log.error("调用服务【{}】的方法【{}】时发生了异常。", targetInterfaceName, methodName, e);
        throw new RuntimeException(e);
    }
    return returnValue;
}
```