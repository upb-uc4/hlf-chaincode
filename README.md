# hyperledger_chaincode

## Repository to provide designated chaincode for UC4 project

Find our API - documentation for the different contracts here: https://github.com/upb-uc4/api/tree/develop/hlf/chaincode

## How to install our chaincode

download the necessary assets for your usecase
```
CHAINCODE_VERSION_PATH="latest/download"
# CHAINCODE_VERSION_PATH="download/v0.12.2"
wget -q -c https://github.com/upb-uc4/hlf-chaincode/releases/"$CHAINCODE_VERSION_PATH"/UC4-chaincode.tar.gz -O - | tar -xz -C "./chaincode/UC4-chaincode"
wget -q -c https://github.com/upb-uc4/hlf-chaincode/releases/"$CHAINCODE_VERSION_PATH"/collections_config.json -O "./chaincode/assets/collections_config_dev.json"
```
And run your peer commands
```
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
  --version 1.0 \
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
  --version 1.0 \
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
    --version 1.0 \
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
