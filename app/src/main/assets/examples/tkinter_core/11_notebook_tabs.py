import tkinter as tk
from tkinter import ttk

root = tk.Tk()
root.title("Notebook")
root.geometry("460x280+30+30")

tabs = ttk.Notebook(root)
tabs.pack(fill="both", expand=True, padx=12, pady=12)
for name, color in (("Editor", "#20252d"), ("Terminal", "#111318"), ("Settings", "#2f3744")):
    frame = tk.Frame(tabs, bg=color)
    tk.Label(frame, text=name, bg=color, fg="white", font=("Arial", 18, "bold")).pack(expand=True)
    tabs.add(frame, text=name)
root.mainloop()
