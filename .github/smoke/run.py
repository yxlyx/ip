#!/usr/bin/env python3
"""Test the exact v0.2 release using GuiSmokeAgent and synthetic save data.

Requires a plain JDK 25 on PATH and a display (use xvfb-run on Linux).
All application data stays in out/release-smoke/work, never project user data.
Only logs, markers, and screenshots go into the report artifact directory.
"""

from pathlib import Path
import hashlib
import shutil
import subprocess
import sys
import traceback
import urllib.request

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "out" / "release-smoke"
WORK = OUTPUT / "work"
REPORT = OUTPUT / "report"
RELEASE_URL = "https://github.com/yxlyx/ip/releases/download/v0.2/Chatty.jar"
RELEASE_SHA256 = "63421a377daf7f139405340b4ec2bba4d4feea2e4ad21fc8242ab5af97b0e974"


def run_logged(command, name, cwd=OUTPUT, timeout=90):
    """Capture output directly to disk, preserving logs on failure or timeout."""
    with (REPORT / (name + ".log")).open("wb") as log:
        try:
            subprocess.run(
                [str(part) for part in command], cwd=cwd, stdout=log,
                stderr=subprocess.STDOUT, timeout=timeout, check=True,
            )
        except subprocess.TimeoutExpired:
            log.write(b"\nSmoke runner: command exceeded its timeout.\n")
            raise


def run_phase(phase, agent):
    """Run the actual release entry point and preserve agent evidence always."""
    print("Running GUI phase: " + phase, flush=True)
    filenames = (phase + ".png", phase + "-scene.png", phase + "-passed.txt")
    try:
        run_logged(
            ["java", "-javaagent:" + str(agent) + "=" + phase, "-jar", "Chatty.jar"],
            phase, cwd=WORK, timeout=90,
        )
        for filename in filenames:
            if not (WORK / filename).is_file():
                raise RuntimeError(phase + ": missing agent evidence " + filename)
    finally:
        for filename in filenames:
            source = WORK / filename
            if source.is_file():
                shutil.copy2(source, REPORT / filename)


def download_release(destination):
    """Download and verify the expected release bytes before executing them."""
    with urllib.request.urlopen(RELEASE_URL, timeout=60) as response:
        with destination.open("wb") as target:
            shutil.copyfileobj(response, target)
    digest = hashlib.sha256()
    with destination.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    actual = digest.hexdigest()
    (REPORT / "release-sha256.txt").write_text(actual + "\n", encoding="utf-8")
    if actual != RELEASE_SHA256:
        raise RuntimeError("Released JAR SHA256 mismatch: " + actual)


def main():
    """Compile the supplied helper and test fresh, persisted, and corrupt data."""
    REPORT.mkdir(parents=True, exist_ok=True)
    try:
        # Refuse stale state instead of deleting any pre-existing local files.
        WORK.mkdir(parents=True, exist_ok=True)
        if any(WORK.iterdir()):
            raise RuntimeError("out/release-smoke/work must be empty before a run")
        run_logged(["java", "-version"], "java-version")
        print((REPORT / "java-version.log").read_text(encoding="utf-8", errors="replace"),
              end="", flush=True)
        application = WORK / "Chatty.jar"
        download_release(application)
        classes = OUTPUT / "agent-classes"
        classes.mkdir(parents=True, exist_ok=True)
        run_logged(
            ["javac", "-cp", application, "-d", classes,
             ROOT / ".github" / "smoke" / "GuiSmokeAgent.java"], "compile-agent",
        )
        manifest = OUTPUT / "agent-manifest.mf"
        manifest.write_text("Manifest-Version: 1.0\nPremain-Class: GuiSmokeAgent\n\n",
                            encoding="utf-8")
        agent = (OUTPUT / "smoke-agent.jar").resolve()
        run_logged(
            ["jar", "--create", "--file", agent, "--manifest", manifest,
             "-C", classes, "."], "package-agent",
        )
        if set(WORK.iterdir()) != {application}:
            raise RuntimeError("Fresh working directory must contain only Chatty.jar")
        run_phase("fresh", agent)
        data = WORK / "data" / "chatty.txt"
        if not data.is_file() or not data.read_bytes():
            raise RuntimeError("Fresh phase did not save synthetic tasks")
        run_phase("reload", agent)
        with data.open("ab") as saved:
            saved.write(b"\nTHIS_IS_A_MALFORMED_SMOKE_TEST_RECORD\n")
        corrupted_bytes = data.read_bytes()
        try:
            run_phase("corrupt", agent)
        finally:
            unchanged = data.is_file() and data.read_bytes() == corrupted_bytes
            (REPORT / "corrupt-data-preservation.txt").write_text(
                "unchanged\n" if unchanged else "CHANGED OR MISSING\n", encoding="utf-8"
            )
        if not unchanged:
            raise RuntimeError("Corrupt phase changed the malformed save file")
        summary = "PASS: Chatty v0.2 checksum verified; fresh, reload, corrupt GUI phases passed."
    except Exception:
        (REPORT / "failure.txt").write_text(traceback.format_exc(), encoding="utf-8")
        summary = "FAIL: Chatty v0.2 GUI smoke test; see report logs and failure.txt."
        (REPORT / "summary.txt").write_text(summary + "\n", encoding="utf-8")
        print(summary, file=sys.stderr, flush=True)
        return 1
    (REPORT / "summary.txt").write_text(summary + "\n", encoding="utf-8")
    print(summary, flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
