from openai import OpenAI

# 1. 填入你的 API Key 和 DeepSeek 接口
client = OpenAI(
    api_key="sk-我的apikey",
    base_url="https://api.deepseek.com"
)

# 2. 读取昨天切好的文本块
with open('docs/guida.txt', 'r', encoding='utf-8') as f:
    text = f.read()
chunk_size = 500
chunks = [text[i:i+chunk_size] for i in range(0, len(text), chunk_size)]

# 3. 用户提问
question = "贵大软件工程的就业方向有哪些？"
print(f"用户提问：{question}\n")

# 4. 最简单的“检索”逻辑：找出包含提问中关键字最多的文本块
# （这里为了演示，我们简单粗暴地按字匹配，真实企业里会用 Embedding 向量搜索）
best_chunk = ""
max_matches = 0
for chunk in chunks:
    # 统计问题中的字在块中出现了几个
    matches = sum(1 for char in question if char in chunk) 
    if matches > max_matches:
        max_matches = matches
        best_chunk = chunk

print(f"--- 系统找到了最相关的资料块 ---")
print(best_chunk[:100] + "...\n") # 只打印前100个字看看

# 5. 组装 Prompt（提示词），把找到的资料塞给大模型
prompt = f"""
你是一个专业的问答助手。请严格根据下面提供的【参考资料】回答用户的问题。
如果资料中没有提到，请直接说“资料中未提及”，绝对不要自己编造。

【参考资料】：
{best_chunk}

【用户问题】：
{question}
"""

# 6. 发送请求给 API
print("正在呼叫大模型生成答案...")
response = client.chat.completions.create(
    model="deepseek-chat",
    messages=[
        {"role": "user", "content": prompt}
    ]
)

print("\n--- AI 最终回答 ---")
print(response.choices[0].message.content)