#!/usr/bin/env bash
# Usage: ./changeVersion.sh <version>
# Sets the casl-android version (single source of truth) and commits.
set -euo pipefail

[[ $# -eq 1 ]] || { echo "Usage: $0 <version>"; exit 1; }
VERSION="$1"
ROOT="$(cd "$(dirname "$0")" && pwd)"

perl -i -pe "s|^version = '[0-9.]+'|version = '${VERSION}'|" "$ROOT/casl-android/build.gradle"

git -C "$ROOT" add casl-android/build.gradle
git -C "$ROOT" diff --cached --quiet || git -C "$ROOT" commit -m "chore: bump version to ${VERSION}"
echo "casl-mobile-monorepo version → ${VERSION}"
