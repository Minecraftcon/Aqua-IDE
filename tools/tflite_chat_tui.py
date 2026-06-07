#!/usr/bin/env python3
"""
Tiny terminal chat UI for the downloaded Aqua TFLite model.

Default model folder inside the app:
  ~/models/Tiny-Hinglish-Chat-21M

Usage:
  python ~/tflite_chat_tui.py
  python ~/tflite_chat_tui.py --model-dir ~/models/Tiny-Hinglish-Chat-21M

Commands:
  /exit       quit
  /reset      clear chat history
"""

from __future__ import annotations

import argparse
import math
import os
import random
import sys
import time
from pathlib import Path

import numpy as np
from tflite_runtime.interpreter import Interpreter, OpResolverType
from tokenizers import Tokenizer


DEFAULT_MODEL_DIRS = [
    "~/models/Tiny-Hinglish-Chat-21M",
    "files/home/models/Tiny-Hinglish-Chat-21M",
    ".cache/tflite-llm/Abhishekcr448-Tiny-Hinglish-Chat-21M",
]


def resolve_model_dir(value: str | None) -> Path:
    candidates = [value] if value else DEFAULT_MODEL_DIRS
    for item in candidates:
        if not item:
            continue
        path = Path(item).expanduser()
        if (path / "model.tflite").exists() and (path / "tokenizer.json").exists():
            return path
    searched = ", ".join(str(Path(p).expanduser()) for p in candidates if p)
    raise SystemExit(f"model not found. searched: {searched}")


def softmax_sample(logits: np.ndarray, temperature: float, top_k: int) -> int:
    logits = logits.astype(np.float64)
    if top_k > 0 and top_k < logits.size:
        keep = np.argpartition(logits, -top_k)[-top_k:]
        masked = np.full_like(logits, -np.inf)
        masked[keep] = logits[keep]
        logits = masked
    if temperature <= 0:
        return int(np.argmax(logits))
    logits = logits / max(temperature, 1e-5)
    logits -= np.nanmax(logits)
    probs = np.exp(logits)
    total = probs.sum()
    if not math.isfinite(total) or total <= 0:
        return int(np.argmax(logits))
    probs /= total
    return int(np.random.choice(np.arange(logits.size), p=probs))


class TFLiteChatModel:
    def __init__(
        self,
        model_dir: Path,
        *,
        max_context: int,
        num_threads: int,
        resolver: str,
    ) -> None:
        self.model_dir = model_dir
        self.max_context = max_context
        self.tokenizer = Tokenizer.from_file(str(model_dir / "tokenizer.json"))

        kwargs = {"model_path": str(model_dir / "model.tflite"), "num_threads": num_threads}
        if resolver == "ref":
            kwargs["experimental_op_resolver_type"] = OpResolverType.BUILTIN_REF
        self.interpreter = Interpreter(**kwargs)

        inputs = {d["name"]: d for d in self.interpreter.get_input_details()}
        if "input_ids" not in inputs or "attention_mask" not in inputs:
            names = ", ".join(inputs)
            raise RuntimeError(f"expected input_ids and attention_mask inputs, got: {names}")
        self.interpreter.resize_tensor_input(inputs["input_ids"]["index"], [1, max_context], strict=False)
        self.interpreter.allocate_tensors()

        self.inputs = {d["name"]: d for d in self.interpreter.get_input_details()}
        self.outputs = self.interpreter.get_output_details()
        self.logits_index = self.outputs[0]["index"]

        self.pad_id = self.token_to_id("[PAD]", 0)
        self.eos_id = self.token_to_id("[EOS]", None)

    def token_to_id(self, token: str, fallback: int | None) -> int | None:
        token_id = self.tokenizer.token_to_id(token)
        return fallback if token_id is None else int(token_id)

    def encode(self, text: str) -> list[int]:
        return list(self.tokenizer.encode(text).ids)

    def decode(self, ids: list[int]) -> str:
        return self.tokenizer.decode(ids)

    def next_token_logits(self, ids: list[int]) -> np.ndarray:
        window = ids[-self.max_context :]
        if not window:
            window = [self.pad_id]
        pos = len(window) - 1
        padded = window + [self.pad_id] * (self.max_context - len(window))
        input_ids = np.asarray([padded], dtype=np.int32)
        attention_mask = np.zeros((1, self.max_context), dtype=np.int32)
        attention_mask[0, : len(window)] = 1

        self.interpreter.set_tensor(self.inputs["input_ids"]["index"], input_ids)
        self.interpreter.set_tensor(self.inputs["attention_mask"]["index"], attention_mask)
        self.interpreter.invoke()
        return self.interpreter.get_tensor(self.logits_index)[0, pos].copy()

    def generate(
        self,
        prompt_ids: list[int],
        *,
        max_new_tokens: int,
        temperature: float,
        top_k: int,
        repeat_penalty: float,
    ) -> list[int]:
        ids = list(prompt_ids)
        out: list[int] = []
        for _ in range(max_new_tokens):
            logits = self.next_token_logits(ids)
            logits[self.pad_id] = -1e9
            if repeat_penalty > 1.0:
                for seen in set(ids[-64:]):
                    logits[seen] /= repeat_penalty
            next_id = softmax_sample(logits, temperature, top_k)
            if self.eos_id is not None and next_id == self.eos_id:
                break
            ids.append(next_id)
            out.append(next_id)
            piece = self.decode([next_id])
            print(piece, end="", flush=True)
        print()
        return out


