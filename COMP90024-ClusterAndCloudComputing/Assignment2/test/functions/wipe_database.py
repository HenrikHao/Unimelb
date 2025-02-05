"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

from flask import request, current_app
import requests, logging, json

def config(k): 
    with open(f'/configs/default/parameters/{k}','r') as f: 
        return f.read()
    
def main(): 
    current_app.logger.debug(f'{config("ES_URL")}/{config("ES_AIR_QUALITY_TEST_DATABASE")}')
    r = requests.delete(f'{config("ES_URL")}/{config("ES_AIR_QUALITY_TEST_DATABASE")}',
                     verify=False,
                     auth=(config("ES_USERNAME"), config("ES_PASSWORD")))
    r = requests.put(f'{config("ES_URL")}/{config("ES_AIR_QUALITY_TEST_DATABASE")}',
                     verify=False,
                     auth=(config("ES_USERNAME"), config("ES_PASSWORD")),
                     data=config("ES_AIR_QUALITY_TEST_SCHEMA"),
                     headers={"Content-Type": "application/json"})
    return r.json(), r.status_code