"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

from elasticsearch8 import Elasticsearch, exceptions as es_exceptions
import csv


def create_index(es, index_name, settings):
    """
    Creates an Elasticsearch index with specified settings.
    Args:
        index_name: Name of the Elasticsearch index.
        settings: Settings for the Elasticsearch index.
        es_host: Hostname or IP address of the Elasticsearch server. Default is 'elasticsearch-master.elastic.svc.cluster.local'.
        es_port: Port number of the Elasticsearch server. Default is 9200.
    """
    try:
        # Check if index already exists
        if es.indices.exists(index=index_name):
            print(f"Index '{index_name}' already exists. Skipping creation.")
            return
        # Create the index with specified mappings
        es.indices.create(index=index_name, body=settings)
        print(f"Successfully created ElasticSearch index {index_name}")
    except es_exceptions.ConnectionError as e:
        print(f'Failed to create index {index_name}: {e}')



def upload_csv_to_elasticsearch(es, csv_file_path, index_name, mappings):
    """
    Uploads data from a CSV file to Elasticsearch.

    Args:
        csv_file_path: Path to the CSV file.
        index_name: Name of the Elasticsearch index.
        es_host: Hostname or IP address of the Elasticsearch server. Default is 'elasticsearch-master.elastic.svc.cluster.local'.
        es_port: Port number of the Elasticsearch server. Default is 9200.
    """
    try:
        # Open CSV file and upload data to Elasticsearch
        with open(csv_file_path, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                # Handle null values
                for key, value in row.items():
                    if value is None or value.lower() == 'null':
                        if isinstance(mappings.get(key), dict) and mappings[key]['type'] == 'string':
                            row[key] = ""
                        elif isinstance(mappings.get(key), dict) and mappings[key]['type'] in ['integer', 'double']:
                            row[key] = 0
                # Upload each row as a document to Elasticsearch
                es.index(index=index_name, body=row)
        print(f"Successfully uploaded {csv_file_path} to index {index_name}")
    except es_exceptions.ConnectionError as e:
        print(f'Failed to upload file to index {index_name}: {e}')