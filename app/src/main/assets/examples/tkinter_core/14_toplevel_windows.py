import tkinter as tk

root = tk.Tk()
root.title("Main Window")
root.geometry("360x220+20+20")

def open_child():
    child = tk.Toplevel(root)
    child.title("Child Window")
    child.geometry("260x150+80+90")
    tk.Label(child, text="Second Tk window", font=("Arial", 14)).pack(expand=True)
    tk.Button(child, text="Close", command=child.destroy).pack(pady=10)

tk.Label(root, text="Toplevel example", font=("Arial", 18)).pack(expand=True)
tk.Button(root, text="Open child", command=open_child).pack(pady=18)
root.mainloop()
