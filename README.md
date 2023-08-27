## Rpc协议定义

### Header

- ```Magic Number 4B```：魔数，用于识别该协议 例如：0xCAFEBABE
- ```Version 1B```：协议版本号
- ```MessageType 1B```：消息类型，例如：0x01表示请求，0x02表示响应
- ```Serialization Type 1B```：序列化方法，例如0x01表示JSON，0x02表示Protobuf
- ```Compress Type 1B```：压缩方法
- ```RequestId 8B```：请求Id，用于标识请求和响应的匹配。
- ```Body Length 4B```：Body部分的长度
- ```Header Lenght 4B```：Header部分的长度

### Body
Body的结构取决于具体的请求或响应的数据

#### 对于请求，Body可以应以下字段

- Service Name：被调用服务的名称
- Method Name：被调用方法的名称
- Method Arguments：被调用方法的参数列表
- Method Argument Types：被调用方法参数的类型列表

#### 对于响应，Body可以应以下字段

- Status Code：响应状态码，例如0x00表示成功，0x01表示失败
- Error Message：错误信息
- Return Value：方法返回值