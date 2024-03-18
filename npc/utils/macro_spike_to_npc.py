import re
"""
这个是spike实现encode.h里面宏定义转scala的小工具，主打一个抄。。。
"""
con="""

"""

input_string = """
#define MASK_VAMOOREI32_V 0xf800707f
...
"""

def process_string(input_string):
    # 将字符串分割为多行
    lines = input_string.split('\n')

    # 遍历每一行
    processed_lines = []
    for line in lines:
        # 删除空行或全为空格的行
        if not line.strip():
            continue

        # 匹配#define 定义且格式为#define MASK_NAME VALUE的形式
        match_result = re.match(r'^#define\s+(\w+)\s+0x(\w+)', line)
        if match_result:
            # 提取名称和值（这里已经是字符串）
            name, value = match_result.groups()
            # 格式化输出并添加到结果列表
            formatted_line = f'def {name} = 0x{value}'
            processed_lines.append(formatted_line)

    # 返回处理后的数据
    return processed_lines

# 处理字符串并打印结果
for line in process_string(con):
    print(line)