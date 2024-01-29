package com.sdu.zrpc.framework.common.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sdu.zrpc.framework.common.transaction.entity.po.SecureInvokeRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecureInvokeRecordMapper extends BaseMapper<SecureInvokeRecord> {

}
