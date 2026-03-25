const fs = require('fs');

// 结构体名转换为下划线小写形式
const structs = ['config', 'dict', 'owner', 'tag', 'task'];

// 逐个创建对应的两个文件
structs.forEach(name => {
    // 创建 .rs 文件
    fs.writeFileSync(`${name}.rs`, '');
    console.log(`创建文件: ${name}.rs`);
    
    // 创建 .html 文件
    fs.writeFileSync(`${name}.html`, '');
    console.log(`创建文件: ${name}.html`);
});

console.log('所有文件创建完成！');