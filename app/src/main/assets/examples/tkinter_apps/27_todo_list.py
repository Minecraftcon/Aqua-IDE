import tkinter as tk

root = tk.Tk()
root.title("Todo List")
root.geometry("420x320+30+30")
entry = tk.Entry(root)
entry.pack(fill="x", padx=12, pady=10)
items = tk.Listbox(root)
items.pack(fill="both", expand=True, padx=12)

def add():
    text = entry.get().strip()
    if text:
        items.insert("end", text)
        entry.delete(0, "end")

def delete():
    for index in reversed(items.curselection()):
        items.delete(index)

tk.Button(root, text="Add", command=add).pack(side="left", expand=True, fill="x", padx=12, pady=10)
tk.Button(root, text="Delete", command=delete).pack(side="left", expand=True, fill="x", padx=12, pady=10)
root.mainloop()
