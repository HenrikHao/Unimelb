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

def main():
    csv_directory = os.path.join(os.path.dirname(__file__), 'dataset')

    # Specify the Elasticsearch index
    index_name = 'pha_sa2'
    mappings = {
        'pha': {'type': 'keyword'},
        'sa2': {'type': 'keyword'},
        'state': {'type': 'keyword'}
    }
    # Create the index with mappings
    create_index(index_name, mappings)
    csv_file_path = os.path.join(csv_directory, "PHA_SA2.csv")
    upload_csv_to_elasticsearch(csv_file_path, index_name, mappings)
    

if __name__ == "__main__":
    main()