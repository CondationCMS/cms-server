# CondationCMS Security Response Process

## Purpose

This document describes the process used by CondationCMS maintainers to receive, assess, resolve, and disclose security vulnerabilities.

This is a lightweight project process and does not constitute a commercial service-level agreement.

## Responsibility

The CondationCMS project maintainer is responsible for:

* monitoring private vulnerability reports,
* coordinating technical analysis,
* determining the affected components and versions,
* preparing fixes and mitigations,
* publishing security advisories and releases,
* documenting relevant decisions.

The current security contact is:

* Email: [security@condation.com](mailto:security@condation.com)
* GitHub organization: CondationCMS

## Intake

When a report is received:

1. Confirm that the report was received.
2. Keep technical details private until coordinated disclosure.
3. Create or use a private GitHub Security Advisory.
4. Record the affected repository, version, reporter, date, and current status.
5. Request missing reproduction information where necessary.

Do not copy confidential vulnerability details into public issues, public pull requests, or public CI logs.

## Initial Assessment

Determine:

* whether the issue can be reproduced,
* which versions are affected,
* whether default installations are affected,
* whether authentication is required,
* whether remote exploitation is possible,
* what data or system functions may be affected,
* whether exploitation is known or suspected,
* whether a vulnerable third-party dependency is involved,
* whether a workaround is available.

## Severity

Use the following initial categories:

### Critical

Likely remote compromise, authentication bypass, arbitrary code execution, or widespread active exploitation.

### High

Significant confidentiality, integrity, or availability impact under realistic conditions.

### Medium

Limited impact, substantial prerequisites, or effective existing mitigations.

### Low

Minor impact, hard-to-exploit weakness, or security hardening issue.

A CVSS score may be added when useful, but the remediation decision should also consider the actual CondationCMS deployment model.

## Response Targets

These are project goals rather than guaranteed service levels:

| Severity | Initial assessment |   Target for mitigation or fix |
| -------- | -----------------: | -----------------------------: |
| Critical |      1 working day | As soon as reasonably possible |
| High     |     3 working days |          Next security release |
| Medium   |    10 working days |    Planned maintenance release |
| Low      |     When scheduled |     Future maintenance release |

## Remediation

Depending on the issue:

* prepare a code fix,
* update or replace an affected dependency,
* disable an unsafe feature,
* provide configuration-based mitigation,
* improve validation or secure defaults,
* add automated regression tests,
* update documentation.

Fixes should be developed privately when early publication would increase risk.

## Release

For confirmed vulnerabilities:

1. Prepare a patched release.
2. Generate release artifacts and an SBOM.
3. Verify that the vulnerability is no longer reproducible.
4. Verify that the fix does not introduce major regressions.
5. Publish a GitHub Security Advisory.
6. Publish release notes with affected and fixed versions.
7. Describe available mitigations for users unable to update immediately.

## Advisory Content

A published advisory should contain:

* a clear title,
* affected products and versions,
* fixed versions,
* impact,
* severity,
* mitigation or workaround,
* upgrade instructions,
* relevant credits,
* CVE identifier where applicable.

Avoid publishing unnecessary exploit details before users have had a reasonable opportunity to update.

## Dependency Vulnerabilities

When a dependency vulnerability is discovered:

1. Verify whether the vulnerable functionality is actually included.
2. Determine whether CondationCMS uses the affected code path.
3. Update, replace, or remove the dependency when necessary.
4. Document why the project is affected or not affected.
5. Regenerate the SBOM after updating the dependency.

A vulnerability in a dependency is not automatically exploitable in CondationCMS, but it must not be ignored without assessment.

## Records

For every confirmed report, retain:

* date received,
* affected versions,
* severity assessment,
* analysis notes,
* remediation decision,
* relevant commits and pull requests,
* release containing the fix,
* advisory publication date.

Do not retain unnecessary personal information or sensitive production data.

## CRA Escalation

If CondationCMS is provided as part of a commercial activity and becomes subject to the Cyber Resilience Act, immediately assess whether the issue is:

* an actively exploited vulnerability, or
* a severe security incident affecting the product.

Where legally required, begin the applicable regulatory reporting process without waiting for the public security advisory.
