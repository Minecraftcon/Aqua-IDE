import tkinter as tk
from tkinter import ttk

root = tk.Tk()
root.title("File Explorer Mock")
root.geometry("520x340+20+20")
tree = ttk.Treeview(root)
tree.pack(side="left", fill="both", expand=True, padx=8, pady=8)
details = tk.Text(root, width=28)
details.pack(side="left", fill="both", padx=8, pady=8)

home = tree.insert("", "end", text="home", open=True)
for folder, files in {"examples": ["demo.py", "model.tflite"], "src": ["main.py", "ui.py"]}.items():
    node = tree.insert(home, "end", text=folder, open=True)
    for file in files:
        tree.insert(node, "end", text=file)

def selected(_=None):
    item = tree.focus()
    details.delete("1.0", "end")
    details.insert("end", tree.item(item, "text"))

tree.bind("<<TreeviewSelect>>", selected)
root.mainloop()
