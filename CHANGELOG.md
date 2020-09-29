# [v1.0.1](https://github.com/upb-uc4/hyperledger_chaincode/compare/v1.0.0...v1.0.1) (2020-09-29)

## Feature
- add certificate contract
- add certificate contract tests

## Refactor
- refactor matriculation-data contract tests
- update matriculation-data contract transaction documentation
- out-source errors to utilities



# [v1.0.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.8.0...v1.0.0) (2020-09-28)

## Feature
-

## Bug Fixes
-

## Refactor
- pseudonymize MatriculationData by replacing first name, last name, and birth date by an enrollment-ID
- move MatriculationData from private-data collection to contract-wide ledger
- move transaction arguments from transient-data field to arguments


## Usability
-



# [v0.8.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.7.2...v0.8.0) (2020-09-14)

## Feature
-

## Bug Fixes
- fix tests not throwing error when querying ```getPrivateDataUTF8``` for empty string [#25](https://github.com/upb-uc4/hlf-chaincode/pull/25)

## Refactor
- 


## Usability
-



# [v0.7.2](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.7.1...v0.7.2) (2020-09-9)

## Feature
- 

## Bug Fixes
- split up *collections_config* for dev/production network [#21](https://github.com/upb-uc4/hlf-chaincode/pull/21)

## Refactor
- refactor ```addEntryToMatriculation``` to take list of entries [#20](https://github.com/upb-uc4/hlf-chaincode/pull/20)
- refactor error format to conform to api [#37](https://github.com/upb-uc4/api/pull/37), [#39](https://github.com/upb-uc4/api/pull/39)


## Usability
-


# [v0.7.1](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.7.0...v0.7.1) (2020-09-7)

## Feature
- move sensitive data to transient data field and store data in private data collection [#17](https://github.com/upb-uc4/hlf-chaincode/pull/17)

## Bug Fixes
-

## Refactor
- refactor tests for private data transactions


## Usability
-


# [v0.7.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.6.0.1...v0.7.0) (2020-08-31)

## Feature
-

## Bug Fixes
-

## Refactor
- add transaction comments
- establish consistency among invalid parameter error reasons
- establish consistency for code style (if statements)
- establish consistency between variable names and java naming convention
- remove unused logger
- remove unnecessary conditions


## Usability
-


# [v0.6.0](https://github.com/upb-uc4/hyperledger_chaincode/compare/v0.5.0...v0.6.0) (2020-08-19)

## Feature
- add licence
- split up errors to be unambiguous
- add tests

## Bug Fixes
- prevent invalid paramters from appearing multiple times in the same error

## Refactor
-

## Usability
-


# v0.5.0 (2020-08-11)

## Feature
- add UC4.MatriculationData chaincode
  - add addMatriculationData transaction
  - add updateMatriculationData transaction
  - add getMatriculationData transaction
  - add addEntryToMatriculationData transaction
- add GenericError
- add UC4.MatriculationData tests

## Bug Fixes
- 

## Refactor
- rework DetailedError to conform to API specification
- rework MatriculationData to conform to API specification
  - rework SubjectMatriculation
  - delete MatriculationInterval
- rework test format to extensively outsource JSON-IO

### Usability
- 
