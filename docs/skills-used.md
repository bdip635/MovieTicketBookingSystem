# Cursor Skills Used During Development

Skills are reusable instruction files from `~/.cursor/skills-cursor/` that guide agent behavior for specific tasks.

## Skills Referenced

| Skill | Path | How It Was Used |
|-------|------|-----------------|
| **create-rule** | `~/.cursor/skills-cursor/create-rule/SKILL.md` | Available for project conventions; user rules already cover commit and PR workflows |
| **create-skill** | `~/.cursor/skills-cursor/create-skill/SKILL.md` | Reference for documenting agent workflows in `AGENTS.md` |

## Skills Available but Not Required Yet

| Skill | When It May Apply |
|-------|-------------------|
| **babysit** | Keep PR merge-ready once a remote PR is open |
| **split-to-prs** | If the branch grows large and needs splitting |
| **canvas** | For visual architecture or data-heavy demo artifacts |
| **loop** | For recurring CI or test runs |

## User Rules Applied

The following user-level rules governed agent behavior throughout this project:

- Git safety protocol (no force-push, commit only when asked, HEREDOC commit messages)
- PR creation via `gh` CLI when requested
- Code principles: minimize scope, avoid over-engineering, match conventions
- Real environment: run commands and verify, do not simulate

## Adding New Skills

If new repeatable workflows emerge (e.g. seed-data generation, integration test templates), add a skill under `.cursor/skills/` or document the pattern here.
