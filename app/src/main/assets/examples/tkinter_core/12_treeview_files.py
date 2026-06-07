import tkinter as tk
from tkinter import ttk

root = tk.Tk()
root.title("Treeview")
root.geometry("420x320+30+30")

tree = ttk.Treeview(root)
tree.pack(fill="both", expand=True, padx=12, pady=12)
project = tree.insert("", "end", text="AquaProject", open=True)
src = tree.insert(project, "end", text="src", open=True)
tree.insert(src, "end", text="main.py")
tree.insert(src, "end", text="engine.c")
assets = tree.insert(project, "end", text="assets", open=True)
tree.insert(assets, "end", text="model.tflite")
tree.insert(project, "end", text="README.md")
root.mainloop()
