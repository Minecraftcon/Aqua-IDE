import tkinter as tk

root = tk.Tk()
root.title("Menu")
root.geometry("420x240+30+30")
status = tk.StringVar(value="Use the menu")

menu = tk.Menu(root)
file_menu = tk.Menu(menu, tearoff=False)
file_menu.add_command(label="New", command=lambda: status.set("New file"))
file_menu.add_command(label="Save", command=lambda: status.set("Saved"))
file_menu.add_separator()
file_menu.add_command(label="Exit", command=root.destroy)
menu.add_cascade(label="File", menu=file_menu)
menu.add_command(label="Run", command=lambda: status.set("Running..."))
root.config(menu=menu)

tk.Label(root, textvariable=status, font=("Arial", 18)).pack(expand=True)
root.mainloop()
