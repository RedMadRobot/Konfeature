#!/usr/bin/env bash
#
# Prepares the library for release. Creates a release branch from the 'main'.
#
# Usage: ./release.sh [version]
# Example: ./release.sh 1.0.0
#
# Original release script: https://github.com/RedMadRobot/android-library-template/blob/main/release.sh

set -euo pipefail

# The script could be run from any directory.
cd "$(dirname "$0")"

# Configure the script
properties="gradle.properties"
changelog="CHANGELOG.md"
readme="README.md"
files_to_update_version=("$properties" "$readme")
github_repository_url="https://github.com/RedMadRobot/Konfeature"

#region Utils
function error() {
  echo "❌ $1"
  return 1
}

function property {
  grep "^${1}=" "$properties" | cut -d'=' -f2
}

function replace() {
  # Escape linebreaks
  local replacement=${2//$'\n'/\\\n}
  # Portable in-place edit.
  # See: https://stackoverflow.com/a/4247319
  sed -i".bak" -E "s~$1~$replacement~g" "$3"
  rm "$3.bak"
}

function diff_link() {
  echo -n "$github_repository_url/compare/${1}...${2}"
}
#endregion

# Validate input parameters
version=${1:-Please, specify the version to be released as a script parameter}
[[ $version != v* ]] || error "The version should not start from 'v'"
version_tag="v$version"

# 0. Fetch remote changes
echo "️⏳ Creating release branch..."
release_branch="release/$version"
git checkout --quiet -b "$release_branch"
git pull --quiet --rebase origin main
echo "✅ Branch '$release_branch' created"
echo

# 1. Calculate version values for later
last_version=$(property "version")
if [[ "$last_version" == "$version" ]]; then
  echo "🤔 Version $version is already set."
  exit 0
fi
echo "🚀 Update $last_version → $version"
echo

# 2. Update version everywhere
for file in "${files_to_update_version[@]}" ; do
  replace "$last_version" "$version" "$file"
  echo "✅ Updated version in $file"
done

# 3. Update header in CHANGELOG.md
date=$(date -u +%Y-%m-%d)
header_replacement=\
"## [Unreleased]

### Changes

- *No changes*

## [$version] ($date)"
replace "^## \[Unreleased\].*" "$header_replacement" "$changelog"
echo "✅ Updated CHANGELOG.md header"

# 4. Add link to version diff
unreleased_diff_link="[unreleased]: $(diff_link "$version_tag" "main")"
version_diff_link="[$version]: $(diff_link "v$last_version" "$version_tag")"
replace "^\[unreleased\]:.*" "$unreleased_diff_link\n$version_diff_link" "$changelog"
echo "✅ Added a diff link to CHANGELOG.md"

# 5. Ask if the changes should be pushed to remote branch
echo
echo "Do you want to commit the changes and push the release branch and tag?"
echo "The release tag push triggers a release workflow on CI."
read -p " Enter 'yes' to continue: " -r input
if [[ "$input" != "yes" ]]; then
  echo "👌 SKIPPED."
  exit 0
fi

# 6. Push changes and trigger release on CI
echo
echo "⏳ Pushing the changes to the remote repository..."
git add "$readme" "$changelog" "$properties"
git commit --quiet --message "version: $version"
git tag "$version_tag"
git push --quiet origin HEAD "$version_tag"
echo "🎉 DONE."
echo "Create a Pull Request: $(diff_link "main" "$release_branch")"
