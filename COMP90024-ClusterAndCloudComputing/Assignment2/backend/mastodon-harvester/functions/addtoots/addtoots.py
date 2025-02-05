"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import logging, json
from flask import current_app, request
from elasticsearch8 import Elasticsearch

def main():
    client = Elasticsearch (
        'https://elasticsearch-master.elastic.svc.cluster.local:9200',
        verify_certs= False,
        basic_auth=('elastic', 'elastic')
    )

    # Retrieve data from the POST request
    toot = request.get_json(force=True)

    try:
        res = client.index(
            index='mastodontoots',
            id=toot['id'],  # Unique identifier for the document
            document={
                'content': toot['content'],
                'timestamp': toot['createdAt'],
                'tootid': toot['id']
            }
        )
    except Exception as e:
        # Log errors and continue processing
        logging.error(f"Error indexing toot {toot['id']}: {str(e)}")

    # Return success and any errors encountered
    return json.dumps({'status': 'ok'}), 200
