/*
 * 语法全覆盖测试DSL
 */
function logicTest() {
    // 变量声明与赋值
    var a = 1;
    var b = 2;
    var c = a + b;
    var s = "hello";
    var t = s + " world";
    var arr = [1, 2, 3];
    var obj = {x: 10, y: 20};
    var flag = true;
    var nothing = null;

    // if/else
    var result = 0;
    if (a < b) {
        result = 100;
    } else {
        result = 200;
    }

    // for
    var sum = 0;
    for (i in arr) {
        sum = sum + current;
    }

    // 逻辑表达式
    var logic = (a < b) && flag || false;

    // 比较表达式
    var eq = (a + b) == c;
    var neq = s != t;

    // 对象和数组访问
    var ox = obj.x;
    var ay = arr[1];

    // 返回所有结果
    return {
        result: result,
        sum: sum,
        logic: logic,
        eq: eq,
        neq: neq,
        ox: ox,
        ay: ay,
        nothing: nothing
    };
} 