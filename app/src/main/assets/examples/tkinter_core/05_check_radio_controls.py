import tkinter as tk

root = tk.Tk()
root.title("Checkbutton + Radiobutton")
root.geometry("420x300+30+30")
root.configure(bg="#20252d")

mode = tk.StringVar(value="Python")
line_numbers = tk.BooleanVar(value=True)
syntax = tk.BooleanVar(value=True)
result = tk.StringVar(value="Configure the editor")

tk.Label(root, text="Editor Options", fg="#f5d361", bg="#20252d", font=("Arial", 16, "bold")).pack(pady=12)
for label, var in (("Line numbers", line_numbers), ("Syntax highlight", syntax)):
    tk.Checkbutton(root, text=label, variable=var, bg="#20252d", fg="#d8dee9", selectcolor="#2f3744").pack(anchor="w", padx=40)

for lang in ("Python", "C", "C++"):
    tk.Radiobutton(root, text=lang, value=lang, variable=mode, bg="#20252d", fg="#d8dee9", selectcolor="#2f3744").pack(anchor="w", padx=40)

def apply():
    result.set(f"{mode.get()} | lines={line_numbers.get()} | syntax={syntax.get()}")

tk.Button(root, text="Apply", command=apply).pack(pady=12)
tk.Label(root, textvariable=result, fg="#8be9fd", bg="#20252d").pack()
root.mainloop()
