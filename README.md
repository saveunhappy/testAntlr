# Business DSL Engine

基于ANTLR4的业务DSL执行引擎，实现了业务方和平台方的分离，让业务方专注于业务逻辑，平台方专注于基础设施。

## 项目特性

### 🚀 核心功能
- **DSL脚本热加载**: 修改脚本文件后自动重新加载，无需重启应用
- **语法高亮编辑器**: 基于CodeMirror的在线编辑器，支持语法高亮
- **实时调试**: 在线编辑、验证、执行DSL脚本
- **内置函数库**: 提供数学、字符串、业务、日志等内置函数
- **RESTful API**: 完整的HTTP API接口，支持脚本管理

### 🏗️ 架构设计
- **平台方**: 提供DSL引擎、热加载机制、编辑器界面、API接口
- **业务方**: 编写DSL脚本，专注于业务逻辑，无需关心平台实现

## 快速开始

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 访问编辑器
打开浏览器访问: http://localhost:8080

### 3. 编写业务脚本
在编辑器中创建新的DSL脚本，例如：

```javascript
// 订单折扣计算
var amount = 1000;
var customerLevel = "VIP";

var discountRate = 0.0;
if (customerLevel == "VIP") {
    discountRate = 0.15;
} else if (customerLevel == "GOLD") {
    discountRate = 0.10;
}

var discountAmount = calculateDiscount(amount, discountRate);
log("订单金额: " + amount);
log("折扣金额: " + discountAmount);

return amount - discountAmount;
```

### 4. 执行脚本
点击"运行"按钮，在"执行上下文"中输入JSON格式的参数：
```json
{
  "amount": 2000,
  "customerLevel": "GOLD"
}
```

## DSL语法

### 变量声明
```javascript
var name = value;
var amount = 1000;
var customerLevel = "VIP";
```

### 条件判断
```javascript
if (condition) {
    // 执行代码
} else {
    // 执行代码
}
```

### 循环
```javascript
while (condition) {
    // 执行代码
}

for (var i = 0; i < 10; i = i + 1) {
    // 执行代码
}
```

### 函数调用
```javascript
var result = functionName(param1, param2);
log("计算结果: " + result);
```

## 内置函数

### 数学函数
- `abs(x)`: 绝对值
- `max(x, y)`: 最大值
- `min(x, y)`: 最小值
- `round(x)`: 四舍五入

### 字符串函数
- `length(str)`: 字符串长度
- `substring(str, start, end)`: 子字符串
- `toUpperCase(str)`: 转大写
- `toLowerCase(str)`: 转小写

### 业务函数
- `calculateDiscount(amount, rate)`: 计算折扣
- `validateOrder(amount)`: 验证订单
- `getCustomerLevel(totalSpent)`: 获取客户等级

### 日志函数
- `log(message)`: 信息日志
- `logError(message)`: 错误日志

## API接口

### 脚本管理
- `GET /api/dsl/scripts`: 获取脚本列表
- `GET /api/dsl/scripts/{name}`: 获取脚本内容
- `POST /api/dsl/scripts/{name}`: 保存脚本
- `DELETE /api/dsl/scripts/{name}`: 删除脚本

### 脚本执行
- `POST /api/dsl/scripts/{name}/execute`: 执行脚本
- `POST /api/dsl/scripts/validate`: 验证脚本语法

### 系统信息
- `GET /api/dsl/info`: 获取系统信息

## 项目结构

```
src/
├── main/
│   ├── antlr4/
│   │   └── com/example/dsl/
│   │       └── BusinessDSL.g4          # ANTLR4语法文件
│   ├── java/com/example/dsl/
│   │   ├── controller/
│   │   │   ├── DSLController.java      # REST API控制器
│   │   │   └── PageController.java     # 页面控制器
│   │   ├── engine/
│   │   │   ├── DSLExecutionEngine.java # DSL执行引擎
│   │   │   └── DSLExecutionException.java
│   │   ├── service/
│   │   │   └── DSLScriptService.java   # 脚本管理服务
│   │   └── visitor/
│   │       └── DSLVisitor.java         # AST访问者
│   └── resources/
│       └── templates/
│           └── dsl-editor.html         # DSL编辑器页面
scripts/                                 # DSL脚本目录
├── order-discount.dsl                   # 订单折扣脚本
└── customer-analysis.dsl                # 客户分析脚本
```

## 技术栈

- **Spring Boot 2.7.18**: 应用框架
- **ANTLR4 4.13.1**: 语法解析器生成器
- **Thymeleaf**: 模板引擎
- **Bootstrap 5**: UI框架
- **CodeMirror**: 代码编辑器
- **H2 Database**: 内存数据库

## 开发指南

### 添加新的内置函数
在 `DSLExecutionEngine.java` 的 `registerBuiltinFunctions()` 方法中添加：

```java
visitor.registerFunction("newFunction", args -> {
    // 函数实现
    return result;
});
```

### 扩展DSL语法
修改 `BusinessDSL.g4` 文件，然后重新编译：

```bash
mvn clean compile
```

### 自定义业务逻辑
业务方只需要：
1. 在编辑器中编写DSL脚本
2. 保存脚本文件
3. 通过API调用执行

平台方负责：
1. 维护DSL引擎
2. 提供基础设施
3. 扩展内置函数

## 优势

### 对业务方
- **专注业务**: 只需编写业务逻辑，无需关心技术实现
- **快速上线**: DSL脚本可以独立部署，无需等待平台发布
- **安全可靠**: DSL层提供保护，不会影响平台稳定性
- **自助服务**: 通过编辑器自主编写和调试脚本

### 对平台方
- **代码分离**: 业务代码不再混入平台代码
- **质量保证**: 无需担心业务代码质量问题
- **架构清晰**: 平台专注于基础设施和性能优化
- **维护简单**: 减少业务需求对平台的影响

## 许可证

MIT License 