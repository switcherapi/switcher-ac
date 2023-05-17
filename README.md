***

<div align="center">
<b>Switcher Account Control</b><br>
Account Managing Service for the cloud-base API
</div>

<div align="center">

[![Master CI](https://github.com/switcherapi/switcher-ac/actions/workflows/master.yml/badge.svg)](https://github.com/switcherapi/switcher-ac/actions/workflows/master.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=switcherapi_switcher-ac&metric=alert_status)](https://sonarcloud.io/dashboard?id=switcherapi_switcher-ac)
[![Known Vulnerabilities](https://snyk.io/test/github/switcherapi/switcher-ac/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/switcherapi/switcher-ac?targetFile=pom.xml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Docker Hub](https://img.shields.io/docker/pulls/trackerforce/switcher-ac.svg)](https://hub.docker.com/r/trackerforce/switcher-ac)
[![Slack: Switcher-HQ](https://img.shields.io/badge/slack-@switcher/hq-blue.svg?logo=slack)](https://switcher-hq.slack.com/)

</div>

***

# About
Switcher Account Control is a simple API written in Java/SpringBoot responsible for managing feature accesses to the Switcher API cloud version.

- Managing access to Switcher API
- Managing plans & limitations
- Features availability
- Admin resources accessible via GitHub OAuth

# Configuration
Switcher AC integrates with other services to run. 

- MongoDB
- GitHub OAuth
- Switcher API

# Running locally

## Maven SpringBoot

1. Supply file based on the production context a new one named: resources/application-local.properties
2. Run ``mvn clean install spring-boot:run -Plocal``

## With Docker-Compose

1. Configure environment variables at docker-env/.env
2. Define volume for Switcher API snapshots and modify docker-compose volume device parameter.
3. Run docker-compose

```
docker-compose --env-file ./docker-env/.env up
```

# Usage

- **API Check** - /api/check [GET]<br /> 
Verifies if API is online.<br /> 
No authorization required.<br /> 


- **Swagger Definition** - /v3/api-docs [GET]<br /> 
Retrieve API swagger definition<br />


- **Swagger UI** - /swagger-ui.html<br /> 
Access Swagger UI.<br /> 
Use /admin/auth/github to generate API access token.<br />


- **Actuator** - /actuator [GET]<br />
Retrieve actuator resources available.<br />
Requires authentication as Admin.<br />


### Admin
- **Login with GitHub** - /admin/auth/github?code=GIT_CODE [POST]

After running the GitHub OAuth callback function via: https://github.com/login/oauth/authorize?client_id=APP_ID
the GIT_CODE will be given to access this resource and eventually proceed with the internal authentication.

- **Refresh JWT** - /admin/auth/refresh?refreshToken=REFRESH_TOKEN [POST]

When authenticating it will be given a pair of tokens. The refresh token will be used to re-generate a new pair once the access token has expired.

- **List plans** - /admin/v1/plan/list [GET]

Return all registered plans.

- **Get plan** - /admin/plan/v1/get?plan={PLAN_NAME} [GET]

Return a specific plan.

- **Create plan** - /admin/v1/plan [POST]

Create new plan.

- **Delete plan** - /admin/v1/plan?plan={PLAN_NAME} [POST]

Delete a specific plan.

- **Change account plan** - /admin/v1/account/change/:externalId?plan={PLAN_NAME} [PATCH]

Change account plan to the one specified.

### Switcher
Switcher authorization token is required.

- **Create account** - /switcher/v1/create [POST]

Create new account with defatul plan.
```json
{
    "value": "{externalId}"
}
```

- **Remove account** - /switcher/v1/remove [POST]

Remove account.
```json
{
    "value": "{externalId}"
}
```

- **Feature validation** - /switcher/v1/validate [POST]

Execute the validation based on the name of the feature and account externalId.
```json
{
    "payload": "{ \"feature\": \"FEATURE_NAME\", \"owner\": \"EXTERNAL_ID\", \"total\": NUM_VALUE }"
}
```

- **Rate Limiter** - /switcher/v1/limiter?value=externalId [GET]

Return rate limit allowed for an account externalId.

- **Verify** - /switcher/v1/verify [GET]

Return code verification used to verify deployment ownership.