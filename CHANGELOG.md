# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- Project scaffolding: Gradle build system, CI/CD workflow (lint, build, sign, nightly release)
- Android manifest with all required permissions and InCallService declaration
- Apotek font, color palette, dimensions, and Material3 theme
- Stub classes for PermissionActivity, MainActivity, and PhoneCallService
- Permission gate: requests contacts, call log, phone, phone state, overlay, and default dialer in sequence before showing any UI — all must be granted to proceed
