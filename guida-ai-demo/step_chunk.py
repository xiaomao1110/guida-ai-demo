# 1. 读取文件
with open('docs/guida.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# 2. 简单的文本切分（每500个字符切一块）
chunk_size = 500
chunks = [text[i:i+chunk_size] for i in range(0, len(text), chunk_size)]

# 3. 打印结果看看
print(f"一共切分了 {len(chunks)} 个块。")
for i, chunk in enumerate(chunks[:3]):  # 只看前3块
    print(f"\n--- 第 {i+1} 块 ---")
    print(chunk)