#!/usr/bin/env python3
"""Require full line/branch coverage for production changes against the v1.4 starting point."""

from pathlib import Path
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
BASE = sys.argv[1] if len(sys.argv) > 1 else "c4e2001de6ec09389d827260d1a985093aa64bc0"
REPORT = ROOT / "build/reports/jacoco/coverage/coverage.xml"


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT, text=True)


def changed_lines(patch):
    result = set()
    for start, length in re.findall(r"^@@ .*? \+(\d+)(?:,(\d+))? @@", patch, re.MULTILINE):
        first = int(start)
        result.update(range(first, first + int(length or "1")))
    return result


report = ET.parse(REPORT).getroot()
sources = {
    f"src/main/java/{package.attrib['name']}/{source.attrib['name']}": source
    for package in report.findall("package")
    for source in package.findall("sourcefile")
}
paths = set(git("diff", "--name-only", BASE).splitlines())
untracked = set(git("ls-files", "--others", "--exclude-standard").splitlines())
paths.update(untracked)
failures = []
line_count = branch_count = 0
for path in sorted(paths):
    if not path.startswith("src/main/java/") or not path.endswith(".java"):
        continue
    if path not in sources:
        failures.append(f"Missing coverage for {path}")
        continue
    if path in untracked:
        added = None
    else:
        added = changed_lines(git("diff", "--unified=0", BASE, "--", path))
    for line in sources[path].findall("line"):
        number = int(line.attrib["nr"])
        if added is not None and number not in added:
            continue
        line_count += 1
        missed_branches = int(line.attrib["mb"])
        branch_count += missed_branches + int(line.attrib["cb"])
        if int(line.attrib["ci"]) == 0 or missed_branches:
            failures.append(f"Uncovered changed line/branch: {path}:{number} {line.attrib}")
if line_count == 0:
    failures.append("No changed executable lines found; coverage was not verified.")
if failures:
    sys.exit("\n".join(failures))
print(f"PASS: {line_count}/{line_count} changed executable lines; "
      f"{branch_count}/{branch_count} changed branches covered (100% each). Base: {BASE}")
