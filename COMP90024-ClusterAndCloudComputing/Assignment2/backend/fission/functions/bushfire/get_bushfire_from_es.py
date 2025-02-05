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
from string import Template


def main():
    # connect to ES
    es = Elasticsearch (
        'https://elasticsearch-master.elastic.svc.cluster.local:9200',
        verify_certs= False,
        basic_auth=('elastic', 'elastic')
    )
    index_name='bushfire1'
    expr ='''{"match_all": {}}'''
    try: 
        bushfire_list=[]
        res= es.search(
            index=index_name,
            query=json.loads(expr),
            size=5000
            )
        for hit in res['hits']['hits']:
            bushfire_list.append(hit['_source'])
        return json.dumps(bushfire_list)
    except Exception as e: 
        error_message = {"error": str(e)}
        return json.dumps(error_message)
    
    

if __name__ == "__main__":
    main()
    
