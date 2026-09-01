# Affiliate Agent

This repository contains the source code and technical resources for Affiliate Agent.

Affiliate Agent is designed as an automated content workflow in which:

1. an AI agent researches a product,
2. relevant information is selected,
3. social-media content and media are prepared,
4. the user reviews and explicitly approves the content,
5. a job is created for the local execution worker,
6. the worker performs privileged actions such as affiliate-link generation and social-network publication.

## Architecture

The project consists of:

* AI Agent
* Cloudflare-based Job API
* D1 database
* R2 media storage
* Kotlin/Spring WebFlux Local Worker
* Social-network clients

The AI agent and the Local Worker communicate through the Job API.

Credentials for privileged services are kept in the Local Worker and are not intended to be stored in job payloads.

## Repository purpose

This repository may contain:

* application source code
* configuration templates
* documentation
* legal information for connected API integrations
* tests and development resources

Secrets, private API keys and production credentials must not be committed to this repository.

## Current integrations

The project is designed to support multiple social-network integrations through a common publishing interface.

Individual integrations may be at different development stages.

## Documentation

See the project documentation and source code for technical details.

## Legal

* [Privacy Policy](privacy)
* [Terms of Service](terms)

## Contact

[REPLACE WITH YOUR CONTACT INFORMATION]
