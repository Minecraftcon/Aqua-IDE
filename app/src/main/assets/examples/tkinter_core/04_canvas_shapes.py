import tkinter as tk

root = tk.Tk()
root.title("Canvas Shapes")
root.geometry("420x320+35+35")

canvas = tk.Canvas(root, bg="#20252d", highlightthickness=0)
canvas.pack(fill="both", expand=True)
canvas.create_rectangle(30, 30, 180, 110, fill="#2f3744", outline="#72d6ff", width=3)
canvas.create_oval(220, 30, 360, 150, fill="#00e5ff", outline="white", width=2)
canvas.create_line(40, 210, 360, 210, fill="#73ffb5", width=6)
canvas.create_polygon(90, 260, 160, 165, 230, 260, fill="#f5d361", outline="#111")
canvas.create_text(210, 292, text="Canvas primitives", fill="#d8dee9", font=("Arial", 13))
root.mainloop()
