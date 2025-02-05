"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import geopandas as gpd
import pandas as pd
import json
from shapely.geometry import mapping
from elasticsearch import Elasticsearch
import warnings

def preprocess_dataset(path):
    original_bushfire = gpd.read_file(path)
    bushfire = original_bushfire[['ignition_date', 'fire_type', 'area_ha', 'state', 'geometry']]
    bushfire = bushfire[(bushfire['ignition_date'].dt.year >= 2013) & (bushfire['ignition_date'].dt.year <= 2021)]
    bushfire['ignition_date'] = bushfire['ignition_date'].dt.strftime('%Y-%m-%d')
    bushfire = bushfire.dropna()
    bushfire = bushfire[bushfire['fire_type']=='Bushfire']
    bushfire = bushfire[bushfire['state'] == 'NSW (New South Wales)']
    bushfire = bushfire[['ignition_date', 'area_ha', 'state',  'geometry']]
    bushfire.to_csv("elasticsearch/dataset/bushfire_files/filtered_bushfire.csv", index=False)
    bushfire_csv = pd.read_csv("elasticsearch/dataset/bushfire_files/filtered_bushfire.csv")

    return bushfire_csv


def create_index(index_name, index_settings):
    warnings.filterwarnings("ignore")
    es=Elasticsearch("https://127.0.0.1:9200", http_auth=('elastic','elastic'), verify_certs=False)

    # check if this index has existed
    if es.indices.exists(index=index_name):
            print(f"Index '{index_name}' already exists. Skipping creation.")
            return
    es.indices.create(index=index_name, body=index_settings)
    print(f'Successfully created ElasticSearch index {index_name}')
    return 



def main():
    es=Elasticsearch("https://127.0.0.1:9200", verify_certs= False,
        ssl_show_warn= False,
        basic_auth=('elastic', 'elastic')
)
    index_name = 'bushfire1'
    index_settings = {
        "settings": {
            "index": {
                "number_of_shards": 3,
                "number_of_replicas": 1
            }
        },
        "mappings": {
            "properties": {
                "ignition_date": {
                    "type": "keyword"
                },
                "area_ha": {
                    "type": "integer"
                },
                "state": {
                    "type": "text"
                },
                "geometry": {
                    "type": "geo_shape"
                },
                
            }
        }
    }
    ## create bushfire index
    create_index(index_name, index_settings)
    # insert into ES
    bushfire = preprocess_dataset('elasticsearch/dataset/bushfire_files/Historical_Bushfire_Boundaries.geojson')

    print('start inserting into es')
    for _, row in bushfire.iterrows():
        record = row.to_dict()
        es.index(index=index_name, body=record)
    print('Finished inserting all documents')
    return 


if __name__ == "__main__":
    main()

