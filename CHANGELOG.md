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
