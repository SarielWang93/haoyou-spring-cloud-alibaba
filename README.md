
# 项目构成

## 项目位置

    haoyou-spring-cloud-alibaba
    
## 存储

### 数据库

    使用docker-compose在虚拟机中部署mysql8
    
### 内存数据库
    
    使用docker-compose在虚拟机中部署redis集群

    
## 服务器代码技术（java）

### 使用工具

    IDEA
    
### 项目管理
    
    maven 3.6.0
    
### 架构

#### 使用spring cloud alibaba构建微服务框架。

    统一的依赖管理 haoyou-spring-cloud-alibaba-dependencies
    
    通用的工具类库 haoyou-spring-cloud-alibaba-commons
    
    通用的领域模型 haoyou-spring-cloud-alibaba-commons-domain
    
    通用的数据访问 POMhaoyou-spring-cloud-alibaba-commons-mapper
    
    通用的代码生成 haoyou-spring-cloud-alibaba-database
    
    创建通用的业务逻辑 haoyou-spring-cloud-alibaba-commons-service
    
#### 三个主要业务服务
    
    主业务逻辑服务 haoyou-spring-cloud-alibaba-manager
    
    与游戏前端通信服务 haoyou-spring-cloud-alibaba-sofabolt
    
    养成系统服务 haoyou-spring-cloud-alibaba-cultivate
    
    战斗系统服务 haoyou-spring-cloud-alibaba-fighting
    
    登录系统服务 haoyou-spring-cloud-alibaba-login
    
    匹配系统服务 haoyou-spring-cloud-alibaba-match
    
### 主要技术

#### SOFABoltT通信
    
    使用阿里巴巴的SOFABolt实现TCP长连接通信，此框架底层实现了Netty。
    
    通信协议使用SOFABolt默认的RpcProtocol协议。
    
#### 内部通信
    
    使用spring cloud 的Feign+Sentinel 通信
    
#### Redis操作

    使用spring cloud 的RedisTemplate 操作redis
    
    
#### 数据库连接

    使用号称性能最好的HikariCP连接池技术
    
    mysql操作使用tk.mybatis