def make_prompt(history: list[tuple[str, str]], user_text: str) -> str:
    lines = [
        "You are Aqua, a small local chat model. Reply briefly.",
    ]
    for role, text in history[-6:]:
        lines.append(f"{role}: {text}")
    lines.append(f"User: {user_text}")
    lines.append("Assistant:")
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description="TFLite local chat TUI")
    parser.add_argument("--model-dir", default=os.environ.get("AQUA_TFLITE_MODEL_DIR"))
    parser.add_argument("--max-context", type=int, default=64)
    parser.add_argument("--max-new-tokens", type=int, default=48)
    parser.add_argument("--temperature", type=float, default=0.8)
    parser.add_argument("--top-k", type=int, default=40)
    parser.add_argument("--repeat-penalty", type=float, default=1.08)
    parser.add_argument("--threads", type=int, default=2)
    parser.add_argument("--resolver", choices=["xnnpack", "ref"], default="xnnpack")
    args = parser.parse_args()

    random.seed()
    np.random.seed(int(time.time()) & 0xFFFF_FFFF)

    model_dir = resolve_model_dir(args.model_dir)
    print(f"loading TFLite model: {model_dir}")
    print("if it crashes on a device, retry with: --resolver ref\n")
    model = TFLiteChatModel(
        model_dir,
        max_context=args.max_context,
        num_threads=args.threads,
        resolver=args.resolver,
    )

    history: list[tuple[str, str]] = []
    print("Aqua TFLite chat ready. /exit to quit, /reset to clear.\n")
    while True:
        try:
            user_text = input("you> ").strip()
        except EOFError:
            print()
            return 0
        except KeyboardInterrupt:
            print("\n/exit")
            return 0

        if not user_text:
            continue
        if user_text == "/exit":
            return 0
        if user_text == "/reset":
            history.clear()
            print("history cleared")
            continue

        prompt = make_prompt(history, user_text)
        prompt_ids = model.encode(prompt)
        if len(prompt_ids) > args.max_context:
            prompt_ids = prompt_ids[-args.max_context :]

        print("ai> ", end="", flush=True)
        start = time.time()
        try:
            out_ids = model.generate(
                prompt_ids,
                max_new_tokens=args.max_new_tokens,
                temperature=args.temperature,
                top_k=args.top_k,
                repeat_penalty=args.repeat_penalty,
            )
        except Exception as exc:
            if args.resolver != "ref":
                print(f"\nerror: {exc}")
                print("retrying once with reference resolver...")
                args.resolver = "ref"
                model = TFLiteChatModel(
                    model_dir,
                    max_context=args.max_context,
                    num_threads=args.threads,
                    resolver="ref",
                )
                print("ai> ", end="", flush=True)
                out_ids = model.generate(
                    prompt_ids,
                    max_new_tokens=args.max_new_tokens,
                    temperature=args.temperature,
                    top_k=args.top_k,
                    repeat_penalty=args.repeat_penalty,
                )
            else:
                print(f"\nerror: {exc}")
                continue

        reply = model.decode(out_ids).strip()
        history.append(("User", user_text))
        history.append(("Assistant", reply))
        print(f"[{time.time() - start:.1f}s]\n")


if __name__ == "__main__":
    raise SystemExit(main())
