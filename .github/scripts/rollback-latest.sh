#!/usr/bin/env bash
set -euo pipefail

# Rolls back the latest benchmark run for a given scenario.
# Finds the timestamped folder matching the current "latest" file,
# deletes it, and promotes the next most recent run of the same scenario.
#
# Usage: rollback-latest.sh <benchmark_name> <scenario>
#   benchmark_name: e.g. spring-quarkus-perf-comparison
#   scenario:       tuned or ootb

if [ $# -ne 2 ]; then
  echo "::error::Usage: $0 <benchmark_name> <scenario>"
  exit 1
fi

BENCHMARK="$1"
SCENARIO="$2"
RESULTS_DIR="results/${BENCHMARK}"
LATEST_ROOT="results/${BENCHMARK}-latest-${SCENARIO}.json"
LATEST_NESTED="${RESULTS_DIR}/results-latest-${SCENARIO}.json"

# Verify the latest file exists
if [ ! -f "$LATEST_ROOT" ]; then
  echo "::error::Latest file not found: ${LATEST_ROOT}"
  exit 1
fi

# Extract timing.stop from the current latest file as a unique identifier
LATEST_STOP=$(jq -r '.timing.stop' "$LATEST_ROOT")
echo "Current latest timing.stop: $LATEST_STOP"

# Build a reverse-sorted list of timestamped folders (most recent first)
FOLDERS=$(ls -1 "$RESULTS_DIR" | grep -E '^[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}$' | sort -r)

# --- Step 1: Find the current latest folder ---
CURRENT_FOLDER=""
for dirname in $FOLDERS; do
  metrics="${RESULTS_DIR}/${dirname}/metrics.json"
  if [ ! -f "$metrics" ]; then
    continue
  fi

  folder_stop=$(jq -r '.timing.stop' "$metrics")
  folder_scenario=$(jq -r '.config.repo.scenario' "$metrics")

  if [ "$folder_stop" = "$LATEST_STOP" ] && [ "$folder_scenario" = "$SCENARIO" ]; then
    CURRENT_FOLDER="$dirname"
    break
  fi
done

if [ -z "$CURRENT_FOLDER" ]; then
  echo "::error::Could not find a timestamped folder matching the current latest ${SCENARIO} results (timing.stop=${LATEST_STOP})"
  exit 1
fi

echo "Identified current latest folder: $CURRENT_FOLDER"

# --- Step 2: Find the next most recent folder with the same scenario ---
NEXT_FOLDER=""
FOUND_CURRENT=false
for dirname in $FOLDERS; do
  if [ "$dirname" = "$CURRENT_FOLDER" ]; then
    FOUND_CURRENT=true
    continue
  fi

  # Only consider folders that come after (older than) the current one
  if [ "$FOUND_CURRENT" != true ]; then
    continue
  fi

  metrics="${RESULTS_DIR}/${dirname}/metrics.json"
  if [ ! -f "$metrics" ]; then
    continue
  fi

  folder_scenario=$(jq -r '.config.repo.scenario' "$metrics")
  if [ "$folder_scenario" = "$SCENARIO" ]; then
    NEXT_FOLDER="$dirname"
    break
  fi
done

if [ -z "$NEXT_FOLDER" ]; then
  echo "::error::No previous ${SCENARIO} run found to promote as the new latest. Cannot roll back further."
  exit 1
fi

echo "Next most recent ${SCENARIO} folder: $NEXT_FOLDER"

# --- Step 3: Delete the current latest folder ---
echo "Deleting folder: ${RESULTS_DIR}/${CURRENT_FOLDER}"
rm -rf "${RESULTS_DIR}/${CURRENT_FOLDER}"

# --- Step 4: Copy the new latest metrics.json to both locations ---
echo "Promoting ${NEXT_FOLDER}/metrics.json as new latest"
cp "${RESULTS_DIR}/${NEXT_FOLDER}/metrics.json" "$LATEST_ROOT"
cp "${RESULTS_DIR}/${NEXT_FOLDER}/metrics.json" "$LATEST_NESTED"

# Expose outputs for GitHub Actions
if [ -n "${GITHUB_OUTPUT:-}" ]; then
  echo "current_folder=$CURRENT_FOLDER" >> "$GITHUB_OUTPUT"
  echo "next_folder=$NEXT_FOLDER" >> "$GITHUB_OUTPUT"
fi
