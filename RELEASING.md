# Releasing

1. Run the script `release.sh` with the desired version as a parameter:
   ```bash
   ./release.sh [version]
   ````

2. The script will ask you if you want to push the release branch and create a release tag.

3. Ensure `CHANGELOG.md` looks good and is ready to be published.

4. Type "yes" to the console if everything is okay.
   Tag push triggers a GitHub Actions workflow,
   which publishes the release artifacts to Maven Central and creates a GitHub release.

5. Click the link displayed in the console to create a Pull Request for release branch.

6. Merge the Pull Request as soon as the "Check" workflow succeeds.
   It is recommended to use fast-forward merge to merge release branches.

## Manual release preparation

To prepare a release manually, follow the steps the script does:

1. Ensure the repository is up to date, and the main branch is checked out.

2. Create the release branch with the name `release/[version]`.

3. Update the version in `gradle.properties` and `README.md` ("Usage" section) with the version to be released.

4. Update the `CHANGELOG.md`:
   1. Replace `Unreleased` section with the release version
   2. Add a link to the diff between the previous and the new version
   3. Add a new empty `Unreleased` section on the top

5. Commit the changes, create a tag on the latest commit, and push it to the remote repository.
   The tag should follow the format `v[version]`.

6. Create a Pull Request for the release branch and merge it.
