print("Checking TensorFlow Lite runtime...")
try:
    import tflite_runtime.interpreter as tflite
    print("tflite_runtime OK")
    print("Interpreter:", tflite.Interpreter)
except Exception as error:
    print("tflite_runtime unavailable:", error)
    print("Install the Aqua TFLite extra package, then run again.")
