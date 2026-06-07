import tkinter as tk

root = tk.Tk()
root.title("PanedWindow")
root.geometry("520x300+20+20")

pane = tk.PanedWindow(root, orient="horizontal", sashwidth=8)
pane.pack(fill="both", expand=True)

files = tk.Listbox(pane)
for name in ("main.py", "engine.c", "README.md", "settings.json"):
    files.insert("end", name)
pane.add(files, minsize=140)

text = tk.Text(pane)
text.insert("end", "# Editor pane\\nprint('Resizable panes')\\n")
pane.add(text, minsize=260)
root.mainloop()
