# [v0.11.3](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.11.2...v0.11.3) (2020-11-03)

## Feature
- add approval contract
- add approval contract tests


# [v0.11.2](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.11.1...v0.11.2) (2020-10-30)

## Refactor
- replace field of study enum by string, allowing for configurable fields of study


# [v0.11.1](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.11.0...v0.11.1) (2020-10-29)

## Refactor
- replace makeshift composite keys by fabric's built in composite keys


# [v0.11.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.10.1...v0.11.0) (2020-10-26)

Release (no changes to previous version)


# [v0.10.1](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.10.0...v0.10.1) (2020-10-20)

## Feature
- add version chaincode



# [v0.10.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.9.2...v0.10.0) (2020-10-19)

## Bug Fixes
- fix error related to updated fabric shim version

## Refactor
- purge courses chaincode



# [v0.9.2](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.9.1...v0.9.2) (2020-10-01)

## Bug Fixes
- fix MatriculationDataContract and CertificateContract storing data under the same key (i.e. enrollmentId)

## Refactor
- refactor tests to do common setup only once per contract



# [v0.9.1](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.9.0...v0.9.1) (2020-09-29)

## Feature
- add certificate contract
- add certificate contract tests

## Refactor
- refactor matriculation-data contract tests
- update matriculation-data contract transaction documentation
- out-source errors to utilities



# [v0.9.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.8.0...v0.9.0) (2020-09-28)


## Refactor
- pseudonymize MatriculationData by replacing first name, last name, and birth date by an enrollment-ID
- move MatriculationData from private-data collection to contract-wide ledger
- move transaction arguments from transient-data field to arguments



# [v0.8.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.7.2...v0.8.0) (2020-09-14)

## Bug Fixes
- fix tests not throwing error when querying ```getPrivateDataUTF8``` for empty string [#25](https://github.com/upb-uc4/hlf-chaincode/pull/25)



# [v0.7.2](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.7.1...v0.7.2) (2020-09-9)

## Bug Fixes
- split up *collections_config* for dev/production network [#21](https://github.com/upb-uc4/hlf-chaincode/pull/21)

## Refactor
- refactor ```addEntryToMatriculation``` to take list of entries [#20](https://github.com/upb-uc4/hlf-chaincode/pull/20)
- refactor error format to conform to api [#37](https://github.com/upb-uc4/api/pull/37), [#39](https://github.com/upb-uc4/api/pull/39)



# [v0.7.1](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.7.0...v0.7.1) (2020-09-7)

## Feature
- move sensitive data to transient data field and store data in private data collection [#17](https://github.com/upb-uc4/hlf-chaincode/pull/17)

## Refactor
- refactor tests for private data transactions



# [v0.7.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.6.0.1...v0.7.0) (2020-08-31)

## Refactor
- add transaction comments
- establish consistency among invalid parameter error reasons
- establish consistency for code style (if statements)
- establish consistency between variable names and java naming convention
- remove unused logger
- remove unnecessary conditions



# [v0.6.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.5.0...v0.6.0) (2020-08-19)

## Feature
- add licence
- split up errors to be unambiguous
- add tests

## Bug Fixes
- prevent invalid paramters from appearing multiple times in the same error



# v0.5.0 (2020-08-11)

## Feature
- add UC4.MatriculationData chaincode
  - add addMatriculationData transaction
  - add updateMatriculationData transaction
  - add getMatriculationData transaction
  - add addEntryToMatriculationData transaction
- add GenericError
- add UC4.MatriculationData tests

## Refactor
- rework DetailedError to conform to API specification
- rework MatriculationData to conform to API specification
  - rework SubjectMatriculation
  - delete MatriculationInterval
- rework test format to extensively outsource JSON-IO
