"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import logging, json
from elasticsearch8 import Elasticsearch

def main():
    # Setup Elasticsearch client
    try:
        client = Elasticsearch(
            'https://elasticsearch-master.elastic.svc.cluster.local:9200',
            verify_certs=False,
            basic_auth=('elastic', 'elastic')
        )
        query = {
            "query": {
                "match_all": {}
            },
            "size": 10000
        }

        index = "mastodontoots"
        response = client.search(index=index, body=query)
        return json.dumps(response['hits']['hits'])
    except Exception as e:
        logging.error(f"Error processing request: {str(e)}")
        return json.dumps({"error": str(e)})