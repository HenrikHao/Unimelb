# Mastodon Harvester Deployment Guide
This guide will help you deploy a Mastodon harvester on Kubernetes using Fission and Kafka.

## Create the Secret in Kubernetes
First, apply for an API key on [aus.social](https://aus.social/home). Store the key in Kubernetes as a secret:

```sh
kubectl create secret generic mastodon-secret --from-literal=api_key='your_api_key_here'
```

To deploy the mastodon harvester on kubernetes
```kubectl apply -f deployment.yaml```

## Deploy the Mastodon Harvester
Deploy the Mastodon harvester using the provided `deployment.yaml`:
```sh
kubectl apply -f deployment.yaml
```

## Adding Index for Toots
Create an Elasticsearch index for storing Mastodon toots:
```sh
curl -XPUT -k 'https://127.0.0.1:9200/mastodontoots' \
   --user 'elastic:elastic' \
   --header 'Content-Type: application/json' \
   --data '{
    "settings": {
        "index": {
            "number_of_shards": 3,
            "number_of_replicas": 1
        }
    },
    "mappings": {
        "properties": {
            "tootid": {
                "type": "keyword"
            },
            "timestamp": {
                "type": "date"
            },
            "content": {
                "type": "text"
            }
        }
    }
}' | jq '.'

```

## Create Kafka Topics
Create the necessary Kafka topics for the Mastodon harvester:
```sh
kubectl apply -f topics/mastodon-topic.yaml --namespace kafka
kubectl apply -f topics/processed-post.yaml --namespace kafka
kubectl apply -f topics/errors.yaml --namespace kafka
```

## Deploy the Processor
Create the processor function using Fission:
```sh
fission function create --name tprocessor --spec --env nodejs --code tprocessor.js
```

## Add MQ Trigger
```sh 
fission mqtrigger create --name toot-processing \
    --spec \
    --function tprocessor \
    --mqtype kafka \
    --mqtkind keda \
    --topic mastodon-topic \
    --resptopic processed-post \
    --errortopic errors \
    --maxretries 3 \
    --metadata bootstrapServers=my-cluster-kafka-bootstrap.kafka.svc:9092 \
    --metadata consumerGroup=my-group \
    --cooldownperiod=30 \
    --pollinginterval=5
```

## Deploy Add Toots Function
Make the build script executable
```sh
chmod +x functions/addtoots/build.sh
```
Package and create the addtoots function:
```
cd functions/addtoots
zip -r addtoots.zip .
mv addtoots.zip ../

fission package create --sourcearchive ./functions/addtoots.zip \
    --spec \
    --env python \
    --name addtoots \
    --buildcmd './build.sh'
    
fission fn create --name addtoots \
    --spec \
    --pkg addtoots \
    --env python \
    --entrypoint "addtoots.main"
```
Create the MQ trigger for the addtoots function:
```sh
fission mqtrigger create --name add-toots \
    --spec \
    --function addtoots \
    --mqtype kafka \
    --mqtkind keda \
    --topic processed-post \
    --errortopic errors \
    --maxretries 3 \
    --metadata bootstrapServers=my-cluster-kafka-bootstrap.kafka.svc:9092 \
    --metadata consumerGroup=my-group \
    --cooldownperiod=30 \
    --pollinginterval=5
```

## Apply Fission Specifications
Apply the Fission specifications:
```sh
fission spec apply --specdir specs --wait
```

## Check Processor Logs
Check the logs of the processor function:
```sh
fission function logs --name tprocessor --follow
```

## Check Add Toots Logs
Check the logs of the `addtoots` function:
```sh
fission function logs --name addtoots --follow
```


## Cleanup
If needed, delete the addtoots function and associated resources:
```sh
fission mqtrigger delete --name add-toots
fission function delete --name addtoots
fission package delete --name addtoots
```
