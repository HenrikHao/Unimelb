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
from string import Template


days_expr= Template('''{
                           "range": {
                               "ignition_date": {
                                   "gte": "${year}-01-01",
                                   "lte": "${year}-12-31"
                               }
                           }
                       }''')

fire_expr= Template('''{
                      "bool": {
                          "must": [
                              {
                                  "match": {
                                      "fire_type": "${fire_type}"
                                  }
                              },
                              {
                                  "range": {
                                    "ignition_date": {
                                        "gte": "${year}-01-01",
                                        "lte": "${year}-12-31"
                               }
                                      
                                  }
                              }
                          ]
                      }
                  }''')




def main():
    try:
        date= request.headers['X-Fission-Params-Date']
        year = date[:4]
    except KeyError:
         date= None

    try:
        fire_type= request.headers['X-Fission-Params-Type']
    except KeyError:
         fire_type= None

    if fire_type is None:
      expr= days_expr.substitute(year=year)
    else:
      expr= fire_expr.substitute(year=year, fire_type=fire_type)


    # connect to ES
    es = Elasticsearch (
        'https://elasticsearch-master.elastic.svc.cluster.local:9200',
        verify_certs= False,
        basic_auth=('elastic', 'elastic')
    )
    index_name='allfire'
    try: 
        fire_list=[]
        res= es.search(
            index=index_name,
            query=json.loads(expr),
            size=5000
            )
        for hit in res['hits']['hits']:
            fire_list.append(hit['_source'])
        return json.dumps(fire_list)
    except Exception as e: 
        error_message = {"error": str(e)}
        return json.dumps(error_message)
    
    

if __name__ == "__main__":
    main()
    