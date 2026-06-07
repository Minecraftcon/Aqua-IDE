import tkinter as tk

root = tk.Tk()
root.title("Markdown Preview")
root.geometry("560x340+20+20")
left = tk.Text(root, wrap="word")
right = tk.Text(root, wrap="word", bg="#20252d", fg="#d8dee9")
left.pack(side="left", fill="both", expand=True)
right.pack(side="left", fill="both", expand=True)
left.insert("end", "# Aqua\\n\\n- edit left\\n- preview right\\n")

def preview(_=None):
    text = left.get("1.0", "end")
    right.delete("1.0", "end")
    for line in text.splitlines():
        if line.startswith("# "):
            right.insert("end", line[2:].upper() + "\\n")
        elif line.startswith("- "):
            right.insert("end", "  * " + line[2:] + "\\n")
        else:
            right.insert("end", line + "\\n")

left.bind("<KeyRelease>", preview)
preview()
root.mainloop()
