

# Claude Code Skill 入门教程

本教程将教你如何创建一个 Skill，使 Claude 能够使用视觉图表和类比来解释代码。你可以通过 `/explain-code` 直接调用它，或者让 Claude 根据上下文自动触发。

## 1. 创建 Skill 目录
Skill 需要存放在特定的文件夹中。

```bash
mkdir -p .claude/skills/explain-code
```

## 2. 编写 `SKILL.md`
每个 Skill 都必须包含一个 `SKILL.md` 文件。它由两部分组成：
* **YAML Frontmatter**：位于 `---` 标记之间，定义 Skill 的元数据。
* **Markdown 内容**：Claude 调用该 Skill 时遵循的具体指令。

创建文件 `.claude/skills/explain-code/SKILL.md`：

```markdown
---
name: explain-code
description: Explains code with visual diagrams and analogies. Use when explaining how code works, teaching about a codebase, or when the user asks "how does this work?"
---

在解释代码时，始终包含以下内容：

1. **先用类比开始**：将代码比作日常生活中的事物。
2. **绘制图表**：使用 ASCII 艺术展示流程、结构或关系。
3. **分步讲解**：逐步解释代码执行过程。
4. **突出注意事项 (Gotcha)**：指出常见的错误或误区。

保持解释的对话感。对于复杂的概念，可以使用多个类比。
```

## 3. 测试 Skill
你可以通过两种方式测试：
1.  **自动调用**：询问符合描述的内容，例如：`How does this code work?`
2.  **手动调用**：使用斜杠命令，例如：`/explain-code src/auth/login.ts`

---

## Skill 的存储位置与优先级

Skill 的存放位置决定了其可见范围。如果不同位置存在同名 Skill，优先级顺序为：**企业 > 个人 > 项目**。

| 位置 | 路径示例 | 适用范围 |
| :--- | :--- | :--- |
| **企业** | 参阅托管设置 | 组织内的所有用户 |
| **个人** | `~/.claude/skills/<name>/SKILL.md` | 你的所有项目 |
| **项目** | `.claude/skills/<name>/SKILL.md` | 仅当前项目 |
| **插件** | `<plugin>/skills/<name>/SKILL.md` | 启用插件的位置 |

---

## 目录结构与自动发现

Claude Code 会自动从嵌套的 `.claude/skills/` 目录中发现 Skill（支持 Monorepo）。一个完整的 Skill 目录结构如下：

```text
my-skill/
├── SKILL.md           # 主要说明（必需）
├── template.md        # Claude 要填写的模板（可选）
├── examples/
│   └── sample.md      # 预期格式的示例输出（可选）
└── scripts/
    └── validate.sh    # Claude 可以执行的校验脚本（可选）
```

> **提示**：建议使用 Skills 而非旧版的 `/commands/`，因为 Skills 支持支持文件和更丰富的功能。

---

## Frontmatter 配置详解

你可以通过 `SKILL.md` 顶部的 YAML 字段精确控制 Claude 的行为。

### 核心字段参考

| 字段 | 必填 | 描述 |
| :--- | :--- | :--- |
| `name` | 否 | Skill 的命令名称。默认为目录名。 |
| `description` | **推荐** | 功能描述。Claude 靠它决定何时自动应用该 Skill。 |
| `disable-model-invocation` | 否 | 设为 `true` 则禁止 Claude 自动加载。仅允许手动输入 `/name` 触发。 |
| `user-invocable` | 否 | 设为 `false` 则在 `/` 菜单中隐藏，仅供 Claude 背景调用。 |
| `allowed-tools` | 否 | 该 Skill 激活时，Claude 无需申请权限即可使用的工具（如 `Read`, `Grep`）。 |
| `context` | 否 | 设置为 `fork` 以在独立的子代理（subagent）中运行。 |

---

## 变量替换与参数传递

Skill 支持在内容中使用动态占位符：

| 变量 | 描述 |
| :--- | :--- |
| `$ARGUMENTS` | 调用时传递的所有参数字符串。 |
| `$0`, `$1`, ... | 访问特定位置的参数（0 为第一个）。 |
| `${CLAUDE_SESSION_ID}` | 当前会话 ID，适用于日志记录。 |
| `${CLAUDE_SKILL_DIR}` | 当前 Skill 所在的绝对路径。 |

### 示例：使用位置参数
```markdown
---
name: migrate
---
将组件 $0 从框架 $1 迁移到 $2。
```
**执行命令**：`/migrate SearchBar React Vue`
**结果**：Claude 会理解为“将组件 SearchBar 从框架 React 迁移到 Vue”。

---

## 进阶控制：调用权限表

| Frontmatter 设置 | 用户可手动调用 | Claude 可自动调用 | 备注 |
| :--- | :--- | :--- | :--- |
| (默认) | 是 | 是 | 描述信息始终加载到上下文。 |
| `disable-model-invocation: true` | **是** | 否 | 适用于有副作用的操作（如部署）。 |
| `user-invocable: false` | 否 | **是** | 适用于背景知识，用户无需手动操作。 |

---

## 最佳实践建议

1.  **保持简洁**：将 `SKILL.md` 保持在 500 行以下。
2.  **使用支持文件**：将大型 API 规范或长篇示例移至同目录下的 `reference.md` 或 `examples.md`，并在 `SKILL.md` 中引用它们。
3.  **安全限制**：对于只需要读取代码的 Skill，设置 `allowed-tools: Read Grep Glob` 以防止误改代码。