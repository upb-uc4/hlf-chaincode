# hyperledger_chaincode

## Repository to provide designated chaincode for UC4 project

Find our API - documentation for the different contracts here: https://github.com/upb-uc4/api/tree/develop/hlf/chaincode

## How to install our chaincode

Download the necessary assets for your usecase
```
CHAINCODE_VERSION_PATH="latest/download"
# CHAINCODE_VERSION_PATH="download/v0.12.2"

mkdir -p chaincode/assets
mkdir -p chaincode/UC4-chaincode
echo "################Download chaincode##################"
wget -q -c https://github.com/upb-uc4/hlf-chaincode/releases/"$CHAINCODE_VERSION_PATH"/UC4-chaincode.tar.gz -O - | tar -xz -C "./chaincode/UC4-chaincode"
echo "################Download assets#####################"
wget -q -c https://github.com/upb-uc4/hlf-chaincode/releases/"$CHAINCODE_VERSION_PATH"/collections_config.json -O "./chaincode/assets/collections_config_dev.json"
```

Read the version and safe locally
``` 
echo "######### Read Chaincode version from file ########"
jarPath=./chaincode/UC4-chaincode/UC4-chaincode/UC4-chaincode*.jar
unzip -c $jarPath META-INF/MANIFEST.MF | grep 'Implementation-Version' | cut -d ':' -f2 | tr -d ' ' | tr -d '\r' | tr -d '\n'>./chaincode/assets/testversion.txt
echo "###### add access rights to file for everyone #########"
chmod 777 ./chaincode/assets/testversion.txt
export CHAINCODE_VERSION=$(cat ./chaincode/assets/testversion.txt)
echo "CHAINCODE VERSION:: $CHAINCODE_VERSION"
```

Run your peer commands
```
echo "############################################################################################"
echo "READ CHAINCODE VERSION"
echo "############################################################################################"
export CHAINCODE_VERSION=$(cat chaincode/assets/testversion.txt)
echo "CHAINCODE VERSION:: $CHAINCODE_VERSION"

echo "############################################################################################"
echo "PACKAGE CHAINCODE"
echo "############################################################################################"
peer lifecycle chaincode package mycc.tar.gz \
    --path UC4-chaincode \
    --lang java \
    --label $CHAINCODE_NAME

echo "############################################################################################"
echo "INSTALL CHAINCODE"
echo "############################################################################################"
# chaincode points to the chaincode directory in the UC4 repo
peer lifecycle chaincode install mycc.tar.gz
export CHAINCODE_ID="$(peer lifecycle chaincode queryinstalled | sed -n '1!p' | sed 's/.*Package ID: \(.*\), Label.*/\1/')"

echo "############################################################################################"
echo "APPROVE CHAINCODE $CHAINCODE_ID"
echo "############################################################################################"
peer lifecycle chaincode approveformyorg \
  --orderer orderer:7050 \
  --channelID "$CHANNEL_NAME" \
  --name "$CHAINCODE_NAME" \
  --version $CHAINCODE_VERSION \
  --package-id "$CHAINCODE_ID" \
  --sequence 1 \
  --collections-config chaincode/assets/collections_config_dev.json
  
echo "############################################################################################"
echo "CHECK COMMIT READINESS CHAINCODE $CHAINCODE_ID"
echo "############################################################################################"
# check approved
peer lifecycle chaincode checkcommitreadiness \
  --channelID "$CHANNEL_NAME" \
  --name "$CHAINCODE_NAME" \
  --version $CHAINCODE_VERSION \
  --sequence 1 \
  --output json \
  --collections-config chaincode/assets/collections_config_dev.json

echo "############################################################################################"
echo "COMMIT CHAINCODE $CHAINCODE_ID"
echo "############################################################################################"
peer lifecycle chaincode commit \
    --orderer orderer:7050 \
    --channelID "$CHANNEL_NAME" \
    --name "$CHAINCODE_NAME" \
    --version $CHAINCODE_VERSION \
    --sequence 1 \
    --peerAddresses peer:7051 \
    --collections-config chaincode/assets/collections_config_dev.json

echo "############################################################################################"
echo "QUERY COMMITTED CHAINCODE"
echo "############################################################################################"
    
peer lifecycle chaincode querycommitted \
  --channelID "$CHANNEL_NAME" \
  --name "$CHAINCODE_NAME" \
  --output json

echo "############################################################################################"
echo "#                                CHAINCODE  INSTALLED                                      #"
echo "#                                  READY FOR ACTION                                        #"
echo "############################################################################################"
```

## How to run local chaincode tests 
clone the chaincode repository: hlf-chaincode \
the chaincode repository requires java jdk1.8, make sure you configure your IDE with this version
```
open folder: hlf-chaincode/UC4-chaincode
open this project in this folder with an IDE for example IntelliJ IDEA
to run the tests you can right-click on the project and select: Run 'Tests in 'UC4-Chaincode.test''

the test scripts can be found in: src\test\java\de\upb\cs\uc4\chaincode
the test configurations can be found in: src\test\resources\test_configs
```
To add a test you can insert a new test in json format like he following example which can be found in 'hlf-chaincode\UC4-chaincode\src\test\resources\test_configs\matriculation_data_contract\AddMatriculationDataTestIO.json':
```
 {
    "name": "addExistingMatriculationData",                                     # Name of the test
    "type": "addMatriculationData_FAILURE",                                     # identify the structure of the test - the assigned value has to match a value in the test-code
    "setup": {                                                                  # the setup for the test that simulates the initial ledger state
      "matriculationDataContract": [
        "0000001",
        {
          "enrollmentId": "0000001",
          "matriculationStatus": [
            {
              "fieldOfStudy": "Computer Science",
              "semesters": [
                "WS2018/19",
                "SS2019",
                "WS2019/20",
                "SS2020"
              ]
            }
          ]
        }
      ],
      "examinationRegulationContract": [
        "Computer Science",
        {
          "name": "Computer Science",
          "active": true,
          "modules": []
        }
      ]
    },
    "input": [                                                                    # the input for the test for example to add a new semester for a student
      {
        "enrollmentId": "0000001",
        "matriculationStatus": [
          {
            "fieldOfStudy": "Computer Science",
            "semesters": [
              "WS2018/19",
              "SS2019",
              "WS2019/20",
              "SS2020"
            ]
          }
        ]
      }
    ],
    "compare": [                                                                   # this is the expected outcome of the test                                                              
      {
        "type": "HLConflict",
        "title": "There is already a MatriculationData for the given enrollmentId"
      }
    ]
  },
  {
    "name": "addEmptyEnrollmentIdMatriculationData",
    "type": "addMatriculationData_FAILURE",
    "setup": {
      "examinationRegulationContract": [
        "Computer Science",
        {
          "name": "Computer Science",
          "active": true,
          "modules": []
        }
      ]
    }
