# MessageCenter 消息中心

1. 主要功能是监听和读取手机上的短信, 实现解析器接口后, 将短信解析为结构化数据, 存储于数据库中.
   1. 解析器接口`com.me.app.messagecenter.service.MessageParse`
   2. 目前主要实现的解析器为`com.me.app.messagecenter.service.impl.PayInfoParseFromBcSms`, 解析交通银行短信
2. 之后根据数据库中的结构化数据, 方便的展示结果给用户查看
   1. 目前主要实现的结构化数据为消费信息`com.me.app.messagecenter.dto.PayInfo`
3. 可根据结构化数据分析日常使用习惯, 如目前可分析自己的消费习惯, 让自己更加理性的消费
