import tkinter as tk

root = tk.Tk()
root.title("Text + Scrollbar")
root.geometry("520x340+20+20")

toolbar = tk.Frame(root, bg="#1b3349")
toolbar.pack(fill="x")
tk.Label(toolbar, text="Tiny Notes", fg="white", bg="#1b3349", font=("Arial", 14, "bold")).pack(side="left", padx=12, pady=8)

body = tk.Frame(root)
body.pack(fill="both", expand=True)
scroll = tk.Scrollbar(body)
scroll.pack(side="right", fill="y")
text = tk.Text(body, wrap="word", yscrollcommand=scroll.set, font=("Courier", 12))
text.pack(fill="both", expand=True)
scroll.config(command=text.yview)

text.insert("end", "Aqua IDE Tkinter text widget\\n\\n")
for i in range(1, 40):
    text.insert("end", f"{i:02d}: editable line with scrollbar support\\n")
root.mainloop()
