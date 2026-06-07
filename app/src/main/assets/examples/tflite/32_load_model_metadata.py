from pathlib import Path

model = Path.home() / "models" / "model.tflite"
print("Model path:", model)
if not model.exists():
    print("Put a .tflite model at ~/models/model.tflite")
else:
    try:
        import tflite_runtime.interpreter as tflite
        interpreter = tflite.Interpreter(model_path=str(model))
        interpreter.allocate_tensors()
        print("Inputs:")
        for item in interpreter.get_input_details():
            print(item["name"], item["shape"], item["dtype"])
        print("Outputs:")
        for item in interpreter.get_output_details():
            print(item["name"], item["shape"], item["dtype"])
    except Exception as error:
        print("Failed:", error)
