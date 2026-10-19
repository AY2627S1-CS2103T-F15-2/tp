#!/usr/bin/env python3
"""Run the recorded JavaFX command plan with fail-fast comparison and a session record."""

import os
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parent.parent
wrapper = "gradlew.bat" if os.name == "nt" else "./gradlew"
result = subprocess.run(
    [wrapper, "test", "--tests", "*DataTransferUiPlanTest", "--fail-fast", "--rerun-tasks"], cwd=root
)
session = root / "_temp/ui-test-session.txt"
if session.exists():
    print(session.read_text())
print(f"Session record: {session}")
sys.exit(result.returncode)
