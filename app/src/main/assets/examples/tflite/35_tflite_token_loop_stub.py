print("Tiny token loop stub")
print("This is a shape for small local LLM/TFLite experiments.")

prompt = "hello aqua"
tokens = prompt.split()
for step in range(6):
    next_token = ["<bos>", "def", "main", "(", ")", ":"][step]
    tokens.append(next_token)
    print(step, " ".join(tokens))

print("Replace the stub with a real tokenizer + TFLite invoke loop.")
