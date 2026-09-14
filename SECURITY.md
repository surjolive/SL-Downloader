# Security Policy

## Supported versions

Security fixes are applied to the current `main` branch and the latest published release when practical. Preview APKs are for testing and should not be treated as production-signed releases.

## Reporting a vulnerability

Please do not disclose security vulnerabilities in a public issue. Contact the maintainer privately through the GitHub profile:

https://github.com/surjolive

Include:

- A clear description of the issue
- Affected file, feature, or version
- Reproduction steps or a proof of concept that does not expose real personal data
- Potential impact
- A suggested mitigation, if known

Allow reasonable time for investigation and a fix before public disclosure. Do not include passwords, access tokens, cookies, private URLs, personal videos, or other secrets in reports.

## Security boundaries

SL Downloader is designed to download authorized direct media files only. It must not be used to bypass DRM, CAPTCHAs, paywalls, authentication, private-content controls, or platform restrictions.

The app should not collect credentials, steal cookies or tokens, execute downloaded files, or upload user media. Security-sensitive changes must preserve these boundaries.
