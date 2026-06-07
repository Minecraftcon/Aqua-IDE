import tkinter as tk
from tkinter import messagebox

root = tk.Tk()
root.title("Messagebox")
root.geometry("360x220+40+40")

tk.Label(root, text="Dialogs", font=("Arial", 18, "bold")).pack(pady=22)
tk.Button(root, text="Info", command=lambda: messagebox.showinfo("Aqua", "Tk messagebox")).pack(pady=6)
tk.Button(root, text="Ask", command=lambda: messagebox.askyesno("Question", "Continue?")).pack(pady=6)
root.mainloop()
