# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This file contains important informations for AI agents like claude, chatgpt and copilot.

# *MOST IMPORTANT RULES THAT MUST BE FOLLOWED*
- *Always commit* every logical step! Don't batch unrelated changes into one commit.
- *Always rebase* the working branch onto the latest main (or master, if main doesn't exist) at the end of a task. Resolve any conflicts during the rebase.

# Project Overview

Android phone/contacts app optimized for motorcycle use with the DMD Remote 2 hardware controller. Touch input is entirely optional — the primary interface is physical button navigation.

DMD Remote 2 spec and button event reference: https://github.com/johnkeel-thork/DMD-For-Devs/blob/main/ControllerExample.java

## Architecture

The app has two distinct UI surfaces, both implemented as overlays:

1. **Incoming Call Overlay** — shown when a call arrives; accept (BUTTON 1) / decline (BUTTON 2)
2. **Sidecar Overlay** — launched from the home screen for initiating calls; two tabs:
   - *History* tab (default): incoming/outgoing calls, newest first
   - *Contacts* tab: favorites first, then alphabetical

Both overlays quit the app on call end or BUTTON 2 dismiss. There is no persistent background activity beyond call reception.

## Remote Button Mappings

| Button   | Sidecar         | Active Call     | Incoming Call |
|----------|-----------------|-----------------|---------------|
| BUTTON 1 | Initiate call   | —               | Accept        |
| BUTTON 2 | Quit app        | End call + quit | Decline       |
| UP/DOWN  | Navigate list   | —               | —             |
| LEFT/RIGHT | Switch tabs   | —               | —             |

## UI Constraints

- Text sizes: **18sp, 22sp, 28sp** only
- Font: **Apotek** — copy from `/home/sdk/androdash/app/src/main/res/font`
- All interactive elements must be usable with gloves (large hit targets)

# Keep A Changelog
Maintain a CHANGELOG.md file in every project, following the specification at:
https://raw.githubusercontent.com/olivierlacan/keep-a-changelog/refs/heads/main/CHANGELOG.md
Update it after each development task with a human-readable description of what changed.
Don't list individual commits. Skip entries for trivial or non-user-facing changes that don't affect app behavior.

# Development Journal
Maintain a file at .github/development-journal.md containing:
- Software Stack Information
- Key Decisions (context and rationale to keep in mind for future work)
- Core Features

# Git Configuration Rules
All git operations are performed on behalf of the user. Before any git operation, configure:
- user.name = c0dev0id
- user.email = sh+git@codevoid.de

Never add Co-Authored-By or any other personal attribution to commits or pull requests.

Remove all lines that contain the word "claude" from pull request and commit messages.

If a .gh_token file is present, use the token to access GitHub and read CI/CD workflow results.

# Build Constraints
Do not attempt to build Android projects locally. All builds are handled by CI/CD.
AGP cannot be accessed due to firewall restrictions. Do not try to work around this.

# Library and Framework Usage
- Always use the latest version available.
- Before implementing a feature from scratch, check whether the libraries and frameworks already in use provide built-in support for it — possibly in a different form than the user requested. If so, explain the available capabilities and let the user decide how to adjust the request.

# Code Style
- KISS — Keep it Simple, Stupid.
- Write testable code.
- Write unit tests that verify assumptions and cover edge cases.
- No database or schema migration code during development (version < 1.0.0).

# Communication Standards
- Be clear, direct, and evidence-based.
- Push back when something seems wrong or suboptimal.
- If the user uses imprecise terminology, provide the correct term.

# Finding Solutions
- Don't jump to conclusions. If there's any ambiguity, ask for clarification first.
- The obvious fix is often not the right one. Approach problems from multiple angles:
  - Consider whether a design pattern would prevent recurring issues.
  - Evaluate whether a different library, technique, or component is a better fit than working around limitations of the current approach.
  - Step back and examine the architecture — the root cause may point to a structural improvement rather than a local patch.

# About the User (Target Group Definition)
- The user is a minimalist who values performance, low latency and over feature richness.
- The user prefers clean software architecture and technical correctness and will adapt workflow or feature expectations to fit the software stack rather than accept complex code or workarounds.
- The user may not be aware of all capabilities offered by the libraries and frameworks in use.
