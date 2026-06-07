import tkinter as tk

root = tk.Tk()
root.title("Listbox Picker")
root.geometry("380x320+40+40")

items = ["Button", "Canvas", "Entry", "Frame", "Label", "Listbox", "Menu", "Scale", "Spinbox", "Text"]
status = tk.StringVar(value="Pick a widget")

listbox = tk.Listbox(root, height=10)
for item in items:
    listbox.insert("end", item)
listbox.pack(fill="both", expand=True, padx=16, pady=12)

def selected(_event=None):
    index = listbox.curselection()
    if index:
        status.set("Selected " + listbox.get(index[0]))

listbox.bind("<<ListboxSelect>>", selected)
tk.Label(root, textvariable=status, fg="#f5d361").pack(pady=8)
root.mainloop()
