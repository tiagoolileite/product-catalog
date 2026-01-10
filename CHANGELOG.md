# Changelog

All notable changes to this project will be documented in this file.

This project adheres to the principles of
- "Keep a Changelog" (https://keepachangelog.com/en/1.0.0/)
- Semantic Versioning (SemVer) — https://semver.org/

Structure and conventions
- Use headings for versions. The top-most section should be `## [Unreleased]`.
- Group changes under the following categories where appropriate:
  - Added: for new features
  - Changed: for changes in existing functionality
  - Deprecated: for soon-to-be removed features
  - Removed: for removed features
  - Fixed: for any bug fixes
  - Security: in case of vulnerabilities
- Each entry should reference issue numbers and PRs when possible (e.g. `(#123)`), and include a concise explanation.
- Prefer short, imperative sentences. One item per line.

Example template

```md
## [Unreleased]
### Added
- Short description of the new feature. (PR #123)

### Changed
- Short description of changes. (PR #124)

### Fixed
- Bugfix description. (Issue #125)
```

Release headings
- Use an ordered history: newest releases at the top.
- For released versions include the date in `YYYY-MM-DD` format.
- Example: `## [0.1.0] - 2026-01-10`

Links and compare URLs
- Optionally add a link section at the bottom that maps versions to GitHub compare/release pages:

```md
[Unreleased]: https://github.com/<OWNER>/<REPO>/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/<OWNER>/<REPO>/releases/tag/v0.1.0
```

Keep a single source of truth
- The `CHANGELOG.md` should be the canonical human-readable history.
- If you use automated release notes generation, ensure the automation updates this file or that a generated release notes page is linked from the `CHANGELOG.md`.

Release process (recommended)
1. Prepare changes on a feature branch. Use conventional commits or include a short changelog entry in the PR description.
2. Open a pull request. In the PR description include the changelog bullet(s) that should appear in `Unreleased` (or add a draft entry to `CHANGELOG.md`).
3. After PR approval and merge to `main`:
   - Update the `CHANGELOG.md`: move the `Unreleased` bullets into a new release section `## [X.Y.Z] - YYYY-MM-DD`.
   - Update the project version (see Maven instructions below).
   - Tag the release and push the tag.

Git commands (examples)

```bash
# Update local main and create a new annotated tag
git checkout main
git pull origin main
# After updating CHANGELOG.md and setting project version
git add CHANGELOG.md pom.xml
git commit -m "chore(release): v0.1.0"
git tag -a v0.1.0 -m "Release v0.1.0"
git push origin main --follow-tags
```

Maven version bump example

```bash
# set a new version
./mvnw versions:set -DnewVersion=0.1.0
# commit the change (the plugin updates pom.xml and creates a backup pom.xml. Remove or keep the backup as you prefer)
git add pom.xml
git commit -m "chore: set version to 0.1.0"
```

Automating release notes
- Consider using one of these automation patterns:
  - Conventional Commits + semantic-release (automated versioning and changelog generation)
  - GitHub Release Drafter (drafts release notes from merged PRs)
  - A GitHub Action that runs on tag push and updates `CHANGELOG.md` or publishes release notes

Example GitHub Action hints
- On `push` with `tags:` trigger, run a job to build and publish the release, or use `actions/create-release` and `actions/upload-release-asset`.
- Use `release-drafter` to keep a draft pull request that aggregates PRs into the next release notes automatically.

How to write good changelog entries
- Be concise and clear. Avoid internal implementation details that aren't useful to the user.
- Prefer the active voice and imperative mood: `Add support for X` instead of `Added support for X` (both acceptable; be consistent).
- Link to issues/PRs: `(PR #123)` or `(#123)`.
- When the change is user-facing, indicate migration steps or breaking changes.

Initial release (example)

## [Unreleased]

### Added
- Base project scaffold and Maven wrapper
- `README.md` with setup, actuator examples, database/JPA guidance, Docker and development notes
- `CHANGELOG.md` (this file)

---

## [0.1.0] - 2026-01-10

### Added
- Initial project scaffold: Spring Boot application entry point and basic resource layout
- Basic `application.properties` example for development and actuator exposure
- Basic run and build instructions in `README.md`

---

Links

Replace `<OWNER>` and `<REPO>` with your GitHub owner and repository name.

```md
[Unreleased]: https://github.com/<OWNER>/<REPO>/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/<OWNER>/<REPO>/releases/tag/v0.1.0
```

Best practices checklist for maintainers
- [ ] Use SemVer for version numbering
- [ ] Keep `Unreleased` up-to-date during PRs or add PR-level changelog bullets
- [ ] Prefer automated tooling (release-drafter/semantic-release) for consistency
- [ ] Link entries to issues/PRs for traceability
- [ ] Document breaking changes and migration notes prominently

Notes
- This `CHANGELOG.md` follows human-readable conventions from "Keep a Changelog" while allowing automated workflows to augment it.
- If you adopt a fully automated workflow (e.g., semantic-release), decide whether to keep a manually edited `CHANGELOG.md` or to generate and commit the output during release.

If you want, I can:
- add GitHub Action examples to automatically draft releases or publish changelog updates,
- enable a conventional-commits enforcement check or commit message linter,
- add a `release` script to `pom.xml` or a small shell script to automate version bump + tag + push steps.

Tell me which of these you'd like me to implement next.
