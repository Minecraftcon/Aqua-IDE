#!/usr/bin/env python3
"""
Tiny terminal chat UI for local Hugging Face causal language models.

Usage:
  python chat_tui.py --model /path/to/model
  AQUA_CHAT_MODEL=/path/to/model python chat_tui.py

Commands inside the chat:
  /exit              quit
  /reset             clear conversation history
  /system TEXT       replace the system prompt
  /save PATH         save the current transcript
"""

from __future__ import annotations

import argparse
import os
import queue
import sys
import threading
import time
from dataclasses import dataclass
from pathlib import Path


DEFAULT_SYSTEM = "You are Aqua, a concise helpful coding assistant running locally."


@dataclass
class ChatTurn:
    role: str
    content: str


def die(message: str, code: int = 1) -> None:
    print(f"\nerror: {message}", file=sys.stderr)
    raise SystemExit(code)


def import_transformers():
    try:
        import torch  # type: ignore
    except Exception as exc:
        die(
            "PyTorch is not installed, so transformers cannot run a CausalLM model yet. "
            "Install/build torch or swap this script to a llama.cpp/onnx/tflite backend. "
            f"Original import error: {exc}"
        )

    try:
        from transformers import AutoModelForCausalLM, AutoTokenizer, TextIteratorStreamer
    except Exception as exc:
        die(f"transformers import failed: {exc}")

    return torch, AutoModelForCausalLM, AutoTokenizer, TextIteratorStreamer


def build_prompt(tokenizer, turns: list[ChatTurn]) -> str:
    messages = [{"role": t.role, "content": t.content} for t in turns]
    if hasattr(tokenizer, "apply_chat_template") and tokenizer.chat_template:
        return tokenizer.apply_chat_template(
            messages,
            tokenize=False,
            add_generation_prompt=True,
        )

    lines: list[str] = []
    for turn in turns:
        if turn.role == "system":
            lines.append(f"System: {turn.content}")
        elif turn.role == "user":
            lines.append(f"User: {turn.content}")
        else:
            lines.append(f"Assistant: {turn.content}")
    lines.append("Assistant:")
    return "\n".join(lines)


def read_multiline(prompt: str = "you> ") -> str:
    print(prompt, end="", flush=True)
    lines: list[str] = []
    while True:
        try:
            line = input()
        except EOFError:
            return "/exit"
        if not lines and line.startswith("/"):
            return line.strip()
        if line.strip() == ".":
            break
        lines.append(line)
        if len(lines) == 1:
            break
    return "\n".join(lines).strip()


def save_transcript(path: Path, turns: list[ChatTurn]) -> None:
    text = []
    for turn in turns:
        text.append(f"## {turn.role}\n\n{turn.content}\n")
    path.write_text("\n".join(text), encoding="utf-8")


def generate_reply(
    *,
    torch,
    model,
    tokenizer,
    streamer_cls,
    turns: list[ChatTurn],
    max_new_tokens: int,
    temperature: float,
    top_p: float,
) -> str:
    prompt = build_prompt(tokenizer, turns)
    inputs = tokenizer(prompt, return_tensors="pt")
    inputs = {k: v.to(model.device) for k, v in inputs.items()}

    streamer = streamer_cls(tokenizer, skip_prompt=True, skip_special_tokens=True)
    kwargs = dict(
        **inputs,
        streamer=streamer,
        max_new_tokens=max_new_tokens,
        do_sample=temperature > 0,
        temperature=max(temperature, 1e-5),
        top_p=top_p,
        pad_token_id=tokenizer.eos_token_id,
        eos_token_id=tokenizer.eos_token_id,
    )

    errq: queue.Queue[BaseException] = queue.Queue()

    def worker() -> None:
        try:
            with torch.inference_mode():
                model.generate(**kwargs)
        except BaseException as exc:
            errq.put(exc)

    thread = threading.Thread(target=worker, daemon=True)
    thread.start()

    print("ai> ", end="", flush=True)
    chunks: list[str] = []
    while thread.is_alive() or not errq.empty():
        if not errq.empty():
            raise errq.get()
        try:
            chunk = next(streamer)
        except StopIteration:
            break
        chunks.append(chunk)
        print(chunk, end="", flush=True)
    thread.join()

    for chunk in streamer:
        chunks.append(chunk)
        print(chunk, end="", flush=True)

    print()
    return "".join(chunks).strip()


def main() -> int:
    parser = argparse.ArgumentParser(description="Aqua local model chat TUI")
    parser.add_argument("--model", default=os.environ.get("AQUA_CHAT_MODEL"))
    parser.add_argument("--system", default=DEFAULT_SYSTEM)
    parser.add_argument("--max-new-tokens", type=int, default=256)
    parser.add_argument("--temperature", type=float, default=0.7)
    parser.add_argument("--top-p", type=float, default=0.9)
    parser.add_argument("--local-files-only", action="store_true")
    parser.add_argument("--trust-remote-code", action="store_true")
    parser.add_argument("--device", default="auto", choices=["auto", "cpu", "cuda"])
    args = parser.parse_args()

    if not args.model:
        die("pass --model /path/to/model or set AQUA_CHAT_MODEL")

    torch, AutoModelForCausalLM, AutoTokenizer, TextIteratorStreamer = import_transformers()

    print(f"loading tokenizer: {args.model}")
    tokenizer = AutoTokenizer.from_pretrained(
        args.model,
        local_files_only=args.local_files_only,
        trust_remote_code=args.trust_remote_code,
    )
    if tokenizer.pad_token_id is None:
        tokenizer.pad_token = tokenizer.eos_token

    print(f"loading model: {args.model}")
    device_map = None if args.device == "cpu" else args.device
    dtype = torch.float16 if args.device == "cuda" and torch.cuda.is_available() else torch.float32
    model = AutoModelForCausalLM.from_pretrained(
        args.model,
        local_files_only=args.local_files_only,
        trust_remote_code=args.trust_remote_code,
        torch_dtype=dtype,
        device_map=device_map,
        low_cpu_mem_usage=True,
    )
    if args.device == "cpu":
        model.to("cpu")
    model.eval()

    turns = [ChatTurn("system", args.system)]
    print("\nAqua chat ready. Type /exit, /reset, /system TEXT, /save PATH.")
    print("Tip: enter a single dot on its own line to finish a multi-line message.\n")

    while True:
        user_text = read_multiline()
        if not user_text:
            continue
        if user_text == "/exit":
            return 0
        if user_text == "/reset":
            turns = [ChatTurn("system", args.system)]
            print("history cleared")
            continue
        if user_text.startswith("/system "):
            args.system = user_text[len("/system ") :].strip() or DEFAULT_SYSTEM
            turns = [ChatTurn("system", args.system)]
            print("system prompt updated and history cleared")
            continue
        if user_text.startswith("/save "):
            path = Path(user_text[len("/save ") :].strip()).expanduser()
            save_transcript(path, turns)
            print(f"saved {path}")
            continue

        turns.append(ChatTurn("user", user_text))
        started = time.time()
        try:
            reply = generate_reply(
                torch=torch,
                model=model,
                tokenizer=tokenizer,
                streamer_cls=TextIteratorStreamer,
                turns=turns,
                max_new_tokens=args.max_new_tokens,
                temperature=args.temperature,
                top_p=args.top_p,
            )
        except KeyboardInterrupt:
            print("\ninterrupted")
            continue
        except Exception as exc:
            print(f"\nerror while generating: {exc}", file=sys.stderr)
            continue
        turns.append(ChatTurn("assistant", reply))
        print(f"[{time.time() - started:.1f}s]\n")


if __name__ == "__main__":
    raise SystemExit(main())
