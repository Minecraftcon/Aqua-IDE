import tkinter as tk

root = tk.Tk()
root.title("Calculator")
root.geometry("320x420+40+20")
expr = tk.StringVar()
display = tk.Entry(root, textvariable=expr, font=("Arial", 22), justify="right")
display.grid(row=0, column=0, columnspan=4, sticky="nsew", padx=8, pady=8)

def press(value):
    if value == "=":
        try:
            expr.set(str(eval(expr.get(), {"__builtins__": {}}, {})))
        except Exception:
            expr.set("error")
    elif value == "C":
        expr.set("")
    else:
        expr.set(expr.get() + value)

buttons = ["7", "8", "9", "/", "4", "5", "6", "*", "1", "2", "3", "-", "C", "0", "=", "+"]
for i, value in enumerate(buttons):
    tk.Button(root, text=value, command=lambda v=value: press(v), font=("Arial", 18)).grid(row=1 + i // 4, column=i % 4, sticky="nsew", padx=4, pady=4)
for i in range(5):
    root.rowconfigure(i, weight=1)
for i in range(4):
    root.columnconfigure(i, weight=1)
root.mainloop()
