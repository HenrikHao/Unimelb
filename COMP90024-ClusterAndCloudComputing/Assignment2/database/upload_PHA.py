"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

from upload_csv_elasticsearch import *
import os
import csv

# Function to upload pha index from CSV file
def upload_pha(file_path):
    index_name = "pha"
    try:
        # Connect to Elasticsearch
        es = Elasticsearch (
            'https://127.0.0.1:9200',
            verify_certs= False,
            ssl_show_warn= False,
            basic_auth=('elastic', 'elastic')
        )
        with open(file_path, 'r', newline='', encoding='utf-8') as csvfile:
            csvreader = csv.DictReader(csvfile)
            for row in csvreader:
                pha_code = row['pha']
                state = row['state']
                if not es.exists(index=index_name, id=pha_code): 
                    es.index(index=index_name, id=pha_code, body={"pha": pha_code, "state": state}, routing=pha_code)
        print(f"Successfully uploaded pha index")
    except es_exceptions.ConnectionError as e:
        print(f'Connection to Elasticsearch failed: {e}')


def main():
    csv_directory = os.path.join(os.path.dirname(__file__), 'dataset')

    # Specify the Elasticsearch index
    index_name = 'pha'
    index_body = {
        "mappings": {
            "properties": {
                "pha": {"type": "keyword"},
                "state": {"type": "keyword"}
            }
        }
    }

    # Create the index with mappings
    create_index(index_name, index_body)
    csv_file_path = os.path.join(csv_directory, "PHA_SA2.csv")
    upload_pha(csv_file_path)
    

if __name__ == "__main__":
    main()

