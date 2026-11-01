import streamlit as st
from openai import OpenAI

# ================= 1. 网页基础设置 =================
st.set_page_config(page_title="贵大 AI 小助手", page_icon="🤖")
st.title("🤖 贵大 AI 小助手")
st.caption("基于 DeepSeek + 本地知识库构建")

# ================= 2. 侧边栏：输入 API Key =================
with st.sidebar:
    st.header("⚙️ 设置")
    # type="password" 会让输入的内容变成小黑点，防止别人偷看
    api_key = st.text_input("请输入你的 DeepSeek API Key", type="password")

# ================= 3. 读取本地资料并切分 =================
# @st.cache_data 是魔法，保证只读取一次文件，不会每次聊天都去读硬盘
@st.cache_data
def load_data():
    try:
        with open('docs/guida.txt', 'r', encoding='utf-8') as f:
            text = f.read()
        chunk_size = 500
        return [text[i:i+chunk_size] for i in range(0, len(text), chunk_size)]
    except FileNotFoundError:
        st.error("❌ 找不到 docs/guida.txt 文件，请检查文件夹结构！")
        return []

chunks = load_data()

# ================= 4. 初始化聊天记录 =================
if "messages" not in st.session_state:
    st.session_state.messages = []

# ================= 5. 把历史聊天记录显示在网页上 =================
for message in st.session_state.messages:
    with st.chat_message(message["role"]):
        st.markdown(message["content"])

# ================= 6. 核心交互：接收用户输入 =================
if prompt := st.chat_input("请问关于贵大软件工程的问题..."):
    
    # 检查有没有填 API Key
    if not api_key:
        st.warning("⚠️ 请先在左侧边栏输入你的 API Key！")
    else:
        # 6.1 把用户的问题显示在屏幕上，并存入记忆
        st.session_state.messages.append({"role": "user", "content": prompt})
        with st.chat_message("user"):
            st.markdown(prompt)

        # 6.2 简单检索：找出包含提问中关键字最多的文本块
        best_chunk = ""
        max_matches = 0
        for chunk in chunks:
            matches = sum(1 for char in prompt if char in chunk)
            if matches > max_matches:
                max_matches = matches
                best_chunk = chunk

        # 6.3 组装 Prompt (给 AI 的提示词)
        sys_prompt = f"""你是一个专业的教务助理。
请严格根据以下参考资料回答用户的问题。如果资料中没有提到，请直接说“资料未提及”，绝对不要自己编造。

【参考资料】：
{best_chunk}"""

        # 6.4 呼叫大模型，并显示回答（带打字机效果）
        with st.chat_message("assistant"):
            try:
                client = OpenAI(api_key=api_key, base_url="https://api.deepseek.com")
                
                # stream=True 开启流式输出
                response = client.chat.completions.create(
                    model="deepseek-chat",
                    messages=[
                        {"role": "system", "content": sys_prompt},
                        *st.session_state.messages
                    ],
                    stream=True
                )
                
                # 在网页上一点点打印出回答
                response_text = st.write_stream(response)
                
                # 6.5 把 AI 的回答存入记忆
                st.session_state.messages.append({"role": "assistant", "content": response_text})
                
            except Exception as e:
                st.error(f"调用 API 失败，请检查 Key 是否正确。错误信息：{e}")