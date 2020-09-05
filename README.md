[![Build Status](https://travis-ci.com/switcherapi/switcher-ac.svg?branch=master)](https://travis-ci.com/switcherapi/switcher-ac)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=switcherapi_switcher-ac&metric=alert_status)](https://sonarcloud.io/dashboard?id=switcherapi_switcher-ac)
[![Known Vulnerabilities](https://snyk.io/test/github/petruki/switcher-ac/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/petruki/switcher-ac?targetFile=pom.xml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Slack: Switcher-HQ](https://img.shields.io/badge/slack-@switcher/hq-blue.svg?logo=slack)](https://switcher-hq.slack.com/)

# About
Switcher Account Control is a simple API written in Java/Spring Boot responsible for managing features access to the Switcher API showcase environment.

https://github.com/switcherapi/switcher-api

It includes:
- Managing access to Switcher API
- Managing plans and its limitations
- Features availability

# Usage

- **API Check** - /api/check [GET]
Verifies if API is online.
No authorization required.

### Admin
Admin authorization token is required.

- **List plans** - /admin/plan/v1/list [GET]

Return all registered plans

- **Get plan** - /admin/plan/v1/get?plan={PLAN_NAME} [GET]

Return a specific plan

- **Create plan** - /admin/plan/v1 [POST]

Create new plan
```json
{
    "name": "BASIC",
    "maxDomains": 1,
    "maxGroups": 5,
    "maxSwitchers": 20,
    "maxComponents": 5,
    "maxEnvironments": 3,
    "maxTeams": 5,
    "maxDailyExecution": 500
}
```

- **Delete plan** - /admin/plan/v1?plan={PLAN_NAME} [POST]

Delete a specific plan

- **Change account plan** - /admin/account/v1/change/:externalId?plan={PLAN_NAME} [PATCH]

Change account plan to the one especified

- **Reset account execution daily limit** - /admin/account/v1/reset/:externalId [PATCH]

Reset the number of executions for a specific account

### Switcher
Switcher authorization token is required.

- **Create account** - /switcher/v1/create [POST]

Create new account with defatul plan
```json
{
    "value": "{externalId}"
}
```

- **Remove account** - /switcher/v1/remove [POST]

Remove account
```json
{
    "value": "{externalId}"
}
```

- **Feature validation** - /switcher/v1/validate [POST]

Execute the validation based on the name of the feature and account externalId.
  - **value**: string containing one of the features: domain, group, switcher, component, environment, team, metrics, history
  - **numeric**: optional string value, not used for metrics and history
```json
{
    "value": "{featureName}#{externalId}",
    "numeric": "0"
}
```

- **Usage validation** - /switcher/v1/execution?value={externalId} [POST]

Verify account usage